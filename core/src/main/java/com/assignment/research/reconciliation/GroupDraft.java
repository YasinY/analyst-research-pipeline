package com.assignment.research.reconciliation;

import com.assignment.research.evidence.SourceTier;
import java.time.LocalDate;
import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
class GroupDraft {

    @NonNull
    private final String modelGroupId;
    @NonNull
    private final String assertion;
    @NonNull
    private final List<String> claimIds;
    @NonNull
    private final List<String> independentSourceIds;
    @NonNull
    private final SourceTier bestTier;
    @NonNull
    private final LocalDate newestDate;
}
