package com.assignment.research.llm;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FakeLLMPort implements LLMPort {

    public static final String MODEL = "fake-model";
    private static final String NO_RAW_TEXT = "";

    private final Function<LLMRequest, Object> responder;
    private final String rawText;
    private final LLMUsage usage;
    private final AtomicReference<LLMRequest> lastRequest = new AtomicReference<>();

    public static FakeLLMPort returning(Object value) {
        return returning(value, NO_RAW_TEXT, LLMUsage.NONE);
    }

    public static FakeLLMPort returning(Object value, String rawText, LLMUsage usage) {
        return new FakeLLMPort(request -> value, rawText, usage);
    }

    public static FakeLLMPort failingWith(LLMException failure) {
        return new FakeLLMPort(request -> {
            throw failure;
        }, NO_RAW_TEXT, LLMUsage.NONE);
    }

    @Override
    public <T> LLMResult<T> complete(LLMRequest request, Class<T> responseType) {
        lastRequest.set(request);
        var value = responseType.cast(responder.apply(request));
        return new LLMResult<>(value, rawText, MODEL, usage, LLMCallStatus.OK);
    }

    public LLMRequest getLastRequest() {
        return lastRequest.get();
    }
}
