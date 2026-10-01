package com.assignment.research.adapter.web;

import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.bootstrap.LLMProvider;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;

@Value
public class ConfigResponse {

    @NonNull
    private final String provider;
    @NonNull
    private final Map<String, String> models;
    @NonNull
    private final Map<String, String> apiUrls;
    private final boolean anthropicKeyPresent;
    private final boolean openaiKeyPresent;

    public static ConfigResponse from(AppConfig config) {
        var models = new LinkedHashMap<String, String>();
        var apiUrls = new LinkedHashMap<String, String>();
        for (var provider : LLMProvider.values()) {
            var defaults = config.defaultsFor(provider);
            models.put(provider.getWireName(), defaults.getModel());
            apiUrls.put(provider.getWireName(), defaults.getApiUrl());
        }
        return new ConfigResponse(config.getProvider().getWireName(), models, apiUrls,
                config.defaultsFor(LLMProvider.ANTHROPIC).hasApiKey(),
                config.defaultsFor(LLMProvider.OPENAI).hasApiKey());
    }
}
