package com.assignment.research.adapter.pricing;

import lombok.Value;

@Value
public class CallCost {

    public static final CallCost ZERO = new CallCost(PricingConstants.NO_PRICE, PricingConstants.NO_PRICE,
            PricingConstants.NO_PRICE);

    private final double freshInputCost;
    private final double cachedInputCost;
    private final double outputCost;

    public CallCost plus(CallCost other) {
        return new CallCost(freshInputCost + other.freshInputCost, cachedInputCost + other.cachedInputCost,
                outputCost + other.outputCost);
    }

    public double getTotal() {
        return freshInputCost + cachedInputCost + outputCost;
    }
}
