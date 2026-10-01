package com.assignment.research.critique;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.synthesis.GroundedStatement;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CritiqueAssemblerTest {

    private static final Set<String> KNOWN_GROUPS = Set.of("q1-g1", "q2-g1");

    private static BriefingDraft draft(GroundedStatement... summary) {
        return new BriefingDraft(List.of(summary), List.of(), List.of(), List.of(), List.of(), List.of());
    }

    @Test
    void flagsUngroundedStatementsMechanicallyBeforeAnyModelFinding() {
        var draft = draft(new GroundedStatement("Grounded.", List.of("q1-g1")),
                new GroundedStatement("Floating.", List.of()));

        var critique = CritiqueAssembler.assemble(draft, CritiqueOutput.clean(), KNOWN_GROUPS);

        assertThat(critique.isApproved()).isFalse();
        assertThat(critique.getFindings()).hasSize(1);
        var finding = critique.getFindings().getFirst();
        assertThat(finding.getType()).isEqualTo(FindingType.UNSUPPORTED);
        assertThat(finding.isMajor()).isTrue();
        assertThat(finding.getQuotedText()).isEqualTo("Floating.");
    }

    @Test
    void filtersUnknownGroupIdsDropsEmptyDetailsAndDerivesKeywordsForMissingEvidence() {
        var output = new CritiqueOutput(List.of(
                new FindingOutput(FindingType.OVERSTATED_CERTAINTY, FindingSeverity.MINOR, "will rise", "Too sure.",
                        List.of("q1-g1", "q9-g9"), List.of()),
                new FindingOutput(FindingType.READABILITY, FindingSeverity.MINOR, "x", "   ", List.of(), List.of()),
                new FindingOutput(FindingType.MISSING_EVIDENCE, FindingSeverity.MAJOR, "",
                        "Nothing covers emissions regulation for bulk carriers.", List.of(), List.of())));
        var draft = draft(new GroundedStatement("Grounded.", List.of("q1-g1")));

        var critique = CritiqueAssembler.assemble(draft, output, KNOWN_GROUPS);

        assertThat(critique.getFindings()).hasSize(2);
        assertThat(critique.getFindings().get(0).getGroupIds()).containsExactly("q1-g1");
        var research = critique.getResearchFindings().getFirst();
        assertThat(research.getSuggestedKeywords()).contains("emissions", "regulation", "bulk", "carriers");
        assertThat(critique.getRewriteFindings()).hasSize(1);
        assertThat(critique.hasMajorFinding()).isTrue();
    }

    @Test
    void cleanOutputOnGroundedDraftIsApproved() {
        var draft = draft(new GroundedStatement("Grounded.", List.of("q1-g1")));

        var critique = CritiqueAssembler.assemble(draft, CritiqueOutput.clean(), KNOWN_GROUPS);

        assertThat(critique.isApproved()).isTrue();
    }
}
