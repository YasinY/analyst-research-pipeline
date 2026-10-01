package com.assignment.research.confidence;

import lombok.NonNull;
import lombok.Value;

@Value
public class ConfidenceFactor {

    @NonNull private final String label;
    private final double delta;
}
