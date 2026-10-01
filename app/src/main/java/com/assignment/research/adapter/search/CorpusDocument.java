package com.assignment.research.adapter.search;

import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceType;
import java.time.LocalDate;
import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class CorpusDocument {

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
    private final String scenario;

    public Source toSource() {
        return new Source(id, title, publisher, type, publishedAt, citesSourceId, keywords, excerpt);
    }
}
