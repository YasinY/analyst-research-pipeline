package com.assignment.research.adapter.pricing;

import com.assignment.research.adapter.output.JSONMapperFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class PricingTable {

    private static final PricingTable EMPTY = new PricingTable(List.of());

    private final List<ModelPrice> prices;

    public PricingTable(List<ModelPrice> prices) {
        this.prices = List.copyOf(prices);
    }

    public static PricingTable empty() {
        return EMPTY;
    }

    public static PricingTable load(Path file) {
        try {
            var pricing = JSONMapperFactory.create().readValue(file.toFile(), PricingFile.class);
            return new PricingTable(pricing.getModels());
        } catch (IOException failure) {
            throw new UncheckedIOException(PricingConstants.READ_FAILURE.formatted(file.toAbsolutePath()), failure);
        }
    }

    public ModelPrice priceFor(String model) {
        var normalized = model.toLowerCase(Locale.ROOT);
        return prices.stream()
                .filter(price -> normalized.startsWith(price.getModelPrefix().toLowerCase(Locale.ROOT)))
                .max(Comparator.comparingInt(price -> price.getModelPrefix().length()))
                .orElse(ModelPrice.FREE);
    }

    public boolean isEmpty() {
        return prices.isEmpty();
    }
}
