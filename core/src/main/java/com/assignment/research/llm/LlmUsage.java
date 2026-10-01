package com.assignment.research.llm;

import lombok.Value;

@Value
public class LLMUsage {

    public static final LLMUsage NONE = new LLMUsage(0, 0);

    private final int inputTokens;
    private final int outputTokens;

    public LLMUsage plus(LLMUsage other) {
        return new LLMUsage(inputTokens + other.inputTokens, outputTokens + other.outputTokens);
    }

    public int getTotalTokens() {
        return inputTokens + outputTokens;
    }
}
