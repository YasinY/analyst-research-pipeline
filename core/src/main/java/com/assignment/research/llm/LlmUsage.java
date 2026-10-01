package com.assignment.research.llm;

import lombok.Value;

@Value
public class LlmUsage {

    public static final LlmUsage NONE = new LlmUsage(0, 0);

    private final int inputTokens;
    private final int outputTokens;

    public LlmUsage plus(LlmUsage other) {
        return new LlmUsage(inputTokens + other.inputTokens, outputTokens + other.outputTokens);
    }

    public int getTotalTokens() {
        return inputTokens + outputTokens;
    }
}
