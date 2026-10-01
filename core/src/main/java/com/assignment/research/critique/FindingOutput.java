package com.assignment.research.critique;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
public class FindingOutput {

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

    public FindingOutput(FindingType type, FindingSeverity severity, String quotedText, String detail,
            List<String> groupIds, List<String> suggestedKeywords) {
        this(type, severity, quotedText, detail, groupIds, suggestedKeywords, null);
    }
}
