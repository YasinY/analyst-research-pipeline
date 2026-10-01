package com.assignment.research.evidence;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FakeSourceSearchPort implements SourceSearchPort {

    private static final int FIXED_SCORE = 1;

    private final List<Source> sources;
    private final Set<String> answeredKeywords;

    public static FakeSourceSearchPort returning(Source... sources) {
        return new FakeSourceSearchPort(List.of(sources), null);
    }

    public static FakeSourceSearchPort returningOnlyFor(Set<String> keywords, Source... sources) {
        return new FakeSourceSearchPort(List.of(sources), keywords);
    }

    public static FakeSourceSearchPort empty() {
        return new FakeSourceSearchPort(List.of(), null);
    }

    @Override
    public List<SearchHit> search(List<String> keywords, int maxResults) {
        if (answeredKeywords != null && keywords.stream().noneMatch(answeredKeywords::contains)) {
            return List.of();
        }
        return sources.stream().limit(maxResults).map(source -> new SearchHit(source, FIXED_SCORE)).toList();
    }
}
