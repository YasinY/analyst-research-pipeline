package com.assignment.research.llm;

public interface LlmPort {

    <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType);
}
