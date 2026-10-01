package com.assignment.research.adapter.search;

import com.assignment.research.evidence.SearchHit;
import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceSearchPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class JsonCorpusSearchAdapter implements SourceSearchPort {

    private static final String FIELD_SEPARATOR = " ";
    private static final int NO_MATCH = 0;

    private final List<Source> sources;

    public JsonCorpusSearchAdapter(List<Source> sources) {
        this.sources = List.copyOf(sources);
    }

    public static JsonCorpusSearchAdapter load(Path corpusFile, ObjectMapper mapper) {
        try {
            List<CorpusDocument> documents = mapper.readValue(corpusFile.toFile(), new TypeReference<>() {
            });
            return new JsonCorpusSearchAdapter(documents.stream().map(CorpusDocument::toSource).toList());
        } catch (IOException failure) {
            throw new UncheckedIOException("cannot read corpus " + corpusFile.toAbsolutePath(), failure);
        }
    }

    @Override
    public List<SearchHit> search(List<String> keywords, int maxResults) {
        var normalized = keywords.stream().map(keyword -> keyword.toLowerCase(Locale.ROOT)).toList();
        return sources.stream()
                .map(source -> new SearchHit(source, score(source, normalized)))
                .filter(hit -> hit.getScore() > NO_MATCH)
                .sorted(Comparator.comparingInt(SearchHit::getScore).reversed()
                        .thenComparing(hit -> hit.getSource().getPublishedAt(), Comparator.reverseOrder()))
                .limit(maxResults)
                .toList();
    }

    public int size() {
        return sources.size();
    }

    private static int score(Source source, List<String> keywords) {
        var haystack = String.join(FIELD_SEPARATOR, source.getTitle(), String.join(FIELD_SEPARATOR,
                source.getKeywords()), source.getExcerpt()).toLowerCase(Locale.ROOT);
        return (int) keywords.stream().filter(haystack::contains).count();
    }
}
