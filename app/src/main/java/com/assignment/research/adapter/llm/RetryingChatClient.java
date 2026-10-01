package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LlmException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RetryingChatClient implements ChatClient {

    private final ChatClient delegate;

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        var attempt = 0;
        while (true) {
            try {
                return delegate.chat(systemPrompt, userPrompt, maxOutputTokens);
            } catch (LlmException failure) {
                attempt++;
                if (!isRetryable(failure) || attempt > LlmAdapterConstants.MAX_TRANSPORT_RETRIES) {
                    throw failure;
                }
                pause();
            }
        }
    }

    private static boolean isRetryable(LlmException failure) {
        if (failure instanceof HttpStatusException httpFailure) {
            return httpFailure.isRetryable();
        }
        return failure.getCause() instanceof java.io.IOException;
    }

    private static void pause() {
        try {
            Thread.sleep(LlmAdapterConstants.RETRY_BACKOFF);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
