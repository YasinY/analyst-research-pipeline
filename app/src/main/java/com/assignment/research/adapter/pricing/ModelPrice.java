package com.assignment.research.adapter.pricing;

import lombok.NonNull;
import lombok.Value;

@Value
public class ModelPrice {

    public static final ModelPrice FREE = new ModelPrice(PricingConstants.CATCH_ALL_PREFIX, PricingConstants.NO_PRICE,
            PricingConstants.NO_PRICE, PricingConstants.NO_PRICE);

    @NonNull
    private final String modelPrefix;
    private final double inputPerMillion;
    private final double cachedInputPerMillion;
    private final double outputPerMillion;
}
