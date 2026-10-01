package com.assignment.research.reconciliation;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class ConflictOutput {

    @NonNull
    private final List<String> groupIds;
    @NonNull
    private final String description;
}
