package com.assignment.research.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.adapter.llm.anthropic.AnthropicConstants;
import com.assignment.research.adapter.llm.openai.OpenAiConstants;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.bootstrap.BootstrapConstants;
import com.assignment.research.bootstrap.LLMProvider;
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
import java.util.concurrent.atomic.AtomicReference;
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
    private static final String VALID_BODY = "{\"query\":\"dry bulk outlook\",\"provider\":\"local\"}";
    private static final String KEYLESS_BODY = "{\"query\":\"dry bulk outlook\",\"provider\":\"anthropic\"}";
    private static final String DEFAULT_PROVIDER_KEYLESS_BODY = "{\"query\":\"dry bulk outlook\"}";
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
    private static final String SERVER_KEY = "server-anthropic-key";
    private static final String RUN_KEY = "sk-run-only-secret";
    private static final String RUN_MODEL = "gpt-run-model";
    private static final String RUN_URL = "http://127.0.0.1:9/v1/chat/completions";
    private static final String OVERRIDE_BODY = """
            {"query":"dry bulk outlook","provider":" OpenAI ","model":"gpt-run-model",\
            "apiKey":"sk-run-only-secret","apiUrl":"http://127.0.0.1:9/v1/chat/completions"}""";
    private static final String UNKNOWN_PROVIDER_BODY = """
            {"query":"dry bulk outlook","provider":"gemini"}""";
    private static final String CONFIG_SUFFIX_PATH = "/configuration";
    private static final String JSON_KEY_PROVIDER = "provider";
    private static final String JSON_KEY_MODEL = "model";
    private static final String JSON_KEY_MODELS = "models";
    private static final String JSON_KEY_API_URLS = "apiUrls";
    private static final String JSON_KEY_ANTHROPIC_KEY_PRESENT = "anthropicKeyPresent";
    private static final String JSON_KEY_OPENAI_KEY_PRESENT = "openaiKeyPresent";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = JSONMapperFactory.create();

    @TempDir
    private Path runsRoot;

    private final AtomicReference<AppConfig> receivedConfig = new AtomicReference<>();

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
    void postWithoutApiKeyForACloudProviderAnswers400() throws Exception {
        start(Runnable::run);

        var explicit = postJson(RESEARCHES, KEYLESS_BODY);
        var implicit = postJson(RESEARCHES, DEFAULT_PROVIDER_KEYLESS_BODY);

        assertThat(explicit.statusCode()).isEqualTo(WebConstants.HTTP_BAD_REQUEST);
        assertThat(explicit.body()).contains(WebConstants.ERROR_MISSING_API_KEY);
        assertThat(implicit.statusCode()).isEqualTo(WebConstants.HTTP_BAD_REQUEST);
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
    void servesTheDefaultsWithoutKeyValues() throws Exception {
        start(Runnable::run);

        var response = get(WebConstants.CONFIG_PATH);
        var body = mapper.readTree(response.body());

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_OK);
        assertThat(body.path(JSON_KEY_PROVIDER).asText()).isEqualTo(LLMProvider.ANTHROPIC.getWireName());
        assertThat(body.path(JSON_KEY_MODELS).path(LLMProvider.OPENAI.getWireName()).asText())
                .isEqualTo(OpenAiConstants.DEFAULT_MODEL);
        assertThat(body.path(JSON_KEY_MODELS).path(LLMProvider.ANTHROPIC.getWireName()).asText())
                .isEqualTo(AnthropicConstants.DEFAULT_MODEL);
        assertThat(body.path(JSON_KEY_API_URLS).path(LLMProvider.LOCAL.getWireName()).asText())
                .isEqualTo(BootstrapConstants.LOCAL_DEFAULT_URL);
        assertThat(body.path(JSON_KEY_ANTHROPIC_KEY_PRESENT).asBoolean()).isTrue();
        assertThat(body.path(JSON_KEY_OPENAI_KEY_PRESENT).asBoolean()).isFalse();
        assertThat(response.body()).doesNotContain(SERVER_KEY);
    }

    @Test
    void answersConfigMisuseWithErrors() throws Exception {
        start(Runnable::run);

        var post = postJson(WebConstants.CONFIG_PATH, VALID_BODY);
        var unknown = get(CONFIG_SUFFIX_PATH);

        assertThat(post.statusCode()).isEqualTo(WebConstants.HTTP_METHOD_NOT_ALLOWED);
        assertThat(post.headers().firstValue(WebConstants.HEADER_ALLOW)).contains(WebConstants.METHOD_GET);
        assertThat(unknown.statusCode()).isEqualTo(WebConstants.HTTP_NOT_FOUND);
    }

    @Test
    void runsWithPerRunProviderOverridesAndNeverEchoesTheKey() throws Exception {
        start(Runnable::run);

        var created = postJson(RESEARCHES, OVERRIDE_BODY);
        var runId = mapper.readTree(created.body()).path(JSON_KEY_ID).asText();
        var status = get(RUN_PATH.formatted(runId));
        var call = get(CALL_PATH.formatted(runId, BriefingFixtures.FIRST_SEQUENCE));
        var config = get(WebConstants.CONFIG_PATH);
        var runConfig = receivedConfig.get();

        assertThat(created.statusCode()).isEqualTo(WebConstants.HTTP_ACCEPTED);
        assertThat(mapper.readTree(status.body()).path(JSON_KEY_PROVIDER).asText())
                .isEqualTo(LLMProvider.OPENAI.getWireName());
        assertThat(mapper.readTree(status.body()).path(JSON_KEY_MODEL).asText()).isEqualTo(RUN_MODEL);
        assertThat(runConfig.getProvider()).isEqualTo(LLMProvider.OPENAI);
        assertThat(runConfig.getModel()).isEqualTo(RUN_MODEL);
        assertThat(runConfig.getApiKey()).isEqualTo(RUN_KEY);
        assertThat(runConfig.getApiUrl()).isEqualTo(RUN_URL);
        assertThat(runConfig.getRunsDirectory()).isEqualTo(runsRoot);
        assertThat(runConfig.toString()).doesNotContain(RUN_KEY);
        assertThat(created.body()).doesNotContain(RUN_KEY);
        assertThat(status.body()).doesNotContain(RUN_KEY);
        assertThat(call.body()).doesNotContain(RUN_KEY);
        assertThat(config.body()).doesNotContain(RUN_KEY);
    }

    @Test
    void rejectsUnknownProvider() throws Exception {
        start(Runnable::run);

        var response = postJson(RESEARCHES, UNKNOWN_PROVIDER_BODY);

        assertThat(response.statusCode()).isEqualTo(WebConstants.HTTP_BAD_REQUEST);
        assertThat(response.body()).contains(WebConstants.ERROR_UNKNOWN_PROVIDER);
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
        var config = AppConfig.fromEnvironment(Map.of(
                BootstrapConstants.ENV_RUNS_DIR, runsRoot.toString(),
                AnthropicConstants.ENV_API_KEY, SERVER_KEY));
        return new ResearchRunRegistry(this::recordingUseCase, config, BriefingFixtures.costEstimator(), mapper,
                FIXED_CLOCK, ResearchRunRegistryTest.silentConsole(), executor);
    }

    private ObservingBriefingUseCase recordingUseCase(AppConfig runConfig) {
        receivedConfig.set(runConfig);
        return new ObservingBriefingUseCase();
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
