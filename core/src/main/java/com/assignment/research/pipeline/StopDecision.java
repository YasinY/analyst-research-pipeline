package com.assignment.research.pipeline;

import lombok.NonNull;
import lombok.Value;

@Value
public class StopDecision {

    @NonNull
    private final StopReason reason;
    @NonNull
    private final String explanation;
}
