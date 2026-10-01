package com.assignment.research.evidence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

@Value
public class Source {

    @NonNull String id;
    @NonNull String title;
    @NonNull String publisher;
    @NonNull SourceType type;
    @NonNull LocalDate publishedAt;
    String citesSourceId;
    @NonNull List<String> keywords;
    @NonNull String excerpt;

    public SourceTier getTier() {
        return type.getTier();
    }

    public Optional<String> getCitedSource() {
        return Optional.ofNullable(citesSourceId);
    }

    public boolean isDerivative() {
        return citesSourceId != null;
    }
}
