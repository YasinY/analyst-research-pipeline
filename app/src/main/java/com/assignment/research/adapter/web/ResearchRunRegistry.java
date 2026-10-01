package com.assignment.research.adapter.web;

import com.assignment.research.adapter.output.MarkdownBriefingRenderer;
import com.assignment.research.adapter.output.RunArchive;
import com.assignment.research.bootstrap.AppConfig;
import com.assignment.research.bootstrap.BootstrapConstants;
import com.assignment.research.pipeline.PipelineAbortedException;
import com.assignment.research.pipeline.ProduceBriefingUseCase;
import com.assignment.research.query.AnalystQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.PrintStream;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
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
    private final Executor executor;
    private final Map<String, ResearchRun> runs = new ConcurrentHashMap<>();
    private final Queue<String> finishedRunIds = new ConcurrentLinkedQueue<>();
    private final AtomicLong counter = new AtomicLong(FIRST_RUN_NUMBER);

    public ResearchRunRegistry(ProduceBriefingUseCase useCase, AppConfig config, ObjectMapper mapper, Clock clock,
            PrintStream console) {
        this(useCase, config, mapper, clock, console, Executors.newVirtualThreadPerTaskExecutor());
    }

    public ResearchRun start(String queryText) {
        var run = new ResearchRun(WebConstants.RUN_ID_FORMAT.formatted(counter.getAndIncrement()), queryText);
        runs.put(run.getId(), run);
        executor.execute(() -> execute(run));
        return run;
    }

    public Optional<ResearchRun> find(String id) {
        return Optional.ofNullable(runs.get(id));
    }

    private void execute(ResearchRun run) {
        run.complete(produceOutcome(run));
        retire(run);
    }

    private RunOutcome produceOutcome(ResearchRun run) {
        try {
            var archive = new RunArchive(config.getRunsDirectory(), clock, mapper, console);
            var result = useCase.produce(new AnalystQuery(run.getQuery()), new CompositeObserver(archive, run));
            var markdown = new MarkdownBriefingRenderer(clock).render(result);
            var directory = archive.writeResult(result, markdown);
            return RunOutcome.finished(result, markdown, directory.toAbsolutePath().toString());
        } catch (Throwable failure) {
            return RunOutcome.failed(describeFailure(failure));
        }
    }

    private void retire(ResearchRun run) {
        finishedRunIds.add(run.getId());
        while (finishedRunIds.size() > WebConstants.MAX_FINISHED_RUNS) {
            Optional.ofNullable(finishedRunIds.poll()).ifPresent(runs::remove);
        }
    }

    private static String describeFailure(Throwable failure) {
        if (!(failure instanceof PipelineAbortedException aborted)) {
            return describe(failure);
        }
        var cause = aborted.getCause() == null ? aborted : aborted.getCause();
        return BootstrapConstants.ABORTED_MESSAGE.formatted(aborted.getMessage(), describe(cause));
    }

    private static String describe(Throwable failure) {
        return failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
    }
}
