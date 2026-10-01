package com.assignment.research.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.llm.FakeLlmPort;
import com.assignment.research.llm.LlmCallStatus;
import com.assignment.research.llm.LlmException;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.llm.LlmUsage;
import com.assignment.research.llm.MalformedLlmResponseException;
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

        assertThat(result.getValue()).isEqualTo("parsed");
        var entry = sink.getEntries().getFirst();
        assertThat(entry.getSequence()).isEqualTo(1);
        assertThat(entry.getLabel()).isEqualTo("planner");
        assertThat(entry.getSystemPrompt()).isEqualTo("system");
        assertThat(entry.getRawResponse()).isEqualTo(RAW_JSON);
        assertThat(entry.getUsage()).isEqualTo(new LlmUsage(100, 20));
        assertThat(entry.getStatus()).isEqualTo(LlmCallStatus.OK);
        assertThat(sink.getTotalUsage().getTotalTokens()).isEqualTo(120);
    }

    @Test
    void recordsMalformedResponseWithRawTextAndRethrows() {
        var delegate = FakeLlmPort.failingWith(new MalformedLlmResponseException("not json", "garbage output"));
        var tracing = new TracingLlmPort(delegate, sink, FIXED_CLOCK);

        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class))
                .isInstanceOf(MalformedLlmResponseException.class);

        var entry = sink.getEntries().getFirst();
        assertThat(entry.getStatus()).isEqualTo(LlmCallStatus.FAILED);
        assertThat(entry.getRawResponse()).isEqualTo("garbage output");
        assertThat(entry.getFailure()).contains("not json");
    }

    @Test
    void sequenceIncrementsAcrossCalls() {
        var delegate = FakeLlmPort.failingWith(new LlmException("provider down"));
        var tracing = new TracingLlmPort(delegate, sink, FIXED_CLOCK);

        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class)).isInstanceOf(LlmException.class);
        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class)).isInstanceOf(LlmException.class);

        assertThat(sink.getEntries()).extracting(TraceEntry::getSequence).containsExactly(1, 2);
    }
}
