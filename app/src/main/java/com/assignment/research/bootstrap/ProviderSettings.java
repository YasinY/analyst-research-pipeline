package com.assignment.research.bootstrap;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
public class ProviderSettings {

    @NonNull
    private final LLMProvider provider;
    @NonNull
    @ToString.Exclude
    private final String apiKey;
    @NonNull
    private final String apiUrl;
    @NonNull
    private final String model;

    public boolean hasApiKey() {
        return !apiKey.isBlank();
    }
}
