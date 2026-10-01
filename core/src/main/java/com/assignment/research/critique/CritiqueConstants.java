package com.assignment.research.critique;

public final class CritiqueConstants {

    public static final String TRACE_LABEL_FORMAT = "critic/round%d/pass%d";
    public static final int MAX_OUTPUT_TOKENS = 4096;

    public static final String QUERY_VARIABLE = "query";
    public static final String DRAFT_VARIABLE = "draft";
    public static final String EVIDENCE_VARIABLE = "evidence";
    public static final String GAPS_VARIABLE = "gaps";

    public static final String UNGROUNDED_STATEMENT_DETAIL =
            "The statement cites no evidence group, so nothing in the collected evidence supports it.";

    private CritiqueConstants() {
    }
}
