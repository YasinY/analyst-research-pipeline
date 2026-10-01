package com.assignment.research.adapter.pricing;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class PricingFile {

    private final String note;
    @NonNull
    private final List<ModelPrice> models;
}
