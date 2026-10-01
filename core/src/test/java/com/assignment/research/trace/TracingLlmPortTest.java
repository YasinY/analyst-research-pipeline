package com.assignment.research.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.llm.FakeLLMPort;
import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMException;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.llm.MalformedLLMResponseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TracingLLMPortTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
    private static final LLMRequest REQUEST = new LLMRequest("planner", "system", "user", 512);
    private static final String RAW_JSON = "{\"raw\":true}";

    private final InMemoryTraceSink sink = new InMemoryTraceSink();

    @Test
    void recordsSuccessfulCallWithUsageAndPrompts() {
        var delegate = FakeLLMPort.returning("parsed", RAW_JSON, new LLMUsage(100, 20));
        var tracing = new TracingLLMPort(delegate, sink, FIXED_CLOCK);

        var result = tracing.complete(REQUEST, String.class);

        assertThat(result.getValue()).isEqualTo("parsed");
        var entry = sink.getEntries().getFirst();
        assertThat(entry.getSequence()).isEqualTo(1);
        assertThat(entry.getLabel()).isEqualTo("planner");
        assertThat(entry.getSystemPrompt()).isEqualTo("system");
        assertThat(entry.getRawResponse()).isEqualTo(RAW_JSON);
        assertThat(entry.getUsage()).isEqualTo(new LLMUsage(100, 20));
        assertThat(entry.getStatus()).isEqualTo(LLMCallStatus.OK);
        assertThat(sink.getTotalUsage().getTotalTokens()).isEqualTo(120);
    }

    @Test
    void recordsMalformedResponseWithRawTextAndRethrows() {
        var delegate = FakeLLMPort.failingWith(new MalformedLLMResponseException("not json", "garbage output"));
        var tracing = new TracingLLMPort(delegate, sink, FIXED_CLOCK);

        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class))
                .isInstanceOf(MalformedLLMResponseException.class);

        var entry = sink.getEntries().getFirst();
        assertThat(entry.getStatus()).isEqualTo(LLMCallStatus.FAILED);
        assertThat(entry.getRawResponse()).isEqualTo("garbage output");
        assertThat(entry.getFailure()).contains("not json");
    }

    @Test
    void sequenceIncrementsAcrossCalls() {
        var delegate = FakeLLMPort.failingWith(new LLMException("provider down"));
        var tracing = new TracingLLMPort(delegate, sink, FIXED_CLOCK);

        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class)).isInstanceOf(LLMException.class);
        assertThatThrownBy(() -> tracing.complete(REQUEST, String.class)).isInstanceOf(LLMException.class);

        assertThat(sink.getEntries()).extracting(TraceEntry::getSequence).containsExactly(1, 2);
    }
}
