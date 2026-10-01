package com.assignment.research.pipeline;

import lombok.NonNull;
import lombok.Value;

@Value
public class AgentFailure {

    @NonNull
    private final String agent;
    private final int round;
    @NonNull
    private final String message;
}
