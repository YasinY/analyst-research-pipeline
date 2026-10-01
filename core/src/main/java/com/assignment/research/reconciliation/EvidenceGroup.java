package com.assignment.research.reconciliation;

import com.assignment.research.evidence.SourceTier;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.Value;

@Value
public class EvidenceGroup {

    @NonNull
    private final String id;
    @NonNull
    private final Set<String> subQuestionIds;
    @NonNull
    private final String assertion;
    @NonNull
    private final List<String> claimIds;
    @NonNull
    private final List<String> independentSourceIds;
    @NonNull
    private final SourceTier bestTier;
    @NonNull
    private final LocalDate newestSourceDate;
    @NonNull
    private final ConflictStatus conflictStatus;
    private final String conflictDescription;
    @NonNull
    private final List<String> conflictingGroupIds;

    public Optional<String> getConflict() {
        return Optional.ofNullable(conflictDescription);
    }

    public int getIndependentSourceCount() {
        return independentSourceIds.size();
    }
}
