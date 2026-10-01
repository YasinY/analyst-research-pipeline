package com.assignment.research.adapter.web;

import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.PipelineStep;
import lombok.NonNull;
import lombok.Value;

@Value
public class StepLine {

    @NonNull
    private final String step;
    private final int round;
    private final int subQuestions;
    private final int claims;
    private final int groups;
    private final int callsSoFar;

    public static StepLine from(PipelineStep step, BriefingState state, int callsSoFar) {
        return new StepLine(step.name(), state.getRound(), state.getSubQuestions().size(),
                state.getClaims().size(), state.getGroups().size(), callsSoFar);
    }
}
