package com.assignment.research.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResearchRunTest {

    private static final String RUN_ID = "run-1";
    private static final String QUERY = "dry bulk outlook";
    private static final String MARKDOWN = "# Briefing";
    private static final String CONFIDENCE = "HIGH";
    private static final String STOP_REASON = "CRITIC_SATISFIED";
    private static final String STOP_EXPLANATION = "no open findings";
    private static final String OUTPUT_DIRECTORY = "/runs/run-1";
    private static final String FAILURE = "boom";

    @Test
    void startsRunningWithoutTerminalData() {
        var response = new ResearchRun(RUN_ID, QUERY).toResponse();

        assertThat(response.getStatus()).isEqualTo(RunStatus.RUNNING);
        assertThat(response.getBriefingMarkdown()).isNull();
        assertThat(response.getError()).isNull();
    }

    @Test
    void finishedSnapshotCarriesAllTerminalDataTogether() {
        var run = new ResearchRun(RUN_ID, QUERY);

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
        var run = new ResearchRun(RUN_ID, QUERY);

        run.complete(RunOutcome.failed(FAILURE));
        var response = run.toResponse();

        assertThat(response.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(response.getError()).isEqualTo(FAILURE);
        assertThat(response.getBriefingMarkdown()).isNull();
    }
}
