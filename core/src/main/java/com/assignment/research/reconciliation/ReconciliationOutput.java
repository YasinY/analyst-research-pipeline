package com.assignment.research.reconciliation;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ReconciliationOutput {

    @NonNull
    private final List<ClaimGroupOutput> groups;
    @NonNull
    private final List<ConflictOutput> conflicts;

    public static ReconciliationOutput empty() {
        return new ReconciliationOutput(List.of(), List.of());
    }
}
