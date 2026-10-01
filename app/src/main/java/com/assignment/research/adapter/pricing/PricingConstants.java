package com.assignment.research.adapter.pricing;

public final class PricingConstants {

    public static final String PRICING_FILE = "pricing.json";
    public static final double TOKENS_PER_MILLION = 1_000_000.0;
    public static final double NO_PRICE = 0.0;
    public static final String CATCH_ALL_PREFIX = "";
    public static final String READ_FAILURE = "cannot read pricing table %s";

    private PricingConstants() {
    }
}
