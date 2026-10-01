package com.assignment.research.confidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.assignment.research.evidence.SourceTier;
import com.assignment.research.reconciliation.ConflictStatus;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ConfidenceCalculatorTest {

    private static final Clock TODAY = Clock.fixed(Instant.parse("2026-10-01T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate RECENT = LocalDate.of(2026, 3, 1);
    private static final LocalDate STALE = LocalDate.of(2019, 6, 1);
    private static final double TOLERANCE = 0.0001;

    private final ConfidenceCalculator calculator = new ConfidenceCalculator(TODAY);

    private static EvidenceGroup group(SourceTier tier, int independentSources, LocalDate newest,
            ConflictStatus conflict) {
        var sourceIds = IntStream.range(0, independentSources).mapToObj(index -> "src-" + index).toList();
        return new EvidenceGroup("q1-g1", "q1", "assertion", List.of("q1-c1"), sourceIds, tier, newest, conflict,
                null, List.of());
    }

    @Test
    void singleRecentTierASourceWithoutConflictIsMedium() {
        var confidence = calculator.score(group(SourceTier.A, 1, RECENT, ConflictStatus.NONE));

        assertThat(confidence.getScore()).isCloseTo(0.60, within(TOLERANCE));
        assertThat(confidence.getLevel()).isEqualTo(ConfidenceLevel.MEDIUM);
        assertThat(confidence.getFactors()).hasSize(1);
    }

    @Test
    void threeIndependentTierASourcesReachHigh() {
        var confidence = calculator.score(group(SourceTier.A, 3, RECENT, ConflictStatus.NONE));

        assertThat(confidence.getScore()).isCloseTo(0.90, within(TOLERANCE));
        assertThat(confidence.getLevel()).isEqualTo(ConfidenceLevel.HIGH);
    }

    @Test
    void corroborationBonusIsCapped() {
        var confidence = calculator.score(group(SourceTier.B, 6, RECENT, ConflictStatus.NONE));

        assertThat(confidence.getScore()).isCloseTo(0.70, within(TOLERANCE));
    }

    @Test
    void loneTierCBlogStaysLowRegardlessOfTone() {
        var confidence = calculator.score(group(SourceTier.C, 1, RECENT, ConflictStatus.NONE));

        assertThat(confidence.getScore()).isCloseTo(0.15, within(TOLERANCE));
        assertThat(confidence.getLevel()).isEqualTo(ConfidenceLevel.LOW);
    }

    @Test
    void openConflictPushesTierAAndBPairBelowMedium() {
        var confidence = calculator.score(group(SourceTier.A, 1, RECENT, ConflictStatus.OPEN));

        assertThat(confidence.getScore()).isCloseTo(0.30, within(TOLERANCE));
        assertThat(confidence.getLevel()).isEqualTo(ConfidenceLevel.LOW);
        assertThat(confidence.getFactors()).extracting(ConfidenceFactor::getLabel)
                .contains("open conflict with another source group");
    }

    @Test
    void staleSourcesArePenalisedAndScoreNeverGoesNegative() {
        var confidence = calculator.score(group(SourceTier.C, 1, STALE, ConflictStatus.SUPERSEDED));

        assertThat(confidence.getScore()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(confidence.getFactors()).extracting(ConfidenceFactor::getLabel)
                .contains("newest source older than 3 years", "superseded by a newer conflicting group");
    }

    @Test
    void resolvedConflictOnlyCostsALittle() {
        var confidence = calculator.score(group(SourceTier.B, 2, RECENT, ConflictStatus.RESOLVED_BY_RECENCY));

        assertThat(confidence.getScore()).isCloseTo(0.45, within(TOLERANCE));
        assertThat(confidence.getLevel()).isEqualTo(ConfidenceLevel.MEDIUM);
    }

    @Test
    void factorsSumExactlyToTheScoreSoTheNumberIsAuditable() {
        var confidence = calculator.score(group(SourceTier.A, 2, RECENT, ConflictStatus.RESOLVED_BY_RECENCY));

        var sum = confidence.getFactors().stream().mapToDouble(ConfidenceFactor::getDelta).sum();
        assertThat(confidence.getScore()).isCloseTo(sum, within(TOLERANCE));
    }
}
