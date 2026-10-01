package com.assignment.research.bootstrap;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
public class RunSettings {

    private final LLMProvider provider;
    @NonNull
    private final String model;
    @NonNull
    @ToString.Exclude
    private final String apiKey;
    @NonNull
    private final String apiUrl;
}
