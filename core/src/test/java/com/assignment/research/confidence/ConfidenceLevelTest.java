package com.assignment.research.confidence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfidenceLevelTest {

    @Test
    void cappingNeverRaisesALevel() {
        assertThat(ConfidenceLevel.HIGH.cappedAt(ConfidenceLevel.MEDIUM)).isEqualTo(ConfidenceLevel.MEDIUM);
        assertThat(ConfidenceLevel.LOW.cappedAt(ConfidenceLevel.MEDIUM)).isEqualTo(ConfidenceLevel.LOW);
    }

    @Test
    void thresholdsMapScoresToLevels() {
        assertThat(ConfidenceCalculator.levelFor(0.70)).isEqualTo(ConfidenceLevel.HIGH);
        assertThat(ConfidenceCalculator.levelFor(0.69)).isEqualTo(ConfidenceLevel.MEDIUM);
        assertThat(ConfidenceCalculator.levelFor(0.40)).isEqualTo(ConfidenceLevel.MEDIUM);
        assertThat(ConfidenceCalculator.levelFor(0.39)).isEqualTo(ConfidenceLevel.LOW);
    }
}
