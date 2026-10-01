package com.assignment.research.application.port;

public class MalformedLlmResponseException extends LlmException {

    private final String rawText;

    public MalformedLlmResponseException(String message, String rawText) {
        super(message);
        this.rawText = rawText;
    }

    public String rawText() {
        return rawText;
    }
}
