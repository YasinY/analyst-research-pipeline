package com.assignment.research.adapter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.llm.LLMException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class HttpJSONPosterTest {

    private static final String HTML_BODY = "<html>gateway maintenance</html>";
    private static final String JSON_BODY = """
            {"answer": {"text": "hello", "nothing": null, "count": 7}}
            """;
    private static final String ERROR_BODY = "{\"error\": \"nope\"}";
    private static final int HTTP_OK = 200;
    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final int HTTP_SERVER_ERROR = 500;
    private static final int HTTP_INFORMATIONAL_UPPER_BOUND = 199;
    private static final String UNUSED_URL = "http://127.0.0.1:9/unused";
    private static final int COUNT = 7;
    private static final int MISSING = 0;

    private final HttpJSONPoster poster = new HttpJSONPoster(JSONMapperFactory.create());

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void successfulStatusWithNonJSONBodyFailsWithoutARetryableTransportCause() {
        try (var provider = StubProvider.answering(HTTP_OK, HTML_BODY)) {
            var url = provider.url();

            var failure = catchThrowableOfType(LLMException.class, () -> poster.post(url, Map.of(), Map.of()));

            assertThat(failure).hasMessageContaining("not JSON").hasMessageContaining(url);
            assertThat(failure.getCause()).isNull();
        }
    }

    @Test
    void successfulJSONReplyIsParsedAndHeadersAndBodyAreSent() {
        try (var provider = StubProvider.answering(HTTP_OK, JSON_BODY)) {
            var response = poster.post(provider.url(), Map.of("X-Test", "yes"), Map.of("q", "v"));

            assertThat(HttpJSONPoster.requiredText(response, "answer", "text")).isEqualTo("hello");
            assertThat(HttpJSONPoster.intOrZero(response, "answer", "count")).isEqualTo(COUNT);
            assertThat(HttpJSONPoster.intOrZero(response, "answer", "absent")).isEqualTo(MISSING);
            assertThat(provider.handler().header("X-Test")).contains("yes");
            assertThat(provider.handler().header(LLMAdapterConstants.HEADER_CONTENT_TYPE))
                    .contains(LLMAdapterConstants.CONTENT_TYPE_JSON);
            assertThat(provider.handler().getReceivedBody()).contains("\"q\"").contains("\"v\"");
        }
    }

    @Test
    void requiredTextRejectsMissingAndNullFieldsNamingTheFieldPath() throws IOException {
        var response = JSONMapperFactory.create().readTree(JSON_BODY);

        var missing = catchThrowableOfType(LLMException.class,
                () -> HttpJSONPoster.requiredText(response, "answer", "absent"));
        var nullValue = catchThrowableOfType(LLMException.class,
                () -> HttpJSONPoster.requiredText(response, "answer", "nothing"));

        assertThat(missing).hasMessageContaining("answer.absent");
        assertThat(nullValue).hasMessageContaining("answer.nothing");
    }

    @Test
    void clientErrorRaisesANonRetryableStatusException() {
        var failure = statusFailure(HTTP_BAD_REQUEST);

        assertThat(failure.getStatus()).isEqualTo(HTTP_BAD_REQUEST);
        assertThat(failure.isRetryable()).isFalse();
        assertThat(failure).hasMessageContaining(ERROR_BODY);
    }

    @Test
    void rateLimitRaisesARetryableStatusException() {
        var failure = statusFailure(HTTP_TOO_MANY_REQUESTS);

        assertThat(failure.getStatus()).isEqualTo(HTTP_TOO_MANY_REQUESTS);
        assertThat(failure.isRetryable()).isTrue();
    }

    @Test
    void serverErrorRaisesARetryableStatusException() {
        var failure = statusFailure(HTTP_SERVER_ERROR);

        assertThat(failure.getStatus()).isEqualTo(HTTP_SERVER_ERROR);
        assertThat(failure.isRetryable()).isTrue();
    }

    @Test
    void statusBelowTheSuccessRangeIsAFailure() {
        var informationalPoster = new HttpJSONPoster(JSONMapperFactory.create(),
                new CannedHttpClient(HTTP_INFORMATIONAL_UPPER_BOUND, ERROR_BODY));

        var failure = catchThrowableOfType(HttpStatusException.class,
                () -> informationalPoster.post(UNUSED_URL, Map.of(), Map.of()));

        assertThat(failure.getStatus()).isEqualTo(HTTP_INFORMATIONAL_UPPER_BOUND);
        assertThat(failure.isRetryable()).isFalse();
    }

    @Test
    void refusedConnectionRaisesAnLLMExceptionWithAnIOCause() {
        String url;
        try (var provider = StubProvider.answering(HTTP_OK, JSON_BODY)) {
            url = provider.url();
        }
        var target = url;

        var failure = catchThrowableOfType(LLMException.class, () -> poster.post(target, Map.of(), Map.of()));

        assertThat(failure).hasMessageContaining(target);
        assertThat(failure.getCause()).isInstanceOf(IOException.class);
    }

    @Test
    void interruptedSendRestoresTheInterruptFlag() {
        try (var provider = StubProvider.answering(HTTP_OK, JSON_BODY)) {
            var url = provider.url();
            Thread.currentThread().interrupt();

            var failure = catchThrowableOfType(LLMException.class, () -> poster.post(url, Map.of(), Map.of()));

            assertThat(failure).hasMessageContaining(LLMAdapterConstants.INTERRUPTED);
            assertThat(failure.getCause()).isInstanceOf(InterruptedException.class);
            assertThat(Thread.interrupted()).isTrue();
        }
    }

    @Test
    void unserializableBodyRaisesAnLLMExceptionBeforeSending() {
        var strictPoster = new HttpJSONPoster(new ObjectMapper());

        var failure = catchThrowableOfType(LLMException.class,
                () -> strictPoster.post(UNUSED_URL, Map.of(), new Object()));

        assertThat(failure.getCause()).isInstanceOf(IOException.class);
    }

    private HttpStatusException statusFailure(int status) {
        try (var provider = StubProvider.answering(status, ERROR_BODY)) {
            var url = provider.url();
            return catchThrowableOfType(HttpStatusException.class, () -> poster.post(url, Map.of(), Map.of()));
        }
    }
}
