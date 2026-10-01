package com.assignment.research.trace;

import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

@Value
public class TraceEntry {

    private final int sequence;
    @NonNull
    private final String label;
    @NonNull
    private final Instant startedAt;
    @NonNull
    private final Duration duration;
    @NonNull
    private final String model;
    @NonNull
    private final String systemPrompt;
    @NonNull
    private final String userPrompt;
    @NonNull
    private final String rawResponse;
    @NonNull
    private final LLMUsage usage;
    @NonNull
    private final LLMCallStatus status;
    private final String failureReason;

    public Optional<String> getFailure() {
        return Optional.ofNullable(failureReason);
    }
}
