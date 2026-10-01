package com.assignment.research.domain;

public enum SourceType {
    REGULATOR(SourceTier.A),
    OFFICIAL_STATISTICS(SourceTier.A),
    INDUSTRY_BODY(SourceTier.A),
    INDUSTRY_REPORT(SourceTier.B),
    BROKER_NOTE(SourceTier.B),
    TRADE_PRESS(SourceTier.B),
    NEWS(SourceTier.B),
    BLOG(SourceTier.C),
    FORUM(SourceTier.C),
    PROMOTIONAL(SourceTier.C);

    private final SourceTier tier;

    SourceType(SourceTier tier) {
        this.tier = tier;
    }

    public SourceTier tier() {
        return tier;
    }
}
