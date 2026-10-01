package com.assignment.research.critique;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.evidence.SourceTier;
import com.assignment.research.llm.FakeLlmPort;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.FakePromptTemplates;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.ConflictStatus;
import com.assignment.research.reconciliation.EvidenceGroup;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.synthesis.GroundedStatement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CriticTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Dry bulk shipping overview");
    private static final EvidenceGroup GROUP = new EvidenceGroup("q1-g1", "q1", "Fleet grew 3.1% in 2025.",
            List.of("q1-c1"), List.of("src-a"), SourceTier.A, LocalDate.of(2026, 3, 1), ConflictStatus.NONE, null,
            List.of());
    private static final GroupConfidence CONFIDENCE = new GroupConfidence("q1-g1", 0.6, ConfidenceLevel.MEDIUM,
            List.of());
    private static final SubQuestion GAP = new SubQuestion("q3", "Emissions rules?", List.of("imo"));

    private final FakePromptTemplates prompts =
            new FakePromptTemplates("{{query}}|D:{{draft}}|E:{{evidence}}|G:{{gaps}}");

    @Test
    void sendsDraftEvidenceAndGapsAndLabelsByRoundAndPass() {
        var draft = new BriefingDraft(List.of(new GroundedStatement("Fleet growth is certain.", List.of("q1-g1"))),
                List.of(), List.of(), List.of("Next?"), List.of(), List.of());
        var output = new CritiqueOutput(List.of(new FindingOutput(FindingType.OVERSTATED_CERTAINTY,
                FindingSeverity.MAJOR, "Fleet growth is certain.", "MEDIUM evidence.", List.of("q1-g1"), List.of())));
        var llm = FakeLlmPort.returning(output);

        var critique = new Critic(llm, prompts).critique(
                new CritiqueInput(QUERY, draft, List.of(GROUP), List.of(CONFIDENCE), List.of(GAP)), 1, 2);

        assertThat(critique.getFindings()).hasSize(1);
        var request = llm.getLastRequest();
        assertThat(request.getLabel()).isEqualTo("critic/round1/pass2");
        assertThat(request.getUserPrompt())
                .contains("- Fleet growth is certain. (groups: q1-g1)")
                .contains("[q1-g1] confidence MEDIUM (0.60)")
                .contains("[q3] Emissions rules?");
    }
}
