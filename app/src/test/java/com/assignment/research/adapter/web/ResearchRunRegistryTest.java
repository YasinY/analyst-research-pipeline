package com.assignment.research.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.adapter.output.OutputConstants;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.bootstrap.BootstrapConstants;
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
    private static final String EXPECTED_ABORT_WITHOUT_CAUSE = BootstrapConstants.ABORTED_MESSAGE.formatted(STEP,
            STEP);

    @TempDir
    private Path runsRoot;

    @Test
    void successfulRunFinishesWithBriefingAndOutputDirectory() {
        var run = registry(new ObservingBriefingUseCase()).start(BriefingFixtures.QUERY);

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
    }

    @Test
    void abortedRunFailsWithFormattedCause() {
        ProduceBriefingUseCase aborting = (query, observer) -> {
            throw new PipelineAbortedException(STEP, new IllegalStateException(CAUSE));
        };

        var run = registry(aborting).start(BriefingFixtures.QUERY);

        assertThat(run.toResponse().getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.toResponse().getError()).isEqualTo(EXPECTED_ABORT);
    }

    @Test
    void abortedRunWithoutCauseDescribesItself() {
        ProduceBriefingUseCase aborting = (query, observer) -> {
            throw new PipelineAbortedException(STEP, null);
        };

        var run = registry(aborting).start(BriefingFixtures.QUERY);

        assertThat(run.toResponse().getError()).isEqualTo(EXPECTED_ABORT_WITHOUT_CAUSE);
    }

    @Test
    void errorThrownByUseCaseFailsTheRun() {
        ProduceBriefingUseCase crashing = (query, observer) -> {
            throw new Error(FATAL);
        };

        var run = registry(crashing).start(BriefingFixtures.QUERY);

        assertThat(run.toResponse().getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.toResponse().getError()).isEqualTo(FATAL);
    }

    @Test
    void failureWithoutMessageIsDescribedByItsType() {
        ProduceBriefingUseCase crashing = (query, observer) -> {
            throw new IllegalStateException();
        };

        var run = registry(crashing).start(BriefingFixtures.QUERY);

        assertThat(run.toResponse().getError()).isEqualTo(IllegalStateException.class.getSimpleName());
    }

    @Test
    void evictsOldestFinishedRunsBeyondTheCap() {
        ProduceBriefingUseCase crashing = (query, observer) -> {
            throw new Error(FATAL);
        };
        var registry = registry(crashing);

        var runs = IntStream.rangeClosed(0, WebConstants.MAX_FINISHED_RUNS)
                .mapToObj(index -> registry.start(BriefingFixtures.QUERY))
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
        return new ResearchRunRegistry(useCase, config(), JSONMapperFactory.create(), FIXED_CLOCK, silentConsole(),
                Runnable::run);
    }

    private AppConfig config() {
        return AppConfig.fromEnvironment(Map.of(BootstrapConstants.ENV_RUNS_DIR, runsRoot.toString()));
    }

    static PrintStream silentConsole() {
        return new PrintStream(OutputStream.nullOutputStream());
    }
}
