package com.assignment.research.application.port;

public interface LlmPort {

    <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType);
}
