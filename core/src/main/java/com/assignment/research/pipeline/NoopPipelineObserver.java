package com.assignment.research.pipeline;

import com.assignment.research.trace.TraceEntry;

public final class NoopPipelineObserver implements PipelineObserver {

    @Override
    public void onTrace(TraceEntry entry) {
    }

    @Override
    public void onStep(PipelineStep step, BriefingState stateAfterStep) {
    }
}
