package com.assignment.research.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.llm.anthropic.AnthropicConstants;
import com.assignment.research.adapter.llm.openai.OpenAiConstants;
import com.assignment.research.adapter.web.WebConstants;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AppConfigTest {

    private static final String OPENAI = "OpenAI";
    private static final String UNKNOWN_PROVIDER = "gemini";
    private static final String API_KEY = "secret";
    private static final String API_URL = "http://localhost:1234/v1/chat/completions";
    private static final String MODEL = "local-model";
    private static final String DATA_DIR = "custom-data";
    private static final String RUNS_DIR = "custom-runs";
    private static final String CORPUS_FILE = "other.json";
    private static final String PADDED_PORT = " 9090 ";
    private static final int PARSED_PORT = 9090;
    private static final String SERVER_ANTHROPIC_KEY = "server-anthropic-key";
    private static final String SERVER_OPENAI_KEY = "server-openai-key";
    private static final String RUN_KEY = "run-key";
    private static final String PADDED_RUN_MODEL = "  run-model  ";
    private static final String RUN_MODEL = "run-model";
    private static final String NO_OVERRIDE = "";
    private static final String BLANK_OVERRIDE = "   ";
    private static final String LOCAL_URL = "http://127.0.0.1:8080/v1/chat/completions";
    private static final String LOCAL_MODEL = "qwen3";
    private static final String PADDED_WIRE_NAME = " Local ";

    @Test
    void defaultsToAnthropicWithStandardLocations() {
        var config = AppConfig.fromEnvironment(Map.of());

        assertThat(config.getProvider()).isEqualTo(LLMProvider.ANTHROPIC);
        assertThat(config.getApiKey()).isEqualTo(BootstrapConstants.DEFAULT_API_KEY);
        assertThat(config.getApiUrl()).isEqualTo(AnthropicConstants.DEFAULT_URL);
        assertThat(config.getModel()).isEqualTo(AnthropicConstants.DEFAULT_MODEL);
        assertThat(config.getDataDirectory()).isEqualTo(Path.of(BootstrapConstants.DEFAULT_DATA_DIR));
        assertThat(config.getRunsDirectory()).isEqualTo(Path.of(BootstrapConstants.DEFAULT_RUNS_DIR));
        assertThat(config.getCorpusFile()).isEqualTo(BootstrapConstants.DEFAULT_CORPUS_FILE);
        assertThat(config.getPort()).isEqualTo(WebConstants.DEFAULT_PORT);
    }

    @Test
    void readsOpenAiSettingsCaseInsensitively() {
        var config = AppConfig.fromEnvironment(Map.of(
                BootstrapConstants.ENV_PROVIDER, OPENAI,
                OpenAiConstants.ENV_API_KEY, API_KEY,
                OpenAiConstants.ENV_API_URL, API_URL,
                OpenAiConstants.ENV_MODEL, MODEL,
                BootstrapConstants.ENV_DATA_DIR, DATA_DIR,
                BootstrapConstants.ENV_RUNS_DIR, RUNS_DIR,
                BootstrapConstants.ENV_CORPUS_FILE, CORPUS_FILE,
                BootstrapConstants.ENV_PORT, PADDED_PORT));

        assertThat(config.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(config.getApiKey()).isEqualTo(API_KEY);
        assertThat(config.getApiUrl()).isEqualTo(API_URL);
        assertThat(config.getModel()).isEqualTo(MODEL);
        assertThat(config.getRunsDirectory()).isEqualTo(Path.of(RUNS_DIR));
        assertThat(config.getPort()).isEqualTo(PARSED_PORT);
        assertThat(config.getPromptsDirectory())
                .isEqualTo(Path.of(DATA_DIR, BootstrapConstants.PROMPTS_SUBDIRECTORY));
        assertThat(config.getCorpusPath())
                .isEqualTo(Path.of(DATA_DIR, BootstrapConstants.CORPUS_SUBDIRECTORY, CORPUS_FILE));
    }

    @Test
    void runWithoutOverridesKeepsTheServerSettings() {
        var config = AppConfig.fromEnvironment(Map.of(AnthropicConstants.ENV_API_KEY, SERVER_ANTHROPIC_KEY));

        var merged = config.withRunSettings(new RunSettings(null, NO_OVERRIDE, BLANK_OVERRIDE, NO_OVERRIDE));

        assertThat(merged).isEqualTo(config);
    }

    @Test
    void runOverridesReplaceOnlyTheGivenFields() {
        var config = AppConfig.fromEnvironment(Map.of(
                AnthropicConstants.ENV_API_KEY, SERVER_ANTHROPIC_KEY,
                OpenAiConstants.ENV_API_KEY, SERVER_OPENAI_KEY,
                BootstrapConstants.ENV_RUNS_DIR, RUNS_DIR));

        var merged = config.withRunSettings(new RunSettings(LLMProvider.OPENAI, PADDED_RUN_MODEL, NO_OVERRIDE,
                API_URL));

        assertThat(merged.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(merged.getModel()).isEqualTo(RUN_MODEL);
        assertThat(merged.getApiKey()).isEqualTo(SERVER_OPENAI_KEY);
        assertThat(merged.getApiUrl()).isEqualTo(API_URL);
        assertThat(merged.getRunsDirectory()).isEqualTo(Path.of(RUNS_DIR));
        assertThat(merged.getPort()).isEqualTo(config.getPort());
    }

    @Test
    void runKeyOverridesTheServerKeyWithoutAppearingInToString() {
        var config = AppConfig.fromEnvironment(Map.of(AnthropicConstants.ENV_API_KEY, SERVER_ANTHROPIC_KEY));

        var merged = config.withRunSettings(new RunSettings(null, NO_OVERRIDE, RUN_KEY, NO_OVERRIDE));

        assertThat(merged.getApiKey()).isEqualTo(RUN_KEY);
        assertThat(merged.toString()).doesNotContain(RUN_KEY).doesNotContain(SERVER_ANTHROPIC_KEY);
    }

    @Test
    void localProviderUsesItsOwnDefaultsAndNoKey() {
        var config = AppConfig.fromEnvironment(Map.of(
                BootstrapConstants.ENV_PROVIDER, LLMProvider.LOCAL.getWireName(),
                BootstrapConstants.ENV_LOCAL_API_URL, LOCAL_URL,
                BootstrapConstants.ENV_LOCAL_MODEL, LOCAL_MODEL,
                OpenAiConstants.ENV_API_KEY, SERVER_OPENAI_KEY));

        assertThat(config.getProvider()).isEqualTo(LLMProvider.LOCAL);
        assertThat(config.getApiKey()).isEqualTo(BootstrapConstants.DEFAULT_API_KEY);
        assertThat(config.getApiUrl()).isEqualTo(LOCAL_URL);
        assertThat(config.getModel()).isEqualTo(LOCAL_MODEL);
        assertThat(config.defaultsFor(LLMProvider.LOCAL).hasApiKey()).isFalse();
        assertThat(config.defaultsFor(LLMProvider.OPENAI).hasApiKey()).isTrue();
        assertThat(config.defaultsFor(LLMProvider.LOCAL).getProvider()).isEqualTo(LLMProvider.LOCAL);
    }

    @Test
    void resolvesProvidersByWireName() {
        assertThat(LLMProvider.fromWireName(PADDED_WIRE_NAME)).contains(LLMProvider.LOCAL);
        assertThat(LLMProvider.fromWireName(UNKNOWN_PROVIDER)).isEmpty();
        assertThat(LLMProvider.ANTHROPIC.getWireName()).isEqualTo(BootstrapConstants.DEFAULT_PROVIDER);
    }

    @Test
    void rejectsUnknownProvider() {
        var env = Map.of(BootstrapConstants.ENV_PROVIDER, UNKNOWN_PROVIDER);

        assertThatThrownBy(() -> AppConfig.fromEnvironment(env)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "65536", "-1"})
    void fallsBackToDefaultPortForInvalidValues(String port) {
        var config = AppConfig.fromEnvironment(Map.of(BootstrapConstants.ENV_PORT, port));

        assertThat(config.getPort()).isEqualTo(WebConstants.DEFAULT_PORT);
    }

    @ParameterizedTest
    @ValueSource(ints = {BootstrapConstants.MIN_PORT, BootstrapConstants.MAX_PORT})
    void acceptsPortsAtTheRangeLimits(int port) {
        var config = AppConfig.fromEnvironment(Map.of(BootstrapConstants.ENV_PORT, String.valueOf(port)));

        assertThat(config.getPort()).isEqualTo(port);
    }
}
