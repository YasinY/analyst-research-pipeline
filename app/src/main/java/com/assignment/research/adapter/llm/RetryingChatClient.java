package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LLMException;
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
            } catch (LLMException failure) {
                attempt++;
                if (!isRetryable(failure) || attempt > LLMAdapterConstants.MAX_TRANSPORT_RETRIES) {
                    throw failure;
                }
                pause();
            }
        }
    }

    private static boolean isRetryable(LLMException failure) {
        if (failure instanceof HttpStatusException httpFailure) {
            return httpFailure.isRetryable();
        }
        return failure.getCause() instanceof java.io.IOException;
    }

    private static void pause() {
        try {
            Thread.sleep(LLMAdapterConstants.RETRY_BACKOFF);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
