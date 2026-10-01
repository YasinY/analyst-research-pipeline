package com.assignment.research.domain;

public record AnalystQuery(String text) {

    public AnalystQuery {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("analyst query must not be blank");
        }
        text = text.strip();
    }
}
