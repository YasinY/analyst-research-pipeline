package com.assignment.research.critique;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class Critique {

    @NonNull
    private final List<CriticFinding> findings;

    public static Critique approved() {
        return new Critique(List.of());
    }

    public boolean isApproved() {
        return findings.isEmpty();
    }

    public boolean hasMajorFinding() {
        return findings.stream().anyMatch(CriticFinding::isMajor);
    }

    public List<CriticFinding> getRewriteFindings() {
        return findings.stream().filter(finding -> !finding.requiresResearch()).toList();
    }

    public List<CriticFinding> getResearchFindings() {
        return findings.stream().filter(CriticFinding::requiresResearch).toList();
    }
}
