package com.assignment.research.confidence;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class GroupConfidence {

    @NonNull
    private final String groupId;
    private final double score;
    @NonNull
    private final ConfidenceLevel level;
    @NonNull
    private final List<ConfidenceFactor> factors;
}
