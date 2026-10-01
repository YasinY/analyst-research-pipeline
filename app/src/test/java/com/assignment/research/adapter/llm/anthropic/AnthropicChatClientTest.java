package com.assignment.research.adapter.llm.anthropic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
