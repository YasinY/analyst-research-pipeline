package com.assignment.research.adapter.web;

import com.assignment.research.trace.RoleStatistics;
import lombok.NonNull;
import lombok.Value;

@Value
public class RoleLine {

    @NonNull
    private final String role;
    private final int calls;
    private final int inputTokens;
    private final int cachedInputTokens;
    private final int outputTokens;
    private final long durationMillis;
    private final double costUsd;

    public static RoleLine from(RoleStatistics statistics, double costUsd) {
        var usage = statistics.getUsage();
        return new RoleLine(statistics.getRole(), statistics.getCalls(), usage.getInputTokens(),
                usage.getCachedInputTokens(), usage.getOutputTokens(), statistics.getDuration().toMillis(), costUsd);
    }
}
