package com.assignment.research.llm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public final class LLMRequest {

    private final String label;
    private final String systemPrompt;
    private final String userPrompt;
    private final int maxOutputTokens;

    public LLMRequest(@NonNull String label, @NonNull String systemPrompt, @NonNull String userPrompt,
            int maxOutputTokens) {
        if (maxOutputTokens <= 0) {
            throw new IllegalArgumentException("maxOutputTokens must be positive");
        }
        this.label = label;
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.maxOutputTokens = maxOutputTokens;
    }
}
