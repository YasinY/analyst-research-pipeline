package com.assignment.research.application.port;

import java.util.Objects;

public record LlmRequest(String label, String systemPrompt, String userPrompt, int maxOutputTokens) {

    public LlmRequest {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(systemPrompt, "systemPrompt");
        Objects.requireNonNull(userPrompt, "userPrompt");
        if (maxOutputTokens <= 0) {
            throw new IllegalArgumentException("maxOutputTokens must be positive");
        }
    }
}
