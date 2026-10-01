package com.assignment.research.adapter.web;

import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.PipelineObserver;
import com.assignment.research.pipeline.PipelineStep;
import com.assignment.research.trace.TraceEntry;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;

public final class ResearchRun implements PipelineObserver {

    @Getter
    private final String id;
    @Getter
    private final String query;
    private final List<TraceEntry> entries = new CopyOnWriteArrayList<>();
    private final List<StepLine> steps = new CopyOnWriteArrayList<>();
    private final AtomicReference<RunStatus> status = new AtomicReference<>(RunStatus.RUNNING);
    private final AtomicReference<BriefingResult> result = new AtomicReference<>();
    private final AtomicReference<String> briefingMarkdown = new AtomicReference<>();
    private final AtomicReference<String> outputDirectory = new AtomicReference<>();
    private final AtomicReference<String> error = new AtomicReference<>();

    public ResearchRun(String id, String query) {
        this.id = id;
        this.query = query;
    }

    @Override
    public void onTrace(TraceEntry entry) {
        entries.add(entry);
    }

    @Override
    public void onStep(PipelineStep step, BriefingState stateAfterStep) {
        steps.add(StepLine.from(step, stateAfterStep, entries.size()));
    }

    public void finish(BriefingResult briefingResult, String markdown, String directory) {
        result.set(briefingResult);
        briefingMarkdown.set(markdown);
        outputDirectory.set(directory);
        status.set(RunStatus.FINISHED);
    }

    public void fail(String message) {
        error.set(message);
        status.set(RunStatus.FAILED);
    }

    public Optional<CallDetail> call(int sequence) {
        return entries.stream().filter(entry -> entry.getSequence() == sequence).findFirst().map(CallDetail::from);
    }

    public RunStatusResponse toResponse() {
        var finished = result.get();
        return new RunStatusResponse(
                id,
                query,
                status.get(),
                List.copyOf(steps),
                entries.stream().map(TraceLine::from).toList(),
                briefingMarkdown.get(),
                finished == null ? null : finished.getConfidence().getLevel().name(),
                finished == null ? null : finished.getStopDecision().getReason().name(),
                finished == null ? null : finished.getStopDecision().getExplanation(),
                outputDirectory.get(),
                error.get());
    }
}
