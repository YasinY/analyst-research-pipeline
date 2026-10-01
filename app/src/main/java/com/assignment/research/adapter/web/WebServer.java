package com.assignment.research.adapter.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WebServer {

    private final ResearchRunRegistry registry;
    private final ObjectMapper mapper;
    private final int port;
    private final PrintStream console;
    private final String indexResource;
    private HttpServer server;
    private ExecutorService executor;

    public WebServer(ResearchRunRegistry registry, ObjectMapper mapper, int port, PrintStream console) {
        this(registry, mapper, port, console, WebConstants.INDEX_RESOURCE);
    }

    public WebServer(ResearchRunRegistry registry, ObjectMapper mapper, int port, PrintStream console,
            String indexResource) {
        this.registry = registry;
        this.mapper = mapper;
        this.port = port;
        this.console = console;
        this.indexResource = indexResource;
    }

    public int start() {
        try {
            server = HttpServer.create(new InetSocketAddress(WebConstants.BIND_HOST, port), WebConstants.BACKLOG);
            var boundPort = server.getAddress().getPort();
            var guard = new RequestGuard(boundPort);
            server.createContext(WebConstants.RESEARCHES_PATH,
                    exchange -> guarded(exchange, guard, this::handleResearches));
            server.createContext(WebConstants.ROOT_PATH, exchange -> guarded(exchange, guard, this::handleIndex));
            executor = Executors.newVirtualThreadPerTaskExecutor();
            server.setExecutor(executor);
            server.start();
            console.println(WebConstants.STARTED_MESSAGE.formatted(WebConstants.BIND_HOST, boundPort));
            return boundPort;
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public void stop() {
        if (server == null) {
            return;
        }
        server.stop(WebConstants.STOP_DELAY_SECONDS);
        executor.shutdown();
        server = null;
    }

    private void guarded(HttpExchange exchange, RequestGuard guard, HttpHandler handler) throws IOException {
        if (!guard.permits(exchange.getRequestMethod(), exchange.getRequestHeaders())) {
            respondError(exchange, WebConstants.HTTP_FORBIDDEN, WebConstants.ERROR_FORBIDDEN);
            return;
        }
        try {
            handler.handle(exchange);
        } catch (RuntimeException failure) {
            respondError(exchange, WebConstants.HTTP_INTERNAL_ERROR, WebConstants.ERROR_INTERNAL);
        }
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        if (!WebConstants.ROOT_PATH.equals(exchange.getRequestURI().getPath())) {
            respondError(exchange, WebConstants.HTTP_NOT_FOUND, WebConstants.ERROR_NOT_FOUND);
            return;
        }
        InputStream page = getClass().getClassLoader().getResourceAsStream(indexResource);
        if (page == null) {
            respondError(exchange, WebConstants.HTTP_INTERNAL_ERROR, WebConstants.ERROR_INDEX_MISSING);
            return;
        }
        try (page) {
            respond(exchange, WebConstants.HTTP_OK, WebConstants.CONTENT_TYPE_HTML, page.readAllBytes());
        }
    }

    private void handleResearches(HttpExchange exchange) throws IOException {
        var path = exchange.getRequestURI().getPath();
        var segments = path.substring(WebConstants.RESEARCHES_PATH.length()).split(WebConstants.PATH_SEPARATOR);
        if (segments.length <= WebConstants.COLLECTION_SEGMENT_COUNT) {
            handleCollection(exchange);
            return;
        }
        handleRun(exchange, segments);
    }

    private void handleCollection(HttpExchange exchange) throws IOException {
        if (!WebConstants.METHOD_POST.equals(exchange.getRequestMethod())) {
            respondMethodNotAllowed(exchange, WebConstants.METHOD_POST);
            return;
        }
        startRun(exchange);
    }

    private void handleRun(HttpExchange exchange, String[] segments) throws IOException {
        if (!WebConstants.METHOD_GET.equals(exchange.getRequestMethod())) {
            respondMethodNotAllowed(exchange, WebConstants.METHOD_GET);
            return;
        }
        var run = registry.find(segments[WebConstants.RUN_ID_SEGMENT_INDEX]);
        if (run.isEmpty()) {
            respondError(exchange, WebConstants.HTTP_NOT_FOUND, WebConstants.ERROR_NOT_FOUND);
            return;
        }
        if (segments.length == WebConstants.RUN_SEGMENT_COUNT) {
            respond(exchange, WebConstants.HTTP_OK, WebConstants.CONTENT_TYPE_JSON, json(run.get().toResponse()));
            return;
        }
        respondCall(exchange, run.get(), segments);
    }

    private void respondCall(HttpExchange exchange, ResearchRun run, String[] segments) throws IOException {
        var detail = isCallPath(segments)
                ? parseSequence(segments[WebConstants.SEQUENCE_SEGMENT_INDEX]).flatMap(run::call)
                : Optional.<CallDetail>empty();
        if (detail.isEmpty()) {
            respondError(exchange, WebConstants.HTTP_NOT_FOUND, WebConstants.ERROR_NOT_FOUND);
            return;
        }
        respond(exchange, WebConstants.HTTP_OK, WebConstants.CONTENT_TYPE_JSON, json(detail.get()));
    }

    private void startRun(HttpExchange exchange) throws IOException {
        var body = readBody(exchange);
        if (body.isEmpty()) {
            respondError(exchange, WebConstants.HTTP_BAD_REQUEST, WebConstants.ERROR_MALFORMED_JSON);
            return;
        }
        var query = body.get().path(WebConstants.JSON_KEY_QUERY).asText(WebConstants.EMPTY_TEXT).strip();
        if (query.isEmpty()) {
            respondError(exchange, WebConstants.HTTP_BAD_REQUEST, WebConstants.ERROR_EMPTY_QUERY);
            return;
        }
        var run = registry.start(query);
        respond(exchange, WebConstants.HTTP_ACCEPTED, WebConstants.CONTENT_TYPE_JSON, json(run.toResponse()));
    }

    private Optional<JsonNode> readBody(HttpExchange exchange) throws IOException {
        try {
            return Optional.ofNullable(mapper.readTree(exchange.getRequestBody()));
        } catch (JsonProcessingException malformed) {
            return Optional.empty();
        }
    }

    private void respondError(HttpExchange exchange, int status, String message) throws IOException {
        respond(exchange, status, WebConstants.CONTENT_TYPE_JSON, json(Map.of(WebConstants.JSON_KEY_ERROR, message)));
    }

    private void respondMethodNotAllowed(HttpExchange exchange, String allowedMethod) throws IOException {
        exchange.getResponseHeaders().set(WebConstants.HEADER_ALLOW, allowedMethod);
        respondError(exchange, WebConstants.HTTP_METHOD_NOT_ALLOWED, WebConstants.ERROR_METHOD);
    }

    private byte[] json(Object value) throws IOException {
        return mapper.writeValueAsBytes(value);
    }

    private static boolean isCallPath(String[] segments) {
        return segments.length == WebConstants.CALL_SEGMENT_COUNT
                && WebConstants.CALLS_SEGMENT.equals(segments[WebConstants.CALLS_SEGMENT_INDEX]);
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
