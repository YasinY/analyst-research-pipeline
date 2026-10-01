package com.assignment.research.pipeline;

public class PipelineAbortedException extends RuntimeException {

    public PipelineAbortedException(String message, Throwable cause) {
        super(message, cause);
    }
}
