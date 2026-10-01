package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LlmException;
import lombok.Getter;

@Getter
public class HttpStatusException extends LlmException {

    private final int status;

    public HttpStatusException(int status, String message) {
        super(message);
        this.status = status;
    }

    public boolean isRetryable() {
        return status == LlmAdapterConstants.HTTP_TOO_MANY_REQUESTS
                || status >= LlmAdapterConstants.HTTP_SERVER_ERROR_MIN;
    }
}
