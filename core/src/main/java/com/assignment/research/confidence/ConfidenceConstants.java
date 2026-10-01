package com.assignment.research.confidence;

public final class ConfidenceConstants {

    public static final double TIER_A_BASE = 0.60;
    public static final double TIER_B_BASE = 0.40;
    public static final double TIER_C_BASE = 0.15;

    public static final double CORROBORATION_BONUS_PER_SOURCE = 0.15;
    public static final double CORROBORATION_BONUS_CAP = 0.30;

    public static final int STALE_AFTER_YEARS = 3;
    public static final double STALE_PENALTY = -0.20;

    public static final double OPEN_CONFLICT_PENALTY = -0.30;
    public static final double RESOLVED_CONFLICT_PENALTY = -0.10;
    public static final double SUPERSEDED_PENALTY = -0.50;

    public static final double HIGH_THRESHOLD = 0.70;
    public static final double MEDIUM_THRESHOLD = 0.40;

    public static final double MIN_SCORE = 0.0;
    public static final double MAX_SCORE = 1.0;
    public static final int SINGLE_SOURCE = 1;

    public static final String TIER_LABEL = "best source tier %s";
    public static final String CORROBORATION_LABEL = "%d additional independent source(s)";
    public static final String STALE_LABEL = "newest source older than %d years";
    public static final String OPEN_CONFLICT_LABEL = "open conflict with another source group";
    public static final String RESOLVED_CONFLICT_LABEL = "conflict resolved in favour of this newer group";
    public static final String SUPERSEDED_LABEL = "superseded by a newer conflicting group";

    private ConfidenceConstants() {
    }
}
