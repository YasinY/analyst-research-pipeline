package com.assignment.research.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;

class RequestGuardTest {

    private static final int PORT = 8787;
    private static final String LOOPBACK_HOST = "127.0.0.1:8787";
    private static final String LOCALHOST_HOST = "localhost:8787";
    private static final String LOOPBACK_ORIGIN = "http://127.0.0.1:8787";
    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    private final RequestGuard guard = new RequestGuard(PORT);

    @Test
    void permitsSameOriginJsonPost() {
        var headers = headers(LOOPBACK_HOST, LOOPBACK_ORIGIN, JSON_CONTENT_TYPE);

        assertThat(guard.permits(WebConstants.METHOD_POST, headers)).isTrue();
    }

    @Test
    void permitsGetOnLocalhostWithoutOrigin() {
        var headers = headers(LOCALHOST_HOST, null, null);

        assertThat(guard.permits(WebConstants.METHOD_GET, headers)).isTrue();
    }

    @Test
    void rejectsForeignHostToBlockDnsRebinding() {
        var headers = headers("attacker.example:8787", null, null);

        assertThat(guard.permits(WebConstants.METHOD_GET, headers)).isFalse();
    }

    @Test
    void rejectsMissingHost() {
        var headers = headers(null, null, JSON_CONTENT_TYPE);

        assertThat(guard.permits(WebConstants.METHOD_POST, headers)).isFalse();
    }

    @Test
    void rejectsHostOnOtherPort() {
        var headers = headers("127.0.0.1:9999", null, null);

        assertThat(guard.permits(WebConstants.METHOD_GET, headers)).isFalse();
    }

    @Test
    void rejectsCrossSiteOrigin() {
        var headers = headers(LOOPBACK_HOST, "https://attacker.example", JSON_CONTENT_TYPE);

        assertThat(guard.permits(WebConstants.METHOD_POST, headers)).isFalse();
    }

    @Test
    void rejectsPostWithoutJsonContentType() {
        var headers = headers(LOOPBACK_HOST, LOOPBACK_ORIGIN, "text/plain");

        assertThat(guard.permits(WebConstants.METHOD_POST, headers)).isFalse();
    }

    @Test
    void rejectsPostWithoutContentType() {
        var headers = headers(LOOPBACK_HOST, null, null);

        assertThat(guard.permits(WebConstants.METHOD_POST, headers)).isFalse();
    }

    private static Headers headers(String host, String origin, String contentType) {
        var headers = new Headers();
        putIfPresent(headers, WebConstants.HEADER_HOST, host);
        putIfPresent(headers, WebConstants.HEADER_ORIGIN, origin);
        putIfPresent(headers, WebConstants.HEADER_CONTENT_TYPE, contentType);
        return headers;
    }

    private static void putIfPresent(Headers headers, String name, String value) {
        if (value == null) {
            return;
        }
        headers.set(name, value);
    }
}
