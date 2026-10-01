package com.assignment.research.adapter.llm;

import java.time.Duration;

public final class LLMAdapterConstants {

    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(120);
    public static final int MAX_TRANSPORT_RETRIES = 2;
    public static final Duration RETRY_BACKOFF = Duration.ofSeconds(2);
    public static final int HTTP_OK_MIN = 200;
    public static final int HTTP_OK_MAX = 299;
    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final int HTTP_SERVER_ERROR_MIN = 500;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String REPAIR_INSTRUCTION = """

            ---
            Your previous reply could not be used because it was not the required JSON object.
            Parse error: %s
            Your previous reply was:
            %s
            Reply again with only the JSON object in the required shape. No prose, no markdown fences.
            """;
    public static final String MALFORMED_AFTER_REPAIR = "model returned malformed JSON twice: %s";
    public static final String MALFORMED_AFTER_TRUNCATION =
            "model output was truncated and still unusable after retrying with %d tokens: %s";
    public static final int TRUNCATION_BUDGET_FACTOR = 2;
    public static final String TRANSPORT_FAILURE = "LLM request to %s failed: %s";
    public static final String HTTP_FAILURE = "LLM provider at %s answered HTTP %d: %s";
    public static final String NON_JSON_RESPONSE = "LLM provider at %s answered HTTP %d with a body that is not JSON: %s";
    public static final String INTERRUPTED = "interrupted";
    public static final int MISSING_INT_VALUE = 0;
    public static final String MISSING_FIELD = "LLM provider response is missing field '%s'";
    public static final String CODE_FENCE = "```";
    public static final String EMPTY = "";
    public static final String JSON_START = "{";
    public static final String JSON_END = "}";
    public static final String NO_JSON_OBJECT = "no JSON object found in reply";
    public static final String PARSE_FAILED = "JSON did not match the expected shape: %s";
    public static final String PATH_SEPARATOR = ".";

    private LLMAdapterConstants() {
    }
}
