package com.assignment.research.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.critique.CriticFinding;
import com.assignment.research.critique.Critique;
import com.assignment.research.critique.FindingSeverity;
import com.assignment.research.critique.FindingType;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ResearchSchedulerTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk shipping market");
    private static final List<SubQuestion> QUESTIONS = List.of(
            new SubQuestion("q1", "What drives demand?", List.of("demand")),
            new SubQuestion("q2", "What drives supply?", List.of("supply")));
    private static final Set<String> ALL_QUESTION_IDS = Set.of("q1", "q2");
    private static final List<String> EMISSIONS_KEYWORDS = List.of("emissions", "imo");
    private static final List<String> EMISSIONS_KEYWORDS_REORDERED = List.of("imo", "emissions");
    private static final String FOLLOW_UP_ID = "q3";
    private static final String FALLBACK_QUESTION = "What does the evidence say about: emissions, imo?";

    private static CriticFinding missingEvidence(List<String> keywords) {
        return new CriticFinding(FindingType.MISSING_EVIDENCE, FindingSeverity.MAJOR, "", "No evidence.", List.of(),
                keywords);
    }

    @Test
    void skipsFindingsWithoutKeywordsAndDuplicateKeywordSets() {
        var critique = new Critique(List.of(missingEvidence(List.of()), missingEvidence(EMISSIONS_KEYWORDS),
                missingEvidence(EMISSIONS_KEYWORDS_REORDERED)));
        var state = BriefingState.initial(QUERY).withSubQuestionsAppended(QUESTIONS)
                .withExhausted(ALL_QUESTION_IDS)
                .withCritiqueAppended(critique);

        var scheduled = ResearchScheduler.scheduleNextRound(state);

        assertThat(scheduled.getSubQuestions()).hasSize(3);
        var followUp = scheduled.getSubQuestions().getLast();
        assertThat(followUp.getId()).isEqualTo(FOLLOW_UP_ID);
        assertThat(followUp.getQuestion()).isEqualTo(FALLBACK_QUESTION);
        assertThat(scheduled.getPendingResearchIds()).containsExactly(FOLLOW_UP_ID);
        assertThat(scheduled.getRound()).isEqualTo(PipelineConstants.FIRST_ROUND + 1);
        assertThat(scheduled.isDraftStale()).isTrue();
    }

    @Test
    void stopsWithNoNewEvidenceWhenEveryOpenQuestionIsExhausted() {
        var state = BriefingState.initial(QUERY).withSubQuestionsAppended(QUESTIONS).withExhausted(ALL_QUESTION_IDS);

        var scheduled = ResearchScheduler.scheduleNextRound(state);

        assertThat(scheduled.getStopDecision()).map(StopDecision::getReason).contains(StopReason.NO_NEW_EVIDENCE);
        assertThat(scheduled.getRound()).isEqualTo(PipelineConstants.FIRST_ROUND);
    }
}
