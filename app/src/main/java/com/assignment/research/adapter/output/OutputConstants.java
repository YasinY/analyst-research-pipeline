package com.assignment.research.adapter.output;

import java.time.format.DateTimeFormatter;

public final class OutputConstants {

    public static final DateTimeFormatter RUN_FOLDER_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss-SSS");
    public static final String RUN_FOLDER_WITH_SUFFIX = "%s-%d";
    public static final int FIRST_RUN_FOLDER_SUFFIX = 2;
    public static final int FIRST_SNAPSHOT = 1;
    public static final String WRITE_FAILURE = "cannot write %s";
    public static final String CREATE_FAILURE = "cannot create %s";
    public static final String BRIEFING_FILE = "briefing.md";
    public static final String STATE_FILE = "state.json";
    public static final String TRACE_FILE = "trace.json";
    public static final String RESULT_FILE = "result.json";
    public static final String CALLS_DIRECTORY = "calls";
    public static final String SNAPSHOTS_DIRECTORY = "state";
    public static final String CALL_PROMPT_FILE = "%02d-%s.prompt.md";
    public static final String CALL_RESPONSE_FILE = "%02d-%s.response.md";
    public static final String SNAPSHOT_FILE = "%02d-after-%s.json";
    public static final String LABEL_SEPARATOR_REPLACEMENT = "_";
    public static final String LABEL_SEPARATOR = "/";
    public static final String PROMPT_FILE_TEMPLATE = """
            # %s

            ## System prompt

            %s

            ## User prompt

            %s
            """;
    public static final String CONSOLE_LINE = "[%02d] %-32s %-22s in=%-6d out=%-6d %5.1fs  %s";
    public static final String CONSOLE_FAILURE_SUFFIX = "  (%s)";
    public static final String CONSOLE_STEP_LINE = "---- %s -> round %d, %d sub-question(s), %d claim(s), %d group(s)";
    public static final String CONSOLE_SUMMARY = """

            Run finished: %s
            Reason: %s
            Rounds: %d | LLM calls: %d | tokens in: %d | tokens out: %d | confidence: %s
            Output: %s
            """;
    public static final double MILLIS_PER_SECOND = 1000.0;

    public static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final String NEWLINE = "\n";
    public static final String EMPTY = "";
    public static final String BULLET = "- ";
    public static final String NONE_PLACEHOLDER = "_None._";
    public static final String BRIEFING_TITLE = "# Analyst briefing";
    public static final String QUERY_LINE = "> **Query:** %s";
    public static final String GENERATED_LINE = "> **Generated:** %s";
    public static final String OVERALL_CONFIDENCE_LINE = "> **Overall confidence:** %s";
    public static final String SCOPE_LINE = "> **Scope as understood by the system:** %s";
    public static final String SECTION_HEADING = "## %s";
    public static final String HEADING_SUB_QUESTIONS = "Sub-questions investigated";
    public static final String HEADING_SUMMARY = "Summary";
    public static final String HEADING_KEY_FACTS = "Key facts";
    public static final String HEADING_UNCERTAINTIES = "Identified uncertainties";
    public static final String HEADING_CONFIDENCE = "Confidence: %s";
    public static final String HEADING_OPEN_FINDINGS = "Open review findings";
    public static final String HEADING_FOLLOW_UPS = "Suggested follow-up questions";
    public static final String HEADING_HOW_PRODUCED = "How this briefing was produced";
    public static final String HEADING_SOURCES = "Sources consulted";
    public static final String GAPS_HEADING = "### Sub-questions without adequate evidence";
    public static final String SUB_QUESTION_LINE = "- %s: %s%s";
    public static final String GAP_LINE = "- %s: %s";
    public static final String COVERAGE_OK = " (adequate evidence found)";
    public static final String COVERAGE_GAP = " (no adequate evidence found)";
    public static final String GROUP_REF = " [%s]";
    public static final String EVIDENCE_NOTE =
            "  - evidence: %d independent source(s), best tier %s, newest %s, conflict %s, confidence %s (%.2f)";
    public static final String CONFIDENCE_INTRO = "Derived from the run record, not asserted by a model:";
    public static final String REVIEWER_APPROVED = "_The independent reviewer approved the final draft._";
    public static final String FINDING_LINE = "- **%s / %s** on \"%s\": %s";
    public static final String STOP_REASON_LINE = "- Stop reason: %s. %s";
    public static final String ROUNDS_LINE = "- Research rounds: %d";
    public static final String MODEL_CALLS_LINE = "- Model calls: %d, tokens in: %d, tokens out: %d";
    public static final String CLAIMS_LINE = "- Claims extracted: %d, evidence groups: %d";
    public static final String DROPPED_STATEMENTS_LINE = "- Statements removed for lacking evidence: %d";
    public static final String DEMOTED_KEY_FACTS_LINE = "- Key facts demoted to uncertainties for weak evidence: %d";
    public static final String DEGRADED_STEPS_LINE = "- Degraded steps:";
    public static final String FAILURE_LINE = "  - round %d, %s: %s";
    public static final String SOURCES_DISCLAIMER =
            "_All sources are part of a fictional mock corpus built for this exercise._";
    public static final String SOURCE_LINE = "- `%s` %s, %s (%s, tier %s), %s%s";
    public static final String CITES_SUFFIX = ", cites `%s`";

    private OutputConstants() {
    }
}
