package com.assignment.research.llm;

import lombok.Getter;

@Getter
public class MalformedLlmResponseException extends LlmException {

    private final String rawText;

    public MalformedLlmResponseException(String message, String rawText) {
        super(message);
        this.rawText = rawText;
    }
}
