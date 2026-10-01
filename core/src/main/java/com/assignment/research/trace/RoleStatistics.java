package com.assignment.research.trace;

import com.assignment.research.llm.LLMUsage;
import java.time.Duration;
import lombok.NonNull;
import lombok.Value;

@Value
public class RoleStatistics {

    @NonNull
    private final String role;
    private final int calls;
    @NonNull
    private final LLMUsage usage;
    @NonNull
    private final Duration duration;

    public static RoleStatistics empty(String role) {
        return new RoleStatistics(role, TraceConstants.NO_CALLS, LLMUsage.NONE, Duration.ZERO);
    }

    public RoleStatistics plus(TraceEntry entry) {
        return new RoleStatistics(role, calls + TraceConstants.ONE_CALL, usage.plus(entry.getUsage()),
                duration.plus(entry.getDuration()));
    }
}
