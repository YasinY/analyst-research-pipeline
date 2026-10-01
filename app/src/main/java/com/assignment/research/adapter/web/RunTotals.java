package com.assignment.research.adapter.web;

import java.util.List;
import lombok.Value;

@Value
public class RunTotals {

    private final int calls;
    private final int inputTokens;
    private final int cachedInputTokens;
    private final int outputTokens;
    private final long durationMillis;
    private final double costUsd;

    public static RunTotals from(List<RoleLine> roles) {
        return new RunTotals(
                roles.stream().mapToInt(RoleLine::getCalls).sum(),
                roles.stream().mapToInt(RoleLine::getInputTokens).sum(),
                roles.stream().mapToInt(RoleLine::getCachedInputTokens).sum(),
                roles.stream().mapToInt(RoleLine::getOutputTokens).sum(),
                roles.stream().mapToLong(RoleLine::getDurationMillis).sum(),
                roles.stream().mapToDouble(RoleLine::getCostUsd).sum());
    }
}
