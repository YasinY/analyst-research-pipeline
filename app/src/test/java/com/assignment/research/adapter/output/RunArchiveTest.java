package com.assignment.research.adapter.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assumptions.assumeThat;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.pipeline.BriefingConfidence;
import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.PipelineStep;
import com.assignment.research.pipeline.StopDecision;
import com.assignment.research.pipeline.StopReason;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.trace.TraceEntry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RunArchiveTest {

    private static final Instant NOW = Instant.parse("2026-05-20T10:15:30.123Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final String FIRST_FOLDER = "2026-05-20T10-15-30-123";
    private static final String SECOND_FOLDER = "2026-05-20T10-15-30-123-2";
    private static final String LABEL = "researcher/sq-1";
    private static final String SAFE_PROMPT_FILE = "03-researcher_sq-1.prompt.md";
    private static final String SAFE_RESPONSE_FILE = "03-researcher_sq-1.response.md";
    private static final String FIRST_SNAPSHOT_FILE = "01-after-plan.json";
    private static final int SEQUENCE = 3;
    private static final String MODEL = "test-model";
    private static final String SYSTEM_PROMPT = "system prompt text";
    private static final String USER_PROMPT = "user prompt text";
    private static final String RAW_RESPONSE = "{\"raw\": true}";
    private static final String FAILURE_REASON = "provider timed out";
    private static final String FAILURE_SUFFIX = "  (provider timed out)";
    private static final Duration DURATION = Duration.ofMillis(1500);
    private static final LLMUsage USAGE = new LLMUsage(120, 30);
    private static final String QUERY = "How is dry bulk supply developing?";
    private static final String BRIEFING_MARKDOWN = "# Analyst briefing";
    private static final String STOP_EXPLANATION = "The round limit was reached.";
    private static final String BLOCKING_FILE = "not-a-directory";
    private static final String NESTED = "nested";

    @TempDir
    private Path runsRoot;

    private final ByteArrayOutputStream consoleBytes = new ByteArrayOutputStream();

    @Test
    void twoArchivesStartedAtTheSameInstantGetSeparateFolders() {
        var first = newArchive();
        var second = newArchive();

        assertThat(first.getDirectory()).isNotEqualTo(second.getDirectory());
        assertThat(first.getDirectory().getFileName()).hasToString(FIRST_FOLDER);
        assertThat(second.getDirectory().getFileName()).hasToString(SECOND_FOLDER);
        assertThat(second.getDirectory().resolve(OutputConstants.CALLS_DIRECTORY)).isDirectory();
        assertThat(second.getDirectory().resolve(OutputConstants.SNAPSHOTS_DIRECTORY)).isDirectory();
    }

    @Test
    void traceEntryIsWrittenUnderASafeFileNameAndPrintedWithItsFailure() throws IOException {
        var archive = newArchive();

        archive.onTrace(traceEntry(LLMCallStatus.FAILED, FAILURE_REASON));

        var calls = archive.getDirectory().resolve(OutputConstants.CALLS_DIRECTORY);
        assertThat(Files.readString(calls.resolve(SAFE_PROMPT_FILE))).contains(LABEL, SYSTEM_PROMPT, USER_PROMPT);
        assertThat(Files.readString(calls.resolve(SAFE_RESPONSE_FILE))).isEqualTo(RAW_RESPONSE);
        assertThat(console()).contains(LABEL, MODEL, LLMCallStatus.FAILED.name(), "1.5s").endsWith(
                FAILURE_SUFFIX + System.lineSeparator());
    }

    @Test
    void successfulTraceEntryIsPrintedWithoutAFailureSuffix() {
        var archive = newArchive();

        archive.onTrace(traceEntry(LLMCallStatus.OK, null));

        assertThat(console()).contains(LLMCallStatus.OK.name()).doesNotContain("(");
    }

    @Test
    void stepSnapshotIsWrittenAndSummarisedOnTheConsole() {
        var archive = newArchive();

        archive.onStep(PipelineStep.PLAN, state());

        assertThat(archive.getDirectory().resolve(OutputConstants.SNAPSHOTS_DIRECTORY).resolve(FIRST_SNAPSHOT_FILE))
                .isRegularFile().content().contains(QUERY);
        assertThat(console()).contains("---- PLAN -> round 1, 0 sub-question(s), 0 claim(s), 0 group(s)");
    }

    @Test
    void resultWritesEveryFileAndPrintsTheSummary() {
        var archive = newArchive();

        var directory = archive.writeResult(result(), BRIEFING_MARKDOWN);

        assertThat(directory).isEqualTo(archive.getDirectory());
        assertThat(directory.resolve(OutputConstants.BRIEFING_FILE)).content().isEqualTo(BRIEFING_MARKDOWN);
        assertThat(directory.resolve(OutputConstants.STATE_FILE)).content().contains(QUERY);
        assertThat(directory.resolve(OutputConstants.TRACE_FILE)).content().contains(LABEL, MODEL);
        assertThat(directory.resolve(OutputConstants.RESULT_FILE)).content().contains(STOP_EXPLANATION);
        assertThat(console()).contains("Run finished: ROUND_LIMIT_REACHED", STOP_EXPLANATION,
                "LLM calls: 1 | tokens in: 120 | tokens out: 30 | confidence: LOW",
                directory.toAbsolutePath().toString());
    }

    @Test
    void runsRootThatIsARegularFileCannotBeCreated() throws IOException {
        var blockingFile = Files.createFile(runsRoot.resolve(BLOCKING_FILE));

        assertThatThrownBy(() -> newArchive(blockingFile.resolve(NESTED)))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("cannot create");
    }

    @Test
    void runFolderThatCannotBeCreatedFailsLoudly() {
        try (var protectedRoot = ProtectedDirectory.protect(runsRoot)) {
            assumeThat(protectedRoot.isEffective()).isTrue();

            assertThatThrownBy(this::newArchive)
                    .isInstanceOf(UncheckedIOException.class)
                    .hasMessageContaining("cannot create")
                    .hasMessageContaining(FIRST_FOLDER);
        }
    }

    @Test
    void traceFileThatCannotBeWrittenFailsLoudly() throws IOException {
        var archive = newArchive();
        replaceWithFile(archive.getDirectory().resolve(OutputConstants.CALLS_DIRECTORY));

        assertThatThrownBy(() -> archive.onTrace(traceEntry(LLMCallStatus.OK, null)))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("cannot write")
                .hasMessageContaining(SAFE_PROMPT_FILE);
    }

    @Test
    void snapshotThatCannotBeWrittenFailsLoudly() throws IOException {
        var archive = newArchive();
        replaceWithFile(archive.getDirectory().resolve(OutputConstants.SNAPSHOTS_DIRECTORY));

        assertThatThrownBy(() -> archive.onStep(PipelineStep.PLAN, state()))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("cannot write")
                .hasMessageContaining(FIRST_SNAPSHOT_FILE);
    }

    private RunArchive newArchive() {
        return newArchive(runsRoot);
    }

    private RunArchive newArchive(Path root) {
        return new RunArchive(root, FIXED_CLOCK, JSONMapperFactory.create(),
                new PrintStream(consoleBytes, true, StandardCharsets.UTF_8));
    }

    private String console() {
        return consoleBytes.toString(StandardCharsets.UTF_8);
    }

    private static void replaceWithFile(Path directory) throws IOException {
        Files.delete(directory);
        Files.createFile(directory);
    }

    private static TraceEntry traceEntry(LLMCallStatus status, String failureReason) {
        return new TraceEntry(SEQUENCE, LABEL, NOW, DURATION, MODEL, SYSTEM_PROMPT, USER_PROMPT, RAW_RESPONSE,
                USAGE, status, failureReason);
    }

    private static BriefingState state() {
        return BriefingState.initial(new AnalystQuery(QUERY));
    }

    private static BriefingResult result() {
        var draft = new BriefingDraft(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        var confidence = new BriefingConfidence(ConfidenceLevel.LOW, 0.0, List.of());
        var stopDecision = new StopDecision(StopReason.ROUND_LIMIT_REACHED, STOP_EXPLANATION);
        return new BriefingResult(draft, confidence, List.of(), List.of(), stopDecision, state(),
                List.of(traceEntry(LLMCallStatus.OK, null)), USAGE);
    }
}
