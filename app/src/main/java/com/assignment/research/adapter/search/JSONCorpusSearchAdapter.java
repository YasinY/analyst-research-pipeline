package com.assignment.research.adapter.search;

import com.assignment.research.evidence.SearchHit;
import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceSearchPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class JSONCorpusSearchAdapter implements SourceSearchPort {

    private final List<IndexedSource> sources;

    public JSONCorpusSearchAdapter(List<Source> sources) {
        this.sources = sources.stream().map(IndexedSource::of).toList();
    }

    public static JSONCorpusSearchAdapter load(Path corpusFile, ObjectMapper mapper) {
        try {
            var documents = mapper.readValue(corpusFile.toFile(), CorpusDocument[].class);
            return new JSONCorpusSearchAdapter(Arrays.stream(documents).map(CorpusDocument::toSource).toList());
        } catch (IOException failure) {
            throw new UncheckedIOException(
                    SearchConstants.CORPUS_READ_FAILURE.formatted(corpusFile.toAbsolutePath()), failure);
        }
    }

    @Override
    public List<SearchHit> search(List<String> keywords, int maxResults) {
        return sources.stream()
                .map(indexed -> new SearchHit(indexed.getSource(), score(indexed, keywords)))
                .filter(hit -> hit.getScore() > SearchConstants.NO_MATCH)
                .sorted(Comparator.comparingInt(SearchHit::getScore).reversed()
                        .thenComparing(hit -> hit.getSource().getPublishedAt(), Comparator.reverseOrder()))
                .limit(maxResults)
                .toList();
    }

    public int size() {
        return sources.size();
    }

    private static int score(IndexedSource source, List<String> keywords) {
        return (int) keywords.stream().filter(source::matches).count();
    }
}
