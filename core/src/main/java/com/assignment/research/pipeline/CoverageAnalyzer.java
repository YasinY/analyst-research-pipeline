package com.assignment.research.pipeline;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CoverageAnalyzer {

    private static final ConfidenceLevel ADEQUATE_EVIDENCE = ConfidenceLevel.MEDIUM;

    private CoverageAnalyzer() {
    }

    public static Set<String> coveredSubQuestionIds(BriefingState state) {
        var confidenceByGroup = state.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, Function.identity()));
        return state.getGroups().stream()
                .filter(group -> isAdequate(confidenceByGroup.get(group.getId())))
                .map(EvidenceGroup::getSubQuestionId)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static List<SubQuestion> uncovered(BriefingState state) {
        var covered = coveredSubQuestionIds(state);
        return state.getSubQuestions().stream().filter(question -> !covered.contains(question.getId())).toList();
    }

    public static List<SubQuestion> openForResearch(BriefingState state) {
        var exhausted = state.getExhaustedSubQuestionIds();
        return uncovered(state).stream().filter(question -> !exhausted.contains(question.getId())).toList();
    }

    private static boolean isAdequate(GroupConfidence confidence) {
        return confidence != null && confidence.getLevel().isAtLeast(ADEQUATE_EVIDENCE);
    }
}
