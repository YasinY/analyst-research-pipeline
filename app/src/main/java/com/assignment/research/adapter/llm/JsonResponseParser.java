package com.assignment.research.adapter.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class JsonResponseParser {

    private final ObjectMapper mapper;

    public <T> T parse(String rawText, Class<T> type) {
        var json = extractJsonObject(rawText);
        try {
            return mapper.readValue(json, type);
        } catch (Exception failure) {
            throw new ResponseParseException(
                    LlmAdapterConstants.PARSE_FAILED.formatted(rootMessage(failure)), failure);
        }
    }

    private static String extractJsonObject(String rawText) {
        var withoutFences = rawText.replace(LlmAdapterConstants.CODE_FENCE, "");
        var start = withoutFences.indexOf(LlmAdapterConstants.JSON_START);
        var end = withoutFences.lastIndexOf(LlmAdapterConstants.JSON_END);
        if (start < 0 || end < start) {
            throw new ResponseParseException(LlmAdapterConstants.NO_JSON_OBJECT);
        }
        return withoutFences.substring(start, end + 1);
    }

    private static String rootMessage(Throwable failure) {
        var cause = failure;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
