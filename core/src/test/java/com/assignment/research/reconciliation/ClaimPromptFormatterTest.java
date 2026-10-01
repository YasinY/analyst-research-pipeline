package com.assignment.research.reconciliation;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Sources;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClaimPromptFormatterTest {

    private static final String KNOWN_SOURCE_ID = "src-a";
    private static final Claim KNOWN_CLAIM = new Claim("c1", "q1", "Fleet grew.", KNOWN_SOURCE_ID);
    private static final Claim ORPHAN_CLAIM = new Claim("c2", "q1", "Rates fell.", "src-missing");
    private static final String EXPECTED_KNOWN_LINE =
            "[c1] (q1; source src-a, Fictional Statistics Office, OFFICIAL_STATISTICS, 2026-03-01): Fleet grew.";
    private static final String EXPECTED_ORPHAN_LINE =
            "[c2] (q1; source src-missing, unknown, unknown, unknown): Rates fell.";

    @Test
    void formatsClaimsWithTheirSourceAndMarksMissingSourcesAsUnknown() {
        var formatted = ClaimPromptFormatter.formatClaims(List.of(KNOWN_CLAIM, ORPHAN_CLAIM),
                List.of(Sources.tierA(KNOWN_SOURCE_ID)));

        assertThat(formatted.lines().toList()).containsExactly(EXPECTED_KNOWN_LINE, EXPECTED_ORPHAN_LINE);
    }
}
