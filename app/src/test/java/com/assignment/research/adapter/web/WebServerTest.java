package com.assignment.research.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.bootstrap.BootstrapConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WebServerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-20T10:15:30Z"), ZoneOffset.UTC);
    private static final int EPHEMERAL_PORT = 0;
    private static final String BASE_URL = "http://127.0.0.1:%d";
    private static final String RUN_PATH = "/researches/%s";
    private static final String CALL_PATH = "/researches/%s/calls/%s";
    private static final String OTHER_RUN_PATH = "/researches/%s/other/%d";
    private static final String CALLS_LIST_PATH = "/researches/%s/calls";
    private static final String UNKNOWN_PATH = "/unknown";
    private static final String RESEARCHES = "/researches";
    private static final String UNKNOWN_RUN = "run-404";
    private static final String UNKNOWN_SEQUENCE = "99";
    private static final String NON_NUMERIC_SEQUENCE = "abc";
    private static final String MISSING_RESOURCE = "web/missing.html";
    private static final String VALID_BODY = "{\"query\":\"dry bulk outlook\"}";
    private static final String BLANK_BODY = "{\"query\":\"   \"}";
    private static final String MALFORMED_BODY = "{\"query\":";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String FOREIGN_ORIGIN = "https://attacker.example";
    private static final String FOREIGN_HOST = "attacker.example";
    private static final String HTML_MARKER = "<html";
    private static final String FIRST_RUN_ID = "run-1";
    private static final String RAW_REQUEST = "GET / HTTP/1.1\r\nHost: %s\r\nConnection: close\r\n\r\n";
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_STATUS = "status";
    private static final String JSON_KEY_LABEL = "label";
    private static final String STATUS_LINE_PREFIX = "HTTP/1.1 %d";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = JSONMapperFactory.create();

    @TempDir
    private Path runsRoot;

    private WebServer server;
    private int port;

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void servesTheIndexPage() throws Exception {
        start(Runnable::run);

        var response = get(WebConstants.ROOT_PATH);

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_OK);
        assertThat(response.headers().firstValue(WebConstants.HEADER_CONTENT_TYPE))
                .contains(WebConstants.CONTENT_TYPE_HTML);
        assertThat(response.body()).contains(HTML_MARKER);
    }

    @Test
    void answersUnknownPathWithNotFound() throws Exception {
        start(Runnable::run);

        assertThat(get(UNKNOWN_PATH).statusCode()).isEqualTo(WebConstants.HTTP_NOT_FOUND);
    }

    @Test
    void answersMissingIndexResourceWithInternalError() throws Exception {
        server = new WebServer(registry(Runnable::run), mapper, EPHEMERAL_PORT,
                ResearchRunRegistryTest.silentConsole(), MISSING_RESOURCE);
        port = server.start();

        var response = get(WebConstants.ROOT_PATH);

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_INTERNAL_ERROR);
        assertThat(response.body()).contains(WebConstants.ERROR_INDEX_MISSING);
    }

    @Test
    void rejectsPostWithoutJsonContentType() throws Exception {
        start(Runnable::run);

        var response = post(RESEARCHES, VALID_BODY, TEXT_PLAIN);

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_FORBIDDEN);
    }

    @Test
    void rejectsForeignOrigin() throws Exception {
        start(Runnable::run);
        var request = HttpRequest.newBuilder(uri(WebConstants.ROOT_PATH))
                .header(WebConstants.HEADER_ORIGIN, FOREIGN_ORIGIN)
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_FORBIDDEN);
    }

    @Test
    void rejectsForeignHost() throws Exception {
        start(Runnable::run);

        assertThat(rawStatusLine(FOREIGN_HOST))
                .startsWith(STATUS_LINE_PREFIX.formatted(WebConstants.HTTP_FORBIDDEN));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        start(Runnable::run);

        var response = postJson(RESEARCHES, MALFORMED_BODY);

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_BAD_REQUEST);
        assertThat(response.body()).contains(WebConstants.ERROR_MALFORMED_JSON);
    }

    @Test
    void rejectsBlankQuery() throws Exception {
        start(Runnable::run);

        var response = postJson(RESEARCHES, BLANK_BODY);

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_BAD_REQUEST);
        assertThat(response.body()).contains(WebConstants.ERROR_EMPTY_QUERY);
    }

    @Test
    void startsRunAndServesItsStatusAndCalls() throws Exception {
        start(Runnable::run);

        var created = postJson(RESEARCHES, VALID_BODY);
        var runId = mapper.readTree(created.body()).path(JSON_KEY_ID).asText();
        var status = get(RUN_PATH.formatted(runId));
        var call = get(CALL_PATH.formatted(runId, BriefingFixtures.SECOND_SEQUENCE));

        assertThat(created.statusCode()).isEqualTo(WebConstants.HTTP_ACCEPTED);
        assertThat(runId).isEqualTo(FIRST_RUN_ID);
        assertThat(status.statusCode()).isEqualTo(WebConstants.HTTP_OK);
        assertThat(mapper.readTree(status.body()).path(JSON_KEY_STATUS).asText()).isEqualTo(RunStatus.FINISHED.name());
        assertThat(call.statusCode()).isEqualTo(WebConstants.HTTP_OK);
        assertThat(mapper.readTree(call.body()).path(JSON_KEY_LABEL).asText()).isEqualTo(BriefingFixtures.CRITIC_LABEL);
    }

    @Test
    void answersUnknownCallsWithNotFound() throws Exception {
        start(Runnable::run);
        postJson(RESEARCHES, VALID_BODY);

        assertThat(get(CALL_PATH.formatted(FIRST_RUN_ID, UNKNOWN_SEQUENCE)).statusCode())
                .isEqualTo(WebConstants.HTTP_NOT_FOUND);
        assertThat(get(CALL_PATH.formatted(FIRST_RUN_ID, NON_NUMERIC_SEQUENCE)).statusCode())
                .isEqualTo(WebConstants.HTTP_NOT_FOUND);
        assertThat(get(CALLS_LIST_PATH.formatted(FIRST_RUN_ID)).statusCode())
                .isEqualTo(WebConstants.HTTP_NOT_FOUND);
        assertThat(get(OTHER_RUN_PATH.formatted(FIRST_RUN_ID, BriefingFixtures.FIRST_SEQUENCE)).statusCode())
                .isEqualTo(WebConstants.HTTP_NOT_FOUND);
    }

    @Test
    void answersUnknownRunWithNotFound() throws Exception {
        start(Runnable::run);

        assertThat(get(RUN_PATH.formatted(UNKNOWN_RUN)).statusCode()).isEqualTo(WebConstants.HTTP_NOT_FOUND);
    }

    @Test
    void answersWrongMethodsWithAllowHeader() throws Exception {
        start(Runnable::run);

        var collection = get(RESEARCHES);
        var run = postJson(RUN_PATH.formatted(FIRST_RUN_ID), VALID_BODY);

        assertThat(collection.statusCode()).isEqualTo(WebConstants.HTTP_METHOD_NOT_ALLOWED);
        assertThat(collection.headers().firstValue(WebConstants.HEADER_ALLOW)).contains(WebConstants.METHOD_POST);
        assertThat(run.statusCode()).isEqualTo(WebConstants.HTTP_METHOD_NOT_ALLOWED);
        assertThat(run.headers().firstValue(WebConstants.HEADER_ALLOW)).contains(WebConstants.METHOD_GET);
    }

    @Test
    void answersFailingHandlerWithInternalError() throws Exception {
        start(task -> {
            throw new RejectedExecutionException();
        });

        var response = postJson(RESEARCHES, VALID_BODY);

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_INTERNAL_ERROR);
        assertThat(response.body()).contains(WebConstants.ERROR_INTERNAL);
    }

    @Test
    void failsToStartOnOccupiedPort() {
        start(Runnable::run);
        var competitor = new WebServer(registry(Runnable::run), mapper, port, ResearchRunRegistryTest.silentConsole());

        assertThatThrownBy(competitor::start).isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void stopIsIdempotent() {
        start(Runnable::run);

        server.stop();
        server.stop();

        assertThat(server).isNotNull();
    }

    private void start(Executor executor) {
        server = new WebServer(registry(executor), mapper, EPHEMERAL_PORT, ResearchRunRegistryTest.silentConsole());
        port = server.start();
    }

    private ResearchRunRegistry registry(Executor executor) {
        var config = AppConfig.fromEnvironment(Map.of(BootstrapConstants.ENV_RUNS_DIR, runsRoot.toString()));
        return new ResearchRunRegistry(new ObservingBriefingUseCase(), config, mapper, FIXED_CLOCK,
                ResearchRunRegistryTest.silentConsole(), executor);
    }

    private URI uri(String path) {
        return URI.create(BASE_URL.formatted(port) + path);
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String body) throws IOException, InterruptedException {
        return post(path, body, WebConstants.MEDIA_TYPE_JSON);
    }

    private HttpResponse<String> post(String path, String body, String contentType)
            throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(uri(path))
                .header(WebConstants.HEADER_CONTENT_TYPE, contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String rawStatusLine(String host) throws IOException {
        try (var socket = new Socket(WebConstants.BIND_HOST, port)) {
            socket.getOutputStream().write(RAW_REQUEST.formatted(host).getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            return new String(socket.getInputStream().readAllBytes(), StandardCharsets.US_ASCII);
        }
    }
}
