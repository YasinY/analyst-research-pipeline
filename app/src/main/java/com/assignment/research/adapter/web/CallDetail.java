package com.assignment.research.adapter.web;

import com.assignment.research.trace.TraceEntry;
import lombok.NonNull;
import lombok.Value;

@Value
public class CallDetail {

    private final int sequence;
    @NonNull
    private final String label;
    @NonNull
    private final String systemPrompt;
    @NonNull
    private final String userPrompt;
    @NonNull
    private final String rawResponse;

    public static CallDetail from(TraceEntry entry) {
        return new CallDetail(entry.getSequence(), entry.getLabel(), entry.getSystemPrompt(),
                entry.getUserPrompt(), entry.getRawResponse());
    }
}
