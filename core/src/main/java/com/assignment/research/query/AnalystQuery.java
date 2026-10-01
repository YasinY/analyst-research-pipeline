package com.assignment.research.query;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public final class AnalystQuery {

    private final String text;

    public AnalystQuery(@NonNull String text) {
        if (text.isBlank()) {
            throw new IllegalArgumentException("analyst query must not be blank");
        }
        this.text = text.strip();
    }
}
