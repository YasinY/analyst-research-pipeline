package com.assignment.research.domain;

import java.util.List;
import java.util.Objects;

public record SubQuestion(String id, String question, List<String> searchKeywords) {

    public SubQuestion {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(question, "question");
        searchKeywords = List.copyOf(Objects.requireNonNull(searchKeywords, "searchKeywords"));
    }
}
