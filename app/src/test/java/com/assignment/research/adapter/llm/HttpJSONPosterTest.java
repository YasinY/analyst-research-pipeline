package com.assignment.research.adapter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.llm.LLMException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpJSONPosterTest {

    private static final String PATH = "/v1/chat";
    private static final String URL_FORMAT = "http://127.0.0.1:%d%s";
    private static final String HTML_BODY = "<html>gateway maintenance</html>";
    private static final int HTTP_OK = 200;
    private static final int ANY_FREE_PORT = 0;
    private static final int DEFAULT_BACKLOG = 0;
    private static final int IMMEDIATE_STOP = 0;

    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), ANY_FREE_PORT),
                DEFAULT_BACKLOG);
        server.createContext(PATH, HttpJSONPosterTest::answerWithHtml);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(IMMEDIATE_STOP);
    }

    @Test
    void successfulStatusWithNonJSONBodyFailsWithoutARetryableTransportCause() {
        var poster = new HttpJSONPoster(JSONMapperFactory.create());
        var url = URL_FORMAT.formatted(server.getAddress().getPort(), PATH);

        var failure = catchThrowableOfType(LLMException.class, () -> poster.post(url, Map.of(), Map.of()));

        assertThat(failure).hasMessageContaining("not JSON").hasMessageContaining(url);
        assertThat(failure.getCause()).isNull();
    }

    private static void answerWithHtml(HttpExchange exchange) throws IOException {
        var body = HTML_BODY.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(HTTP_OK, body.length);
        try (var response = exchange.getResponseBody()) {
            response.write(body);
        }
    }
}
