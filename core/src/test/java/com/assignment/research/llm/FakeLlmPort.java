package com.assignment.research.llm;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FakeLlmPort implements LlmPort {

    public static final String MODEL = "fake-model";

    private final Function<LlmRequest, Object> responder;
    private final String rawText;
    private final LlmUsage usage;

    public static FakeLlmPort returning(Object value, String rawText, LlmUsage usage) {
        return new FakeLlmPort(request -> value, rawText, usage);
    }

    public static FakeLlmPort failingWith(LlmException failure) {
        return new FakeLlmPort(request -> {
            throw failure;
        }, "", LlmUsage.NONE);
    }

    @Override
    public <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType) {
        var value = responseType.cast(responder.apply(request));
        return new LlmResult<>(value, rawText, MODEL, usage, LlmCallStatus.OK);
    }
}
