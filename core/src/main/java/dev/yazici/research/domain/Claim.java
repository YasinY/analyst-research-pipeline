package dev.yazici.research.domain;

import java.util.Objects;

public record Claim(String id, String subQuestionId, String statement, String sourceId) {

    public Claim {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(subQuestionId, "subQuestionId");
        Objects.requireNonNull(statement, "statement");
        Objects.requireNonNull(sourceId, "sourceId");
    }
}
