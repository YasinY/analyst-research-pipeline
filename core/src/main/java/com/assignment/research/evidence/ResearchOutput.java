package com.assignment.research.evidence;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ResearchOutput {

    @NonNull private final List<ExtractedClaim> claims;
}
