package com.assignment.research.adapter.llm;

import com.fasterxml.jackson.databind.ObjectMapper;

final class ThrowingObjectMapper extends ObjectMapper {

    private final transient RuntimeException failure;

    ThrowingObjectMapper(RuntimeException failure) {
        this.failure = failure;
    }

    @Override
    public <T> T readValue(String content, Class<T> valueType) {
        throw failure;
    }
}
