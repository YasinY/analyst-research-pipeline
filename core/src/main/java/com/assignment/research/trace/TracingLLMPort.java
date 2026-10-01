package com.assignment.research.trace;

import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMException;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.llm.LLMResult;
import com.assignment.research.llm.LLMUsage;
import com.assignment.research.llm.MalformedLLMResponseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class TracingLLMPort implements LLMPort {

    private final LLMPort delegate;
    private final TraceSink sink;
    private final Clock clock;
    private final AtomicInteger sequence = new AtomicInteger(TraceConstants.FIRST_SEQUENCE);

    @Override
    public <T> LLMResult<T> complete(LLMRequest request, Class<T> responseType) {
        var startedAt = clock.instant();
        var entrySequence = sequence.getAndIncrement();
        try {
            var result = delegate.complete(request, responseType);
            sink.accept(successEntry(entrySequence, request, startedAt, result));
            return result;
        } catch (MalformedLLMResponseException malformed) {
            sink.accept(failureEntry(entrySequence, request, startedAt, malformed.getRawText(), malformed));
            throw malformed;
        } catch (LLMException failure) {
            sink.accept(failureEntry(entrySequence, request, startedAt, TraceConstants.NO_RESPONSE, failure));
            throw failure;
        }
    }

    private TraceEntry successEntry(int entrySequence, LLMRequest request, Instant startedAt, LLMResult<?> result) {
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
            int entrySequence, LLMRequest request, Instant startedAt, String rawText, LLMException failure) {
        return new TraceEntry(
                entrySequence,
                request.getLabel(),
                startedAt,
                elapsedSince(startedAt),
                TraceConstants.UNKNOWN_MODEL,
                request.getSystemPrompt(),
                request.getUserPrompt(),
                rawText,
                LLMUsage.NONE,
                LLMCallStatus.FAILED,
                failure.getMessage());
    }

    private Duration elapsedSince(Instant startedAt) {
        return Duration.between(startedAt, clock.instant());
    }
}
