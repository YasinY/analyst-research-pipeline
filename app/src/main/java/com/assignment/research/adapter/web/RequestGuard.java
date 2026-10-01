package com.assignment.research.adapter.web;

import com.sun.net.httpserver.Headers;
import java.util.Locale;
import java.util.Set;

public final class RequestGuard {

    private final Set<String> allowedHosts;
    private final Set<String> allowedOrigins;

    public RequestGuard(int port) {
        this.allowedHosts = Set.of(
                WebConstants.HOST_FORMAT.formatted(WebConstants.BIND_HOST, port),
                WebConstants.HOST_FORMAT.formatted(WebConstants.LOCALHOST, port));
        this.allowedOrigins = Set.of(
                WebConstants.ORIGIN_FORMAT.formatted(WebConstants.BIND_HOST, port),
                WebConstants.ORIGIN_FORMAT.formatted(WebConstants.LOCALHOST, port));
    }

    public boolean permits(String method, Headers headers) {
        if (!matches(allowedHosts, headers.getFirst(WebConstants.HEADER_HOST))) {
            return false;
        }
        var origin = headers.getFirst(WebConstants.HEADER_ORIGIN);
        if (origin != null && !matches(allowedOrigins, origin)) {
            return false;
        }
        return !WebConstants.METHOD_POST.equals(method) || isJson(headers.getFirst(WebConstants.HEADER_CONTENT_TYPE));
    }

    private static boolean matches(Set<String> allowed, String value) {
        return value != null && allowed.contains(normalize(value));
    }

    private static boolean isJson(String contentType) {
        return contentType != null && normalize(contentType).startsWith(WebConstants.MEDIA_TYPE_JSON);
    }

    private static String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
