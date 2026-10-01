package com.assignment.research.evidence;

import java.time.LocalDate;
import java.util.List;

public final class Sources {

    public static final LocalDate RECENT = LocalDate.of(2026, 3, 1);
    public static final LocalDate STALE = LocalDate.of(2019, 6, 1);

    private static final List<String> KEYWORDS = List.of("fleet", "growth");

    private Sources() {
    }

    public static Source tierA(String id) {
        return tierA(id, RECENT);
    }

    public static Source tierA(String id, LocalDate publishedAt) {
        return new Source(id, "Report " + id, "Fictional Statistics Office", SourceType.OFFICIAL_STATISTICS,
                publishedAt, null, KEYWORDS, "Excerpt of " + id);
    }

    public static Source tierB(String id, LocalDate publishedAt) {
        return new Source(id, "Note " + id, "Fictional Brokerage", SourceType.BROKER_NOTE, publishedAt, null,
                KEYWORDS, "Excerpt of " + id);
    }

    public static Source tierC(String id) {
        return new Source(id, "Blog " + id, "Anonymous Blog", SourceType.BLOG, RECENT, null, KEYWORDS,
                "Excerpt of " + id);
    }

    public static Source derivativeOf(String id, String citedSourceId) {
        return new Source(id, "Article " + id, "Fictional Trade Press", SourceType.TRADE_PRESS, RECENT,
                citedSourceId, KEYWORDS, "Excerpt of " + id);
    }
}
