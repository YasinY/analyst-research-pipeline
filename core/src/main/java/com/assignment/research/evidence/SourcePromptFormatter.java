package com.assignment.research.evidence;

import java.util.List;
import java.util.stream.Collectors;

public final class SourcePromptFormatter {

    private SourcePromptFormatter() {
    }

    public static String format(List<Source> sources) {
        return sources.stream()
                .map(SourcePromptFormatter::formatOne)
                .collect(Collectors.joining(EvidenceConstants.BLOCK_SEPARATOR));
    }

    private static String formatOne(Source source) {
        return EvidenceConstants.SOURCE_BLOCK.formatted(
                source.getId(),
                source.getTitle(),
                source.getPublisher(),
                source.getType(),
                source.getPublishedAt(),
                source.getExcerpt());
    }
}
