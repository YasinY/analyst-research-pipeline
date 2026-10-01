package com.assignment.research.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.critique.CriticFinding;
import com.assignment.research.critique.Critique;
import com.assignment.research.critique.FindingSeverity;
import com.assignment.research.critique.FindingType;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.trace.InMemoryTraceSink;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TransitionPolicyTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk shipping market");
    private static final List<SubQuestion> QUESTIONS = List.of(
            new SubQuestion("q1", "What drives demand?", List.of("demand")),
            new SubQuestion("q2", "What drives supply?", List.of("supply")));
    private static final int NO_CALLS = 0;

    private static final BriefingDraft DRAFT =
            new BriefingDraft(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    private static final CriticFinding RESEARCH_FINDING = new CriticFinding(FindingType.MISSING_EVIDENCE,
            FindingSeverity.MAJOR, "", "No evidence on emissions.", List.of(), List.of("emissions"));
    private static final CriticFinding REWRITE_FINDING = new CriticFinding(FindingType.OVERSTATED_CERTAINTY,
            FindingSeverity.MAJOR, "Rates will rise.", "Too sure.", List.of(), List.of());

    private static BriefingState researched() {
        return BriefingState.initial(QUERY).withSubQuestionsAppended(QUESTIONS);
    }

    private static BriefingState reviewedInLastRound(CriticFinding... findings) {
        return researched().toBuilder()
                .draft(DRAFT)
                .round(PipelineConstants.MAX_RESEARCH_ROUNDS)
                .build()
                .withCritiqueAppended(new Critique(List.of(findings)));
    }

    @Test
    void anExistingStopDecisionFinishesImmediately() {
        var stopped = researched().toBuilder()
                .stopDecision(new StopDecision(StopReason.NO_NEW_EVIDENCE, PipelineConstants.EXPLANATION_NO_NEW_EVIDENCE))
                .build();

        assertThat(TransitionPolicy.next(stopped, NO_CALLS).getStep()).isEqualTo(PipelineStep.FINISH);
    }

    @Test
    void exhaustedCallBudgetFinishesAnUnapprovedDraft() {
        var transition = TransitionPolicy.next(reviewedInLastRound(REWRITE_FINDING), PipelineConstants.MAX_LLM_CALLS);

        assertThat(transition.getStopDecision()).map(StopDecision::getReason)
                .contains(StopReason.CALL_BUDGET_EXHAUSTED);
    }

    @Test
    void researchRequestAfterTheLastRoundFinishesWithRoundLimit() {
        var transition = TransitionPolicy.next(reviewedInLastRound(RESEARCH_FINDING), NO_CALLS);

        assertThat(transition.getStep()).isEqualTo(PipelineStep.FINISH);
        assertThat(transition.getStopDecision()).map(StopDecision::getReason)
                .contains(StopReason.ROUND_LIMIT_REACHED);
    }

    @Test
    void researchAndRewriteRequestAfterTheLastRoundRewritesTheDraft() {
        var transition = TransitionPolicy.next(reviewedInLastRound(RESEARCH_FINDING, REWRITE_FINDING), NO_CALLS);

        assertThat(transition.getStep()).isEqualTo(PipelineStep.SYNTHESIZE);
        assertThat(transition.getStopDecision()).isEmpty();
    }

    @Test
    void withoutADraftTheNextStepIsNeverFinish() {
        var withoutDraft = List.of(
                BriefingState.initial(QUERY),
                researched().toBuilder().pendingResearchIds(List.of("q1", "q2")).build(),
                researched(),
                researched().withExhausted(Set.of("q1", "q2")),
                researched().toBuilder().round(PipelineConstants.MAX_RESEARCH_ROUNDS).build(),
                researched().toBuilder().round(PipelineConstants.MAX_RESEARCH_ROUNDS).critiqueFailed(true).build());

        assertThat(withoutDraft)
                .extracting(state -> TransitionPolicy.next(state, PipelineConstants.MAX_LLM_CALLS).getStep())
                .doesNotContain(PipelineStep.FINISH);
    }

    @Test
    void withoutADraftAndNoResearchLeftTheNextStepIsSynthesize() {
        var exhausted = researched().withExhausted(Set.of("q1", "q2"));

        assertThat(TransitionPolicy.next(exhausted, NO_CALLS).getStep()).isEqualTo(PipelineStep.SYNTHESIZE);
    }

    @Test
    void assemblingAResultWithoutADraftIsRejected() {
        var finishedWithoutDraft = researched().toBuilder()
                .stopDecision(new StopDecision(StopReason.NO_NEW_EVIDENCE, PipelineConstants.EXPLANATION_NO_NEW_EVIDENCE))
                .build();

        assertThatThrownBy(() -> BriefingResultAssembler.assemble(finishedWithoutDraft, new InMemoryTraceSink()))
                .isInstanceOf(IllegalStateException.class);
    }
}
