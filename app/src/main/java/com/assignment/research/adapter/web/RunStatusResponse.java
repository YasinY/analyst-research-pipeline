package com.assignment.research.adapter.web;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class RunStatusResponse {

    @NonNull
    private final String id;
    @NonNull
    private final String query;
    @NonNull
    private final RunStatus status;
    @NonNull
    private final List<StepLine> steps;
    @NonNull
    private final List<TraceLine> calls;
    private final String briefingMarkdown;
    private final String confidence;
    private final String stopReason;
    private final String stopExplanation;
    private final String outputDirectory;
    private final String error;
}
