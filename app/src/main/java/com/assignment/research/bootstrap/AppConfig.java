package com.assignment.research.bootstrap;

import com.assignment.research.adapter.web.WebConstants;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
public class AppConfig {

    @NonNull
    private final LLMProvider provider;
    @NonNull
    @ToString.Exclude
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
    @NonNull
    @ToString.Exclude
    private final Map<LLMProvider, ProviderSettings> providerDefaults;

    public static AppConfig fromEnvironment(Map<String, String> env) {
        var provider = LLMProvider.valueOf(env.getOrDefault(BootstrapConstants.ENV_PROVIDER,
                BootstrapConstants.DEFAULT_PROVIDER).toUpperCase(Locale.ROOT));
        var defaults = new EnumMap<LLMProvider, ProviderSettings>(LLMProvider.class);
        for (var candidate : LLMProvider.values()) {
            defaults.put(candidate, candidate.settingsFrom(env));
        }
        var active = defaults.get(provider);
        return new AppConfig(
                provider,
                active.getApiKey(),
                active.getApiUrl(),
                active.getModel(),
                Path.of(env.getOrDefault(BootstrapConstants.ENV_DATA_DIR, BootstrapConstants.DEFAULT_DATA_DIR)),
                Path.of(env.getOrDefault(BootstrapConstants.ENV_RUNS_DIR, BootstrapConstants.DEFAULT_RUNS_DIR)),
                env.getOrDefault(BootstrapConstants.ENV_CORPUS_FILE, BootstrapConstants.DEFAULT_CORPUS_FILE),
                parsePort(env.get(BootstrapConstants.ENV_PORT)),
                Map.copyOf(defaults));
    }

    public AppConfig withRunSettings(RunSettings settings) {
        var chosen = Objects.requireNonNullElse(settings.getProvider(), provider);
        var base = defaultsFor(chosen);
        return new AppConfig(
                chosen,
                override(settings.getApiKey(), base.getApiKey()),
                override(settings.getApiUrl(), base.getApiUrl()),
                override(settings.getModel(), base.getModel()),
                dataDirectory,
                runsDirectory,
                corpusFile,
                port,
                providerDefaults);
    }

    public ProviderSettings defaultsFor(LLMProvider candidate) {
        return providerDefaults.get(candidate);
    }

    public Path getPromptsDirectory() {
        return dataDirectory.resolve(BootstrapConstants.PROMPTS_SUBDIRECTORY);
    }

    public Path getCorpusPath() {
        return dataDirectory.resolve(BootstrapConstants.CORPUS_SUBDIRECTORY).resolve(corpusFile);
    }

    private static String override(String requested, String fallback) {
        return requested.isBlank() ? fallback : requested.strip();
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
