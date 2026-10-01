package com.assignment.research.reconciliation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class IntraGroupConflictSplitterTest {

    @Test
    void conflictPointingAtOneGroupSplitsThatGroupIntoOnePartPerClaim() {
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Fleet grew in 2025.", List.of("q3-c1", "q3-c6")),
                        new ClaimGroupOutput("g2", "Deliveries were about 30 million dwt.", List.of("q3-c2"))),
                List.of(new ConflictOutput(List.of("g1", "g1"), "2.4 percent vs 3.1 percent")));

        var normalized = IntraGroupConflictSplitter.split(output);

        assertThat(normalized.getGroups()).extracting(ClaimGroupOutput::getId).containsExactly("g2", "g1.1", "g1.2");
        assertThat(normalized.getGroups().get(1).getClaimIds()).containsExactly("q3-c1");
        assertThat(normalized.getConflicts()).hasSize(1);
        assertThat(normalized.getConflicts().getFirst().getGroupIds()).containsExactly("g1.1", "g1.2");
        assertThat(normalized.getConflicts().getFirst().getDescription()).isEqualTo("2.4 percent vs 3.1 percent");
    }

    @Test
    void otherConflictsReferencingASplitGroupAreRewrittenToAllItsParts() {
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Fleet grew in 2025.", List.of("q3-c1", "q3-c6")),
                        new ClaimGroupOutput("g2", "Fleet shrank in 2025.", List.of("q3-c2"))),
                List.of(new ConflictOutput(List.of("g2", "g1"), "grew vs shrank"),
                        new ConflictOutput(List.of("g1"), "2.4 percent vs 3.1 percent")));

        var normalized = IntraGroupConflictSplitter.split(output);

        assertThat(normalized.getConflicts()).extracting(ConflictOutput::getGroupIds)
                .containsExactly(List.of("g2", "g1.1", "g1.2"), List.of("g1.1", "g1.2"));
        assertThat(normalized.getConflicts()).extracting(ConflictOutput::getDescription)
                .containsExactly("grew vs shrank", "2.4 percent vs 3.1 percent");
    }

    @Test
    void ordinaryConflictsAndSingleClaimGroupsAreLeftUntouched() {
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "A", List.of("c1")), new ClaimGroupOutput("g2", "B", List.of("c2"))),
                List.of(new ConflictOutput(List.of("g1", "g2"), "A vs B"),
                        new ConflictOutput(List.of("g1", "g1"), "nonsense self conflict")));

        var normalized = IntraGroupConflictSplitter.split(output);

        assertThat(normalized.getGroups()).hasSize(2);
        assertThat(normalized.getConflicts()).containsExactlyElementsOf(output.getConflicts());
    }
}
