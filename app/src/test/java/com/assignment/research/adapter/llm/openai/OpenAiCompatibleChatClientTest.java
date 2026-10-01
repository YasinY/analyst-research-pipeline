package com.assignment.research.adapter.llm.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.llm.ChatReply;
import com.assignment.research.adapter.llm.HttpJSONPoster;
import com.assignment.research.adapter.llm.LLMAdapterConstants;
import com.assignment.research.adapter.llm.StubProvider;
import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.llm.LLMException;
import org.junit.jupiter.api.Test;

class OpenAiCompatibleChatClientTest {

    private static final int HTTP_OK = 200;
    private static final String API_KEY = "sk-test";
    private static final String NO_API_KEY = " ";
    private static final String CONFIGURED_MODEL = "configured-model";
    private static final String SYSTEM = "be precise";
    private static final String USER = "what happened?";
    private static final int BUDGET = 128;
    private static final int PROMPT_TOKENS = 11;
    private static final int COMPLETION_TOKENS = 7;
    private static final String COMPLETED = """
            {"model": "reported-model",
             "choices": [{"message": {"role": "assistant", "content": "the answer"}, "finish_reason": "stop"}],
             "usage": {"prompt_tokens": 11, "completion_tokens": 7}}
            """;
    private static final String CUT_OFF = """
            {"choices": [{"message": {"content": "the ans"}, "finish_reason": "length"}]}
            """;
    private static final String WITHOUT_CONTENT = """
            {"choices": [{"message": {"role": "assistant"}, "finish_reason": "stop"}]}
            """;

    @Test
    void returnsTextModelAndUsageAndSendsABearerTokenAndTheMessages() {
        try (var provider = StubProvider.answering(HTTP_OK, COMPLETED)) {
            var reply = chat(provider, API_KEY);

            assertThat(reply.getText()).isEqualTo("the answer");
            assertThat(reply.getModel()).isEqualTo("reported-model");
            assertThat(reply.getUsage().getInputTokens()).isEqualTo(PROMPT_TOKENS);
            assertThat(reply.getUsage().getOutputTokens()).isEqualTo(COMPLETION_TOKENS);
            assertThat(reply.isTruncated()).isFalse();
            assertThat(provider.handler().header(LLMAdapterConstants.HEADER_AUTHORIZATION))
                    .contains(LLMAdapterConstants.BEARER_PREFIX + API_KEY);
            assertThat(provider.handler().getReceivedBody())
                    .contains(SYSTEM, USER, CONFIGURED_MODEL, OpenAiConstants.FIELD_MAX_COMPLETION_TOKENS);
        }
    }

    @Test
    void blankApiKeySendsNoAuthorizationHeader() {
        try (var provider = StubProvider.answering(HTTP_OK, COMPLETED)) {
            chat(provider, NO_API_KEY);

            assertThat(provider.handler().header(LLMAdapterConstants.HEADER_AUTHORIZATION)).isEmpty();
        }
    }

    @Test
    void lengthFinishReasonMarksTheReplyTruncatedAndFallsBackToTheConfiguredModel() {
        try (var provider = StubProvider.answering(HTTP_OK, CUT_OFF)) {
            var reply = chat(provider, API_KEY);

            assertThat(reply.isTruncated()).isTrue();
            assertThat(reply.getModel()).isEqualTo(CONFIGURED_MODEL);
            assertThat(reply.getUsage().getInputTokens()).isZero();
        }
    }

    @Test
    void missingMessageContentFailsNamingTheFieldPath() {
        try (var provider = StubProvider.answering(HTTP_OK, WITHOUT_CONTENT)) {
            assertThatThrownBy(() -> chat(provider, API_KEY))
                    .isInstanceOf(LLMException.class)
                    .hasMessageContaining("message.content");
        }
    }

    private static ChatReply chat(StubProvider provider, String apiKey) {
        var poster = new HttpJSONPoster(JSONMapperFactory.create());
        return new OpenAiCompatibleChatClient(poster, provider.url(), apiKey, CONFIGURED_MODEL)
                .chat(SYSTEM, USER, BUDGET);
    }
}
