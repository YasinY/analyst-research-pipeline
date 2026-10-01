package com.assignment.research.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LLMResultTest {

    private static final String VALUE = "value";
    private static final String RAW_TEXT = "{}";
    private static final String MODEL = "model";

    @Test
    void acceptsRepairedStatus() {
        var result = new LLMResult<>(VALUE, RAW_TEXT, MODEL, LLMUsage.NONE, LLMCallStatus.REPAIRED);

        assertThat(result.getStatus()).isEqualTo(LLMCallStatus.REPAIRED);
        assertThat(result.getValue()).isEqualTo(VALUE);
    }

    @Test
    void rejectsFailedStatus() {
        assertThatThrownBy(() -> new LLMResult<>(VALUE, RAW_TEXT, MODEL, LLMUsage.NONE, LLMCallStatus.FAILED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new LLMResult<>(null, RAW_TEXT, MODEL, LLMUsage.NONE, LLMCallStatus.OK))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullRawText() {
        assertThatThrownBy(() -> new LLMResult<>(VALUE, null, MODEL, LLMUsage.NONE, LLMCallStatus.OK))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullModel() {
        assertThatThrownBy(() -> new LLMResult<>(VALUE, RAW_TEXT, null, LLMUsage.NONE, LLMCallStatus.OK))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullUsage() {
        assertThatThrownBy(() -> new LLMResult<>(VALUE, RAW_TEXT, MODEL, null, LLMCallStatus.OK))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullStatus() {
        assertThatThrownBy(() -> new LLMResult<>(VALUE, RAW_TEXT, MODEL, LLMUsage.NONE, null))
                .isInstanceOf(NullPointerException.class);
    }
}
