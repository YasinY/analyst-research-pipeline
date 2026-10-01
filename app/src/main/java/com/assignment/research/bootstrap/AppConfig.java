package com.assignment.research.bootstrap;

import java.nio.file.Path;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;

@Value
public class AppConfig {

    @NonNull
    private final LlmProvider provider;
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

    public static AppConfig fromEnvironment(Map<String, String> env) {
        var provider = LlmProvider.valueOf(
                env.getOrDefault(BootstrapConstants.ENV_PROVIDER, BootstrapConstants.DEFAULT_PROVIDER).toUpperCase());
        return new AppConfig(
                provider,
                env.getOrDefault(provider.getApiKeyVariable(), ""),
                env.getOrDefault(provider.getUrlVariable(), provider.getDefaultUrl()),
                env.getOrDefault(provider.getModelVariable(), provider.getDefaultModel()),
                Path.of(env.getOrDefault(BootstrapConstants.ENV_DATA_DIR, BootstrapConstants.DEFAULT_DATA_DIR)),
                Path.of(env.getOrDefault(BootstrapConstants.ENV_RUNS_DIR, BootstrapConstants.DEFAULT_RUNS_DIR)),
                env.getOrDefault(BootstrapConstants.ENV_CORPUS_FILE, BootstrapConstants.DEFAULT_CORPUS_FILE));
    }

    public Path getPromptsDirectory() {
        return dataDirectory.resolve(BootstrapConstants.PROMPTS_SUBDIRECTORY);
    }

    public Path getCorpusPath() {
        return dataDirectory.resolve(BootstrapConstants.CORPUS_SUBDIRECTORY).resolve(corpusFile);
    }
}
