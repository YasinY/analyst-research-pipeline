package com.assignment.research.evidence;

import java.util.List;
import java.util.stream.Collectors;

public final class SourcePromptFormatter {

    private static final String SOURCE_BLOCK = """
            [sourceId: %s]
            Title: %s
            Publisher: %s (%s)
            Published: %s
            Excerpt: %s
            """;
    private static final String BLOCK_SEPARATOR = "\n";

    private SourcePromptFormatter() {
    }

    public static String format(List<Source> sources) {
        return sources.stream().map(SourcePromptFormatter::formatOne).collect(Collectors.joining(BLOCK_SEPARATOR));
    }

    private static String formatOne(Source source) {
        return SOURCE_BLOCK.formatted(
                source.getId(),
                source.getTitle(),
                source.getPublisher(),
                source.getType(),
                source.getPublishedAt(),
                source.getExcerpt());
    }
}
