package com.assignment.research.critique;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class CritiqueOutput {

    @NonNull
    private final List<FindingOutput> findings;

    public static CritiqueOutput clean() {
        return new CritiqueOutput(List.of());
    }
}
