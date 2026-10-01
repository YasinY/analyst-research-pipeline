package com.assignment.research.confidence;

import static com.assignment.research.confidence.ConfidenceConstants.CORROBORATION_BONUS_CAP;
import static com.assignment.research.confidence.ConfidenceConstants.CORROBORATION_BONUS_PER_SOURCE;
import static com.assignment.research.confidence.ConfidenceConstants.CORROBORATION_LABEL;
import static com.assignment.research.confidence.ConfidenceConstants.HIGH_THRESHOLD;
import static com.assignment.research.confidence.ConfidenceConstants.MAX_SCORE;
import static com.assignment.research.confidence.ConfidenceConstants.MEDIUM_THRESHOLD;
import static com.assignment.research.confidence.ConfidenceConstants.MIN_SCORE;
import static com.assignment.research.confidence.ConfidenceConstants.OPEN_CONFLICT_LABEL;
import static com.assignment.research.confidence.ConfidenceConstants.OPEN_CONFLICT_PENALTY;
import static com.assignment.research.confidence.ConfidenceConstants.RESOLVED_CONFLICT_LABEL;
import static com.assignment.research.confidence.ConfidenceConstants.RESOLVED_CONFLICT_PENALTY;
import static com.assignment.research.confidence.ConfidenceConstants.SINGLE_SOURCE;
import static com.assignment.research.confidence.ConfidenceConstants.STALE_AFTER_YEARS;
import static com.assignment.research.confidence.ConfidenceConstants.STALE_LABEL;
import static com.assignment.research.confidence.ConfidenceConstants.STALE_PENALTY;
import static com.assignment.research.confidence.ConfidenceConstants.SUPERSEDED_LABEL;
import static com.assignment.research.confidence.ConfidenceConstants.SUPERSEDED_PENALTY;
import static com.assignment.research.confidence.ConfidenceConstants.TIER_A_BASE;
import static com.assignment.research.confidence.ConfidenceConstants.TIER_B_BASE;
import static com.assignment.research.confidence.ConfidenceConstants.TIER_C_BASE;
import static com.assignment.research.confidence.ConfidenceConstants.TIER_LABEL;

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
