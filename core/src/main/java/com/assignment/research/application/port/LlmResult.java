package com.assignment.research.application.port;

import com.assignment.research.domain.LlmCallStatus;
import com.assignment.research.domain.LlmUsage;
import java.util.Objects;

public record LlmResult<T>(T value, String rawText, String model, LlmUsage usage, LlmCallStatus status) {

    public LlmResult {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(rawText, "rawText");
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(usage, "usage");
        Objects.requireNonNull(status, "status");
        if (status == LlmCallStatus.FAILED) {
            throw new IllegalArgumentException("a failed call is reported as an exception, not as a result");
        }
    }
}
