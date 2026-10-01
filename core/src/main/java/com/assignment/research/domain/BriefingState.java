package com.assignment.research.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public record BriefingState(
        AnalystQuery query,
        List<SubQuestion> subQuestions,
        List<Source> sources,
        List<Claim> claims,
        int round) {

    private static final int FIRST_ROUND = 1;

    public BriefingState {
        Objects.requireNonNull(query, "query");
        subQuestions = List.copyOf(Objects.requireNonNull(subQuestions, "subQuestions"));
        sources = List.copyOf(Objects.requireNonNull(sources, "sources"));
        claims = List.copyOf(Objects.requireNonNull(claims, "claims"));
    }

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
