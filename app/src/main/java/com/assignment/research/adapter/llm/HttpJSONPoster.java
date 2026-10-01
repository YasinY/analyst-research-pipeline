package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LLMException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public final class HttpJSONPoster {

    private final HttpClient http;
    private final ObjectMapper mapper;

    public HttpJSONPoster(ObjectMapper mapper) {
        this.mapper = mapper;
        this.http = HttpClient.newBuilder().connectTimeout(LLMAdapterConstants.CONNECT_TIMEOUT).build();
    }

    public JsonNode post(String url, Map<String, String> headers, Object body) {
        var request = buildRequest(url, headers, body);
        try {
            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(url, response);
        } catch (IOException failure) {
            throw new LLMException(LLMAdapterConstants.TRANSPORT_FAILURE.formatted(url, failure.getMessage()),
                    failure);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new LLMException(LLMAdapterConstants.TRANSPORT_FAILURE.formatted(url, "interrupted"),
                    interrupted);
        }
    }

    private HttpRequest buildRequest(String url, Map<String, String> headers, Object body) {
        try {
            var builder = HttpRequest.newBuilder(URI.create(url))
                    .timeout(LLMAdapterConstants.REQUEST_TIMEOUT)
                    .header(LLMAdapterConstants.HEADER_CONTENT_TYPE, LLMAdapterConstants.CONTENT_TYPE_JSON)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
            headers.forEach(builder::header);
            return builder.build();
        } catch (IOException failure) {
            throw new LLMException(LLMAdapterConstants.TRANSPORT_FAILURE.formatted(url, failure.getMessage()),
                    failure);
        }
    }

    private JsonNode parseResponse(String url, HttpResponse<String> response) throws IOException {
        var status = response.statusCode();
        if (status < LLMAdapterConstants.HTTP_OK_MIN || status > LLMAdapterConstants.HTTP_OK_MAX) {
            throw new HttpStatusException(status,
                    LLMAdapterConstants.HTTP_FAILURE.formatted(url, status, response.body()));
        }
        return mapper.readTree(response.body());
    }

    public static String requiredText(JsonNode node, String... path) {
        var current = node;
        for (var segment : path) {
            current = current.path(segment);
        }
        if (current.isMissingNode() || current.isNull()) {
            var fieldPath = String.join(LLMAdapterConstants.PATH_SEPARATOR, path);
            throw new LLMException(LLMAdapterConstants.MISSING_FIELD.formatted(fieldPath));
        }
        return current.asText();
    }

    public static int intOrZero(JsonNode node, String... path) {
        var current = node;
        for (var segment : path) {
            current = current.path(segment);
        }
        return current.asInt(0);
    }
}
