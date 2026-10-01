package com.assignment.research.pipeline;

import com.assignment.research.critique.Critique;
import com.assignment.research.critique.CritiqueInput;
import com.assignment.research.evidence.EvidenceConstants;
import com.assignment.research.evidence.ResearchResult;
import com.assignment.research.evidence.SourceSearchPort;
import com.assignment.research.llm.LLMException;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.EvidenceGroupAssembler;
import com.assignment.research.reconciliation.Reconciliation;
import com.assignment.research.reconciliation.ReconciliationOutput;
import com.assignment.research.synthesis.SynthesisInput;
import com.assignment.research.trace.InMemoryTraceSink;
import com.assignment.research.trace.TraceEntry;
import com.assignment.research.trace.TracingLLMPort;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class BriefingOrchestrator implements ProduceBriefingUseCase {

    private final LLMPort llm;
    private final SourceSearchPort search;
    private final PromptTemplates prompts;
    private final Clock clock;

    @Override
    public BriefingResult produce(AnalystQuery query, PipelineObserver observer) {
        var trace = new InMemoryTraceSink();
        var tracingLlm = new TracingLLMPort(llm, entry -> record(trace, observer, entry), clock);
        var agents = new RunAgents(tracingLlm, search, prompts, clock);

        var state = BriefingState.initial(query);
        while (true) {
            var transition = TransitionPolicy.next(state, trace.getEntries().size());
            if (transition.getStep() == PipelineStep.FINISH) {
                var finished = withStopDecision(state, transition);
                observer.onStep(PipelineStep.FINISH, finished);
                return BriefingResultAssembler.assemble(finished, trace);
            }
            state = apply(transition.getStep(), state, agents);
            observer.onStep(transition.getStep(), state);
        }
    }

    private static BriefingState withStopDecision(BriefingState state, Transition transition) {
        return transition.getStopDecision()
                .map(decision -> state.toBuilder().stopDecision(decision).build())
                .orElse(state);
    }

    private static void record(InMemoryTraceSink trace, PipelineObserver observer, TraceEntry entry) {
        trace.accept(entry);
        observer.onTrace(entry);
    }

    private static BriefingState apply(PipelineStep step, BriefingState state, RunAgents agents) {
        try {
            return run(step, state, agents);
        } catch (PipelineAbortedException aborted) {
            throw aborted;
        } catch (RuntimeException failure) {
            throw new PipelineAbortedException(step.name(), failure);
        }
    }

    private static BriefingState run(PipelineStep step, BriefingState state, RunAgents agents) {
        return switch (step) {
            case PLAN -> plan(state, agents);
            case RESEARCH -> research(state, agents);
            case SCHEDULE_RESEARCH -> ResearchScheduler.scheduleNextRound(state);
            case SYNTHESIZE -> synthesize(state, agents);
            case CRITIQUE -> critique(state, agents);
            case FINISH -> state;
        };
    }

    private static BriefingState plan(BriefingState state, RunAgents agents) {
        var plan = agents.getPlanner().plan(state.getQuery());
        var pendingIds = plan.getSubQuestions().stream().map(SubQuestion::getId).toList();
        return state.toBuilder()
                .interpretation(plan.getInterpretation())
                .subQuestions(plan.getSubQuestions())
                .pendingResearchIds(pendingIds)
                .build();
    }

    private static BriefingState research(BriefingState state, RunAgents agents) {
        var current = state;
        var exhausted = new HashSet<String>();
        for (var question : pendingQuestions(state)) {
            current = researchOne(current, question, agents, exhausted);
        }
        current = reconcileAll(current, agents);
        return current.toBuilder().pendingResearchIds(List.of()).build().withExhausted(exhausted);
    }

    private static BriefingState researchOne(BriefingState state, SubQuestion question, RunAgents agents,
            Set<String> exhausted) {
        var round = state.getRound();
        ResearchResult result;
        try {
            result = agents.getResearcher().research(question, round, nextClaimNumber(state, question));
        } catch (LLMException failure) {
            exhausted.add(question.getId());
            return state.withFailure(new AgentFailure(
                    PipelineConstants.FAILURE_RESEARCHER_FORMAT.formatted(question.getId()), round,
                    failure.getMessage()));
        }
        if (!result.hasClaims()) {
            exhausted.add(question.getId());
        }
        return state.withResearchAppended(result);
    }

    private static BriefingState reconcileAll(BriefingState state, RunAgents agents) {
        var claims = state.getClaims();
        var sources = state.getSources();
        var current = state;
        Reconciliation reconciliation;
        try {
            reconciliation = agents.getReconciler().reconcile(state.getSubQuestions(), claims, sources,
                    state.getRound());
        } catch (LLMException failure) {
            reconciliation = EvidenceGroupAssembler.assemble(claims, sources, ReconciliationOutput.empty());
            current = state.withFailure(new AgentFailure(PipelineConstants.FAILURE_RECONCILER, state.getRound(),
                    failure.getMessage()));
        }
        var groups = reconciliation.getGroups();
        return current.withEvidence(groups, agents.getConfidenceCalculator().scoreAll(groups));
    }

    private static BriefingState synthesize(BriefingState state, RunAgents agents) {
        var input = synthesisInput(state);
        try {
            var draft = agents.getSynthesizer().synthesize(input, state.getRound());
            var countsAsRewrite = input.isRevision() && !state.isDraftStale();
            var rewrites = countsAsRewrite
                    ? state.getRewritesInRound() + PipelineConstants.SINGLE_REWRITE
                    : state.getRewritesInRound();
            return state.toBuilder()
                    .draft(draft)
                    .draftReviewed(false)
                    .draftStale(false)
                    .rewritesInRound(rewrites)
                    .build();
        } catch (LLMException failure) {
            if (!state.hasDraft()) {
                throw new PipelineAbortedException(PipelineConstants.FAILURE_SYNTHESIZER, failure);
            }
            return state.withFailure(new AgentFailure(PipelineConstants.FAILURE_SYNTHESIZER, state.getRound(),
                            failure.getMessage()))
                    .toBuilder()
                    .draftReviewed(true)
                    .draftStale(false)
                    .stopDecision(new StopDecision(StopReason.AGENT_FAILURE,
                            PipelineConstants.EXPLANATION_AGENT_FAILURE.formatted(
                                    PipelineConstants.FAILURE_SYNTHESIZER)))
                    .build();
        }
    }

    private static SynthesisInput synthesisInput(BriefingState state) {
        var firstDraft = SynthesisInput.firstDraft(state.getQuery(),
                state.getInterpretation().orElse(PipelineConstants.NO_INTERPRETATION), state.getGroups(),
                state.getConfidences(), CoverageAnalyzer.uncovered(state));
        var previousDraft = state.getDraft();
        var latestCritique = state.getLatestCritique();
        if (previousDraft.isEmpty() || latestCritique.isEmpty()) {
            return firstDraft;
        }
        return firstDraft.revisedWith(previousDraft.get(), latestCritique.get().getFindings());
    }

    private static BriefingState critique(BriefingState state, RunAgents agents) {
        var input = new CritiqueInput(state.getQuery(), state.getDraft().orElseThrow(), state.getGroups(),
                state.getConfidences(), CoverageAnalyzer.uncovered(state));
        var pass = state.getRewritesInRound() + PipelineConstants.FIRST_CRITIQUE_PASS;
        try {
            Critique critique = agents.getCritic().critique(input, state.getRound(), pass);
            return state.withCritiqueAppended(critique);
        } catch (LLMException failure) {
            return state.withFailure(new AgentFailure(PipelineConstants.FAILURE_CRITIC, state.getRound(),
                            failure.getMessage()))
                    .toBuilder()
                    .critiqueFailed(true)
                    .draftReviewed(true)
                    .build();
        }
    }

    private static int nextClaimNumber(BriefingState state, SubQuestion question) {
        var questionId = question.getId();
        var existingClaims = state.getClaims().stream()
                .filter(claim -> claim.getSubQuestionId().equals(questionId))
                .count();
        return Math.toIntExact(existingClaims) + EvidenceConstants.FIRST_CLAIM_NUMBER;
    }

    private static List<SubQuestion> pendingQuestions(BriefingState state) {
        var pending = Set.copyOf(state.getPendingResearchIds());
        return state.getSubQuestions().stream().filter(question -> pending.contains(question.getId())).toList();
    }
}
