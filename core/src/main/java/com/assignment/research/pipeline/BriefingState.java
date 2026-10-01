package com.assignment.research.pipeline;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Source;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import java.util.List;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.Value;

@Value
public class BriefingState {

    private static final int FIRST_ROUND = 1;

    @NonNull private final AnalystQuery query;
    @NonNull private final List<SubQuestion> subQuestions;
    @NonNull private final List<Source> sources;
    @NonNull private final List<Claim> claims;
    private final int round;

    public static BriefingState initial(AnalystQuery query) {
        return new BriefingState(query, List.of(), List.of(), List.of(), FIRST_ROUND);
    }

    public BriefingState withSubQuestions(List<SubQuestion> added) {
        return new BriefingState(query, concat(subQuestions, added), sources, claims, round);
    }

    public BriefingState withSources(List<Source> added) {
        return new BriefingState(query, subQuestions, concat(sources, added), claims, round);
    }

    public BriefingState withClaims(List<Claim> added) {
        return new BriefingState(query, subQuestions, sources, concat(claims, added), round);
    }

    public BriefingState nextRound() {
        return new BriefingState(query, subQuestions, sources, claims, round + 1);
    }

    private static <T> List<T> concat(List<T> existing, List<T> added) {
        return Stream.concat(existing.stream(), added.stream()).toList();
    }
}
