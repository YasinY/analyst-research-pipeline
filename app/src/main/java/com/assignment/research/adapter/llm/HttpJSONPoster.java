package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LLMException;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        var response = send(url, buildRequest(url, headers, body));
        return parseResponse(url, response);
    }

    public static String requiredText(JsonNode node, String... path) {
        var field = at(node, path);
        if (field.isMissingNode() || field.isNull()) {
            var fieldPath = String.join(LLMAdapterConstants.PATH_SEPARATOR, path);
            throw new LLMException(LLMAdapterConstants.MISSING_FIELD.formatted(fieldPath));
        }
        return field.asText();
    }

    public static int intOrZero(JsonNode node, String... path) {
        return at(node, path).asInt(LLMAdapterConstants.MISSING_INT_VALUE);
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

    private HttpResponse<String> send(String url, HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException failure) {
            throw new LLMException(LLMAdapterConstants.TRANSPORT_FAILURE.formatted(url, failure.getMessage()),
                    failure);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new LLMException(
                    LLMAdapterConstants.TRANSPORT_FAILURE.formatted(url, LLMAdapterConstants.INTERRUPTED),
                    interrupted);
        }
    }

    private JsonNode parseResponse(String url, HttpResponse<String> response) {
        var status = response.statusCode();
        if (status < LLMAdapterConstants.HTTP_OK_MIN || status > LLMAdapterConstants.HTTP_OK_MAX) {
            throw new HttpStatusException(status,
                    LLMAdapterConstants.HTTP_FAILURE.formatted(url, status, response.body()));
        }
        try {
            return mapper.readTree(response.body());
        } catch (JsonProcessingException notJson) {
            throw new LLMException(LLMAdapterConstants.NON_JSON_RESPONSE.formatted(url, status,
                    notJson.getOriginalMessage()));
        }
    }

    private static JsonNode at(JsonNode node, String... path) {
        var current = node;
        for (var segment : path) {
            current = current.path(segment);
        }
        return current;
    }
}
