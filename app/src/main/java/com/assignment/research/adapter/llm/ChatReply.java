package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LlmUsage;
import lombok.NonNull;
import lombok.Value;

@Value
public class ChatReply {

    @NonNull
    private final String text;
    @NonNull
    private final String model;
    @NonNull
    private final LlmUsage usage;
}
