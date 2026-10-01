package com.assignment.research.pipeline;

import com.assignment.research.critique.Critique;
import com.assignment.research.trace.InMemoryTraceSink;
import java.util.List;

public final class BriefingResultAssembler {

    private BriefingResultAssembler() {
    }

    public static BriefingResult assemble(BriefingState state, InMemoryTraceSink trace) {
        var draft = state.getDraft().orElseThrow(() -> new IllegalStateException(
                "pipeline finished without a draft; this is a bug in the transition policy"));
        var openFindings = state.getLatestCritique()
                .filter(critique -> !critique.isApproved())
                .map(Critique::getFindings)
                .orElse(List.of());
        var stopDecision = state.getStopDecision().orElseThrow(() -> new IllegalStateException(
                "pipeline finished without a stop decision; this is a bug in the transition policy"));
        return new BriefingResult(
                draft,
                BriefingConfidenceAggregator.aggregate(state),
                openFindings,
                CoverageAnalyzer.uncovered(state),
                stopDecision,
                state,
                trace.getEntries(),
                trace.getTotalUsage());
    }
}
