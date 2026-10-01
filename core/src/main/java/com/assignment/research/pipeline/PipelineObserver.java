package com.assignment.research.pipeline;

import com.assignment.research.trace.TraceEntry;

public interface PipelineObserver {

    void onTrace(TraceEntry entry);

    void onStep(PipelineStep step, BriefingState stateAfterStep);
}
