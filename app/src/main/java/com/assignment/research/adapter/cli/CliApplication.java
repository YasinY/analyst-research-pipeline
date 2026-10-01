package com.assignment.research.adapter.cli;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.adapter.output.MarkdownBriefingRenderer;
import com.assignment.research.adapter.output.RunArchive;
import com.assignment.research.adapter.web.ResearchRunRegistry;
import com.assignment.research.adapter.web.WebConstants;
import com.assignment.research.adapter.web.WebServer;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.bootstrap.BootstrapConstants;
import com.assignment.research.bootstrap.PipelineFactory;
import com.assignment.research.pipeline.PipelineAbortedException;
import com.assignment.research.pipeline.ProduceBriefingUseCase;
import com.assignment.research.query.AnalystQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintStream;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CliApplication implements AutoCloseable {

    private final Clock clock;
    private final ObjectMapper mapper;
    private final Function<AppConfig, ProduceBriefingUseCase> useCaseFactory;
    private final AtomicReference<WebServer> server = new AtomicReference<>();

    public static CliApplication standard() {
        var clock = Clock.systemDefaultZone();
        var mapper = JSONMapperFactory.create();
        return new CliApplication(clock, mapper, config -> new PipelineFactory(config, mapper, clock).createUseCase());
    }

    public int run(List<String> arguments, Map<String, String> env, PrintStream out, PrintStream err) {
        var config = AppConfig.fromEnvironment(env);
        if (arguments.contains(CliConstants.SERVE_FLAG)) {
            serve(config, out);
            return CliConstants.EXIT_OK;
        }
        var queryText = queryFrom(arguments);
        if (queryText.isEmpty()) {
            err.println(CliConstants.USAGE.formatted(WebConstants.BIND_HOST, WebConstants.DEFAULT_PORT));
            return CliConstants.EXIT_USAGE;
        }
        return research(config, queryText.get(), out, err);
    }

    @Override
    public void close() {
        Optional.ofNullable(server.getAndSet(null)).ifPresent(WebServer::stop);
    }

    private int research(AppConfig config, String queryText, PrintStream out, PrintStream err) {
        var archive = new RunArchive(config.getRunsDirectory(), clock, mapper, out);
        var useCase = useCaseFactory.apply(config);
        try {
            var result = useCase.produce(new AnalystQuery(queryText), archive);
            archive.writeResult(result, new MarkdownBriefingRenderer(clock).render(result));
            return CliConstants.EXIT_OK;
        } catch (PipelineAbortedException aborted) {
            err.println(BootstrapConstants.ABORTED_MESSAGE.formatted(aborted.getMessage(),
                    aborted.getCause().getMessage()));
            return CliConstants.EXIT_ABORTED;
        }
    }

    private void serve(AppConfig config, PrintStream out) {
        var registry = new ResearchRunRegistry(useCaseFactory.apply(config), config, mapper, clock, out);
        var webServer = new WebServer(registry, mapper, config.getPort(), out);
        webServer.start();
        server.set(webServer);
    }

    private static Optional<String> queryFrom(List<String> arguments) {
        var index = arguments.indexOf(CliConstants.QUERY_FLAG);
        if (index < 0 || index + 1 >= arguments.size()) {
            return Optional.empty();
        }
        return Optional.of(String.join(CliConstants.QUERY_WORD_SEPARATOR,
                arguments.subList(index + 1, arguments.size())));
    }
}
