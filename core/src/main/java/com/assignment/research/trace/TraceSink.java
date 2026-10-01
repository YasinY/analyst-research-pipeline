package com.assignment.research.trace;

public interface TraceSink {

    void accept(TraceEntry entry);
}
