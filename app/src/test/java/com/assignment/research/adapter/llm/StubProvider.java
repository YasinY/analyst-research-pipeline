package com.assignment.research.adapter.llm;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public final class StubProvider implements AutoCloseable {

    private static final String PATH = "/v1/stub";
    private static final String URL_FORMAT = "http://127.0.0.1:%d%s";
    private static final int ANY_FREE_PORT = 0;
    private static final int DEFAULT_BACKLOG = 0;
    private static final int IMMEDIATE_STOP = 0;

    private final HttpServer server;
    private final RecordingHandler handler;

    private StubProvider(HttpServer server, RecordingHandler handler) {
        this.server = server;
        this.handler = handler;
    }

    public static StubProvider answering(int status, String body) {
        try {
            var server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), ANY_FREE_PORT),
                    DEFAULT_BACKLOG);
            var handler = new RecordingHandler(status, body);
            server.createContext(PATH, handler);
            server.start();
            return new StubProvider(server, handler);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public String url() {
        return URL_FORMAT.formatted(server.getAddress().getPort(), PATH);
    }

    public RecordingHandler handler() {
        return handler;
    }

    @Override
    public void close() {
        server.stop(IMMEDIATE_STOP);
    }
}
