package com.assignment.research.adapter.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceTier;
import com.assignment.research.evidence.SourceType;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.pipeline.BriefingConfidence;
import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.pipeline.StopDecision;
import com.assignment.research.pipeline.StopReason;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.ConflictStatus;
import com.assignment.research.reconciliation.EvidenceGroup;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.synthesis.GroundedStatement;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MarkdownBriefingRendererTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-20T10:15:30Z"), ZoneOffset.UTC);
    private static final LocalDate PUBLISHED = LocalDate.of(2026, 3, 1);
    private static final String GROUP_ID = "g-fleet";
    private static final String SOURCE_ID = "src-fleet-stats";
    private static final SubQuestion COVERED = new SubQuestion("sq-1", "How fast did the fleet grow?", List.of());
    private static final SubQuestion GAP = new SubQuestion("sq-2", "What does regulation require?", List.of());
    private static final String KEY_FACT = "The dry bulk fleet grew 3.1 percent in 2025.";
    private static final String FIRST_REASON = "Key facts rest on tier A sources.";
    private static final String SECOND_REASON = "One sub-question has no adequate evidence.";
    private static final String STOP_EXPLANATION = "The round limit of 3 was reached.";

    private static final String EXPECTED_HEADER = """
            # Analyst briefing

            > **Query:** How is dry bulk supply developing?
            > **Generated:** 2026-05-20 10:15
            > **Overall confidence:** MEDIUM
            """;
    private static final String EXPECTED_KEY_FACT = """
            ## Key facts

            - The dry bulk fleet grew 3.1 percent in 2025. [g-fleet]
              - evidence: 1 independent source(s), best tier A, newest 2026-03-01, conflict NONE, confidence HIGH (0.82)
            """;
    private static final String EXPECTED_GAP = """
            ### Sub-questions without adequate evidence

            - sq-2: What does regulation require?
            """;
    private static final String EXPECTED_CONFIDENCE = """
            ## Confidence: MEDIUM

            Derived from the run record, not asserted by a model:

            - Key facts rest on tier A sources.
            - One sub-question has no adequate evidence.
            """;
    private static final String EXPECTED_STOP_REASON =
            "- Stop reason: ROUND_LIMIT_REACHED. The round limit of 3 was reached.";
    private static final List<String> EXPECTED_HEADINGS = List.of(
            "## Sub-questions investigated",
            "## Summary",
            "## Key facts",
            "## Identified uncertainties",
            "## Confidence: MEDIUM",
            "## Open review findings",
            "## Suggested follow-up questions",
            "## How this briefing was produced",
            "## Sources consulted");

    private final MarkdownBriefingRenderer renderer = new MarkdownBriefingRenderer(FIXED_CLOCK);

    @Test
    void rendersSectionsKeyFactEvidenceGapConfidenceReasonsAndStopReason() {
        var markdown = renderer.render(briefingResult());

        assertThat(markdown).startsWith(EXPECTED_HEADER);
        assertThat(markdown).containsSubsequence(EXPECTED_HEADINGS);
        assertThat(markdown).contains(EXPECTED_KEY_FACT);
        assertThat(markdown).contains(EXPECTED_GAP);
        assertThat(markdown).contains(EXPECTED_CONFIDENCE);
        assertThat(markdown).contains(EXPECTED_STOP_REASON);
    }

    private static BriefingResult briefingResult() {
        var draft = new BriefingDraft(List.of(), List.of(new GroundedStatement(KEY_FACT, List.of(GROUP_ID))),
                List.of(), List.of(), List.of(), List.of());
        var confidence = new BriefingConfidence(ConfidenceLevel.MEDIUM, 0.82, List.of(FIRST_REASON, SECOND_REASON));
        var stopDecision = new StopDecision(StopReason.ROUND_LIMIT_REACHED, STOP_EXPLANATION);
        return new BriefingResult(draft, confidence, List.of(), List.of(GAP), stopDecision, finalState(), List.of(),
                new LLMUsage(100, 50));
    }

    private static BriefingState finalState() {
        var source = new Source(SOURCE_ID, "Fleet statistics 2025", "Maritime Statistics Bureau",
                SourceType.OFFICIAL_STATISTICS, PUBLISHED, null, List.of(), "The fleet grew 3.1 percent.");
        var group = new EvidenceGroup(GROUP_ID, Set.of(COVERED.getId()), KEY_FACT, List.of("c-1"),
                List.of(SOURCE_ID), SourceTier.A, PUBLISHED, ConflictStatus.NONE, null, List.of());
        var groupConfidence = new GroupConfidence(GROUP_ID, 0.82, ConfidenceLevel.HIGH, List.of());
        return BriefingState.initial(new AnalystQuery("How is dry bulk supply developing?")).toBuilder()
                .subQuestions(List.of(COVERED, GAP))
                .sources(List.of(source))
                .groups(List.of(group))
                .confidences(List.of(groupConfidence))
                .round(3)
                .build();
    }
}
