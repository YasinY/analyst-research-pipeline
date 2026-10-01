package com.assignment.research.llm;

public interface LLMPort {

    <T> LLMResult<T> complete(LLMRequest request, Class<T> responseType);
}
