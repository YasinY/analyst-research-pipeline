package com.assignment.research.adapter.search;

import com.assignment.research.evidence.Source;
import com.assignment.research.planning.Keywords;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.NonNull;
import lombok.Value;

@Value
class IndexedSource {

    @NonNull
    private final Source source;
    @NonNull
    private final String wordSequence;
    @NonNull
    private final Set<String> significantWords;

    static IndexedSource of(Source source) {
        var text = String.join(SearchConstants.FIELD_SEPARATOR, source.getTitle(),
                String.join(SearchConstants.FIELD_SEPARATOR, source.getKeywords()), source.getExcerpt());
        return new IndexedSource(source, wordSequence(words(text)), Set.copyOf(Keywords.fromText(text)));
    }

    boolean matches(String keyword) {
        var keywordWords = words(keyword);
        if (keywordWords.isEmpty()) {
            return false;
        }
        if (wordSequence.contains(wordSequence(keywordWords))) {
            return true;
        }
        var significantKeywordWords = Keywords.fromText(keyword);
        return !significantKeywordWords.isEmpty() && significantWords.containsAll(significantKeywordWords);
    }

    private static List<String> words(String text) {
        return SearchConstants.WORD_SEPARATOR.splitAsStream(text.toLowerCase(Locale.ROOT))
                .filter(word -> !word.isEmpty())
                .toList();
    }

    private static String wordSequence(List<String> words) {
        return SearchConstants.WORD_DELIMITER + String.join(SearchConstants.WORD_DELIMITER, words)
                + SearchConstants.WORD_DELIMITER;
    }
}
