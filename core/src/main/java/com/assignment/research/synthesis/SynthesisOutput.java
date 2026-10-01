package com.assignment.research.synthesis;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class SynthesisOutput {

    @NonNull
    private final List<GroundedStatement> summary;
    @NonNull
    private final List<GroundedStatement> keyFacts;
    @NonNull
    private final List<GroundedStatement> uncertainties;
    @NonNull
    private final List<String> followUpQuestions;
}
