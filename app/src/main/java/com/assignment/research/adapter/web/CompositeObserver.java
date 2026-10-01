package com.assignment.research.adapter.web;

import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.PipelineObserver;
import com.assignment.research.pipeline.PipelineStep;
import com.assignment.research.trace.TraceEntry;
import java.util.List;

public final class CompositeObserver implements PipelineObserver {

    private final List<PipelineObserver> observers;

    public CompositeObserver(PipelineObserver... observers) {
        this.observers = List.of(observers);
    }

    @Override
    public void onTrace(TraceEntry entry) {
        observers.forEach(observer -> observer.onTrace(entry));
    }

    @Override
    public void onStep(PipelineStep step, BriefingState stateAfterStep) {
        observers.forEach(observer -> observer.onStep(step, stateAfterStep));
    }
}
