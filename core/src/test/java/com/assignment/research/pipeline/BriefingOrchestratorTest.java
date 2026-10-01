package com.assignment.research.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.critique.CritiqueOutput;
import com.assignment.research.critique.FindingOutput;
import com.assignment.research.critique.FindingSeverity;
import com.assignment.research.critique.FindingType;
import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.ExtractedClaim;
import com.assignment.research.evidence.FakeSourceSearchPort;
import com.assignment.research.evidence.ResearchOutput;
import com.assignment.research.evidence.Sources;
import com.assignment.research.llm.LLMException;
import com.assignment.research.llm.ScriptedLLMPort;
import com.assignment.research.planning.PlanOutput;
import com.assignment.research.planning.PlannedQuestion;
import com.assignment.research.prompt.FakePromptTemplates;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.ClaimGroupOutput;
import com.assignment.research.reconciliation.EvidenceGroup;
import com.assignment.research.reconciliation.ReconciliationOutput;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.synthesis.GroundedStatement;
import com.assignment.research.synthesis.SynthesisInput;
import com.assignment.research.synthesis.SynthesisOutput;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BriefingOrchestratorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-01T10:00:00Z"), ZoneOffset.UTC);
    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk shipping market");

    private static final PlanOutput TWO_QUESTIONS = new PlanOutput("overview", List.of(
            new PlannedQuestion("What drives demand?", List.of("demand")),
            new PlannedQuestion("What drives supply?", List.of("supply"))));
    private static final ResearchOutput TWO_CLAIMS = new ResearchOutput(List.of(
            new ExtractedClaim("Fleet grew 3.1% in 2025.", "src-a"),
            new ExtractedClaim("Fleet grew about 3% in 2025.", "src-b")));
    private static final ReconciliationOutput ONE_GROUP = new ReconciliationOutput(
            List.of(new ClaimGroupOutput("g1", "Fleet grew about 3% in 2025.", List.of("q1-c1", "q1-c2", "q2-c1",
                    "q2-c2"))), List.of());
    private static final CritiqueOutput CLEAN = CritiqueOutput.clean();
    private static final String GROUP_OF_Q1_C1 = "g-q1-c1";
    private static final String RUN_METHOD = "run";
    private static final String SYNTHESIS_INPUT_METHOD = "synthesisInput";

    private final FakePromptTemplates prompts = new FakePromptTemplates("prompt without placeholders");
    private final FakeSourceSearchPort search = FakeSourceSearchPort.returning(Sources.tierA("src-a"),
            Sources.tierB("src-b", Sources.RECENT));
    private final RecordingObserver observer = new RecordingObserver();

    private static SynthesisOutput groundedDraft(String... groupIds) {
        var statement = new GroundedStatement("Fleet grew about 3% in 2025.", List.of(groupIds));
        return new SynthesisOutput(List.of(statement), List.of(statement), List.of(), List.of("What next?"));
    }

    private static FindingOutput missingEvidence() {
        return new FindingOutput(FindingType.MISSING_EVIDENCE, FindingSeverity.MAJOR, "",
                "No evidence on emissions regulation for bulk carriers.", List.of(), List.of("emissions", "imo"));
    }

    private static FindingOutput overstated() {
        return new FindingOutput(FindingType.OVERSTATED_CERTAINTY, FindingSeverity.MAJOR,
                "Fleet grew about 3% in 2025.", "Evidence is medium.", List.of(GROUP_OF_Q1_C1), List.of());
    }

    @Test
    void happyPathFinishesApprovedAfterOneRoundAndSnapshotsEveryStep() {
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", TWO_CLAIMS)
                .on("reconciler", ONE_GROUP)
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1))
                .on("critic", CLEAN);

        var result = new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer);

        assertThat(result.getStopDecision().getReason()).isEqualTo(StopReason.APPROVED);
        assertThat(result.getFinalState().getRound()).isEqualTo(1);
        assertThat(result.getConfidence().getLevel()).isEqualTo(ConfidenceLevel.HIGH);
        assertThat(result.getOpenFindings()).isEmpty();
        assertThat(result.getGaps()).isEmpty();
        assertThat(result.getTrace()).hasSize(6);
        assertThat(result.getUsage().getTotalTokens()).isEqualTo(6 * 150);
        assertThat(observer.getSteps()).containsExactly(PipelineStep.PLAN, PipelineStep.RESEARCH, PipelineStep.SYNTHESIZE,
                PipelineStep.CRITIQUE, PipelineStep.FINISH);
        assertThat(llm.getLabels()).containsExactly("planner", "researcher/q1/round1", "researcher/q2/round1",
                "reconciler/round1", "synthesizer/round1", "critic/round1/pass1");
    }

    @Test
    void missingEvidenceFindingTriggersTargetedSecondRoundThenRevisionAndApproval() {
        var searchWithoutFollowUpHits = FakeSourceSearchPort.returningOnlyFor(Set.of("demand", "supply"),
                Sources.tierA("src-a"), Sources.tierB("src-b", Sources.RECENT));
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", TWO_CLAIMS)
                .on("reconciler", ONE_GROUP)
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1))
                .on("critic", new CritiqueOutput(List.of(missingEvidence())), CLEAN);

        var result = new BriefingOrchestrator(llm, searchWithoutFollowUpHits, prompts, CLOCK)
                .produce(QUERY, observer);

        assertThat(result.getStopDecision().getReason()).isEqualTo(StopReason.APPROVED);
        assertThat(result.getFinalState().getRound()).isEqualTo(2);
        assertThat(result.getFinalState().getSubQuestions()).hasSize(3);
        assertThat(result.getFinalState().getExhaustedSubQuestionIds()).containsExactly("q3");
        assertThat(result.getGaps()).extracting(gap -> gap.getId()).containsExactly("q3");
        assertThat(result.getConfidence().getLevel()).isEqualTo(ConfidenceLevel.MEDIUM);
        assertThat(llm.getLabels()).contains("synthesizer/round2/revision", "critic/round2/pass1");
    }

    @Test
    void persistentMajorFindingHitsRewriteLimitAndCapsConfidenceLow() {
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", TWO_CLAIMS)
                .on("reconciler", ONE_GROUP)
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1))
                .on("critic", new CritiqueOutput(List.of(overstated())));

        var result = new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer);

        assertThat(result.getStopDecision().getReason()).isEqualTo(StopReason.REWRITE_LIMIT_REACHED);
        assertThat(result.getOpenFindings()).hasSize(1);
        assertThat(result.getConfidence().getLevel()).isEqualTo(ConfidenceLevel.LOW);
        assertThat(llm.getLabels()).containsSubsequence("synthesizer/round1", "critic/round1/pass1",
                "synthesizer/round1/revision", "critic/round1/pass2");
    }

    @Test
    void criticFailureDeliversUnverifiedBriefingWithLowConfidence() {
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", TWO_CLAIMS)
                .on("reconciler", ONE_GROUP)
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1))
                .on("critic", new LLMException("provider down"));

        var result = new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer);

        assertThat(result.getStopDecision().getReason()).isEqualTo(StopReason.AGENT_FAILURE);
        assertThat(result.getConfidence().getLevel()).isEqualTo(ConfidenceLevel.LOW);
        assertThat(result.getFinalState().getFailures()).extracting(AgentFailure::getAgent).containsExactly("critic");
        assertThat(result.getDraft().getKeyFacts()).isNotEmpty();
    }

    @Test
    void plannerFailureAbortsTheRun() {
        var llm = new ScriptedLLMPort().on("planner", new LLMException("provider down"));

        assertThatThrownBy(() -> new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer))
                .isInstanceOf(PipelineAbortedException.class);
    }

    @Test
    void unexpectedRuntimeFailureIsWrappedIntoPipelineAbortedWithTheStepName() {
        var unexpected = new IllegalStateException("bug in adapter");
        var llm = new ScriptedLLMPort().on("planner", unexpected);

        assertThatThrownBy(() -> new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer))
                .isInstanceOf(PipelineAbortedException.class)
                .hasMessage(PipelineStep.PLAN.name())
                .hasCause(unexpected);
    }

    @Test
    void secondRoundResearchOnAnExistingSubQuestionContinuesItsClaimNumbering() {
        var weakClaim = new ResearchOutput(List.of(new ExtractedClaim("A blog expects rates to rise.", "src-c")));
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", weakClaim)
                .on("reconciler", ReconciliationOutput.empty())
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1))
                .on("critic", CLEAN);

        var result = new BriefingOrchestrator(llm, FakeSourceSearchPort.returning(Sources.tierC("src-c")), prompts,
                CLOCK).produce(QUERY, observer);

        assertThat(result.getFinalState().getRound()).isEqualTo(2);
        assertThat(result.getFinalState().getClaims()).extracting(Claim::getId)
                .containsExactly("q1-c1", "q2-c1", "q1-c2", "q2-c2");
        assertThat(result.getFinalState().getGroups()).extracting(EvidenceGroup::getId)
                .containsExactly(GROUP_OF_Q1_C1, "g-q2-c1", "g-q1-c2", "g-q2-c2");
    }

    @Test
    void neverFinishesWithoutADraftEvenWhenNoEvidenceIsFound() {
        var emptyDraft = new SynthesisOutput(List.of(), List.of(), List.of(), List.of());
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("synthesizer", emptyDraft)
                .on("critic", CLEAN);

        var result = new BriefingOrchestrator(llm, FakeSourceSearchPort.empty(), prompts, CLOCK)
                .produce(QUERY, observer);

        assertThat(result.getDraft()).isNotNull();
        assertThat(observer.getSteps()).containsSubsequence(PipelineStep.SYNTHESIZE, PipelineStep.FINISH);
        assertThat(result.getGaps()).extracting(gap -> gap.getId()).containsExactly("q1", "q2");
    }

    @Test
    void researcherFailureBecomesAVisibleGapInsteadOfAbortingTheRun() {
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher/q1", TWO_CLAIMS)
                .on("researcher/q2", new LLMException("timeout"))
                .on("reconciler", ONE_GROUP)
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1))
                .on("critic", CLEAN);

        var result = new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer);

        assertThat(result.getStopDecision().getReason()).isEqualTo(StopReason.APPROVED);
        assertThat(result.getGaps()).extracting(gap -> gap.getId()).containsExactly("q2");
        assertThat(result.getFinalState().getFailures()).extracting(AgentFailure::getAgent)
                .containsExactly("researcher/q2");
        assertThat(result.getConfidence().getLevel()).isEqualTo(ConfidenceLevel.MEDIUM);
    }

    @Test
    void reconcilerFailureFallsBackToOneGroupPerClaimAndRecordsTheFailure() {
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", TWO_CLAIMS)
                .on("reconciler", new LLMException("provider down"))
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1))
                .on("critic", CLEAN);

        var result = new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer);

        assertThat(result.getStopDecision().getReason()).isEqualTo(StopReason.APPROVED);
        assertThat(result.getFinalState().getFailures()).extracting(AgentFailure::getAgent)
                .containsExactly(PipelineConstants.FAILURE_RECONCILER);
        assertThat(result.getFinalState().getGroups()).hasSize(4);
    }

    @Test
    void synthesizerFailureBeforeTheFirstDraftAbortsTheRun() {
        var failure = new LLMException("provider down");
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", TWO_CLAIMS)
                .on("reconciler", ONE_GROUP)
                .on("synthesizer", failure);

        assertThatThrownBy(() -> new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer))
                .isInstanceOf(PipelineAbortedException.class)
                .hasMessage(PipelineConstants.FAILURE_SYNTHESIZER)
                .hasCause(failure);
    }

    @Test
    void synthesizerFailureOnARevisionDeliversThePreviousDraft() {
        var llm = new ScriptedLLMPort()
                .on("planner", TWO_QUESTIONS)
                .on("researcher", TWO_CLAIMS)
                .on("reconciler", ONE_GROUP)
                .on("synthesizer", groundedDraft(GROUP_OF_Q1_C1), new LLMException("provider down"))
                .on("critic", new CritiqueOutput(List.of(overstated())));

        var result = new BriefingOrchestrator(llm, search, prompts, CLOCK).produce(QUERY, observer);

        assertThat(result.getStopDecision().getReason()).isEqualTo(StopReason.AGENT_FAILURE);
        assertThat(result.getFinalState().getFailures()).extracting(AgentFailure::getAgent)
                .containsExactly(PipelineConstants.FAILURE_SYNTHESIZER);
        assertThat(result.getDraft().getKeyFacts()).isNotEmpty();
    }

    @Test
    void finishStepLeavesTheStateUnchanged() throws ReflectiveOperationException {
        var run = BriefingOrchestrator.class.getDeclaredMethod(RUN_METHOD, PipelineStep.class, BriefingState.class,
                RunAgents.class);
        run.setAccessible(true);
        var state = BriefingState.initial(QUERY);

        assertThat(run.invoke(null, PipelineStep.FINISH, state, null)).isSameAs(state);
    }

    @Test
    void draftWithoutCritiqueIsSynthesizedAsAFirstDraft() throws ReflectiveOperationException {
        var synthesisInput = BriefingOrchestrator.class.getDeclaredMethod(SYNTHESIS_INPUT_METHOD,
                BriefingState.class);
        synthesisInput.setAccessible(true);
        var draft = new BriefingDraft(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        var state = BriefingState.initial(QUERY).toBuilder().draft(draft).build();

        var input = (SynthesisInput) synthesisInput.invoke(null, state);

        assertThat(input.getPreviousDraft()).isEmpty();
    }
}
