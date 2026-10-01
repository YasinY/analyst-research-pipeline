package com.assignment.research.evidence;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ResearchResult {

    @NonNull private final String subQuestionId;
    @NonNull private final List<Source> consultedSources;
    @NonNull private final List<Claim> claims;
    @NonNull private final List<String> rejectedSourceIds;

    public static ResearchResult withoutEvidence(String subQuestionId) {
        return new ResearchResult(subQuestionId, List.of(), List.of(), List.of());
    }

    public boolean hasClaims() {
        return !claims.isEmpty();
    }
}
