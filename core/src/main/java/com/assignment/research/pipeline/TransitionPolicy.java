package com.assignment.research.pipeline;

import com.assignment.research.critique.Critique;

public final class TransitionPolicy {

    private TransitionPolicy() {
    }

    public static Transition next(BriefingState state, int llmCallsSoFar) {
        if (state.getStopDecision().isPresent()) {
            return Transition.to(PipelineStep.FINISH);
        }
        if (state.getSubQuestions().isEmpty()) {
            return Transition.to(PipelineStep.PLAN);
        }
        if (!state.getPendingResearchIds().isEmpty()) {
            return Transition.to(PipelineStep.RESEARCH);
        }
        if (!state.hasDraft()) {
            return beforeFirstDraft(state);
        }
        if (state.isDraftStale()) {
            return Transition.to(PipelineStep.SYNTHESIZE);
        }
        if (!state.isDraftReviewed()) {
            return Transition.to(PipelineStep.CRITIQUE);
        }
        return afterCritique(state, llmCallsSoFar);
    }

    private static Transition beforeFirstDraft(BriefingState state) {
        if (canResearchMore(state)) {
            return Transition.to(PipelineStep.SCHEDULE_RESEARCH);
        }
        return Transition.to(PipelineStep.SYNTHESIZE);
    }

    private static Transition afterCritique(BriefingState state, int llmCallsSoFar) {
        if (state.isCritiqueFailed()) {
            return Transition.finish(StopReason.AGENT_FAILURE,
                    PipelineConstants.EXPLANATION_AGENT_FAILURE.formatted(PipelineConstants.FAILURE_CRITIC));
        }
        var critique = state.getLatestCritique().orElseThrow();
        if (critique.isApproved()) {
            return Transition.finish(StopReason.APPROVED, PipelineConstants.EXPLANATION_APPROVED);
        }
        if (llmCallsSoFar >= PipelineConstants.MAX_LLM_CALLS) {
            return Transition.finish(StopReason.CALL_BUDGET_EXHAUSTED,
                    PipelineConstants.EXPLANATION_CALL_BUDGET.formatted(PipelineConstants.MAX_LLM_CALLS));
        }
        if (!critique.getResearchFindings().isEmpty()) {
            if (state.getRound() < PipelineConstants.MAX_RESEARCH_ROUNDS) {
                return Transition.to(PipelineStep.SCHEDULE_RESEARCH);
            }
            if (critique.getRewriteFindings().isEmpty()) {
                return roundLimit(state);
            }
        }
        if (state.getRewritesInRound() < PipelineConstants.MAX_REWRITES_PER_ROUND) {
            return Transition.to(PipelineStep.SYNTHESIZE);
        }
        return rewriteLimit(state, critique);
    }

    private static boolean canResearchMore(BriefingState state) {
        return state.getRound() < PipelineConstants.MAX_RESEARCH_ROUNDS
                && !CoverageAnalyzer.openForResearch(state).isEmpty();
    }

    private static Transition roundLimit(BriefingState state) {
        var uncovered = CoverageAnalyzer.uncovered(state).size();
        return Transition.finish(StopReason.ROUND_LIMIT_REACHED,
                PipelineConstants.EXPLANATION_ROUND_LIMIT.formatted(PipelineConstants.MAX_RESEARCH_ROUNDS, uncovered));
    }

    private static Transition rewriteLimit(BriefingState state, Critique critique) {
        return Transition.finish(StopReason.REWRITE_LIMIT_REACHED,
                PipelineConstants.EXPLANATION_REWRITE_LIMIT.formatted(state.getRewritesInRound(),
                        critique.getFindings().size()));
    }
}
