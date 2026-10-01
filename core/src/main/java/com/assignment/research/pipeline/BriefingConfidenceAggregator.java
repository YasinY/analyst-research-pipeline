package com.assignment.research.pipeline;

import com.assignment.research.confidence.ConfidenceCalculator;
import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.Critique;
import com.assignment.research.synthesis.BriefingDraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BriefingConfidenceAggregator {

    private static final double NO_SCORE = 0.0;
    private static final String REASON_KEY_FACTS = "%d key fact(s) with a median evidence score of %.2f";
    private static final String REASON_NO_KEY_FACTS = "no key fact rests on adequate evidence";
    private static final String REASON_COVERAGE = "%d of %d sub-question(s) covered by adequate evidence";
    private static final String REASON_COVERAGE_CAP = "confidence capped at MEDIUM because of uncovered sub-questions";
    private static final String REASON_OPEN_FINDINGS = "%d open review finding(s), %d of them major";
    private static final String REASON_MAJOR_CAP = "confidence capped at LOW because a major review finding is open";
    private static final String REASON_UNVERIFIED = "confidence capped at LOW because the review step failed";
    private static final String REASON_APPROVED = "the reviewer approved the final draft";

    private BriefingConfidenceAggregator() {
    }

    public static BriefingConfidence aggregate(BriefingState state) {
        var reasons = new ArrayList<String>();
        var median = medianKeyFactScore(state, reasons);
        var level = ConfidenceCalculator.levelFor(median);

        var covered = CoverageAnalyzer.coveredSubQuestionIds(state).size();
        var total = state.getSubQuestions().size();
        reasons.add(REASON_COVERAGE.formatted(covered, total));
        if (covered < total) {
            level = level.cappedAt(ConfidenceLevel.MEDIUM);
            reasons.add(REASON_COVERAGE_CAP);
        }

        level = applyReview(state, level, reasons);
        return new BriefingConfidence(level, median, List.copyOf(reasons));
    }

    private static double medianKeyFactScore(BriefingState state, List<String> reasons) {
        var scoresByGroup = state.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, GroupConfidence::getScore));
        var scores = state.getDraft().map(BriefingDraft::getKeyFacts).orElse(List.of()).stream()
                .map(fact -> fact.getGroupIds().stream().map(scoresByGroup::get).filter(score -> score != null)
                        .mapToDouble(Double::doubleValue).max().orElse(NO_SCORE))
                .sorted()
                .toList();
        if (scores.isEmpty()) {
            reasons.add(REASON_NO_KEY_FACTS);
            return NO_SCORE;
        }
        var middle = scores.size() / 2;
        var median = scores.size() % 2 == 1 ? scores.get(middle) : (scores.get(middle - 1) + scores.get(middle)) / 2;
        reasons.add(String.format(Locale.ROOT, REASON_KEY_FACTS, scores.size(), median));
        return median;
    }

    private static ConfidenceLevel applyReview(BriefingState state, ConfidenceLevel level, List<String> reasons) {
        if (state.isCritiqueFailed()) {
            reasons.add(REASON_UNVERIFIED);
            return ConfidenceLevel.LOW;
        }
        var critique = state.getLatestCritique().map(Function.identity()).orElse(null);
        if (critique == null || critique.isApproved()) {
            reasons.add(REASON_APPROVED);
            return level;
        }
        return applyOpenFindings(critique, level, reasons);
    }

    private static ConfidenceLevel applyOpenFindings(Critique critique, ConfidenceLevel level, List<String> reasons) {
        var majors = critique.getFindings().stream().filter(finding -> finding.isMajor()).count();
        reasons.add(REASON_OPEN_FINDINGS.formatted(critique.getFindings().size(), majors));
        if (majors == 0) {
            return level;
        }
        reasons.add(REASON_MAJOR_CAP);
        return ConfidenceLevel.LOW;
    }
}
