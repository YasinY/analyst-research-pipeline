package com.assignment.research.llm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LLMUsageTest {

    @Test
    void twoArgumentConstructorReportsNoCachedTokens() {
        var usage = new LLMUsage(100, 20);

        assertThat(usage.getCachedInputTokens()).isZero();
        assertThat(usage.getFreshInputTokens()).isEqualTo(100);
        assertThat(usage.getTotalTokens()).isEqualTo(120);
    }

    @Test
    void plusSumsInputOutputAndCachedTokens() {
        var sum = new LLMUsage(100, 20, 60).plus(new LLMUsage(50, 10, 40));

        assertThat(sum).isEqualTo(new LLMUsage(150, 30, 100));
        assertThat(sum.getFreshInputTokens()).isEqualTo(50);
    }

    @Test
    void noneIsAllZero() {
        assertThat(LLMUsage.NONE).isEqualTo(new LLMUsage(0, 0, 0));
    }
}
