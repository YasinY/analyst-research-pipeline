package com.assignment.research.adapter.web;

import com.assignment.research.adapter.output.MarkdownBriefingRenderer;
import com.assignment.research.adapter.output.RunArchive;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.pipeline.ProduceBriefingUseCase;
import com.assignment.research.query.AnalystQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintStream;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ResearchRunRegistry {

    private static final long FIRST_RUN_NUMBER = 1;

    private final ProduceBriefingUseCase useCase;
    private final AppConfig config;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final PrintStream console;
    private final Map<String, ResearchRun> runs = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(FIRST_RUN_NUMBER);
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ResearchRun start(String queryText) {
        var run = new ResearchRun(WebConstants.RUN_ID_FORMAT.formatted(counter.getAndIncrement()), queryText);
        runs.put(run.getId(), run);
        executor.submit(() -> execute(run));
        return run;
    }

    public Optional<ResearchRun> find(String id) {
        return Optional.ofNullable(runs.get(id));
    }

    private void execute(ResearchRun run) {
        try {
            var archive = new RunArchive(config.getRunsDirectory(), clock, mapper, console);
            var result = useCase.produce(new AnalystQuery(run.getQuery()), new CompositeObserver(archive, run));
            var markdown = new MarkdownBriefingRenderer(clock).render(result);
            var directory = archive.writeResult(result, markdown);
            run.finish(result, markdown, directory.toAbsolutePath().toString());
        } catch (RuntimeException failure) {
            run.fail(failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage());
        }
    }
}
