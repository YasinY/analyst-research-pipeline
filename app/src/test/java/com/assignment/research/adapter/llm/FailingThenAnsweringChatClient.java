package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LLMException;
import com.assignment.research.llm.LLMUsage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

final class FailingThenAnsweringChatClient implements ChatClient {

    static final String ANSWER = "answer";
    private static final String MODEL = "failing-then-answering";
    private static final LLMUsage USAGE = new LLMUsage(1, 1);

    private final Deque<LLMException> failures;
    private int calls;

    FailingThenAnsweringChatClient(List<LLMException> failuresInOrder) {
        this.failures = new ArrayDeque<>(failuresInOrder);
    }

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        calls++;
        var failure = failures.poll();
        if (failure != null) {
            throw failure;
        }
        return new ChatReply(ANSWER, MODEL, USAGE, false);
    }

    int getCalls() {
        return calls;
    }
}
