package com.assignment.research.adapter.cli;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.adapter.output.MarkdownBriefingRenderer;
import com.assignment.research.adapter.output.RunArchive;
import com.assignment.research.adapter.web.ResearchRunRegistry;
import com.assignment.research.adapter.web.WebConstants;
import com.assignment.research.adapter.web.WebServer;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.bootstrap.PipelineFactory;
import com.assignment.research.pipeline.PipelineAbortedException;
import com.assignment.research.query.AnalystQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.Arrays;

public final class Main {

    private static final String QUERY_FLAG = "--query";
    private static final String SERVE_FLAG = "--serve";
    private static final String USAGE = """
            Usage: java -jar app/target/research-pipeline.jar --query "<analyst question>"
                   java -jar app/target/research-pipeline.jar --serve      (web UI on http://127.0.0.1:8787)

            Environment:
              LLM_PROVIDER        anthropic (default) or openai
              ANTHROPIC_API_KEY   key for the Anthropic Messages API
              ANTHROPIC_MODEL     default claude-sonnet-5-5
              OPENAI_API_KEY      key for the OpenAI-compatible endpoint (empty allowed for local servers)
              OPENAI_API_URL      default https://api.openai.com/v1/chat/completions
              OPENAI_MODEL        default gpt-5.4-mini
              DATA_DIR            default ./data (prompts and corpus)
              RUNS_DIR            default ./runs (one folder per run)
              PORT                web UI port for --serve, default 8787
            """;
    private static final String ABORTED = "Run aborted: the %s step failed and no briefing could be produced. Cause: %s";
    private static final int EXIT_USAGE = 2;
    private static final int EXIT_ABORTED = 1;

    private Main() {
    }

    public static void main(String[] args) {
        var clock = Clock.systemDefaultZone();
        var mapper = JSONMapperFactory.create();
        var config = AppConfig.fromEnvironment(System.getenv());
        if (Arrays.asList(args).contains(SERVE_FLAG)) {
            serve(config, mapper, clock);
            return;
        }
        var queryText = queryFrom(args);
        if (queryText == null) {
            System.err.println(USAGE);
            System.exit(EXIT_USAGE);
            return;
        }
        var archive = new RunArchive(config.getRunsDirectory(), clock, mapper, System.out);
        var useCase = new PipelineFactory(config, mapper, clock).createUseCase();
        try {
            var result = useCase.produce(new AnalystQuery(queryText), archive);
            archive.writeResult(result, new MarkdownBriefingRenderer(clock).render(result));
        } catch (PipelineAbortedException aborted) {
            System.err.println(ABORTED.formatted(aborted.getMessage(), aborted.getCause().getMessage()));
            System.exit(EXIT_ABORTED);
        }
    }

    private static void serve(AppConfig config, ObjectMapper mapper, Clock clock) {
        var useCase = new PipelineFactory(config, mapper, clock).createUseCase();
        var registry = new ResearchRunRegistry(useCase, config, mapper, clock, System.out);
        var port = Integer.parseInt(System.getenv().getOrDefault(WebConstants.ENV_PORT,
                String.valueOf(WebConstants.DEFAULT_PORT)));
        new WebServer(registry, mapper, port).start();
    }

    private static String queryFrom(String[] args) {
        var arguments = Arrays.asList(args);
        var index = arguments.indexOf(QUERY_FLAG);
        if (index < 0 || index + 1 >= arguments.size()) {
            return null;
        }
        return String.join(" ", arguments.subList(index + 1, arguments.size()));
    }
}
