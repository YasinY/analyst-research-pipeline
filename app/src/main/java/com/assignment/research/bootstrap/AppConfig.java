package com.assignment.research.bootstrap;

import com.assignment.research.adapter.web.WebConstants;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;

@Value
public class AppConfig {

    @NonNull
    private final LLMProvider provider;
    @NonNull
    private final String apiKey;
    @NonNull
    private final String apiUrl;
    @NonNull
    private final String model;
    @NonNull
    private final Path dataDirectory;
    @NonNull
    private final Path runsDirectory;
    @NonNull
    private final String corpusFile;
    private final int port;

    public static AppConfig fromEnvironment(Map<String, String> env) {
        var provider = LLMProvider.valueOf(env.getOrDefault(BootstrapConstants.ENV_PROVIDER,
                BootstrapConstants.DEFAULT_PROVIDER).toUpperCase(Locale.ROOT));
        return new AppConfig(
                provider,
                env.getOrDefault(provider.getApiKeyVariable(), BootstrapConstants.DEFAULT_API_KEY),
                env.getOrDefault(provider.getUrlVariable(), provider.getDefaultUrl()),
                env.getOrDefault(provider.getModelVariable(), provider.getDefaultModel()),
                Path.of(env.getOrDefault(BootstrapConstants.ENV_DATA_DIR, BootstrapConstants.DEFAULT_DATA_DIR)),
                Path.of(env.getOrDefault(BootstrapConstants.ENV_RUNS_DIR, BootstrapConstants.DEFAULT_RUNS_DIR)),
                env.getOrDefault(BootstrapConstants.ENV_CORPUS_FILE, BootstrapConstants.DEFAULT_CORPUS_FILE),
                parsePort(env.get(BootstrapConstants.ENV_PORT)));
    }

    public Path getPromptsDirectory() {
        return dataDirectory.resolve(BootstrapConstants.PROMPTS_SUBDIRECTORY);
    }

    public Path getCorpusPath() {
        return dataDirectory.resolve(BootstrapConstants.CORPUS_SUBDIRECTORY).resolve(corpusFile);
    }

    private static int parsePort(String value) {
        if (value == null) {
            return WebConstants.DEFAULT_PORT;
        }
        try {
            var port = Integer.parseInt(value.strip());
            return port < BootstrapConstants.MIN_PORT || port > BootstrapConstants.MAX_PORT
                    ? WebConstants.DEFAULT_PORT
                    : port;
        } catch (NumberFormatException invalid) {
            return WebConstants.DEFAULT_PORT;
        }
    }
}
