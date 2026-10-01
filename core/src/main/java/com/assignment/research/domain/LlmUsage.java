package com.assignment.research.domain;

public record LlmUsage(int inputTokens, int outputTokens) {

    public static final LlmUsage NONE = new LlmUsage(0, 0);

    public LlmUsage plus(LlmUsage other) {
        return new LlmUsage(inputTokens + other.inputTokens, outputTokens + other.outputTokens);
    }

    public int totalTokens() {
        return inputTokens + outputTokens;
    }
}
