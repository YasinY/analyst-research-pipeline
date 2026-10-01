package com.assignment.research.evidence;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FakeSourceSearchPort implements SourceSearchPort {

    private static final int FIXED_SCORE = 1;

    private final List<Source> sources;

    public static FakeSourceSearchPort returning(Source... sources) {
        return new FakeSourceSearchPort(List.of(sources));
    }

    public static FakeSourceSearchPort empty() {
        return new FakeSourceSearchPort(List.of());
    }

    @Override
    public List<SearchHit> search(List<String> keywords, int maxResults) {
        return sources.stream().limit(maxResults).map(source -> new SearchHit(source, FIXED_SCORE)).toList();
    }
}
