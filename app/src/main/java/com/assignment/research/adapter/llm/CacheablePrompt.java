package com.assignment.research.adapter.llm;

import java.util.Optional;
import lombok.Value;

@Value
public class CacheablePrompt {

    private final String stablePrefix;
    private final String remainder;

    public static CacheablePrompt of(String prompt) {
        var boundary = prompt.indexOf(LLMAdapterConstants.CACHE_BOUNDARY);
        if (boundary == LLMAdapterConstants.NO_CACHE_BOUNDARY) {
            return new CacheablePrompt(null, prompt);
        }
        var prefix = prompt.substring(LLMAdapterConstants.PROMPT_START, boundary).strip();
        var rest = prompt.substring(boundary + LLMAdapterConstants.CACHE_BOUNDARY.length()).strip();
        return new CacheablePrompt(prefix, rest);
    }

    public static String withoutBoundary(String prompt) {
        return prompt.replace(LLMAdapterConstants.CACHE_BOUNDARY, LLMAdapterConstants.EMPTY);
    }

    public Optional<String> getStablePrefix() {
        return Optional.ofNullable(stablePrefix);
    }
}
