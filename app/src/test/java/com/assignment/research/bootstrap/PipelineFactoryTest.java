package com.assignment.research.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.adapter.llm.StructuredOutputLLMPort;
import com.assignment.research.adapter.llm.anthropic.AnthropicConstants;
import com.assignment.research.adapter.llm.openai.OpenAiConstants;
import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.pipeline.BriefingOrchestrator;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PipelineFactoryTest {

    private static final Path DATA = Path.of(System.getProperty("basedir", "app")).resolveSibling("data");

    @Test
    void createsTheOrchestratorFromTheDataDirectory() {
        var factory = factory(LLMProvider.ANTHROPIC);

        assertThat(factory.createUseCase()).isInstanceOf(BriefingOrchestrator.class);
    }

    @Test
    void createsAUseCasePerConfig() {
        var config = AppConfig.fromEnvironment(Map.of(BootstrapConstants.ENV_DATA_DIR, DATA.toString()));

        var useCase = PipelineFactory.useCasesFor(JSONMapperFactory.create(), Clock.systemUTC()).apply(config);

        assertThat(useCase).isInstanceOf(BriefingOrchestrator.class);
    }

    @ParameterizedTest
    @EnumSource(LLMProvider.class)
    void createsAStructuredLLMPortForEveryProvider(LLMProvider provider) {
        assertThat(factory(provider).createLLMPort()).isInstanceOf(StructuredOutputLLMPort.class);
    }

    @Test
    void providersExposeTheirEnvironmentVariablesAndDefaults() {
        assertThat(LLMProvider.OPENAI.getApiKeyVariable()).isEqualTo(OpenAiConstants.ENV_API_KEY);
        assertThat(LLMProvider.OPENAI.getUrlVariable()).isEqualTo(OpenAiConstants.ENV_API_URL);
        assertThat(LLMProvider.OPENAI.getModelVariable()).isEqualTo(OpenAiConstants.ENV_MODEL);
        assertThat(LLMProvider.OPENAI.getDefaultUrl()).isEqualTo(OpenAiConstants.DEFAULT_URL);
        assertThat(LLMProvider.OPENAI.getDefaultModel()).isEqualTo(OpenAiConstants.DEFAULT_MODEL);
        assertThat(LLMProvider.ANTHROPIC.getApiKeyVariable()).isEqualTo(AnthropicConstants.ENV_API_KEY);
        assertThat(LLMProvider.ANTHROPIC.getDefaultUrl()).isEqualTo(AnthropicConstants.DEFAULT_URL);
        assertThat(LLMProvider.LOCAL.getUrlVariable()).isEqualTo(BootstrapConstants.ENV_LOCAL_API_URL);
        assertThat(LLMProvider.LOCAL.getDefaultUrl()).isEqualTo(BootstrapConstants.LOCAL_DEFAULT_URL);
    }

    private static PipelineFactory factory(LLMProvider provider) {
        var config = AppConfig.fromEnvironment(Map.of(
                BootstrapConstants.ENV_PROVIDER, provider.name(),
                BootstrapConstants.ENV_DATA_DIR, DATA.toString()));
        return new PipelineFactory(config, JSONMapperFactory.create(), Clock.systemUTC());
    }
}
