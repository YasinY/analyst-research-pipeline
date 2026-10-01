package com.assignment.research.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LLMRequestTest {

    private static final String LABEL = "planner";
    private static final String SYSTEM_PROMPT = "system";
    private static final String USER_PROMPT = "user";
    private static final int MAX_TOKENS = 100;
    private static final int ZERO_TOKENS = 0;

    @Test
    void keepsAllValues() {
        var request = new LLMRequest(LABEL, SYSTEM_PROMPT, USER_PROMPT, MAX_TOKENS);

        assertThat(request.getLabel()).isEqualTo(LABEL);
        assertThat(request.getSystemPrompt()).isEqualTo(SYSTEM_PROMPT);
        assertThat(request.getUserPrompt()).isEqualTo(USER_PROMPT);
        assertThat(request.getMaxOutputTokens()).isEqualTo(MAX_TOKENS);
    }

    @Test
    void rejectsNonPositiveTokenLimit() {
        assertThatThrownBy(() -> new LLMRequest(LABEL, SYSTEM_PROMPT, USER_PROMPT, ZERO_TOKENS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullLabel() {
        assertThatThrownBy(() -> new LLMRequest(null, SYSTEM_PROMPT, USER_PROMPT, MAX_TOKENS))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullSystemPrompt() {
        assertThatThrownBy(() -> new LLMRequest(LABEL, null, USER_PROMPT, MAX_TOKENS))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullUserPrompt() {
        assertThatThrownBy(() -> new LLMRequest(LABEL, SYSTEM_PROMPT, null, MAX_TOKENS))
                .isInstanceOf(NullPointerException.class);
    }
}
