package com.assignment.research.adapter.output;

import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.PipelineObserver;
import com.assignment.research.pipeline.PipelineStep;
import com.assignment.research.trace.TraceEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

public final class RunArchive implements PipelineObserver {

    private static final int FIRST_SNAPSHOT = 1;

    @Getter
    private final Path directory;
    private final ObjectMapper mapper;
    private final PrintStream console;
    private final AtomicInteger snapshotCounter = new AtomicInteger(FIRST_SNAPSHOT);

    public RunArchive(Path runsRoot, Clock clock, ObjectMapper mapper, PrintStream console) {
        this.mapper = mapper;
        this.console = console;
        var folder = LocalDateTime.now(clock).format(OutputConstants.RUN_FOLDER_FORMAT);
        this.directory = createDirectories(runsRoot.resolve(folder));
        createDirectories(directory.resolve(OutputConstants.CALLS_DIRECTORY));
        createDirectories(directory.resolve(OutputConstants.SNAPSHOTS_DIRECTORY));
    }

    @Override
    public void onTrace(TraceEntry entry) {
        var safeLabel = entry.getLabel().replace(OutputConstants.LABEL_SEPARATOR,
                OutputConstants.LABEL_SEPARATOR_REPLACEMENT);
        var calls = directory.resolve(OutputConstants.CALLS_DIRECTORY);
        write(calls.resolve(OutputConstants.CALL_PROMPT_FILE.formatted(entry.getSequence(), safeLabel)),
                OutputConstants.PROMPT_FILE_TEMPLATE.formatted(entry.getLabel(), entry.getSystemPrompt(),
                        entry.getUserPrompt()));
        write(calls.resolve(OutputConstants.CALL_RESPONSE_FILE.formatted(entry.getSequence(), safeLabel)),
                entry.getRawResponse());
        console.println(consoleLine(entry));
    }

    @Override
    public void onStep(PipelineStep step, BriefingState state) {
        var file = OutputConstants.SNAPSHOT_FILE.formatted(snapshotCounter.getAndIncrement(),
                step.name().toLowerCase(Locale.ROOT));
        writeJson(directory.resolve(OutputConstants.SNAPSHOTS_DIRECTORY).resolve(file), state);
        console.println(OutputConstants.CONSOLE_STEP_LINE.formatted(step, state.getRound(),
                state.getSubQuestions().size(), state.getClaims().size(), state.getGroups().size()));
    }

    public Path writeResult(BriefingResult result, String briefingMarkdown) {
        write(directory.resolve(OutputConstants.BRIEFING_FILE), briefingMarkdown);
        writeJson(directory.resolve(OutputConstants.STATE_FILE), result.getFinalState());
        writeJson(directory.resolve(OutputConstants.TRACE_FILE), result.getTrace());
        writeJson(directory.resolve(OutputConstants.RESULT_FILE), result);
        console.println(OutputConstants.CONSOLE_SUMMARY.formatted(
                result.getStopDecision().getReason(),
                result.getStopDecision().getExplanation(),
                result.getFinalState().getRound(),
                result.getTrace().size(),
                result.getUsage().getInputTokens(),
                result.getUsage().getOutputTokens(),
                result.getConfidence().getLevel(),
                directory.toAbsolutePath()));
        return directory;
    }

    private static String consoleLine(TraceEntry entry) {
        var seconds = entry.getDuration().toMillis() / OutputConstants.MILLIS_PER_SECOND;
        var line = String.format(Locale.ROOT, OutputConstants.CONSOLE_LINE, entry.getSequence(), entry.getLabel(),
                entry.getModel(), entry.getUsage().getInputTokens(), entry.getUsage().getOutputTokens(), seconds,
                entry.getStatus());
        return entry.getFailure().map(reason -> line + OutputConstants.CONSOLE_FAILURE_SUFFIX.formatted(reason))
                .orElse(line);
    }

    private void writeJson(Path file, Object value) {
        try {
            mapper.writeValue(file.toFile(), value);
        } catch (IOException failure) {
            throw new UncheckedIOException("cannot write " + file, failure);
        }
    }

    private static void write(Path file, String content) {
        try {
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("cannot write " + file, failure);
        }
    }

    private static Path createDirectories(Path path) {
        try {
            return Files.createDirectories(path);
        } catch (IOException failure) {
            throw new UncheckedIOException("cannot create " + path, failure);
        }
    }
}
