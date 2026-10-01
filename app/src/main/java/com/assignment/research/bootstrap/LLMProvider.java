package com.assignment.research.bootstrap;

import com.assignment.research.adapter.llm.anthropic.AnthropicConstants;
import com.assignment.research.adapter.llm.openai.OpenAiConstants;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LLMProvider {
    OPENAI(OpenAiConstants.ENV_API_KEY, OpenAiConstants.ENV_API_URL, OpenAiConstants.ENV_MODEL,
            OpenAiConstants.DEFAULT_URL, OpenAiConstants.DEFAULT_MODEL),
    ANTHROPIC(AnthropicConstants.ENV_API_KEY, AnthropicConstants.ENV_API_URL, AnthropicConstants.ENV_MODEL,
            AnthropicConstants.DEFAULT_URL, AnthropicConstants.DEFAULT_MODEL),
    LOCAL(BootstrapConstants.NO_API_KEY_VARIABLE, BootstrapConstants.ENV_LOCAL_API_URL,
            BootstrapConstants.ENV_LOCAL_MODEL, BootstrapConstants.LOCAL_DEFAULT_URL,
            BootstrapConstants.LOCAL_DEFAULT_MODEL);

    private final String apiKeyVariable;
    private final String urlVariable;
    private final String modelVariable;
    private final String defaultUrl;
    private final String defaultModel;

    public boolean requiresApiKey() {
        return this != LOCAL;
    }

    public static Optional<LLMProvider> fromWireName(String wireName) {
        var normalized = wireName.strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(provider -> provider.getWireName().equals(normalized)).findFirst();
    }

    public String getWireName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ProviderSettings settingsFrom(Map<String, String> env) {
        return new ProviderSettings(this, apiKeyFrom(env), env.getOrDefault(urlVariable, defaultUrl),
                env.getOrDefault(modelVariable, defaultModel));
    }

    private String apiKeyFrom(Map<String, String> env) {
        if (apiKeyVariable.isEmpty()) {
            return BootstrapConstants.DEFAULT_API_KEY;
        }
        return env.getOrDefault(apiKeyVariable, BootstrapConstants.DEFAULT_API_KEY);
    }
}
