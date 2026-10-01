package com.assignment.research.adapter.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.adapter.output.OutputConstants;
import com.assignment.research.adapter.web.BriefingFixtures;
import com.assignment.research.adapter.web.ObservingBriefingUseCase;
import com.assignment.research.adapter.web.WebConstants;
import com.assignment.research.bootstrap.BootstrapConstants;
import com.assignment.research.pipeline.PipelineAbortedException;
import com.assignment.research.pipeline.ProduceBriefingUseCase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CliApplicationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-20T10:15:30Z"), ZoneOffset.UTC);
    private static final Path DATA = Path.of(System.getProperty("basedir", "app")).resolveSibling("data");
    private static final String STEP = "plan";
    private static final String CAUSE = "model unreachable";
    private static final String USAGE_MARKER = "Usage:";
    private static final String QUERY_WORD = "dry";
    private static final String SECOND_QUERY_WORD = "bulk";
    private static final String LISTENING_MARKER = "listening on";
    private static final int EPHEMERAL_PORT = 0;

    private final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(outBuffer, true, StandardCharsets.UTF_8);
    private final PrintStream err = new PrintStream(errBuffer, true, StandardCharsets.UTF_8);

    @TempDir
    private Path runsRoot;

    private CliApplication application;

    @AfterEach
    void closeApplication() {
        application.close();
    }

    @Test
    void printsUsageWithoutArguments() {
        application = application(new ObservingBriefingUseCase());

        var exitCode = application.run(List.of(), env(), out, err);

        assertThat(exitCode).isEqualTo(CliConstants.EXIT_USAGE);
        assertThat(err()).contains(USAGE_MARKER);
    }

    @Test
    void printsUsageWhenQueryFlagHasNoText() {
        application = application(new ObservingBriefingUseCase());

        var exitCode = application.run(List.of(CliConstants.QUERY_FLAG), env(), out, err);

        assertThat(exitCode).isEqualTo(CliConstants.EXIT_USAGE);
        assertThat(err()).contains(USAGE_MARKER);
    }

    @Test
    void writesTheBriefingForAQuery() throws IOException {
        application = application(new ObservingBriefingUseCase());

        var exitCode = application.run(List.of(CliConstants.QUERY_FLAG, QUERY_WORD, SECOND_QUERY_WORD), env(), out,
                err);

        assertThat(exitCode).isEqualTo(CliConstants.EXIT_OK);
        assertThat(singleRunDirectory().resolve(OutputConstants.BRIEFING_FILE)).content()
                .contains(BriefingFixtures.SUMMARY);
        assertThat(err()).isEmpty();
    }

    @Test
    void reportsAbortedRunWithItsCause() {
        ProduceBriefingUseCase aborting = (query, observer) -> {
            throw new PipelineAbortedException(STEP, new IllegalStateException(CAUSE));
        };
        application = application(aborting);

        var exitCode = application.run(List.of(CliConstants.QUERY_FLAG, QUERY_WORD), env(), out, err);

        assertThat(exitCode).isEqualTo(CliConstants.EXIT_ABORTED);
        assertThat(err()).contains(BootstrapConstants.ABORTED_MESSAGE.formatted(STEP, CAUSE));
    }

    @Test
    void servesTheWebUiWithTheStandardPipeline() throws IOException {
        application = CliApplication.standard();
        var env = Map.of(
                BootstrapConstants.ENV_DATA_DIR, DATA.toString(),
                BootstrapConstants.ENV_RUNS_DIR, runsRoot.toString(),
                BootstrapConstants.ENV_PORT, String.valueOf(freePort()));

        var exitCode = application.run(List.of(CliConstants.SERVE_FLAG), env, out, err);

        assertThat(exitCode).isEqualTo(CliConstants.EXIT_OK);
        assertThat(outBuffer.toString(StandardCharsets.UTF_8)).contains(LISTENING_MARKER)
                .contains(WebConstants.BIND_HOST);
    }

    private CliApplication application(ProduceBriefingUseCase useCase) {
        return new CliApplication(FIXED_CLOCK, JSONMapperFactory.create(), config -> useCase);
    }

    private Map<String, String> env() {
        return Map.of(BootstrapConstants.ENV_RUNS_DIR, runsRoot.toString());
    }

    private String err() {
        return errBuffer.toString(StandardCharsets.UTF_8);
    }

    private Path singleRunDirectory() throws IOException {
        try (Stream<Path> folders = Files.list(runsRoot)) {
            return folders.findFirst().orElseThrow();
        }
    }

    private static int freePort() throws IOException {
        try (var socket = new ServerSocket(EPHEMERAL_PORT)) {
            return socket.getLocalPort();
        }
    }
}
