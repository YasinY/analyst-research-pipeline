package com.assignment.research.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record TraceEntry(
        int sequence,
        String label,
        Instant startedAt,
        Duration duration,
        String model,
        String systemPrompt,
        String userPrompt,
        String rawResponse,
        LlmUsage usage,
        LlmCallStatus status,
        String failureReason) {

    public TraceEntry {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(systemPrompt, "systemPrompt");
        Objects.requireNonNull(userPrompt, "userPrompt");
        Objects.requireNonNull(rawResponse, "rawResponse");
        Objects.requireNonNull(usage, "usage");
        Objects.requireNonNull(status, "status");
    }

    public Optional<String> failure() {
        return Optional.ofNullable(failureReason);
    }
}
