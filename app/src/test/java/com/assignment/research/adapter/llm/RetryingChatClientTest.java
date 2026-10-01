package com.assignment.research.adapter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.llm.LLMException;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RetryingChatClientTest {

    private static final Duration NO_BACKOFF = Duration.ZERO;
    private static final String SYSTEM = "system";
    private static final String USER = "user";
    private static final int BUDGET = 64;
    private static final int TOO_MANY_REQUESTS = 429;
    private static final int SERVER_ERROR = 503;
    private static final int BAD_REQUEST = 400;
    private static final String FAILURE = "provider failed";
    private static final int CALLS_FOR_ONE_RETRY = 2;
    private static final int CALLS_UNTIL_GIVING_UP = 1 + LLMAdapterConstants.MAX_TRANSPORT_RETRIES;
    private static final int SINGLE_CALL = 1;

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void retriesARetryableStatusAndReturnsTheLaterReply() {
        var delegate = new FailingThenAnsweringChatClient(List.of(status(TOO_MANY_REQUESTS)));

        var reply = new RetryingChatClient(delegate, NO_BACKOFF).chat(SYSTEM, USER, BUDGET);

        assertThat(reply.getText()).isEqualTo(FailingThenAnsweringChatClient.ANSWER);
        assertThat(delegate.getCalls()).isEqualTo(CALLS_FOR_ONE_RETRY);
    }

    @Test
    void givesUpAfterTheMaximumNumberOfRetries() {
        var lastFailure = status(SERVER_ERROR);
        var delegate = new FailingThenAnsweringChatClient(
                List.of(status(SERVER_ERROR), status(SERVER_ERROR), lastFailure));

        assertThatThrownBy(() -> new RetryingChatClient(delegate, NO_BACKOFF).chat(SYSTEM, USER, BUDGET))
                .isSameAs(lastFailure);
        assertThat(delegate.getCalls()).isEqualTo(CALLS_UNTIL_GIVING_UP);
    }

    @Test
    void doesNotRetryANonRetryableStatus() {
        var failure = status(BAD_REQUEST);
        var delegate = new FailingThenAnsweringChatClient(List.of(failure));

        assertThatThrownBy(() -> new RetryingChatClient(delegate).chat(SYSTEM, USER, BUDGET)).isSameAs(failure);
        assertThat(delegate.getCalls()).isEqualTo(SINGLE_CALL);
    }

    @Test
    void doesNotRetryAFailureWithoutAnIOCause() {
        var failure = new LLMException(FAILURE, new IllegalStateException(FAILURE));
        var delegate = new FailingThenAnsweringChatClient(List.of(failure));

        assertThatThrownBy(() -> new RetryingChatClient(delegate, NO_BACKOFF).chat(SYSTEM, USER, BUDGET))
                .isSameAs(failure);
        assertThat(delegate.getCalls()).isEqualTo(SINGLE_CALL);
    }

    @Test
    void retriesAFailureCausedByAnIOException() {
        var delegate = new FailingThenAnsweringChatClient(List.of(new LLMException(FAILURE, new IOException(FAILURE))));

        var reply = new RetryingChatClient(delegate, NO_BACKOFF).chat(SYSTEM, USER, BUDGET);

        assertThat(reply.getText()).isEqualTo(FailingThenAnsweringChatClient.ANSWER);
        assertThat(delegate.getCalls()).isEqualTo(CALLS_FOR_ONE_RETRY);
    }

    @Test
    void interruptedBackoffRestoresTheInterruptFlagAndStillRetries() {
        var delegate = new FailingThenAnsweringChatClient(List.of(status(TOO_MANY_REQUESTS)));
        Thread.currentThread().interrupt();

        var reply = new RetryingChatClient(delegate).chat(SYSTEM, USER, BUDGET);

        assertThat(reply.getText()).isEqualTo(FailingThenAnsweringChatClient.ANSWER);
        assertThat(Thread.interrupted()).isTrue();
    }

    private static HttpStatusException status(int status) {
        return new HttpStatusException(status, FAILURE);
    }
}
