package com.assignment.research.evidence;

import lombok.NonNull;
import lombok.Value;

@Value
public class Claim {

    @NonNull String id;
    @NonNull String subQuestionId;
    @NonNull String statement;
    @NonNull String sourceId;
}
