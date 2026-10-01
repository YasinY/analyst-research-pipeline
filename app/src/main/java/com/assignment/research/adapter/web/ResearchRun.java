package com.assignment.research.adapter.web;

import com.assignment.research.adapter.pricing.CostEstimator;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.PipelineObserver;
import com.assignment.research.pipeline.PipelineStep;
import com.assignment.research.trace.TraceEntry;
import com.assignment.research.trace.TraceStatistics;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.Getter;

public final class ResearchRun implements PipelineObserver {

    @Getter
    private final String id;
    @Getter
    private final String query;
    private final String provider;
    private final String model;
    private final CostEstimator costEstimator;
    private final List<TraceEntry> entries = new CopyOnWriteArrayList<>();
    private final List<StepLine> steps = new CopyOnWriteArrayList<>();
    private final AtomicReference<RunOutcome> outcome = new AtomicReference<>(RunOutcome.running());

    public ResearchRun(String id, String query, String provider, String model, CostEstimator costEstimator) {
        this.id = id;
        this.query = query;
        this.provider = provider;
        this.model = model;
        this.costEstimator = costEstimator;
    }

    @Override
    public void onTrace(TraceEntry entry) {
        entries.add(entry);
    }

    @Override
    public void onStep(PipelineStep step, BriefingState stateAfterStep) {
        steps.add(StepLine.from(step, stateAfterStep, entries.size()));
    }

    public void complete(RunOutcome terminalOutcome) {
        outcome.set(terminalOutcome);
    }

    public Optional<CallDetail> call(int sequence) {
        return entries.stream().filter(entry -> entry.getSequence() == sequence).findFirst().map(CallDetail::from);
    }

    public RunStatusResponse toResponse() {
        var snapshot = outcome.get();
        var entrySnapshot = List.copyOf(entries);
        var roles = roleLines(entrySnapshot);
        return new RunStatusResponse(
                id,
                query,
                provider,
                model,
                snapshot.getStatus(),
                List.copyOf(steps),
                entrySnapshot.stream().map(entry -> TraceLine.from(entry, costEstimator)).toList(),
                RunTotals.from(roles),
                roles,
                snapshot.getBriefingMarkdown(),
                snapshot.getConfidence(),
                snapshot.getStopReason(),
                snapshot.getStopExplanation(),
                snapshot.getOutputDirectory(),
                snapshot.getError());
    }

    private List<RoleLine> roleLines(List<TraceEntry> entrySnapshot) {
        var costByRole = entrySnapshot.stream().collect(Collectors.groupingBy(
                entry -> TraceStatistics.roleOf(entry.getLabel()),
                Collectors.summingDouble(entry -> costEstimator.estimate(entry).getTotal())));
        return TraceStatistics.byRole(entrySnapshot).stream()
                .map(statistics -> RoleLine.from(statistics, costByRole.get(statistics.getRole())))
                .toList();
    }
}
