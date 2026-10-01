package com.assignment.research.adapter.web;

import com.assignment.research.adapter.pricing.CostEstimator;
import com.assignment.research.trace.TraceEntry;
import com.assignment.research.trace.TraceStatistics;
import lombok.NonNull;
import lombok.Value;

@Value
public class TraceLine {

    private final int sequence;
    @NonNull
    private final String label;
    @NonNull
    private final String role;
    private final int round;
    private final long startedAtMillis;
    @NonNull
    private final String model;
    private final int inputTokens;
    private final int cachedInputTokens;
    private final int outputTokens;
    private final long durationMillis;
    private final double costUsd;
    @NonNull
    private final String status;
    private final String failure;

    public static TraceLine from(TraceEntry entry, CostEstimator costEstimator) {
        var usage = entry.getUsage();
        var label = entry.getLabel();
        return new TraceLine(entry.getSequence(), label, TraceStatistics.roleOf(label), TraceStatistics.roundOf(label),
                entry.getStartedAt().toEpochMilli(), entry.getModel(), usage.getInputTokens(),
                usage.getCachedInputTokens(), usage.getOutputTokens(), entry.getDuration().toMillis(),
                costEstimator.estimate(entry).getTotal(), entry.getStatus().name(), entry.getFailureReason());
    }
}
