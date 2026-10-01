package com.assignment.research.adapter.output;

import java.time.format.DateTimeFormatter;

public final class OutputConstants {

    public static final DateTimeFormatter RUN_FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
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

    private OutputConstants() {
    }
}
