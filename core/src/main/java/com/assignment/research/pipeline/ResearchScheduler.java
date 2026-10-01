package com.assignment.research.pipeline;

import com.assignment.research.critique.CriticFinding;
import com.assignment.research.critique.Critique;
import com.assignment.research.planning.SubQuestion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ResearchScheduler {

    private ResearchScheduler() {
    }

    public static BriefingState scheduleNextRound(BriefingState state) {
        var followUps = followUpQuestions(state);
        var withQuestions = state.withSubQuestionsAppended(followUps);
        var pendingIds = CoverageAnalyzer.openForResearch(withQuestions).stream().map(SubQuestion::getId).toList();
        if (pendingIds.isEmpty()) {
            return withQuestions.toBuilder()
                    .stopDecision(new StopDecision(StopReason.NO_NEW_EVIDENCE,
                            PipelineConstants.EXPLANATION_NO_NEW_EVIDENCE))
                    .build();
        }
        return withQuestions.toBuilder()
                .pendingResearchIds(pendingIds)
                .round(state.getRound() + 1)
                .rewritesInRound(PipelineConstants.NO_REWRITES)
                .draftStale(true)
                .build();
    }

    private static List<SubQuestion> followUpQuestions(BriefingState state) {
        var findings = state.getLatestCritique().map(Critique::getResearchFindings).orElse(List.of());
        var seenKeywordSets = new HashSet<Set<String>>();
        var questions = new ArrayList<SubQuestion>();
        var nextNumber = state.getSubQuestions().size() + 1;
        for (CriticFinding finding : findings) {
            var keywords = finding.getSuggestedKeywords();
            if (keywords.isEmpty() || !seenKeywordSets.add(Set.copyOf(keywords))) {
                continue;
            }
            var id = PipelineConstants.FOLLOW_UP_QUESTION_ID_FORMAT.formatted(nextNumber++);
            var fallback = PipelineConstants.FOLLOW_UP_QUESTION_FALLBACK.formatted(
                    String.join(PipelineConstants.KEYWORD_SEPARATOR, keywords));
            questions.add(new SubQuestion(id, finding.getSuggestedQuestion().orElse(fallback), keywords));
        }
        return questions;
    }
}
