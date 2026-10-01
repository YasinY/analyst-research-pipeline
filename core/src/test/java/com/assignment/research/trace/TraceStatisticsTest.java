package com.assignment.research.trace;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class TraceStatisticsTest {

    private static final Instant STARTED_AT = Instant.parse("2026-10-01T12:00:00Z");
    private static final String MODEL = "model";

    @Test
    void groupsCallsByTheLabelPrefixInFirstAppearanceOrder() {
        var entries = List.of(
                entry(1, "planner", new LLMUsage(100, 10, 0), 1000),
                entry(2, "researcher/sq-1", new LLMUsage(200, 20, 50), 2000),
                entry(3, "researcher/sq-2", new LLMUsage(300, 30, 150), 3000));

        var statistics = TraceStatistics.byRole(entries);

        assertThat(statistics).containsExactly(
                new RoleStatistics("planner", 1, new LLMUsage(100, 10, 0), Duration.ofMillis(1000)),
                new RoleStatistics("researcher", 2, new LLMUsage(500, 50, 200), Duration.ofMillis(5000)));
    }

    @Test
    void emptyTraceHasNoRoles() {
        assertThat(TraceStatistics.byRole(List.of())).isEmpty();
    }

    @Test
    void roleIsTheWholeLabelWithoutASeparator() {
        assertThat(TraceStatistics.roleOf("critic")).isEqualTo("critic");
        assertThat(TraceStatistics.roleOf("synthesizer/round-2")).isEqualTo("synthesizer");
    }

    private static TraceEntry entry(int sequence, String label, LLMUsage usage, long millis) {
        return new TraceEntry(sequence, label, STARTED_AT, Duration.ofMillis(millis), MODEL, "system", "user",
                "raw", usage, LLMCallStatus.OK, null);
    }
}
