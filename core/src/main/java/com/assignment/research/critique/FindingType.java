package com.assignment.research.critique;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FindingType {
    UNSUPPORTED(FindingConsequence.REWRITE),
    CONTRADICTS_EVIDENCE(FindingConsequence.REWRITE),
    OVERSTATED_CERTAINTY(FindingConsequence.REWRITE),
    SMOOTHED_CONFLICT(FindingConsequence.REWRITE),
    MISSING_EVIDENCE(FindingConsequence.RESEARCH),
    READABILITY(FindingConsequence.REWRITE);

    private final FindingConsequence consequence;
}
