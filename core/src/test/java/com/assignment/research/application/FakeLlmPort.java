package com.assignment.research.application;

import com.assignment.research.application.port.LlmException;
import com.assignment.research.application.port.LlmPort;
import com.assignment.research.application.port.LlmRequest;
import com.assignment.research.application.port.LlmResult;
import com.assignment.research.domain.LlmCallStatus;
import com.assignment.research.domain.LlmUsage;
import java.util.function.Function;

public final class FakeLlmPort implements LlmPort {

    public static final String MODEL = "fake-model";

    private final Function<LlmRequest, Object> responder;
    private final String rawText;
    private final LlmUsage usage;

    private FakeLlmPort(Function<LlmRequest, Object> responder, String rawText, LlmUsage usage) {
        this.responder = responder;
        this.rawText = rawText;
        this.usage = usage;
    }

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
