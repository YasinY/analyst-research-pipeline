package com.assignment.research.application.trace;

import com.assignment.research.application.port.LlmException;
import com.assignment.research.application.port.LlmPort;
import com.assignment.research.application.port.LlmRequest;
import com.assignment.research.application.port.LlmResult;
import com.assignment.research.application.port.MalformedLlmResponseException;
import com.assignment.research.application.port.TraceSink;
import com.assignment.research.domain.LlmCallStatus;
import com.assignment.research.domain.LlmUsage;
import com.assignment.research.domain.TraceEntry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public final class TracingLlmPort implements LlmPort {

    private static final int FIRST_SEQUENCE = 1;
    private static final String UNKNOWN_MODEL = "unknown";
    private static final String NO_RESPONSE = "";

    private final LlmPort delegate;
    private final TraceSink sink;
    private final Clock clock;
    private final AtomicInteger sequence = new AtomicInteger(FIRST_SEQUENCE);

    public TracingLlmPort(LlmPort delegate, TraceSink sink, Clock clock) {
        this.delegate = delegate;
        this.sink = sink;
        this.clock = clock;
    }

    @Override
    public <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType) {
        var startedAt = clock.instant();
        var entrySequence = sequence.getAndIncrement();
        try {
            var result = delegate.complete(request, responseType);
            sink.accept(successEntry(entrySequence, request, startedAt, result));
            return result;
        } catch (MalformedLlmResponseException malformed) {
            sink.accept(failureEntry(entrySequence, request, startedAt, malformed.rawText(), malformed));
            throw malformed;
        } catch (LlmException failure) {
            sink.accept(failureEntry(entrySequence, request, startedAt, NO_RESPONSE, failure));
            throw failure;
        }
    }

    private TraceEntry successEntry(int entrySequence, LlmRequest request, Instant startedAt, LlmResult<?> result) {
        return new TraceEntry(
                entrySequence,
                request.label(),
                startedAt,
                elapsedSince(startedAt),
                result.model(),
                request.systemPrompt(),
                request.userPrompt(),
                result.rawText(),
                result.usage(),
                result.status(),
                null);
    }

    private TraceEntry failureEntry(
            int entrySequence, LlmRequest request, Instant startedAt, String rawText, LlmException failure) {
        return new TraceEntry(
                entrySequence,
                request.label(),
                startedAt,
                elapsedSince(startedAt),
                UNKNOWN_MODEL,
                request.systemPrompt(),
                request.userPrompt(),
                rawText,
                LlmUsage.NONE,
                LlmCallStatus.FAILED,
                failure.getMessage());
    }

    private Duration elapsedSince(Instant startedAt) {
        return Duration.between(startedAt, clock.instant());
    }
}
