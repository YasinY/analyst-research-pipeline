package com.assignment.research.pipeline;

import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

@Value
public class Transition {

    @NonNull
    private final PipelineStep step;
    private final StopDecision stopDecision;

    public static Transition to(PipelineStep step) {
        return new Transition(step, null);
    }

    public static Transition finish(StopReason reason, String explanation) {
        return new Transition(PipelineStep.FINISH, new StopDecision(reason, explanation));
    }

    public Optional<StopDecision> getStopDecision() {
        return Optional.ofNullable(stopDecision);
    }
}
