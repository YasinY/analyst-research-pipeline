package com.assignment.research.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.evidence.SourceTier;
import com.assignment.research.evidence.Sources;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.ConflictStatus;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CoverageAnalyzerTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk shipping market");
    private static final String QUESTION_ID = "q1";
    private static final SubQuestion QUESTION = new SubQuestion(QUESTION_ID, "What drives demand?",
            List.of("demand"));
    private static final EvidenceGroup UNSCORED_GROUP = new EvidenceGroup("g-q1-c1", Set.of(QUESTION_ID),
            "Demand grew.", List.of("q1-c1"), List.of("src-a"), SourceTier.A, Sources.RECENT, ConflictStatus.NONE,
            null, List.of());

    @Test
    void groupWithoutConfidenceDoesNotCoverItsSubQuestion() {
        var state = BriefingState.initial(QUERY).withSubQuestionsAppended(List.of(QUESTION))
                .withEvidence(List.of(UNSCORED_GROUP), List.of());

        assertThat(CoverageAnalyzer.coveredSubQuestionIds(state)).isEmpty();
        assertThat(CoverageAnalyzer.uncovered(state)).containsExactly(QUESTION);
    }
}
