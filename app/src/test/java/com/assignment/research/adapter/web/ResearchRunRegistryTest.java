package com.assignment.research.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.adapter.output.OutputConstants;
import com.assignment.research.adapter.llm.anthropic.AnthropicConstants;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.bootstrap.BootstrapConstants;
import com.assignment.research.bootstrap.LLMProvider;
import com.assignment.research.bootstrap.RunSettings;
import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.pipeline.PipelineAbortedException;
import com.assignment.research.pipeline.ProduceBriefingUseCase;
import com.assignment.research.pipeline.StopReason;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResearchRunRegistryTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-20T10:15:30Z"), ZoneOffset.UTC);
    private static final String STEP = "plan";
    private static final String CAUSE = "model unreachable";
    private static final String FATAL = "out of memory";
    private static final String UNKNOWN_ID = "run-404";
    private static final String FIRST_RUN_ID = "run-1";
    private static final String EXPECTED_ABORT = BootstrapConstants.ABORTED_MESSAGE.formatted(STEP, CAUSE);
    private static final String RUN_MODEL = "llama3.2";
    private static final String RUN_KEY = "run-only-key";
    private static final String COST_LINE_MARKER = "Estimated cost: USD";
    private static final int CALLS_IN_FIXTURE = 2;
    private static final String EXPECTED_ABORT_WITHOUT_CAUSE = BootstrapConstants.ABORTED_MESSAGE.formatted(STEP,
            STEP);

    @TempDir
    private Path runsRoot;

    @Test
    void successfulRunFinishesWithBriefingAndOutputDirectory() {
        var run = registry(new ObservingBriefingUseCase()).start(BriefingFixtures.QUERY,
                BriefingFixtures.SERVER_DEFAULTS);

        var response = run.toResponse();

        assertThat(response.getStatus()).isEqualTo(RunStatus.FINISHED);
        assertThat(response.getBriefingMarkdown()).contains(BriefingFixtures.SUMMARY);
        assertThat(response.getConfidence()).isEqualTo(ConfidenceLevel.HIGH.name());
        assertThat(response.getStopReason()).isEqualTo(StopReason.APPROVED.name());
        assertThat(response.getStopExplanation()).isEqualTo(BriefingFixtures.STOP_EXPLANATION);
        assertThat(Path.of(response.getOutputDirectory())).startsWith(runsRoot.toAbsolutePath())
                .isDirectory();
        assertThat(Path.of(response.getOutputDirectory()).resolve(OutputConstants.BRIEFING_FILE)).isRegularFile();
        assertThat(response.getSteps()).hasSize(2);
        assertThat(response.getSteps().getFirst().getCallsSoFar()).isEqualTo(1);
        assertThat(response.getCalls()).hasSize(2);
        assertThat(response.getCalls().getLast().getFailure()).isEqualTo(BriefingFixtures.FAILURE_REASON);
        assertThat(response.getCalls().getFirst().getDurationMillis()).isEqualTo(BriefingFixtures.DURATION_MILLIS);
        assertThat(response.getProvider()).isEqualTo(LLMProvider.ANTHROPIC.getWireName());
        assertThat(response.getModel()).isEqualTo(AnthropicConstants.DEFAULT_MODEL);
        assertThat(response.getBriefingMarkdown()).contains(COST_LINE_MARKER);
    }

    @Test
    void statusCarriesTotalsAndRolesWithCost() {
        var response = registry(new ObservingBriefingUseCase()).start(BriefingFixtures.QUERY,
                BriefingFixtures.SERVER_DEFAULTS).toResponse();

        assertThat(response.getRoles()).extracting(RoleLine::getRole)
                .containsExactly(BriefingFixtures.PLANNER_LABEL, BriefingFixtures.CRITIC_LABEL);
        assertThat(response.getTotals().getCalls()).isEqualTo(CALLS_IN_FIXTURE);
        assertThat(response.getTotals().getCachedInputTokens())
                .isEqualTo(CALLS_IN_FIXTURE * BriefingFixtures.CACHED_INPUT_TOKENS);
        assertThat(response.getTotals().getCostUsd()).isCloseTo(CALLS_IN_FIXTURE * BriefingFixtures.CALL_COST_USD,
                within(BriefingFixtures.COST_TOLERANCE));
    }

    @Test
    void createsTheUseCaseFromTheMergedRunConfig() {
        var received = new AtomicReference<AppConfig>();
        var registry = new ResearchRunRegistry(config -> {
            received.set(config);
            return new ObservingBriefingUseCase();
        }, config(), BriefingFixtures.costEstimator(), JSONMapperFactory.create(), FIXED_CLOCK, silentConsole(),
                Runnable::run);

        var response = registry.start(BriefingFixtures.QUERY,
                new RunSettings(LLMProvider.LOCAL, RUN_MODEL, RUN_KEY,
                BriefingFixtures.NO_OVERRIDE)).toResponse();

        assertThat(received.get().getProvider()).isEqualTo(LLMProvider.LOCAL);
        assertThat(received.get().getModel()).isEqualTo(RUN_MODEL);
        assertThat(received.get().getApiKey()).isEqualTo(RUN_KEY);
        assertThat(received.get().getApiUrl()).isEqualTo(BootstrapConstants.LOCAL_DEFAULT_URL);
        assertThat(response.getProvider()).isEqualTo(LLMProvider.LOCAL.getWireName());
        assertThat(response.getModel()).isEqualTo(RUN_MODEL);
    }

    @Test
    void failingUseCaseFactoryFailsTheRun() {
        var registry = new ResearchRunRegistry(config -> {
            throw new IllegalStateException(CAUSE);
        }, config(), BriefingFixtures.costEstimator(), JSONMapperFactory.create(), FIXED_CLOCK, silentConsole(),
                Runnable::run);

        var response = registry.start(BriefingFixtures.QUERY, BriefingFixtures.SERVER_DEFAULTS).toResponse();

        assertThat(response.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(response.getError()).isEqualTo(CAUSE);
    }

    @Test
    void describesTheServerDefaults() {
        var defaults = registry(new ObservingBriefingUseCase()).describeDefaults();

        assertThat(defaults.getProvider()).isEqualTo(LLMProvider.ANTHROPIC.getWireName());
        assertThat(defaults.getModels()).containsEntry(LLMProvider.LOCAL.getWireName(),
                BootstrapConstants.LOCAL_DEFAULT_MODEL);
    }

    @Test
    void abortedRunFailsWithFormattedCause() {
        ProduceBriefingUseCase aborting = (query, observer) -> {
            throw new PipelineAbortedException(STEP, new IllegalStateException(CAUSE));
        };

        var run = registry(aborting).start(BriefingFixtures.QUERY, BriefingFixtures.SERVER_DEFAULTS);

        assertThat(run.toResponse().getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.toResponse().getError()).isEqualTo(EXPECTED_ABORT);
    }

    @Test
    void abortedRunWithoutCauseDescribesItself() {
        ProduceBriefingUseCase aborting = (query, observer) -> {
            throw new PipelineAbortedException(STEP, null);
        };

        var run = registry(aborting).start(BriefingFixtures.QUERY, BriefingFixtures.SERVER_DEFAULTS);

        assertThat(run.toResponse().getError()).isEqualTo(EXPECTED_ABORT_WITHOUT_CAUSE);
    }

    @Test
    void errorThrownByUseCaseFailsTheRun() {
        ProduceBriefingUseCase crashing = (query, observer) -> {
            throw new Error(FATAL);
        };

        var run = registry(crashing).start(BriefingFixtures.QUERY, BriefingFixtures.SERVER_DEFAULTS);

        assertThat(run.toResponse().getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.toResponse().getError()).isEqualTo(FATAL);
    }

    @Test
    void failureWithoutMessageIsDescribedByItsType() {
        ProduceBriefingUseCase crashing = (query, observer) -> {
            throw new IllegalStateException();
        };

        var run = registry(crashing).start(BriefingFixtures.QUERY, BriefingFixtures.SERVER_DEFAULTS);

        assertThat(run.toResponse().getError()).isEqualTo(IllegalStateException.class.getSimpleName());
    }

    @Test
    void evictsOldestFinishedRunsBeyondTheCap() {
        ProduceBriefingUseCase crashing = (query, observer) -> {
            throw new Error(FATAL);
        };
        var registry = registry(crashing);

        var runs = IntStream.rangeClosed(0, WebConstants.MAX_FINISHED_RUNS)
                .mapToObj(index -> registry.start(BriefingFixtures.QUERY, BriefingFixtures.SERVER_DEFAULTS))
                .toList();

        assertThat(registry.find(FIRST_RUN_ID)).isEmpty();
        assertThat(registry.find(runs.getLast().getId())).contains(runs.getLast());
        assertThat(registry.find(runs.get(1).getId())).contains(runs.get(1));
    }

    @Test
    void findOfUnknownIdIsEmpty() {
        assertThat(registry(new ObservingBriefingUseCase()).find(UNKNOWN_ID)).isEmpty();
    }

    private ResearchRunRegistry registry(ProduceBriefingUseCase useCase) {
        return new ResearchRunRegistry(config -> useCase, config(), BriefingFixtures.costEstimator(),
                JSONMapperFactory.create(), FIXED_CLOCK, silentConsole(), Runnable::run);
    }

    private AppConfig config() {
        return AppConfig.fromEnvironment(Map.of(BootstrapConstants.ENV_RUNS_DIR, runsRoot.toString()));
    }

    static PrintStream silentConsole() {
        return new PrintStream(OutputStream.nullOutputStream());
    }
}
