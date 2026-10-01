package com.assignment.research.llm;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class LLMUsage {

    private static final int NO_TOKENS = 0;

    public static final LLMUsage NONE = new LLMUsage(NO_TOKENS, NO_TOKENS, NO_TOKENS);

    private final int inputTokens;
    private final int outputTokens;
    private final int cachedInputTokens;

    public LLMUsage(int inputTokens, int outputTokens) {
        this(inputTokens, outputTokens, NO_TOKENS);
    }

    public LLMUsage plus(LLMUsage other) {
        return new LLMUsage(inputTokens + other.inputTokens, outputTokens + other.outputTokens,
                cachedInputTokens + other.cachedInputTokens);
    }

    public int getTotalTokens() {
        return inputTokens + outputTokens;
    }

    public int getFreshInputTokens() {
        return inputTokens - cachedInputTokens;
    }
}
