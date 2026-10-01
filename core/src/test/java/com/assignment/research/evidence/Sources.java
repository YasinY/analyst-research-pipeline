package com.assignment.research.evidence;

import java.time.LocalDate;
import java.util.List;

public final class Sources {

    private static final LocalDate RECENT = LocalDate.of(2026, 3, 1);

    private Sources() {
    }

    public static Source tierA(String id) {
        return new Source(id, "Report " + id, "Fictional Statistics Office", SourceType.OFFICIAL_STATISTICS, RECENT,
                null, List.of("fleet", "growth"), "Excerpt of " + id);
    }

    public static Source tierC(String id) {
        return new Source(id, "Blog " + id, "Anonymous Blog", SourceType.BLOG, RECENT,
                null, List.of("freight", "rates"), "Excerpt of " + id);
    }
}
