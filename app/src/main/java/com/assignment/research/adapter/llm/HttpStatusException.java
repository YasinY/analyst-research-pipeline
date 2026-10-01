package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LLMException;
import lombok.Getter;

@Getter
public class HttpStatusException extends LLMException {

    private final int status;

    public HttpStatusException(int status, String message) {
        super(message);
        this.status = status;
    }

    public boolean isRetryable() {
        return status == LLMAdapterConstants.HTTP_TOO_MANY_REQUESTS
                || status >= LLMAdapterConstants.HTTP_SERVER_ERROR_MIN;
    }
}
