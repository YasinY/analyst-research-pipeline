package com.assignment.research.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.assignment.research.adapter.pricing.CostEstimator;
import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.trace.TraceEntry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ResearchRunTest {

    private static final String RUN_ID = "run-1";
    private static final String QUERY = "dry bulk outlook";
    private static final String PROVIDER = "anthropic";
    private static final String MODEL = "claude-sonnet-5-5";
    private static final String MARKDOWN = "# Briefing";
    private static final String CONFIDENCE = "HIGH";
    private static final String STOP_REASON = "CRITIC_SATISFIED";
    private static final String STOP_EXPLANATION = "no open findings";
    private static final String OUTPUT_DIRECTORY = "/runs/run-1";
    private static final String FAILURE = "boom";
    private static final String RESEARCHER_ROLE = "researcher";
    private static final String RESEARCHER_LABEL = "researcher/q1";
    private static final int RESEARCHER_SEQUENCE = 3;
    private static final int TWO_CALLS = 2;
    private static final int FOUR_CALLS = 4;

    @Test
    void startsRunningWithoutTerminalData() {
        var response = run().toResponse();

        assertThat(response.getStatus()).isEqualTo(RunStatus.RUNNING);
        assertThat(response.getProvider()).isEqualTo(PROVIDER);
        assertThat(response.getModel()).isEqualTo(MODEL);
        assertThat(response.getBriefingMarkdown()).isNull();
        assertThat(response.getError()).isNull();
        assertThat(response.getRoles()).isEmpty();
        assertThat(response.getTotals()).isEqualTo(new RunTotals(0, 0, 0, 0, 0, 0.0));
    }

    @Test
    void finishedSnapshotCarriesAllTerminalDataTogether() {
        var run = run();

        run.complete(new RunOutcome(RunStatus.FINISHED, MARKDOWN, CONFIDENCE, STOP_REASON, STOP_EXPLANATION,
                OUTPUT_DIRECTORY, null));
        var response = run.toResponse();

        assertThat(response.getStatus()).isEqualTo(RunStatus.FINISHED);
        assertThat(response.getBriefingMarkdown()).isEqualTo(MARKDOWN);
        assertThat(response.getConfidence()).isEqualTo(CONFIDENCE);
        assertThat(response.getStopReason()).isEqualTo(STOP_REASON);
        assertThat(response.getStopExplanation()).isEqualTo(STOP_EXPLANATION);
        assertThat(response.getOutputDirectory()).isEqualTo(OUTPUT_DIRECTORY);
        assertThat(response.getError()).isNull();
    }

    @Test
    void failedSnapshotCarriesTheError() {
        var run = run();

        run.complete(RunOutcome.failed(FAILURE));
        var response = run.toResponse();

        assertThat(response.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(response.getError()).isEqualTo(FAILURE);
        assertThat(response.getBriefingMarkdown()).isNull();
    }

    @Test
    void callLinesCarryCachedTokensAndCost() {
        var run = run();

        run.onTrace(BriefingFixtures.plannerEntry());
        var call = run.toResponse().getCalls().getFirst();

        assertThat(call.getInputTokens()).isEqualTo(BriefingFixtures.INPUT_TOKENS);
        assertThat(call.getCachedInputTokens()).isEqualTo(BriefingFixtures.CACHED_INPUT_TOKENS);
        assertThat(call.getOutputTokens()).isEqualTo(BriefingFixtures.OUTPUT_TOKENS);
        assertThat(call.getCostUsd()).isCloseTo(BriefingFixtures.CALL_COST_USD,
                within(BriefingFixtures.COST_TOLERANCE));
    }

    @Test
    void groupsCallsByRoleAndSumsTotals() {
        var run = run();

        run.onTrace(BriefingFixtures.plannerEntry());
        run.onTrace(researcherEntry());
        run.onTrace(BriefingFixtures.failedCriticEntry());
        run.onTrace(researcherEntry());
        var response = run.toResponse();

        assertThat(response.getRoles()).extracting(RoleLine::getRole)
                .containsExactly(BriefingFixtures.PLANNER_LABEL, RESEARCHER_ROLE, BriefingFixtures.CRITIC_LABEL);
        var researcher = response.getRoles().get(1);
        assertThat(researcher.getCalls()).isEqualTo(TWO_CALLS);
        assertThat(researcher.getInputTokens()).isEqualTo(TWO_CALLS * BriefingFixtures.INPUT_TOKENS);
        assertThat(researcher.getCachedInputTokens()).isEqualTo(TWO_CALLS * BriefingFixtures.CACHED_INPUT_TOKENS);
        assertThat(researcher.getOutputTokens()).isEqualTo(TWO_CALLS * BriefingFixtures.OUTPUT_TOKENS);
        assertThat(researcher.getDurationMillis()).isEqualTo(TWO_CALLS * BriefingFixtures.DURATION_MILLIS);
        assertThat(researcher.getCostUsd()).isCloseTo(TWO_CALLS * BriefingFixtures.CALL_COST_USD,
                within(BriefingFixtures.COST_TOLERANCE));
        var totals = response.getTotals();
        assertThat(totals.getCalls()).isEqualTo(FOUR_CALLS);
        assertThat(totals.getInputTokens()).isEqualTo(FOUR_CALLS * BriefingFixtures.INPUT_TOKENS);
        assertThat(totals.getCachedInputTokens()).isEqualTo(FOUR_CALLS * BriefingFixtures.CACHED_INPUT_TOKENS);
        assertThat(totals.getOutputTokens()).isEqualTo(FOUR_CALLS * BriefingFixtures.OUTPUT_TOKENS);
        assertThat(totals.getDurationMillis()).isEqualTo(FOUR_CALLS * BriefingFixtures.DURATION_MILLIS);
        assertThat(totals.getCostUsd()).isCloseTo(FOUR_CALLS * BriefingFixtures.CALL_COST_USD,
                within(BriefingFixtures.COST_TOLERANCE));
    }

    @Test
    void freeEstimatorReportsZeroCost() {
        var run = new ResearchRun(RUN_ID, QUERY, PROVIDER, MODEL, CostEstimator.free());

        run.onTrace(BriefingFixtures.plannerEntry());

        assertThat(run.toResponse().getTotals().getCostUsd()).isZero();
        assertThat(run.toResponse().getCalls().getFirst().getCostUsd()).isZero();
    }

    private static ResearchRun run() {
        return new ResearchRun(RUN_ID, QUERY, PROVIDER, MODEL, BriefingFixtures.costEstimator());
    }

    private static TraceEntry researcherEntry() {
        return new TraceEntry(RESEARCHER_SEQUENCE, RESEARCHER_LABEL, BriefingFixtures.STARTED_AT,
                Duration.ofMillis(BriefingFixtures.DURATION_MILLIS), BriefingFixtures.MODEL,
                BriefingFixtures.SYSTEM_PROMPT, BriefingFixtures.USER_PROMPT, BriefingFixtures.RAW_RESPONSE,
                new LLMUsage(BriefingFixtures.INPUT_TOKENS, BriefingFixtures.OUTPUT_TOKENS,
                        BriefingFixtures.CACHED_INPUT_TOKENS),
                LLMCallStatus.OK, null);
    }
}
