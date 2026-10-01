package com.assignment.research.adapter.web;

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
    private final AtomicReference<RunOutcome> outcome = new AtomicReference<>(RunOutcome.running());

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

    public void complete(RunOutcome terminalOutcome) {
        outcome.set(terminalOutcome);
    }

    public Optional<CallDetail> call(int sequence) {
        return entries.stream().filter(entry -> entry.getSequence() == sequence).findFirst().map(CallDetail::from);
    }

    public RunStatusResponse toResponse() {
        var snapshot = outcome.get();
        return new RunStatusResponse(
                id,
                query,
                snapshot.getStatus(),
                List.copyOf(steps),
                entries.stream().map(TraceLine::from).toList(),
                snapshot.getBriefingMarkdown(),
                snapshot.getConfidence(),
                snapshot.getStopReason(),
                snapshot.getStopExplanation(),
                snapshot.getOutputDirectory(),
                snapshot.getError());
    }
}
