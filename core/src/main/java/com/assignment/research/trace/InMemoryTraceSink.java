package com.assignment.research.trace;

import com.assignment.research.llm.LlmUsage;
import java.util.ArrayList;
import java.util.List;

public final class InMemoryTraceSink implements TraceSink {

    private final List<TraceEntry> entries = new ArrayList<>();

    @Override
    public synchronized void accept(TraceEntry entry) {
        entries.add(entry);
    }

    public synchronized List<TraceEntry> getEntries() {
        return List.copyOf(entries);
    }

    public synchronized LlmUsage getTotalUsage() {
        return entries.stream().map(TraceEntry::getUsage).reduce(LlmUsage.NONE, LlmUsage::plus);
    }
}
