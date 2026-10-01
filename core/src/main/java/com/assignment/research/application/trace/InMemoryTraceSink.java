package com.assignment.research.application.trace;

import com.assignment.research.application.port.TraceSink;
import com.assignment.research.domain.LlmUsage;
import com.assignment.research.domain.TraceEntry;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryTraceSink implements TraceSink {

    private final List<TraceEntry> entries = new ArrayList<>();

    @Override
    public synchronized void accept(TraceEntry entry) {
        entries.add(entry);
    }

    public synchronized List<TraceEntry> entries() {
        return List.copyOf(entries);
    }

    public synchronized LlmUsage totalUsage() {
        return entries.stream().map(TraceEntry::usage).reduce(LlmUsage.NONE, LlmUsage::plus);
    }
}
