package com.assignment.research.application.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.application.FakeLlmPort;
import com.assignment.research.application.port.LlmException;
import com.assignment.research.application.port.LlmRequest;
import com.assignment.research.application.port.MalformedLlmResponseException;
import com.assignment.research.domain.LlmCallStatus;
import com.assignment.research.domain.LlmUsage;
import com.assignment.research.domain.TraceEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TracingLlmPortTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
    private static final LlmRequest REQUEST = new LlmRequest("planner", "system", "user", 512);
    private static final String RAW_JSON = "{\"raw\":true}";

    private final InMemoryTraceSink sink = new InMemoryTraceSink();

    @Test
    void recordsSuccessfulCallWithUsageAndPrompts() {
        var delegate = FakeLlmPort.returning("parsed", RAW_JSON, new LlmUsage(100, 20));
        var tracing = new TracingLlmPort(delegate, sink, FIXED_CLOCK);

        var result = tracing.complete(REQUEST, String.class);

        assertThat(result.value()).isEqualTo("parsed");
        var entry = sink.entries().getFirst();
        assertThat(entry.sequence()).isEqualTo(1);
        assertThat(entry.label()).isEqualTo("planner");
        assertThat(entry.systemPrompt()).isEqualTo("system");
        assertThat(entry.rawResponse()).isEqualTo(RAW_JSON);
        assertThat(entry.usage()).isEqualTo(new LlmUsage(100, 20));
        assertThat(entry.status()).isEqualTo(LlmCallStatus.OK);
        assertThat(sink.totalUsage().totalTokens()).isEqualTo(120);
    }

    @Test
    void recordsMalformedResponseWithRawTextAndRethrows() {
        var delegate = FakeLlmPort.failingWith(new MalformedLlmResponseException("not json", "garbage output"));
        var tracing = new TracingLlmPort(delegate, sink, FIXED_CLOCK);

        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class))
                .isInstanceOf(MalformedLlmResponseException.class);

        var entry = sink.entries().getFirst();
        assertThat(entry.status()).isEqualTo(LlmCallStatus.FAILED);
        assertThat(entry.rawResponse()).isEqualTo("garbage output");
        assertThat(entry.failure()).contains("not json");
    }

    @Test
    void sequenceIncrementsAcrossCalls() {
        var delegate = FakeLlmPort.failingWith(new LlmException("provider down"));
        var tracing = new TracingLlmPort(delegate, sink, FIXED_CLOCK);

        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class)).isInstanceOf(LlmException.class);
        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class)).isInstanceOf(LlmException.class);

        assertThat(sink.entries()).extracting(TraceEntry::sequence).containsExactly(1, 2);
    }
}
