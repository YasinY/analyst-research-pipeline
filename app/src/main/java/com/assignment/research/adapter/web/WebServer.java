package com.assignment.research.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WebServer {

    private final ResearchRunRegistry registry;
    private final ObjectMapper mapper;
    private final int port;

    public void start() {
        try {
            var server = HttpServer.create(new InetSocketAddress(WebConstants.BIND_HOST, port), WebConstants.BACKLOG);
            server.createContext(WebConstants.RESEARCHES_PATH, this::handleResearches);
            server.createContext(WebConstants.ROOT_PATH, this::handleIndex);
            server.start();
            System.out.println(WebConstants.STARTED_MESSAGE.formatted(WebConstants.BIND_HOST, port));
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        if (!WebConstants.ROOT_PATH.equals(exchange.getRequestURI().getPath())) {
            respond(exchange, WebConstants.HTTP_NOT_FOUND, WebConstants.CONTENT_TYPE_JSON,
                    json(Map.of("error", WebConstants.ERROR_NOT_FOUND)));
            return;
        }
        try (InputStream page = getClass().getClassLoader().getResourceAsStream(WebConstants.INDEX_RESOURCE)) {
            respond(exchange, WebConstants.HTTP_OK, WebConstants.CONTENT_TYPE_HTML, page.readAllBytes());
        }
    }

    private void handleResearches(HttpExchange exchange) throws IOException {
        var path = exchange.getRequestURI().getPath();
        var segments = path.substring(WebConstants.RESEARCHES_PATH.length()).split(WebConstants.PATH_SEPARATOR);
        var method = exchange.getRequestMethod();

        if (segments.length <= 1) {
            if (WebConstants.METHOD_POST.equals(method)) {
                startRun(exchange);
                return;
            }
            respond(exchange, WebConstants.HTTP_METHOD_NOT_ALLOWED, WebConstants.CONTENT_TYPE_JSON,
                    json(Map.of("error", WebConstants.ERROR_METHOD)));
            return;
        }
        var run = registry.find(segments[1]);
        if (run.isEmpty() || !WebConstants.METHOD_GET.equals(method)) {
            respond(exchange, WebConstants.HTTP_NOT_FOUND, WebConstants.CONTENT_TYPE_JSON,
                    json(Map.of("error", WebConstants.ERROR_NOT_FOUND)));
            return;
        }
        if (segments.length == 2) {
            respond(exchange, WebConstants.HTTP_OK, WebConstants.CONTENT_TYPE_JSON, json(run.get().toResponse()));
            return;
        }
        respondCall(exchange, run.get(), segments);
    }

    private void respondCall(HttpExchange exchange, ResearchRun run, String[] segments) throws IOException {
        var detail = segments.length == 4 && WebConstants.CALLS_SEGMENT.equals(segments[2])
                ? parseSequence(segments[3]).flatMap(run::call)
                : Optional.<CallDetail>empty();
        if (detail.isEmpty()) {
            respond(exchange, WebConstants.HTTP_NOT_FOUND, WebConstants.CONTENT_TYPE_JSON,
                    json(Map.of("error", WebConstants.ERROR_NOT_FOUND)));
            return;
        }
        respond(exchange, WebConstants.HTTP_OK, WebConstants.CONTENT_TYPE_JSON, json(detail.get()));
    }

    private void startRun(HttpExchange exchange) throws IOException {
        var body = mapper.readTree(exchange.getRequestBody());
        var query = body.path("query").asText("").strip();
        if (query.isEmpty()) {
            respond(exchange, WebConstants.HTTP_BAD_REQUEST, WebConstants.CONTENT_TYPE_JSON,
                    json(Map.of("error", WebConstants.ERROR_EMPTY_QUERY)));
            return;
        }
        var run = registry.start(query);
        respond(exchange, WebConstants.HTTP_ACCEPTED, WebConstants.CONTENT_TYPE_JSON, json(run.toResponse()));
    }

    private byte[] json(Object value) throws IOException {
        return mapper.writeValueAsBytes(value);
    }

    private static Optional<Integer> parseSequence(String segment) {
        try {
            return Optional.of(Integer.parseInt(segment));
        } catch (NumberFormatException invalid) {
            return Optional.empty();
        }
    }

    private static void respond(HttpExchange exchange, int status, String contentType, byte[] body)
            throws IOException {
        exchange.getResponseHeaders().set(WebConstants.HEADER_CONTENT_TYPE, contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (var out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
