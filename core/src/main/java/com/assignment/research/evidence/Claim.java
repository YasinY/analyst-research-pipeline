package com.assignment.research.evidence;

import lombok.NonNull;
import lombok.Value;

@Value
public class Claim {

    @NonNull private final String id;
    @NonNull private final String subQuestionId;
    @NonNull private final String statement;
    @NonNull private final String sourceId;
}
