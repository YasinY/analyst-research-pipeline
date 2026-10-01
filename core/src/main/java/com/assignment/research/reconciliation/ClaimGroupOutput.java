package com.assignment.research.reconciliation;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ClaimGroupOutput {

    @NonNull
    private final String id;
    @NonNull
    private final String assertion;
    @NonNull
    private final List<String> claimIds;
}
