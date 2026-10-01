package com.assignment.research.reconciliation;

public final class ReconciliationConstants {

    public static final String GROUP_ID_FORMAT = "%s-g%d";
    public static final String SPLIT_GROUP_ID_FORMAT = "%s.%d";
    public static final String EMPTY_ASSERTION = "";
    public static final int FIRST_GROUP_NUMBER = 1;
    public static final int MIN_GROUPS_IN_CONFLICT = 2;
    public static final int RECENCY_THRESHOLD_YEARS = 3;

    public static final String TRACE_LABEL_FORMAT = "reconciler/%s/round%d";
    public static final String QUESTION_VARIABLE = "question";
    public static final String CLAIMS_VARIABLE = "claims";
    public static final int MIN_CLAIMS_WORTH_COMPARING = 2;
    public static final int MAX_OUTPUT_TOKENS = 4096;

    public static final String CLAIM_LINE = "[%s] (source %s, %s, %s, %s): %s";
    public static final String UNKNOWN = "unknown";
    public static final String LINE_SEPARATOR = "\n";

    private ReconciliationConstants() {
    }
}
