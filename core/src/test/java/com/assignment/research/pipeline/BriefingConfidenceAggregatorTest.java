package com.assignment.research.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.critique.Critique;
import com.assignment.research.critique.FindingSeverity;
import com.assignment.research.critique.FindingType;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.synthesis.GroundedStatement;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class BriefingConfidenceAggregatorTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk shipping market");
    private static final String STRONG_GROUP = "g-strong";
    private static final String MEDIUM_GROUP = "g-medium";
    private static final double STRONG_SCORE = 0.8;
    private static final double MEDIUM_SCORE = 0.6;
    private static final double EXPECTED_MEDIAN = 0.7;
    private static final double TOLERANCE = 1e-9;
    private static final String EXPECTED_OPEN_FINDINGS_REASON = "1 open review finding(s), 0 of them major";

    private static BriefingDraft draftWithKeyFacts(String... groupIds) {
        var keyFacts = Stream.of(groupIds)
                .map(groupId -> new GroundedStatement("Fact of " + groupId, List.of(groupId)))
                .toList();
        return new BriefingDraft(List.of(), keyFacts, List.of(), List.of(), List.of(), List.of());
    }

    private static BriefingState stateWith(BriefingDraft draft) {
        return BriefingState.initial(QUERY).toBuilder()
                .draft(draft)
                .confidences(List.of(
                        new GroupConfidence(STRONG_GROUP, STRONG_SCORE, ConfidenceLevel.HIGH, List.of()),
                        new GroupConfidence(MEDIUM_GROUP, MEDIUM_SCORE, ConfidenceLevel.MEDIUM, List.of())))
                .build();
    }

    @Test
    void evenNumberOfKeyFactsAveragesTheTwoMiddleScores() {
        var confidence = BriefingConfidenceAggregator.aggregate(stateWith(draftWithKeyFacts(STRONG_GROUP,
                MEDIUM_GROUP)));

        assertThat(confidence.getMedianKeyFactScore()).isCloseTo(EXPECTED_MEDIAN, within(TOLERANCE));
        assertThat(confidence.getReasons()).contains(PipelineConstants.REASON_APPROVED);
    }

    @Test
    void noKeyFactsYieldsNoScoreAndLowConfidence() {
        var confidence = BriefingConfidenceAggregator.aggregate(stateWith(draftWithKeyFacts()));

        assertThat(confidence.getMedianKeyFactScore()).isEqualTo(PipelineConstants.NO_SCORE);
        assertThat(confidence.getLevel()).isEqualTo(ConfidenceLevel.LOW);
        assertThat(confidence.getReasons()).contains(PipelineConstants.REASON_NO_KEY_FACTS);
    }

    @Test
    void openMinorFindingsAreReportedWithoutCappingTheLevel() {
        var minor = new CriticFinding(FindingType.READABILITY, FindingSeverity.MINOR, "x", "Long sentence.",
                List.of(), List.of());
        var state = stateWith(draftWithKeyFacts(STRONG_GROUP)).withCritiqueAppended(new Critique(List.of(minor)));

        var confidence = BriefingConfidenceAggregator.aggregate(state);

        assertThat(confidence.getLevel()).isEqualTo(ConfidenceLevel.HIGH);
        assertThat(confidence.getReasons()).contains(EXPECTED_OPEN_FINDINGS_REASON)
                .doesNotContain(PipelineConstants.REASON_MAJOR_CAP);
    }
}
