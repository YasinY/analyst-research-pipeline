package com.assignment.research.adapter.web;

import com.assignment.research.adapter.pricing.CostEstimator;
import com.assignment.research.adapter.pricing.ModelPrice;
import com.assignment.research.adapter.pricing.PricingTable;
import com.assignment.research.bootstrap.RunSettings;
import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.pipeline.BriefingConfidence;
import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.StopDecision;
import com.assignment.research.pipeline.StopReason;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.synthesis.GroundedStatement;
import com.assignment.research.trace.TraceEntry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class BriefingFixtures {

    public static final String QUERY = "dry bulk outlook";
    public static final String SUMMARY = "Rates are firming.";
    public static final String STOP_EXPLANATION = "critic approved the draft";
    public static final String MODEL = "test-model";
    public static final String SYSTEM_PROMPT = "system prompt";
    public static final String USER_PROMPT = "user prompt";
    public static final String RAW_RESPONSE = "{\"ok\":true}";
    public static final String PLANNER_LABEL = "planner";
    public static final String CRITIC_LABEL = "critic";
    public static final String FAILURE_REASON = "schema mismatch";
    public static final int FIRST_SEQUENCE = 1;
    public static final int SECOND_SEQUENCE = 2;
    public static final int INPUT_TOKENS = 120;
    public static final int OUTPUT_TOKENS = 30;
    public static final int CACHED_INPUT_TOKENS = 100;
    public static final double INPUT_PRICE_PER_MILLION = 2.0;
    public static final double CACHED_INPUT_PRICE_PER_MILLION = 0.2;
    public static final double OUTPUT_PRICE_PER_MILLION = 10.0;
    public static final double CALL_COST_USD = 0.00036;
    public static final double COST_TOLERANCE = 1e-12;
    public static final long DURATION_MILLIS = 1500;
    public static final double MEDIAN_SCORE = 0.8;
    public static final String NO_OVERRIDE = "";
    public static final RunSettings SERVER_DEFAULTS = new RunSettings(null, NO_OVERRIDE, NO_OVERRIDE, NO_OVERRIDE);
    public static final Instant STARTED_AT = Instant.parse("2026-05-20T10:15:30Z");

    private BriefingFixtures() {
    }

    public static BriefingState state() {
        return BriefingState.initial(new AnalystQuery(QUERY));
    }

    public static TraceEntry plannerEntry() {
        return entry(FIRST_SEQUENCE, PLANNER_LABEL, LLMCallStatus.OK, null);
    }

    public static TraceEntry failedCriticEntry() {
        return entry(SECOND_SEQUENCE, CRITIC_LABEL, LLMCallStatus.FAILED, FAILURE_REASON);
    }

    public static CostEstimator costEstimator() {
        return new CostEstimator(new PricingTable(List.of(new ModelPrice(MODEL, INPUT_PRICE_PER_MILLION,
                CACHED_INPUT_PRICE_PER_MILLION, OUTPUT_PRICE_PER_MILLION))));
    }

    public static BriefingResult result() {
        var usage = new LLMUsage(INPUT_TOKENS, OUTPUT_TOKENS);
        var draft = new BriefingDraft(List.of(new GroundedStatement(SUMMARY, List.of())), List.of(), List.of(),
                List.of(), List.of(), List.of());
        return new BriefingResult(
                draft,
                new BriefingConfidence(ConfidenceLevel.HIGH, MEDIAN_SCORE, List.of()),
                List.of(),
                List.of(),
                new StopDecision(StopReason.APPROVED, STOP_EXPLANATION),
                state(),
                List.of(plannerEntry(), failedCriticEntry()),
                usage);
    }

    private static TraceEntry entry(int sequence, String label, LLMCallStatus status, String failureReason) {
        return new TraceEntry(sequence, label, STARTED_AT, Duration.ofMillis(DURATION_MILLIS), MODEL, SYSTEM_PROMPT,
                USER_PROMPT, RAW_RESPONSE, new LLMUsage(INPUT_TOKENS, OUTPUT_TOKENS, CACHED_INPUT_TOKENS), status,
                failureReason);
    }
}
