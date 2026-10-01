package com.assignment.research.adapter.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingTableTest {

    private static final Path DATA = Path.of(System.getProperty("basedir", "app")).resolveSibling("data");
    private static final Path PRICING_FILE = DATA.resolve(PricingConstants.PRICING_FILE);
    private static final Path MISSING_FILE = DATA.resolve("missing-pricing.json");

    private final PricingTable table = PricingTable.load(PRICING_FILE);

    @Test
    void shippedTableMatchesReportedModelNamesByTheLongestPrefix() {
        assertThat(table.priceFor("claude-sonnet-5-5-20260901").getModelPrefix()).isEqualTo("claude-sonnet-5-5");
        assertThat(table.priceFor("claude-haiku-4-5").getModelPrefix()).isEqualTo("claude-haiku-4-5");
        assertThat(table.priceFor("gpt-5.4-mini-2026-03-05").getModelPrefix()).isEqualTo("gpt-5.4-mini");
        assertThat(table.priceFor("GPT-5.4-2026-03-05").getModelPrefix()).isEqualTo("gpt-5.4");
        assertThat(table.isEmpty()).isFalse();
    }

    @Test
    void unknownModelsFallBackToTheFreeCatchAllEntry() {
        var price = table.priceFor("qwen3-local");

        assertThat(price.getModelPrefix()).isEmpty();
        assertThat(price.getInputPerMillion()).isZero();
        assertThat(price.getCachedInputPerMillion()).isZero();
        assertThat(price.getOutputPerMillion()).isZero();
    }

    @Test
    void tableWithoutAMatchingEntryReturnsTheFreePrice() {
        var onlyClaude = new PricingTable(List.of(new ModelPrice("claude-", 1.0, 0.1, 5.0)));

        assertThat(onlyClaude.priceFor("gpt-5.4")).isEqualTo(ModelPrice.FREE);
        assertThat(PricingTable.empty().priceFor("claude-sonnet-5-5")).isEqualTo(ModelPrice.FREE);
        assertThat(PricingTable.empty().isEmpty()).isTrue();
    }

    @Test
    void missingFileFailsNamingThePath() {
        assertThatThrownBy(() -> PricingTable.load(MISSING_FILE))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining(MISSING_FILE.toAbsolutePath().toString());
    }
}
