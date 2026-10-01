package com.assignment.research.adapter.web;

import com.assignment.research.pipeline.BriefingResult;
import lombok.NonNull;
import lombok.Value;

@Value
public class RunOutcome {

    @NonNull
    private final RunStatus status;
    private final String briefingMarkdown;
    private final String confidence;
    private final String stopReason;
    private final String stopExplanation;
    private final String outputDirectory;
    private final String error;

    public static RunOutcome running() {
        return new RunOutcome(RunStatus.RUNNING, null, null, null, null, null, null);
    }

    public static RunOutcome finished(BriefingResult result, String markdown, String outputDirectory) {
        var stopDecision = result.getStopDecision();
        return new RunOutcome(RunStatus.FINISHED, markdown, result.getConfidence().getLevel().name(),
                stopDecision.getReason().name(), stopDecision.getExplanation(), outputDirectory, null);
    }

    public static RunOutcome failed(String error) {
        return new RunOutcome(RunStatus.FAILED, null, null, null, null, null, error);
    }
}
