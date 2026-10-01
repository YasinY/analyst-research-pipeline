package com.assignment.research.adapter.llm;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RecordingHandler implements HttpHandler {

    private static final String VALUE_SEPARATOR = ",";

    private final int status;
    private final byte[] responseBody;
    private final Map<String, String> receivedHeaders = new ConcurrentHashMap<>();
    private volatile String receivedBody = "";

    public RecordingHandler(int status, String responseBody) {
        this.status = status;
        this.responseBody = responseBody.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getRequestHeaders().forEach((name, values) -> receivedHeaders.put(name.toLowerCase(Locale.ROOT),
                String.join(VALUE_SEPARATOR, values)));
        try (var request = exchange.getRequestBody()) {
            receivedBody = new String(request.readAllBytes(), StandardCharsets.UTF_8);
        }
        exchange.sendResponseHeaders(status, responseBody.length);
        try (var response = exchange.getResponseBody()) {
            response.write(responseBody);
        }
    }

    public Optional<String> header(String name) {
        return Optional.ofNullable(receivedHeaders.get(name.toLowerCase(Locale.ROOT)));
    }

    public String getReceivedBody() {
        return receivedBody;
    }
}
