package com.assignment.research.evidence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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
}
