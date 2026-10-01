package com.assignment.research.adapter.web;

public final class WebConstants {

    public static final String ENV_PORT = "PORT";
    public static final int DEFAULT_PORT = 8787;
    public static final int BACKLOG = 16;
    public static final String BIND_HOST = "127.0.0.1";

    public static final String ROOT_PATH = "/";
    public static final String RESEARCHES_PATH = "/researches";
    public static final String CALLS_SEGMENT = "calls";
    public static final String INDEX_RESOURCE = "web/index.html";

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_HTML = "text/html; charset=utf-8";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final int HTTP_OK = 200;
    public static final int HTTP_ACCEPTED = 202;
    public static final int HTTP_BAD_REQUEST = 400;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_METHOD_NOT_ALLOWED = 405;
    public static final String PATH_SEPARATOR = "/";

    public static final String ERROR_NOT_FOUND = "not found";
    public static final String ERROR_METHOD = "method not allowed";
    public static final String ERROR_EMPTY_QUERY = "query must not be blank";
    public static final String RUN_ID_FORMAT = "run-%d";
    public static final String STARTED_MESSAGE = "Analyst Research Pipeline listening on http://%s:%d";

    private WebConstants() {
    }
}
