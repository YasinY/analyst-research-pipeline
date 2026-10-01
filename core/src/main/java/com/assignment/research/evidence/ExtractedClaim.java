package com.assignment.research.evidence;

import lombok.NonNull;
import lombok.Value;

@Value
public class ExtractedClaim {

    @NonNull
    private final String statement;
    @NonNull
    private final String sourceId;
}
