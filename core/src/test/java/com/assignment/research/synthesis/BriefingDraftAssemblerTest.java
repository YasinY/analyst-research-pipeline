package com.assignment.research.synthesis;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BriefingDraftAssemblerTest {

    private static final Map<String, GroupConfidence> CONFIDENCES = Map.of(
            "q1-g1", new GroupConfidence("q1-g1", 0.9, ConfidenceLevel.HIGH, List.of()),
            "q2-g1", new GroupConfidence("q2-g1", 0.15, ConfidenceLevel.LOW, List.of()));

    private static GroundedStatement statement(String text, String... groupIds) {
        return new GroundedStatement(text, List.of(groupIds));
    }

    @Test
    void keepsKeyFactsOnEligibleGroupsDemotesWeakOnesAndDropsUngroundedOnes() {
        var output = new SynthesisOutput(
                List.of(statement("Summary.", "q1-g1")),
                List.of(statement("Strong fact.", "q1-g1"),
                        statement("Weak fact.", "q2-g1"),
                        statement("Invented fact.", "q9-g9"),
                        statement("No sources at all.")),
                List.of(statement("Known gap.")),
                List.of(" What next? ", "What next?", "  "));

        var draft = BriefingDraftAssembler.assemble(output, CONFIDENCES);

        assertThat(draft.getKeyFacts()).extracting(GroundedStatement::getText).containsExactly("Strong fact.");
        assertThat(draft.getDemotedKeyFacts()).containsExactly("Weak fact.");
        assertThat(draft.getDroppedStatements()).containsExactly("Invented fact.", "No sources at all.");
        assertThat(draft.getUncertainties()).extracting(GroundedStatement::getText)
                .containsExactly("Weak fact.", "Known gap.");
        assertThat(draft.getFollowUpQuestions()).containsExactly("What next?");
    }

    @Test
    void stripsUnknownGroupIdsFromSummaryButKeepsTheSentenceForTheCritic() {
        var output = new SynthesisOutput(
                List.of(statement("Partly grounded.", "q1-g1", "q7-g7"), statement("Floating claim.", "q7-g7")),
                List.of(), List.of(), List.of());

        var draft = BriefingDraftAssembler.assemble(output, CONFIDENCES);

        assertThat(draft.getSummary().get(0).getGroupIds()).containsExactly("q1-g1");
        assertThat(draft.getSummary().get(1).isGrounded()).isFalse();
    }
}
