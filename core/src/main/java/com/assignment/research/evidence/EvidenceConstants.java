package com.assignment.research.evidence;

public final class EvidenceConstants {

    public static final String TRACE_LABEL_FORMAT = "researcher/%s/round%d";
    public static final String QUESTION_VARIABLE = "question";
    public static final String SOURCES_VARIABLE = "sources";
    public static final String CLAIM_ID_FORMAT = "%s-c%d";
    public static final int FIRST_CLAIM_NUMBER = 1;
    public static final int MAX_SEARCH_HITS = 5;
    public static final int MAX_OUTPUT_TOKENS = 4096;

    public static final String SOURCE_BLOCK = """
            [sourceId: %s]
            Title: %s
            Publisher: %s (%s)
            Published: %s
            Excerpt: %s
            """;
    public static final String BLOCK_SEPARATOR = "\n";
    public static final String STATEMENT_KEY_SEPARATOR = "|";

    private EvidenceConstants() {
    }
}
