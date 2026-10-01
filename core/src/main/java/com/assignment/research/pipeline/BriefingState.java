package com.assignment.research.pipeline;

import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.Critique;
import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.ResearchResult;
import com.assignment.research.evidence.Source;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.EvidenceGroup;
import com.assignment.research.synthesis.BriefingDraft;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class BriefingState {

    @NonNull
    private final AnalystQuery query;
    private final String interpretation;
    @NonNull
    private final List<SubQuestion> subQuestions;
    @NonNull
    private final List<String> pendingResearchIds;
    @NonNull
    private final Set<String> exhaustedSubQuestionIds;
    @NonNull
    private final List<Source> sources;
    @NonNull
    private final List<Claim> claims;
    @NonNull
    private final List<ResearchResult> researchResults;
    @NonNull
    private final List<EvidenceGroup> groups;
    @NonNull
    private final List<GroupConfidence> confidences;
    private final BriefingDraft draft;
    private final boolean draftReviewed;
    private final boolean draftStale;
    @NonNull
    private final List<Critique> critiques;
    private final boolean critiqueFailed;
    private final int round;
    private final int rewritesInRound;
    @NonNull
    private final List<AgentFailure> failures;
    private final StopDecision stopDecision;

    public static BriefingState initial(AnalystQuery query) {
        return BriefingState.builder()
                .query(query)
                .subQuestions(List.of())
                .pendingResearchIds(List.of())
                .exhaustedSubQuestionIds(Set.of())
                .sources(List.of())
                .claims(List.of())
                .researchResults(List.of())
                .groups(List.of())
                .confidences(List.of())
                .critiques(List.of())
                .failures(List.of())
                .round(PipelineConstants.FIRST_ROUND)
                .build();
    }

    public Optional<String> getInterpretation() {
        return Optional.ofNullable(interpretation);
    }

    public Optional<BriefingDraft> getDraft() {
        return Optional.ofNullable(draft);
    }

    public Optional<Critique> getLatestCritique() {
        return critiques.isEmpty() ? Optional.empty() : Optional.of(critiques.getLast());
    }

    public Optional<StopDecision> getStopDecision() {
        return Optional.ofNullable(stopDecision);
    }

    public boolean hasDraft() {
        return draft != null;
    }

    public BriefingState withSubQuestionsAppended(List<SubQuestion> added) {
        return toBuilder().subQuestions(concat(subQuestions, added)).build();
    }

    public BriefingState withResearchAppended(ResearchResult result) {
        return toBuilder()
                .sources(concatDistinctSources(result.getConsultedSources()))
                .claims(concat(claims, result.getClaims()))
                .researchResults(concat(researchResults, List.of(result)))
                .build();
    }

    public BriefingState withEvidence(List<EvidenceGroup> allGroups, List<GroupConfidence> allConfidences) {
        return toBuilder().groups(allGroups).confidences(allConfidences).build();
    }

    public BriefingState withFailure(AgentFailure failure) {
        return toBuilder().failures(concat(failures, List.of(failure))).build();
    }

    public BriefingState withCritiqueAppended(Critique critique) {
        return toBuilder().critiques(concat(critiques, List.of(critique))).draftReviewed(true).build();
    }

    public BriefingState withExhausted(Set<String> ids) {
        return toBuilder()
                .exhaustedSubQuestionIds(Stream.concat(exhaustedSubQuestionIds.stream(), ids.stream())
                        .collect(Collectors.toUnmodifiableSet()))
                .build();
    }

    private List<Source> concatDistinctSources(List<Source> added) {
        var knownIds = sources.stream().map(Source::getId).collect(Collectors.toSet());
        var fresh = added.stream().filter(source -> !knownIds.contains(source.getId())).toList();
        return concat(sources, fresh);
    }

    private static <T> List<T> concat(List<T> existing, List<T> added) {
        return Stream.concat(existing.stream(), added.stream()).toList();
    }
}
