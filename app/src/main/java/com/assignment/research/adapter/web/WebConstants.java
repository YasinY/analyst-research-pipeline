package com.assignment.research.adapter.web;

public final class WebConstants {

    public static final int DEFAULT_PORT = 8787;
    public static final int BACKLOG = 16;
    public static final int STOP_DELAY_SECONDS = 0;
    public static final String BIND_HOST = "127.0.0.1";
    public static final String LOCALHOST = "localhost";
    public static final String HOST_FORMAT = "%s:%d";
    public static final String ORIGIN_FORMAT = "http://%s:%d";

    public static final String ROOT_PATH = "/";
    public static final String RESEARCHES_PATH = "/researches";
    public static final String CONFIG_PATH = "/config";
    public static final String CALLS_SEGMENT = "calls";
    public static final String INDEX_RESOURCE = "web/index.html";
    public static final String PATH_SEPARATOR = "/";
    public static final int COLLECTION_SEGMENT_COUNT = 1;
    public static final int RUN_SEGMENT_COUNT = 2;
    public static final int CALL_SEGMENT_COUNT = 4;
    public static final int RUN_ID_SEGMENT_INDEX = 1;
    public static final int CALLS_SEGMENT_INDEX = 2;
    public static final int SEQUENCE_SEGMENT_INDEX = 3;

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_ORIGIN = "Origin";
    public static final String HEADER_ALLOW = "Allow";
    public static final String MEDIA_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_HTML = "text/html; charset=utf-8";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final int HTTP_OK = 200;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_METHOD_NOT_ALLOWED = 405;
    public static final int HTTP_INTERNAL_ERROR = 500;

    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_QUERY = "query";
    public static final String JSON_KEY_PROVIDER = "provider";
    public static final String JSON_KEY_MODEL = "model";
    public static final String JSON_KEY_API_KEY = "apiKey";
    public static final String JSON_KEY_API_URL = "apiUrl";
    public static final String EMPTY_TEXT = "";

    public static final String ERROR_NOT_FOUND = "not found";
    public static final String ERROR_METHOD = "method not allowed";
    public static final String ERROR_FORBIDDEN = "forbidden";
    public static final String ERROR_EMPTY_QUERY = "query must not be blank";
    public static final String ERROR_UNKNOWN_PROVIDER = "provider must be anthropic, openai or local";
    public static final String ERROR_MALFORMED_JSON = "request body is not valid JSON";
    public static final String ERROR_INDEX_MISSING = "web page resource is missing";
    public static final String ERROR_INTERNAL = "internal server error";
    public static final String RUN_ID_FORMAT = "run-%d";
    public static final String STARTED_MESSAGE = "Analyst Research Pipeline listening on http://%s:%d";
    public static final int MAX_FINISHED_RUNS = 20;

    private WebConstants() {
    }
}
