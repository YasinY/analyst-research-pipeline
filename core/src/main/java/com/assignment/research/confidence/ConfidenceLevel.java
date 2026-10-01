package com.assignment.research.confidence;

public enum ConfidenceLevel {
    LOW,
    MEDIUM,
    HIGH;

    public boolean isAtLeast(ConfidenceLevel other) {
        return ordinal() >= other.ordinal();
    }

    public ConfidenceLevel cappedAt(ConfidenceLevel cap) {
        return ordinal() <= cap.ordinal() ? this : cap;
    }
}
