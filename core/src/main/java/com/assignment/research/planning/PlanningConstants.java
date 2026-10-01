package com.assignment.research.planning;

import java.util.regex.Pattern;

public final class PlanningConstants {

    public static final String TRACE_LABEL = "planner";
    public static final String QUERY_VARIABLE = "query";
    public static final String SUB_QUESTION_ID_FORMAT = "q%d";
    public static final int FIRST_ID = 1;
    public static final int MAX_SUB_QUESTIONS = 5;
    public static final int MAX_OUTPUT_TOKENS = 1024;
    public static final int MIN_FALLBACK_KEYWORD_LENGTH = 4;
    public static final Pattern WORD_SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");

    private PlanningConstants() {
    }
}
