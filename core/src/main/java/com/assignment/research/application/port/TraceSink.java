package com.assignment.research.application.port;

import com.assignment.research.domain.TraceEntry;

public interface TraceSink {

    void accept(TraceEntry entry);
}
