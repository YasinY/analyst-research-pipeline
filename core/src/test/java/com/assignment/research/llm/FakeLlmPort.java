package com.assignment.research.llm;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FakeLlmPort implements LlmPort {

    public static final String MODEL = "fake-model";
    private static final String NO_RAW_TEXT = "";

    private final Function<LlmRequest, Object> responder;
    private final String rawText;
    private final LlmUsage usage;
    private final AtomicReference<LlmRequest> lastRequest = new AtomicReference<>();

    public static FakeLlmPort returning(Object value) {
        return returning(value, NO_RAW_TEXT, LlmUsage.NONE);
    }

    public static FakeLlmPort returning(Object value, String rawText, LlmUsage usage) {
        return new FakeLlmPort(request -> value, rawText, usage);
    }

    public static FakeLlmPort failingWith(LlmException failure) {
        return new FakeLlmPort(request -> {
            throw failure;
        }, NO_RAW_TEXT, LlmUsage.NONE);
    }

    @Override
    public <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType) {
        lastRequest.set(request);
        var value = responseType.cast(responder.apply(request));
        return new LlmResult<>(value, rawText, MODEL, usage, LlmCallStatus.OK);
    }

    public LlmRequest getLastRequest() {
        return lastRequest.get();
    }
}
