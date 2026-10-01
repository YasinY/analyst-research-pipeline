package com.assignment.research.synthesis;

public final class SynthesisConstants {

    public static final String FIRST_DRAFT_LABEL_FORMAT = "synthesizer/round%d";
    public static final String REVISION_LABEL_FORMAT = "synthesizer/round%d/revision";
    public static final int MAX_OUTPUT_TOKENS = 6144;

    public static final String QUERY_VARIABLE = "query";
    public static final String INTERPRETATION_VARIABLE = "interpretation";
    public static final String EVIDENCE_VARIABLE = "evidence";
    public static final String WEAK_EVIDENCE_VARIABLE = "weakEvidence";
    public static final String GAPS_VARIABLE = "gaps";
    public static final String REVISION_VARIABLE = "revision";

    public static final String NONE_PLACEHOLDER = "(none)";
    public static final String FIRST_DRAFT_NOTE = "This is the first draft. There is no previous draft to revise.";
    public static final String LINE_SEPARATOR = "\n";
    public static final String EVIDENCE_LINE =
            "[%s] confidence %s (%.2f): %s | %d independent source(s), best tier %s, newest %s, conflict %s";
    public static final String CONFLICT_SUFFIX = " | conflicts with %s: %s";
    public static final String GAP_LINE = "[%s] %s";
    public static final String STATEMENT_LINE = "- %s (groups: %s)";
    public static final String FINDING_LINE = "- %s %s on \"%s\": %s";
    public static final String SECTION_FORMAT = "%s:%n%s";
    public static final String PREVIOUS_DRAFT_HEADING = "Previous draft";
    public static final String FINDINGS_HEADING = "Review findings to resolve";
    public static final String SUMMARY_HEADING = "Summary";
    public static final String KEY_FACTS_HEADING = "Key facts";
    public static final String UNCERTAINTIES_HEADING = "Uncertainties";
    public static final String FOLLOW_UPS_HEADING = "Follow-up questions";
    public static final String GROUP_ID_SEPARATOR = ", ";

    private SynthesisConstants() {
    }
}
