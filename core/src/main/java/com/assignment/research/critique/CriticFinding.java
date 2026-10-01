package com.assignment.research.critique;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
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
    private final String suggestedQuestion;

    public CriticFinding(FindingType type, FindingSeverity severity, String quotedText, String detail,
            List<String> groupIds, List<String> suggestedKeywords) {
        this(type, severity, quotedText, detail, groupIds, suggestedKeywords, null);
    }

    public Optional<String> getSuggestedQuestion() {
        return Optional.ofNullable(suggestedQuestion);
    }

    public boolean requiresResearch() {
        return type.getConsequence() == FindingConsequence.RESEARCH;
    }

    public boolean isMajor() {
        return severity == FindingSeverity.MAJOR;
    }
}
