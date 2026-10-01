package com.assignment.research.adapter.output;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.adapter.pricing.CostEstimator;
import com.assignment.research.adapter.pricing.ModelPrice;
import com.assignment.research.adapter.pricing.PricingTable;
import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.critique.FindingSeverity;
import com.assignment.research.critique.FindingType;
import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceTier;
import com.assignment.research.evidence.SourceType;
import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.pipeline.AgentFailure;
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
import com.assignment.research.trace.TraceEntry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
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

            - The dry bulk fleet grew 3.1 percent in 2025. Sources: Maritime Statistics Bureau (2026).
              Evidence: one independent source, official statistics or industry body, published March 2026, no conflicting source. Confidence HIGH.

            """;
    private static final String EXPECTED_GAP = """
            ### What we could not answer

            - What does regulation require?
            """;
    private static final String EXPECTED_SUB_QUESTIONS = """
            ## Questions we investigated

            - How fast did the fleet grow? (adequate evidence found)
            - What does regulation require? (no adequate evidence found)
            """;
    private static final String EXPECTED_CONFIDENCE = """
            ## Confidence: MEDIUM

            Derived from the run record, not asserted by a model:

            - Key facts rest on tier A sources.
            - One sub-question has no adequate evidence.
            """;
    private static final String EXPECTED_STOP_REASON =
            "- Stop reason: ROUND_LIMIT_REACHED. The round limit of 3 was reached.";
    private static final String EXPECTED_AUDIT_LINES = """
            - Median key-fact evidence score: 0.82
            - Key fact 1 rests on groups g-fleet
            """;
    private static final List<String> EXPECTED_HEADINGS = List.of(
            "## Summary",
            "## Key facts",
            "## Identified uncertainties",
            "### What we could not answer",
            "## Confidence: MEDIUM",
            "## Suggested follow-up questions",
            "## Open review findings",
            "## Questions we investigated",
            "## How this briefing was produced",
            "## Sources consulted");
    private static final String INTERNAL_GROUP_REFERENCE = "[g-";

    private static final String SUMMARY = "Supply grows moderately.";
    private static final String EXPECTED_SUMMARY = """
            ## Summary

            - Supply grows moderately. Sources: Maritime Statistics Bureau (2026).

            ## Key facts
            """;
    private static final String MISSING_GROUP_ID = "g-missing";
    private static final String UNGROUNDED_FACT = "Ports were congested.";
    private static final String DERIVATIVE_ID = "src-tradepress";
    private static final String INTERPRETATION = "dry bulk supply side only";
    private static final String FOLLOW_UP = "How will the orderbook evolve?";
    private static final String EXPECTED_UNGROUNDED_FACT = """
            - Ports were congested. Sources: not identified.

            """;
    private static final String EXPECTED_FINDING = "- **Major, unsupported by the evidence:** "
            + "\"Ports were congested.\" Reviewer note: no evidence group supports it";
    private static final String EXPECTED_FOLLOW_UPS = """
            ## Suggested follow-up questions

            - How will the orderbook evolve?
            """;
    private static final String EXPECTED_COUNTS = """
            - Key fact 1 rests on groups g-missing
            - Statements removed for lacking evidence: 2
            - Key facts demoted to uncertainties for weak evidence: 1
            - Degraded steps:
              - round 2, critic: model returned malformed JSON twice
            """;
    private static final String EXPECTED_CITING_SOURCE =
            "- `src-tradepress` Fleet grows, Trade Press (TRADE_PRESS, tier B), 2026-03-01, cites `src-fleet-stats`";

    private static final String BROKER_ID = "src-broker";
    private static final String PRESS_ID = "src-press";
    private static final String BLOG_ID = "src-blog";
    private static final String UNKNOWN_SOURCE_ID = "src-unknown";
    private static final String OTHER_UNKNOWN_SOURCE_ID = "src-unknown-2";
    private static final String TWO_SOURCES_GROUP_ID = "g-two";
    private static final String THREE_SOURCES_GROUP_ID = "g-three";
    private static final String FOUR_SOURCES_GROUP_ID = "g-four";
    private static final String TWO_SOURCES_FACT = "The dry bulk fleet grew 3.1 percent in 2025.";
    private static final String THREE_SOURCES_FACT = "Owners are cautious about newbuild orders.";
    private static final String FOUR_SOURCES_FACT = "Scrapping will accelerate in 2026.";
    private static final String UNCITED_FACT = "Port congestion eased in the first quarter.";
    private static final String UNCERTAINTY = "Demand from China may soften.";
    private static final String EXPECTED_KEY_FACTS = """
            ## Key facts

            - The dry bulk fleet grew 3.1 percent in 2025. Sources: Maritime Statistics Bureau (2026), Nordhaven Shipbrokers (2026), Bulk Trade Weekly (2025).
              Evidence: three independent sources, the strongest being official statistics or industry body, the newest published March 2026, another source disagrees. Confidence HIGH.
            - Owners are cautious about newbuild orders. Sources: Deck Log Blog (2026).
              Evidence: three independent sources, the strongest being blog or forum, the newest published January 2026, an older source disagrees, this one is newer. Confidence LOW.
            - Scrapping will accelerate in 2026. Sources: Maritime Statistics Bureau (2026), Nordhaven Shipbrokers (2026), Bulk Trade Weekly (2025), Deck Log Blog (2026).
              Evidence: 4 independent sources, the strongest being official statistics or industry body, the newest published March 2026, a newer source disagrees. Confidence HIGH.
            - Port congestion eased in the first quarter. Sources: not identified.

            ## Identified uncertainties

            - Demand from China may soften. Sources: Nordhaven Shipbrokers (2026), Bulk Trade Weekly (2025).

            """;
    private static final String EXPECTED_KEY_FACT_GROUPS = """
            - Key fact 1 rests on groups g-fleet, g-two
            - Key fact 2 rests on groups g-three
            - Key fact 3 rests on groups g-four, g-fleet
            - Key fact 4 rests on no evidence group
            """;
    private static final String QUOTED_TEXT = "Scrapping will accelerate.";
    private static final String FINDING_DETAIL = "check the wording";
    private static final String EXPECTED_MINOR_FINDING =
            "- **Minor, hard to read:** \"Scrapping will accelerate.\" Reviewer note: check the wording";

    private static final String MODEL = "claude-sonnet-5-5";
    private static final PricingTable PRICING = new PricingTable(List.of(new ModelPrice(MODEL, 2.0, 0.2, 10.0)));
    private static final String EXPECTED_USAGE = """
            - Model calls: 3, tokens in: 3000 (of which cached: 1000), tokens out: 300
            - Estimated cost: USD 0.0072 (fresh input 0.0040, cached input 0.0002, output 0.0030; indicative list prices)
            """;
    private static final String EXPECTED_ROLE_TABLE = """
            | Agent role | Calls | Tokens in | Cached | Tokens out | Seconds |
            |---|---:|---:|---:|---:|---:|
            | planner | 1 | 1000 | 0 | 100 | 1.5 |
            | researcher | 2 | 2000 | 1000 | 200 | 3.0 |

            ## Sources consulted
            """;

    private final MarkdownBriefingRenderer renderer = new MarkdownBriefingRenderer(FIXED_CLOCK);

    @Test
    void rendersAnalystSectionsInReaderOrderWithPlainEvidenceNotesAndNoGroupIds() {
        var markdown = renderer.render(briefingResult());

        assertThat(markdown).startsWith(EXPECTED_HEADER);
        assertThat(markdown).containsSubsequence(EXPECTED_HEADINGS);
        assertThat(markdown).contains(EXPECTED_KEY_FACT);
        assertThat(markdown).contains(EXPECTED_GAP);
        assertThat(markdown).contains(EXPECTED_SUB_QUESTIONS);
        assertThat(markdown).contains(EXPECTED_CONFIDENCE);
        assertThat(markdown).contains(EXPECTED_STOP_REASON);
        assertThat(markdown).contains(EXPECTED_AUDIT_LINES);
        assertThat(markdown).doesNotContain(INTERNAL_GROUP_REFERENCE, "0.82)", "sq-1", "sq-2");
    }

    @Test
    void attributesStatementsToDistinctPublishersAndDescribesEveryTierConflictAndSourceCount() {
        var markdown = renderer.render(multiSourceBriefingResult());

        assertThat(markdown).contains(EXPECTED_KEY_FACTS);
        assertThat(markdown).contains(EXPECTED_KEY_FACT_GROUPS);
        assertThat(markdown).contains(EXPECTED_MINOR_FINDING);
        assertThat(markdown).doesNotContain(INTERNAL_GROUP_REFERENCE, UNKNOWN_SOURCE_ID);
    }

    @Test
    void wordTablesCoverEveryEnumValue() {
        assertThat(OutputConstants.TIER_WORDS).containsOnlyKeys(SourceTier.values());
        assertThat(OutputConstants.CONFLICT_WORDS).containsOnlyKeys(ConflictStatus.values());
        assertThat(OutputConstants.SEVERITY_WORDS).containsOnlyKeys(FindingSeverity.values());
        assertThat(OutputConstants.FINDING_TYPE_WORDS).containsOnlyKeys(FindingType.values());
    }

    @Test
    void rendersEveryFindingTypeInPlainWords() {
        var findings = Arrays.stream(FindingType.values())
                .map(type -> new CriticFinding(type, FindingSeverity.MINOR, QUOTED_TEXT, FINDING_DETAIL, List.of(),
                        List.of()))
                .toList();
        var base = briefingResult();
        var result = new BriefingResult(base.getDraft(), base.getConfidence(), findings, base.getGaps(),
                base.getStopDecision(), base.getFinalState(), List.of(), base.getUsage());

        var markdown = renderer.render(result);

        assertThat(markdown).contains(OutputConstants.FINDING_TYPE_WORDS.values());
        assertThat(markdown).doesNotContain(FindingType.CONTRADICTS_EVIDENCE.name(), FindingSeverity.MINOR.name());
    }

    @Test
    void rendersOpenFindingsFollowUpsDegradedStepsCountsAndCitationsWithoutGaps() {
        var markdown = renderer.render(degradedBriefingResult());

        assertThat(markdown).contains("> **Scope as understood by the system:** " + INTERPRETATION);
        assertThat(markdown).contains(EXPECTED_SUMMARY);
        assertThat(markdown).contains(EXPECTED_UNGROUNDED_FACT);
        assertThat(markdown).doesNotContain(OutputConstants.GAPS_HEADING, OutputConstants.REVIEWER_APPROVED);
        assertThat(markdown).contains(EXPECTED_FINDING);
        assertThat(markdown).contains(EXPECTED_FOLLOW_UPS);
        assertThat(markdown).contains(EXPECTED_COUNTS);
        assertThat(markdown).contains(EXPECTED_CITING_SOURCE);
    }

    @Test
    void pricedRendererShowsCachedTokensEstimatedCostAndAPerRoleTable() {
        var pricedRenderer = new MarkdownBriefingRenderer(FIXED_CLOCK, new CostEstimator(PRICING));

        var markdown = pricedRenderer.render(tracedBriefingResult());

        assertThat(markdown).contains(EXPECTED_USAGE);
        assertThat(markdown).contains(EXPECTED_ROLE_TABLE);
    }

    @Test
    void freeRendererOmitsTheCostLineAndAnEmptyTraceOmitsTheRoleTable() {
        var markdown = renderer.render(briefingResult());

        assertThat(markdown).contains("- Model calls: 0, tokens in: 100 (of which cached: 0), tokens out: 50");
        assertThat(markdown).doesNotContain("Estimated cost", OutputConstants.ROLE_TABLE_HEADER);
        assertThat(renderer.render(tracedBriefingResult())).contains(OutputConstants.ROLE_TABLE_HEADER)
                .doesNotContain("Estimated cost");
    }

    @Test
    void approvedDraftWithoutFailuresOmitsCountsAndShowsTheApproval() {
        var markdown = renderer.render(briefingResult());

        assertThat(markdown).contains(OutputConstants.REVIEWER_APPROVED);
        assertThat(markdown).doesNotContain(OutputConstants.DEGRADED_STEPS_LINE, "Statements removed",
                "Key facts demoted");
    }

    private static BriefingResult multiSourceBriefingResult() {
        var keyFacts = List.of(
                new GroundedStatement(TWO_SOURCES_FACT, List.of(GROUP_ID, TWO_SOURCES_GROUP_ID)),
                new GroundedStatement(THREE_SOURCES_FACT, List.of(THREE_SOURCES_GROUP_ID)),
                new GroundedStatement(FOUR_SOURCES_FACT, List.of(FOUR_SOURCES_GROUP_ID, GROUP_ID)),
                new GroundedStatement(UNCITED_FACT, List.of()));
        var uncertainties = List.of(new GroundedStatement(UNCERTAINTY, List.of(TWO_SOURCES_GROUP_ID)));
        var draft = new BriefingDraft(List.of(), keyFacts, uncertainties, List.of(), List.of(), List.of());
        var finding = new CriticFinding(FindingType.READABILITY, FindingSeverity.MINOR, QUOTED_TEXT, FINDING_DETAIL,
                List.of(), List.of());
        var base = briefingResult();
        var baseState = base.getFinalState();
        var state = baseState.toBuilder()
                .sources(List.of(
                        baseState.getSources().getFirst(),
                        source(BROKER_ID, "Nordhaven Shipbrokers", SourceType.BROKER_NOTE, LocalDate.of(2026, 2, 10)),
                        source(PRESS_ID, "Bulk Trade Weekly", SourceType.TRADE_PRESS, LocalDate.of(2025, 11, 20)),
                        source(BLOG_ID, "Deck Log Blog", SourceType.BLOG, LocalDate.of(2026, 1, 5))))
                .groups(List.of(
                        baseState.getGroups().getFirst(),
                        group(TWO_SOURCES_GROUP_ID, List.of(BROKER_ID, PRESS_ID), SourceTier.B,
                                LocalDate.of(2026, 2, 10), ConflictStatus.OPEN),
                        group(THREE_SOURCES_GROUP_ID, List.of(BLOG_ID, UNKNOWN_SOURCE_ID, OTHER_UNKNOWN_SOURCE_ID),
                                SourceTier.C, LocalDate.of(2026, 1, 5), ConflictStatus.RESOLVED_BY_RECENCY),
                        group(FOUR_SOURCES_GROUP_ID, List.of(SOURCE_ID, BROKER_ID, PRESS_ID, BLOG_ID), SourceTier.A,
                                PUBLISHED, ConflictStatus.SUPERSEDED)))
                .confidences(List.of(
                        baseState.getConfidences().getFirst(),
                        new GroupConfidence(TWO_SOURCES_GROUP_ID, 0.5, ConfidenceLevel.MEDIUM, List.of()),
                        new GroupConfidence(THREE_SOURCES_GROUP_ID, 0.2, ConfidenceLevel.LOW, List.of()),
                        new GroupConfidence(FOUR_SOURCES_GROUP_ID, 0.6, ConfidenceLevel.MEDIUM, List.of())))
                .build();
        return new BriefingResult(draft, base.getConfidence(), List.of(finding), List.of(), base.getStopDecision(),
                state, List.of(), base.getUsage());
    }

    private static Source source(String id, String publisher, SourceType type, LocalDate publishedAt) {
        return new Source(id, publisher + " note", publisher, type, publishedAt, null, List.of(), "Excerpt.");
    }

    private static EvidenceGroup group(String id, List<String> sourceIds, SourceTier tier, LocalDate newest,
            ConflictStatus conflict) {
        return new EvidenceGroup(id, Set.of(COVERED.getId()), KEY_FACT, List.of("c-" + id), sourceIds, tier, newest,
                conflict, null, List.of());
    }

    private static BriefingResult degradedBriefingResult() {
        var summary = new GroundedStatement(SUMMARY, List.of(GROUP_ID));
        var draft = new BriefingDraft(List.of(summary), List.of(new GroundedStatement(UNGROUNDED_FACT,
                List.of(MISSING_GROUP_ID))), List.of(), List.of(FOLLOW_UP), List.of("weak fact"),
                List.of("dropped one", "dropped two"));
        var finding = new CriticFinding(FindingType.UNSUPPORTED, FindingSeverity.MAJOR, UNGROUNDED_FACT,
                "no evidence group supports it", List.of(MISSING_GROUP_ID), List.of());
        var confidence = new BriefingConfidence(ConfidenceLevel.LOW, 0.2, List.of(SECOND_REASON));
        var stopDecision = new StopDecision(StopReason.ROUND_LIMIT_REACHED, STOP_EXPLANATION);
        var base = finalState();
        var derivative = new Source(DERIVATIVE_ID, "Fleet grows", "Trade Press", SourceType.TRADE_PRESS,
                PUBLISHED, SOURCE_ID, List.of(), "Citing the bureau.");
        var state = base.toBuilder()
                .interpretation(INTERPRETATION)
                .sources(List.of(base.getSources().getFirst(), derivative))
                .failures(List.of(new AgentFailure("critic", 2, "model returned malformed JSON twice")))
                .build();
        return new BriefingResult(draft, confidence, List.of(finding), List.of(), stopDecision, state, List.of(),
                new LLMUsage(100, 50));
    }

    private static BriefingResult tracedBriefingResult() {
        var trace = List.of(
                traceEntry(1, "planner", new LLMUsage(1000, 100, 0), 1500),
                traceEntry(2, "researcher/sq-1", new LLMUsage(1000, 100, 500), 1000),
                traceEntry(3, "researcher/sq-2", new LLMUsage(1000, 100, 500), 2000));
        var usage = trace.stream().map(TraceEntry::getUsage).reduce(LLMUsage.NONE, LLMUsage::plus);
        var base = briefingResult();
        return new BriefingResult(base.getDraft(), base.getConfidence(), base.getOpenFindings(), base.getGaps(),
                base.getStopDecision(), base.getFinalState(), trace, usage);
    }

    private static TraceEntry traceEntry(int sequence, String label, LLMUsage usage, long millis) {
        return new TraceEntry(sequence, label, Instant.EPOCH, Duration.ofMillis(millis), MODEL, "system", "user",
                "raw", usage, LLMCallStatus.OK, null);
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
