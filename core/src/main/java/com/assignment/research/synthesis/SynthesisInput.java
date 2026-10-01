package com.assignment.research.synthesis;

import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.Value;

@Value
public class SynthesisInput {

    @NonNull
    private final AnalystQuery query;
    @NonNull
    private final String interpretation;
    @NonNull
    private final List<EvidenceGroup> groups;
    @NonNull
    private final List<GroupConfidence> confidences;
    @NonNull
    private final List<SubQuestion> gaps;
    private final BriefingDraft previousDraft;
    @NonNull
    private final List<CriticFinding> findings;

    public static SynthesisInput firstDraft(AnalystQuery query, String interpretation, List<EvidenceGroup> groups,
            List<GroupConfidence> confidences, List<SubQuestion> gaps) {
        return new SynthesisInput(query, interpretation, groups, confidences, gaps, null, List.of());
    }

    public SynthesisInput revisedWith(BriefingDraft draft, List<CriticFinding> criticFindings) {
        return new SynthesisInput(query, interpretation, groups, confidences, gaps, draft, criticFindings);
    }

    public Optional<BriefingDraft> getPreviousDraft() {
        return Optional.ofNullable(previousDraft);
    }

    public boolean isRevision() {
        return previousDraft != null && !findings.isEmpty();
    }
}
