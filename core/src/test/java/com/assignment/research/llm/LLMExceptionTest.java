package com.assignment.research.llm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LLMExceptionTest {

    private static final String MESSAGE = "provider down";

    @Test
    void keepsMessageAndCause() {
        var cause = new IllegalStateException();

        var exception = new LLMException(MESSAGE, cause);

        assertThat(exception).hasMessage(MESSAGE).hasCause(cause);
    }
}
