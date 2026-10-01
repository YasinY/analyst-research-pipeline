package com.assignment.research.application.port;

import com.assignment.research.domain.Source;
import java.util.Objects;

public record SearchHit(Source source, int score) {

    public SearchHit {
        Objects.requireNonNull(source, "source");
    }
}
