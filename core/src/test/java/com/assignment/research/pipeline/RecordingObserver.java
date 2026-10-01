package com.assignment.research.pipeline;

import com.assignment.research.trace.TraceEntry;
import java.util.ArrayList;
import java.util.List;

final class RecordingObserver implements PipelineObserver {

    private final List<PipelineStep> steps = new ArrayList<>();
    private final List<TraceEntry> traces = new ArrayList<>();

    @Override
    public void onTrace(TraceEntry entry) {
        traces.add(entry);
    }

    @Override
    public void onStep(PipelineStep step, BriefingState stateAfterStep) {
        steps.add(step);
    }

    List<PipelineStep> getSteps() {
        return List.copyOf(steps);
    }

    List<TraceEntry> getTraces() {
        return List.copyOf(traces);
    }
}
