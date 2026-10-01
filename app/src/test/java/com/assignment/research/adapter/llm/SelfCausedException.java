package com.assignment.research.adapter.llm;

final class SelfCausedException extends RuntimeException {

    SelfCausedException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable getCause() {
        return this;
    }
}
