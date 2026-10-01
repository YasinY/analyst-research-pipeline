package com.assignment.research.synthesis;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.critique.FindingSeverity;
import com.assignment.research.critique.FindingType;
import com.assignment.research.evidence.SourceTier;
import com.assignment.research.llm.FakeLlmPort;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.FakePromptTemplates;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.ConflictStatus;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SynthesizerTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Dry bulk shipping overview");
    private static final String INTERPRETATION = "market overview";
    private static final LocalDate RECENT = LocalDate.of(2026, 3, 1);
    private static final SubQuestion GAP = new SubQuestion("q3", "What about emissions regulation?", List.of("imo"));

    private final FakePromptTemplates prompts = new FakePromptTemplates(
            "{{query}}|{{interpretation}}|E:{{evidence}}|W:{{weakEvidence}}|G:{{gaps}}|R:{{revision}}");

    private static EvidenceGroup group(String id, ConflictStatus conflict, String conflictingWith) {
        var conflictingIds = conflictingWith == null ? List.<String>of() : List.of(conflictingWith);
        var description = conflictingWith == null ? null : "3.1% vs 2.4%";
        return new EvidenceGroup(id, "q1", "Fleet grew 3.1% in 2025.", List.of(id + "-c1"), List.of("src-a"),
                SourceTier.A, RECENT, conflict, description, conflictingIds);
    }

    private static GroupConfidence confidence(String groupId, double score, ConfidenceLevel level) {
        return new GroupConfidence(groupId, score, level, List.of());
    }

    @Test
    void splitsEvidenceIntoEligibleAndWeakAndListsGapsInThePrompt() {
        var strong = group("q1-g1", ConflictStatus.NONE, null);
        var weak = group("q2-g1", ConflictStatus.OPEN, "q2-g2");
        var input = SynthesisInput.firstDraft(QUERY, INTERPRETATION, List.of(strong, weak),
                List.of(confidence("q1-g1", 0.9, ConfidenceLevel.HIGH), confidence("q2-g1", 0.3, ConfidenceLevel.LOW)),
                List.of(GAP));
        var llm = FakeLlmPort.returning(new SynthesisOutput(List.of(), List.of(), List.of(), List.of()));

        new Synthesizer(llm, prompts).synthesize(input, 1);

        var prompt = llm.getLastRequest().getUserPrompt();
        var eligibleSection = prompt.substring(prompt.indexOf("E:"), prompt.indexOf("|W:"));
        var weakSection = prompt.substring(prompt.indexOf("W:"), prompt.indexOf("|G:"));
        assertThat(eligibleSection).contains("[q1-g1] confidence HIGH (0.90)").doesNotContain("q2-g1");
        assertThat(weakSection).contains("[q2-g1] confidence LOW (0.30)").contains("conflicts with q2-g2: 3.1% vs 2.4%");
        assertThat(prompt).contains("[q3] What about emissions regulation?");
        assertThat(prompt).contains(SynthesisConstants.FIRST_DRAFT_NOTE);
        assertThat(llm.getLastRequest().getLabel()).isEqualTo("synthesizer/round1");
    }

    @Test
    void revisionIncludesPreviousDraftAndFindingsAndChangesTheLabel() {
        var strong = group("q1-g1", ConflictStatus.NONE, null);
        var previous = new BriefingDraft(List.of(new GroundedStatement("Old summary.", List.of("q1-g1"))),
                List.of(), List.of(), List.of("Old question?"), List.of(), List.of());
        var finding = new CriticFinding(FindingType.OVERSTATED_CERTAINTY, FindingSeverity.MAJOR, "Old summary.",
                "Evidence is medium, text says certain.", List.of("q1-g1"), List.of());
        var input = SynthesisInput.firstDraft(QUERY, INTERPRETATION, List.of(strong),
                List.of(confidence("q1-g1", 0.6, ConfidenceLevel.MEDIUM)), List.of()).revisedWith(previous,
                List.of(finding));
        var llm = FakeLlmPort.returning(new SynthesisOutput(List.of(), List.of(), List.of(), List.of()));

        new Synthesizer(llm, prompts).synthesize(input, 2);

        var prompt = llm.getLastRequest().getUserPrompt();
        assertThat(prompt).contains("- Old summary. (groups: q1-g1)")
                .contains("- MAJOR OVERSTATED_CERTAINTY on \"Old summary.\": Evidence is medium, text says certain.")
                .contains("- Old question?");
        assertThat(llm.getLastRequest().getLabel()).isEqualTo("synthesizer/round2/revision");
    }
}
