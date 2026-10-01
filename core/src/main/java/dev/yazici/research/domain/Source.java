package dev.yazici.research.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Source(
        String id,
        String title,
        String publisher,
        SourceType type,
        LocalDate publishedAt,
        String citesSourceId,
        List<String> keywords,
        String excerpt) {

    public Source {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(publisher, "publisher");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(publishedAt, "publishedAt");
        Objects.requireNonNull(excerpt, "excerpt");
        keywords = List.copyOf(Objects.requireNonNull(keywords, "keywords"));
    }

    public SourceTier tier() {
        return type.tier();
    }

    public Optional<String> citedSource() {
        return Optional.ofNullable(citesSourceId);
    }

    public boolean isDerivative() {
        return citesSourceId != null;
    }
}
