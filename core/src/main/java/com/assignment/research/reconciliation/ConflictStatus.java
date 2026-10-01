package com.assignment.research.reconciliation;

public enum ConflictStatus {
    NONE,
    RESOLVED_BY_RECENCY,
    SUPERSEDED,
    OPEN;

    public ConflictStatus worseOf(ConflictStatus other) {
        return ordinal() >= other.ordinal() ? this : other;
    }
}
