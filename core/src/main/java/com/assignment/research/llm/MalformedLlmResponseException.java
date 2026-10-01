package com.assignment.research.llm;

import lombok.Getter;

@Getter
public class MalformedLLMResponseException extends LLMException {

    private final String rawText;

    public MalformedLLMResponseException(String message, String rawText) {
        super(message);
        this.rawText = rawText;
    }
}
