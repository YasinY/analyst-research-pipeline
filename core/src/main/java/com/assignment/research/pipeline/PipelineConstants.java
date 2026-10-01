package com.assignment.research.pipeline;

public final class PipelineConstants {

    public static final int FIRST_ROUND = 1;
    public static final int MAX_RESEARCH_ROUNDS = 2;
    public static final int MAX_REWRITES_PER_ROUND = 1;
    public static final int MAX_LLM_CALLS = 30;
    public static final int FIRST_CRITIQUE_PASS = 1;
    public static final String FOLLOW_UP_QUESTION_ID_FORMAT = "q%d";
    public static final String FOLLOW_UP_QUESTION_FALLBACK = "What does the evidence say about: %s?";
    public static final String KEYWORD_SEPARATOR = ", ";
    public static final String NO_INTERPRETATION = "";
    public static final int NO_REWRITES = 0;
    public static final int SINGLE_REWRITE = 1;

    public static final String EXPLANATION_APPROVED =
            "The reviewer approved the briefing and no sub-question that could still be researched remained open.";
    public static final String EXPLANATION_ROUND_LIMIT =
            "The maximum of %d research rounds was reached while %d sub-question(s) still lacked adequate evidence.";
    public static final String EXPLANATION_REWRITE_LIMIT =
            "The briefing was rewritten %d time(s) in the last round and the reviewer still had %d open finding(s).";
    public static final String EXPLANATION_CALL_BUDGET =
            "The budget of %d model calls was exhausted before the reviewer approved the briefing.";
    public static final String EXPLANATION_NO_NEW_EVIDENCE =
            "Further research was requested, but every open sub-question had already been searched without new evidence.";
    public static final String EXPLANATION_AGENT_FAILURE =
            "A pipeline step failed and could not be recovered: %s. The briefing is delivered as far as it got.";

    public static final String FAILURE_RESEARCHER_FORMAT = "researcher/%s";
    public static final String FAILURE_RECONCILER = "reconciler";
    public static final String FAILURE_SYNTHESIZER = "synthesizer";
    public static final String FAILURE_CRITIC = "critic";

    public static final double NO_SCORE = 0.0;
    public static final int MEDIAN_HALVES = 2;
    public static final String REASON_KEY_FACTS = "%d key fact(s) with a median evidence score of %.2f";
    public static final String REASON_NO_KEY_FACTS = "no key fact rests on adequate evidence";
    public static final String REASON_COVERAGE = "%d of %d sub-question(s) covered by adequate evidence";
    public static final String REASON_COVERAGE_CAP = "confidence capped at MEDIUM because of uncovered sub-questions";
    public static final String REASON_OPEN_FINDINGS = "%d open review finding(s), %d of them major";
    public static final String REASON_MAJOR_CAP = "confidence capped at LOW because a major review finding is open";
    public static final String REASON_UNVERIFIED = "confidence capped at LOW because the review step failed";
    public static final String REASON_APPROVED = "the reviewer approved the final draft";

    private PipelineConstants() {
    }
}
