package com.assignment.research.adapter.pricing;

import com.assignment.research.trace.TraceEntry;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CostEstimator {

    private final PricingTable pricingTable;

    public static CostEstimator free() {
        return new CostEstimator(PricingTable.empty());
    }

    public boolean isFree() {
        return pricingTable.isEmpty();
    }

    public CallCost estimate(TraceEntry entry) {
        var price = pricingTable.priceFor(entry.getModel());
        var usage = entry.getUsage();
        return new CallCost(
                perMillion(usage.getFreshInputTokens(), price.getInputPerMillion()),
                perMillion(usage.getCachedInputTokens(), price.getCachedInputPerMillion()),
                perMillion(usage.getOutputTokens(), price.getOutputPerMillion()));
    }

    public CallCost estimateRun(List<TraceEntry> entries) {
        return entries.stream().map(this::estimate).reduce(CallCost.ZERO, CallCost::plus);
    }

    private static double perMillion(int tokens, double pricePerMillion) {
        return tokens * pricePerMillion / PricingConstants.TOKENS_PER_MILLION;
    }
}
