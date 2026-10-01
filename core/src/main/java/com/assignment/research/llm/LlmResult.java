package com.assignment.research.llm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public final class LlmResult<T> {

    private final T value;
    private final String rawText;
    private final String model;
    private final LlmUsage usage;
    private final LlmCallStatus status;

    public LlmResult(@NonNull T value, @NonNull String rawText, @NonNull String model, @NonNull LlmUsage usage,
            @NonNull LlmCallStatus status) {
        if (status == LlmCallStatus.FAILED) {
            throw new IllegalArgumentException("a failed call is reported as an exception, not as a result");
        }
        this.value = value;
        this.rawText = rawText;
        this.model = model;
        this.usage = usage;
        this.status = status;
    }
}
