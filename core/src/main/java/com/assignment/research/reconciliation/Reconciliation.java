package com.assignment.research.reconciliation;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class Reconciliation {

    @NonNull
    private final List<EvidenceGroup> groups;

    public static Reconciliation empty() {
        return new Reconciliation(List.of());
    }
}
