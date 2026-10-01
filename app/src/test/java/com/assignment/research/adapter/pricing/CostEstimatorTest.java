package com.assignment.research.adapter.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.trace.TraceEntry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

class CostEstimatorTest {

    private static final Offset<Double> PRECISION = within(1e-9);
    private static final Instant STARTED_AT = Instant.parse("2026-10-01T12:00:00Z");
    private static final String PRICED_MODEL = "claude-sonnet-5-5";
    private static final String LOCAL_MODEL = "llama-local";
    private static final PricingTable TABLE = new PricingTable(List.of(
            new ModelPrice(PRICED_MODEL, 2.0, 0.2, 10.0),
            new ModelPrice("", 0.0, 0.0, 0.0)));

    private final CostEstimator estimator = new CostEstimator(TABLE);

    @Test
    void pricesFreshCachedAndOutputTokensSeparately() {
        var cost = estimator.estimate(entry(PRICED_MODEL, new LLMUsage(1_000_000, 100_000, 400_000)));

        assertThat(cost.getFreshInputCost()).isCloseTo(1.2, PRECISION);
        assertThat(cost.getCachedInputCost()).isCloseTo(0.08, PRECISION);
        assertThat(cost.getOutputCost()).isCloseTo(1.0, PRECISION);
        assertThat(cost.getTotal()).isCloseTo(2.28, PRECISION);
        assertThat(estimator.isFree()).isFalse();
    }

    @Test
    void runCostSumsEveryCallAndLocalModelsCostNothing() {
        var entries = List.of(
                entry(PRICED_MODEL, new LLMUsage(1_000_000, 0)),
                entry(PRICED_MODEL, new LLMUsage(0, 1_000_000)),
                entry(LOCAL_MODEL, new LLMUsage(5_000_000, 5_000_000)));

        var cost = estimator.estimateRun(entries);

        assertThat(cost.getTotal()).isCloseTo(12.0, PRECISION);
        assertThat(estimator.estimateRun(List.of())).isEqualTo(CallCost.ZERO);
    }

    @Test
    void freeEstimatorPricesEverythingAtZero() {
        var free = CostEstimator.free();

        assertThat(free.isFree()).isTrue();
        assertThat(free.estimate(entry(PRICED_MODEL, new LLMUsage(1_000, 1_000))).getTotal()).isZero();
    }

    private static TraceEntry entry(String model, LLMUsage usage) {
        return new TraceEntry(1, "planner", STARTED_AT, Duration.ofSeconds(1), model, "system", "user", "raw",
                usage, LLMCallStatus.OK, null);
    }
}
