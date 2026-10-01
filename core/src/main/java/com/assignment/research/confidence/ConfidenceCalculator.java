package com.assignment.research.confidence;

import com.assignment.research.evidence.SourceTier;
import com.assignment.research.reconciliation.ConflictStatus;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ConfidenceCalculator {

    static final double TIER_A_BASE = 0.60;
    static final double TIER_B_BASE = 0.40;
    static final double TIER_C_BASE = 0.15;
    static final double CORROBORATION_BONUS_PER_SOURCE = 0.15;
    static final double CORROBORATION_BONUS_CAP = 0.30;
    static final double STALE_PENALTY = -0.20;
    static final double OPEN_CONFLICT_PENALTY = -0.30;
    static final double RESOLVED_CONFLICT_PENALTY = -0.10;
    static final double SUPERSEDED_PENALTY = -0.50;
    static final int STALE_AFTER_YEARS = 3;
    static final double HIGH_THRESHOLD = 0.70;
    static final double MEDIUM_THRESHOLD = 0.40;

    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 1.0;
    private static final int SINGLE_SOURCE = 1;

    private static final String TIER_LABEL = "best source tier %s";
    private static final String CORROBORATION_LABEL = "%d additional independent source(s)";
    private static final String STALE_LABEL = "newest source older than %d years";
    private static final String OPEN_CONFLICT_LABEL = "open conflict with another source group";
    private static final String RESOLVED_CONFLICT_LABEL = "conflict resolved in favour of this newer group";
    private static final String SUPERSEDED_LABEL = "superseded by a newer conflicting group";

    private static final Map<SourceTier, Double> BASE_BY_TIER = new EnumMap<>(Map.of(
            SourceTier.A, TIER_A_BASE,
            SourceTier.B, TIER_B_BASE,
            SourceTier.C, TIER_C_BASE));
    private static final Map<ConflictStatus, Double> CONFLICT_PENALTIES = new EnumMap<>(Map.of(
            ConflictStatus.OPEN, OPEN_CONFLICT_PENALTY,
            ConflictStatus.RESOLVED_BY_RECENCY, RESOLVED_CONFLICT_PENALTY,
            ConflictStatus.SUPERSEDED, SUPERSEDED_PENALTY));
    private static final Map<ConflictStatus, String> CONFLICT_LABELS = new EnumMap<>(Map.of(
            ConflictStatus.OPEN, OPEN_CONFLICT_LABEL,
            ConflictStatus.RESOLVED_BY_RECENCY, RESOLVED_CONFLICT_LABEL,
            ConflictStatus.SUPERSEDED, SUPERSEDED_LABEL));

    private final Clock clock;

    public GroupConfidence score(EvidenceGroup group) {
        var factors = new ArrayList<ConfidenceFactor>();
        factors.add(new ConfidenceFactor(TIER_LABEL.formatted(group.getBestTier()),
                BASE_BY_TIER.get(group.getBestTier())));
        corroboration(group).ifPresent(factors::add);
        staleness(group).ifPresent(factors::add);
        conflict(group).ifPresent(factors::add);

        var score = clamp(factors.stream().mapToDouble(ConfidenceFactor::getDelta).sum());
        return new GroupConfidence(group.getId(), score, levelFor(score), List.copyOf(factors));
    }

    public List<GroupConfidence> scoreAll(List<EvidenceGroup> groups) {
        return groups.stream().map(this::score).toList();
    }

    public static ConfidenceLevel levelFor(double score) {
        if (score >= HIGH_THRESHOLD) {
            return ConfidenceLevel.HIGH;
        }
        if (score >= MEDIUM_THRESHOLD) {
            return ConfidenceLevel.MEDIUM;
        }
        return ConfidenceLevel.LOW;
    }

    private static Optional<ConfidenceFactor> corroboration(EvidenceGroup group) {
        var additional = group.getIndependentSourceCount() - SINGLE_SOURCE;
        if (additional <= 0) {
            return Optional.empty();
        }
        var bonus = Math.min(additional * CORROBORATION_BONUS_PER_SOURCE, CORROBORATION_BONUS_CAP);
        return Optional.of(new ConfidenceFactor(CORROBORATION_LABEL.formatted(additional), bonus));
    }

    private Optional<ConfidenceFactor> staleness(EvidenceGroup group) {
        var age = Period.between(group.getNewestSourceDate(), LocalDate.now(clock)).getYears();
        if (age < STALE_AFTER_YEARS) {
            return Optional.empty();
        }
        return Optional.of(new ConfidenceFactor(STALE_LABEL.formatted(STALE_AFTER_YEARS), STALE_PENALTY));
    }

    private static Optional<ConfidenceFactor> conflict(EvidenceGroup group) {
        var status = group.getConflictStatus();
        if (status == ConflictStatus.NONE) {
            return Optional.empty();
        }
        return Optional.of(new ConfidenceFactor(CONFLICT_LABELS.get(status), CONFLICT_PENALTIES.get(status)));
    }

    private static double clamp(double score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }
}
