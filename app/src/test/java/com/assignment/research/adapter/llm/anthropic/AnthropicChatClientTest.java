package com.assignment.research.adapter.llm.anthropic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.llm.ChatReply;
import com.assignment.research.adapter.llm.HttpJSONPoster;
import com.assignment.research.adapter.llm.StubProvider;
import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.llm.LLMException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AnthropicChatClientTest {

    private static final String THINKING_THEN_TEXT = """
            {"content": [{"type": "thinking", "thinking": "..."},
                         {"type": "text", "text": "{\\"a\\":1"},
                         {"type": "text", "text": "}"}],
             "stop_reason": "end_turn"}
            """;
    private static final String THINKING_ONLY = """
            {"content": [{"type": "thinking", "thinking": "..."}], "stop_reason": "max_tokens"}
            """;
    private static final String WITHOUT_CONTENT = """
            {"stop_reason": "end_turn"}
            """;
    private static final String COMPLETED = """
            {"model": "reported-model", "content": [{"type": "text", "text": "the answer"}],
             "stop_reason": "end_turn", "usage": {"input_tokens": 13, "output_tokens": 5}}
            """;
    private static final String CUT_OFF = """
            {"content": [{"type": "text", "text": "the ans"}], "stop_reason": "max_tokens"}
            """;
    private static final int HTTP_OK = 200;
    private static final String API_KEY = "anthropic-key";
    private static final String CONFIGURED_MODEL = "configured-model";
    private static final String SYSTEM = "be precise";
    private static final String USER = "what happened?";
    private static final int BUDGET = 256;
    private static final int INPUT_TOKENS = 13;
    private static final int OUTPUT_TOKENS = 5;

    private final ObjectMapper mapper = JSONMapperFactory.create();

    @Test
    void skipsThinkingBlocksAndJoinsTextBlocks() throws Exception {
        var response = mapper.readTree(THINKING_THEN_TEXT);

        assertThat(AnthropicChatClient.extractText(response)).isEqualTo("{\"a\":1\n}");
    }

    @Test
    void failsWithDiagnosticsWhenNoTextBlockIsPresent() throws Exception {
        var response = mapper.readTree(THINKING_ONLY);

        assertThatThrownBy(() -> AnthropicChatClient.extractText(response))
                .isInstanceOf(LLMException.class)
                .hasMessageContaining("max_tokens")
                .hasMessageContaining("thinking");
    }

    @Test
    void failsWhenTheReplyHasNoContentAtAll() throws Exception {
        var response = mapper.readTree(WITHOUT_CONTENT);

        assertThatThrownBy(() -> AnthropicChatClient.extractText(response))
                .isInstanceOf(LLMException.class)
                .hasMessageContaining("[]");
    }

    @Test
    void returnsTextModelAndUsageAndSendsHeadersThinkingAndPrompts() {
        try (var provider = StubProvider.answering(HTTP_OK, COMPLETED)) {
            var reply = chat(provider);
            var handler = provider.handler();

            assertThat(reply.getText()).isEqualTo("the answer");
            assertThat(reply.getModel()).isEqualTo("reported-model");
            assertThat(reply.getUsage().getInputTokens()).isEqualTo(INPUT_TOKENS);
            assertThat(reply.getUsage().getOutputTokens()).isEqualTo(OUTPUT_TOKENS);
            assertThat(reply.isTruncated()).isFalse();
            assertThat(handler.header(AnthropicConstants.HEADER_API_KEY)).contains(API_KEY);
            assertThat(handler.header(AnthropicConstants.HEADER_VERSION)).contains(AnthropicConstants.API_VERSION);
            assertThat(handler.getReceivedBody()).contains(AnthropicConstants.FIELD_THINKING,
                    AnthropicConstants.THINKING_BETWEEN_TOOLS_ONLY, SYSTEM, USER, CONFIGURED_MODEL);
        }
    }

    @Test
    void maxTokensStopReasonMarksTheReplyTruncatedAndFallsBackToTheConfiguredModel() {
        try (var provider = StubProvider.answering(HTTP_OK, CUT_OFF)) {
            var reply = chat(provider);

            assertThat(reply.isTruncated()).isTrue();
            assertThat(reply.getModel()).isEqualTo(CONFIGURED_MODEL);
            assertThat(reply.getUsage().getOutputTokens()).isZero();
        }
    }

    private static ChatReply chat(StubProvider provider) {
        var poster = new HttpJSONPoster(JSONMapperFactory.create());
        return new AnthropicChatClient(poster, provider.url(), API_KEY, CONFIGURED_MODEL).chat(SYSTEM, USER, BUDGET);
    }
}
