package com.assignment.research.critique;

import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.EvidenceGroup;
import com.assignment.research.synthesis.BriefingDraft;
import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class CritiqueInput {

    @NonNull
    private final AnalystQuery query;
    @NonNull
    private final BriefingDraft draft;
    @NonNull
    private final List<EvidenceGroup> groups;
    @NonNull
    private final List<GroupConfidence> confidences;
    @NonNull
    private final List<SubQuestion> gaps;
}
