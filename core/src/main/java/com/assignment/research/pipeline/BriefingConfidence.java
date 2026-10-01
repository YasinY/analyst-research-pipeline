package com.assignment.research.pipeline;

import com.assignment.research.confidence.ConfidenceLevel;
import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class BriefingConfidence {

    @NonNull
    private final ConfidenceLevel level;
    private final double medianKeyFactScore;
    @NonNull
    private final List<String> reasons;
}
