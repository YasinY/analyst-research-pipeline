package com.assignment.research.critique;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class CriticFinding {

    @NonNull
    private final FindingType type;
    @NonNull
    private final FindingSeverity severity;
    @NonNull
    private final String quotedText;
    @NonNull
    private final String detail;
    @NonNull
    private final List<String> groupIds;
    @NonNull
    private final List<String> suggestedKeywords;

    public boolean requiresResearch() {
        return type.getConsequence() == FindingConsequence.RESEARCH;
    }

    public boolean isMajor() {
        return severity == FindingSeverity.MAJOR;
    }
}
