package com.assignment.research.pipeline;

import com.assignment.research.confidence.ConfidenceCalculator;
import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.critique.Critique;
import com.assignment.research.synthesis.BriefingDraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BriefingConfidenceAggregator {

    private BriefingConfidenceAggregator() {
    }

    public static BriefingConfidence aggregate(BriefingState state) {
        var reasons = new ArrayList<String>();
        var median = medianKeyFactScore(state, reasons);
        var level = ConfidenceCalculator.levelFor(median);

        var covered = CoverageAnalyzer.coveredSubQuestionIds(state).size();
        var total = state.getSubQuestions().size();
        reasons.add(PipelineConstants.REASON_COVERAGE.formatted(covered, total));
        if (covered < total) {
            level = level.cappedAt(ConfidenceLevel.MEDIUM);
            reasons.add(PipelineConstants.REASON_COVERAGE_CAP);
        }

        level = applyReview(state, level, reasons);
        return new BriefingConfidence(level, median, List.copyOf(reasons));
    }

    private static double medianKeyFactScore(BriefingState state, List<String> reasons) {
        var scoresByGroup = state.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, GroupConfidence::getScore));
        var scores = state.getDraft().map(BriefingDraft::getKeyFacts).orElse(List.of()).stream()
                .map(fact -> fact.getGroupIds().stream().map(scoresByGroup::get).filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue).max().orElse(PipelineConstants.NO_SCORE))
                .sorted()
                .toList();
        if (scores.isEmpty()) {
            reasons.add(PipelineConstants.REASON_NO_KEY_FACTS);
            return PipelineConstants.NO_SCORE;
        }
        var count = scores.size();
        var middle = count / PipelineConstants.MEDIAN_HALVES;
        var isOdd = count % PipelineConstants.MEDIAN_HALVES != 0;
        var median = isOdd
                ? scores.get(middle)
                : (scores.get(middle - 1) + scores.get(middle)) / PipelineConstants.MEDIAN_HALVES;
        reasons.add(String.format(Locale.ROOT, PipelineConstants.REASON_KEY_FACTS, count, median));
        return median;
    }

    private static ConfidenceLevel applyReview(BriefingState state, ConfidenceLevel level, List<String> reasons) {
        if (state.isCritiqueFailed()) {
            reasons.add(PipelineConstants.REASON_UNVERIFIED);
            return ConfidenceLevel.LOW;
        }
        var critique = state.getLatestCritique();
        if (critique.map(Critique::isApproved).orElse(true)) {
            reasons.add(PipelineConstants.REASON_APPROVED);
            return level;
        }
        return applyOpenFindings(critique.orElseThrow(), level, reasons);
    }

    private static ConfidenceLevel applyOpenFindings(Critique critique, ConfidenceLevel level, List<String> reasons) {
        var findings = critique.getFindings();
        var majors = findings.stream().filter(CriticFinding::isMajor).count();
        reasons.add(PipelineConstants.REASON_OPEN_FINDINGS.formatted(findings.size(), majors));
        if (!critique.hasMajorFinding()) {
            return level;
        }
        reasons.add(PipelineConstants.REASON_MAJOR_CAP);
        return ConfidenceLevel.LOW;
    }
}
