package com.assignment.research.pipeline;

import com.assignment.research.reconciliation.EvidenceGroup;
import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
class ReconciledResearch {

    @NonNull
    private final BriefingState state;
    @NonNull
    private final List<EvidenceGroup> groups;
}
