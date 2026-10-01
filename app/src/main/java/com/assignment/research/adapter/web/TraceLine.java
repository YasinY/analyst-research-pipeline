package com.assignment.research.adapter.web;

import com.assignment.research.trace.TraceEntry;
import lombok.NonNull;
import lombok.Value;

@Value
public class TraceLine {

    private final int sequence;
    @NonNull
    private final String label;
    @NonNull
    private final String model;
    private final int inputTokens;
    private final int outputTokens;
    private final long durationMillis;
    @NonNull
    private final String status;
    private final String failure;

    public static TraceLine from(TraceEntry entry) {
        return new TraceLine(entry.getSequence(), entry.getLabel(), entry.getModel(),
                entry.getUsage().getInputTokens(), entry.getUsage().getOutputTokens(),
                entry.getDuration().toMillis(), entry.getStatus().name(), entry.getFailureReason());
    }
}
