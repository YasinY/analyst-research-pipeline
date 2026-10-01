package com.assignment.research.pipeline;

import com.assignment.research.critique.CriticFinding;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.trace.TraceEntry;
import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class BriefingResult {

    @NonNull
    private final BriefingDraft draft;
    @NonNull
    private final BriefingConfidence confidence;
    @NonNull
    private final List<CriticFinding> openFindings;
    @NonNull
    private final List<SubQuestion> gaps;
    @NonNull
    private final StopDecision stopDecision;
    @NonNull
    private final BriefingState finalState;
    @NonNull
    private final List<TraceEntry> trace;
    @NonNull
    private final LLMUsage usage;
}
