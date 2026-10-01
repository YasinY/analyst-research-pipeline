package com.assignment.research.trace;

import com.assignment.research.llm.LlmCallStatus;
import com.assignment.research.llm.LlmUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

@Value
public class TraceEntry {

    int sequence;
    @NonNull String label;
    @NonNull Instant startedAt;
    @NonNull Duration duration;
    @NonNull String model;
    @NonNull String systemPrompt;
    @NonNull String userPrompt;
    @NonNull String rawResponse;
    @NonNull LlmUsage usage;
    @NonNull LlmCallStatus status;
    String failureReason;

    public Optional<String> getFailure() {
        return Optional.ofNullable(failureReason);
    }
}
