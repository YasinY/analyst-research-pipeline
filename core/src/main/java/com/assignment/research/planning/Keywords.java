package com.assignment.research.planning;

import java.util.List;
import java.util.Locale;

public final class Keywords {

    private Keywords() {
    }

    public static List<String> normalize(List<String> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.strip().toLowerCase(Locale.ROOT))
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .toList();
    }

    public static List<String> fromText(String text) {
        return PlanningConstants.WORD_SEPARATOR.splitAsStream(text.toLowerCase(Locale.ROOT))
                .filter(word -> word.length() >= PlanningConstants.MIN_FALLBACK_KEYWORD_LENGTH)
                .distinct()
                .toList();
    }

    public static List<String> normalizeOrDerive(List<String> keywords, String fallbackText) {
        var normalized = normalize(keywords);
        if (normalized.isEmpty()) {
            return fromText(fallbackText);
        }
        return normalized;
    }
}
