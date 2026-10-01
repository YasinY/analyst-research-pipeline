package com.assignment.research.adapter.llm;

public class ResponseParseException extends RuntimeException {

    public ResponseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResponseParseException(String message) {
        super(message);
    }
}
