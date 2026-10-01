package com.assignment.research.evidence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

@Value
public class Source {

    @NonNull
    private final String id;
    @NonNull
    private final String title;
    @NonNull
    private final String publisher;
    @NonNull
    private final SourceType type;
    @NonNull
    private final LocalDate publishedAt;
    private final String citesSourceId;
    @NonNull
    private final List<String> keywords;
    @NonNull
    private final String excerpt;

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
