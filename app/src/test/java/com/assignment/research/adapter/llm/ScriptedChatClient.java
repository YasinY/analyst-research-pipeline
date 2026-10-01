package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LlmUsage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

final class ScriptedChatClient implements ChatClient {

    private static final String MODEL = "scripted";
    private static final LlmUsage USAGE = new LlmUsage(10, 5);

    private final Deque<String> replies = new ArrayDeque<>();
    private final List<String> userPrompts = new ArrayList<>();

    ScriptedChatClient(String... repliesInOrder) {
        replies.addAll(List.of(repliesInOrder));
    }

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        userPrompts.add(userPrompt);
        return new ChatReply(replies.poll(), MODEL, USAGE);
    }

    List<String> getUserPrompts() {
        return List.copyOf(userPrompts);
    }
}
