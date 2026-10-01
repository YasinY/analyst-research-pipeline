package com.assignment.research.trace;

import static com.assignment.research.trace.TraceConstants.FIRST_SEQUENCE;
import static com.assignment.research.trace.TraceConstants.NO_RESPONSE;
import static com.assignment.research.trace.TraceConstants.UNKNOWN_MODEL;

import com.assignment.research.llm.LlmCallStatus;
import com.assignment.research.llm.LlmException;
import com.assignment.research.llm.LlmPort;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.llm.LlmResult;
import com.assignment.research.llm.LlmUsage;
import com.assignment.research.llm.MalformedLlmResponseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TracingLlmPort implements LlmPort {

    private final LlmPort delegate;
    private final TraceSink sink;
    private final Clock clock;
    private final AtomicInteger sequence = new AtomicInteger(FIRST_SEQUENCE);

    @Override
    public <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType) {
        var startedAt = clock.instant();
        var entrySequence = sequence.getAndIncrement();
        try {
            var result = delegate.complete(request, responseType);
            sink.accept(successEntry(entrySequence, request, startedAt, result));
            return result;
        } catch (MalformedLlmResponseException malformed) {
            sink.accept(failureEntry(entrySequence, request, startedAt, malformed.getRawText(), malformed));
            throw malformed;
        } catch (LlmException failure) {
            sink.accept(failureEntry(entrySequence, request, startedAt, NO_RESPONSE, failure));
            throw failure;
        }
    }

    private TraceEntry successEntry(int entrySequence, LlmRequest request, Instant startedAt, LlmResult<?> result) {
        return new TraceEntry(
                entrySequence,
                request.getLabel(),
                startedAt,
                elapsedSince(startedAt),
                result.getModel(),
                request.getSystemPrompt(),
                request.getUserPrompt(),
                result.getRawText(),
                result.getUsage(),
                result.getStatus(),
                null);
    }

    private TraceEntry failureEntry(
            int entrySequence, LlmRequest request, Instant startedAt, String rawText, LlmException failure) {
        return new TraceEntry(
                entrySequence,
                request.getLabel(),
                startedAt,
                elapsedSince(startedAt),
                UNKNOWN_MODEL,
                request.getSystemPrompt(),
                request.getUserPrompt(),
                rawText,
                LlmUsage.NONE,
                LlmCallStatus.FAILED,
                failure.getMessage());
    }

    private Duration elapsedSince(Instant startedAt) {
        return Duration.between(startedAt, clock.instant());
    }
}
