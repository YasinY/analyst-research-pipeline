package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LLMUsage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

final class ScriptedChatClient implements ChatClient {

    private static final String MODEL = "scripted";
    private static final LLMUsage USAGE = new LLMUsage(10, 5);

    private final Deque<String> replies = new ArrayDeque<>();
    private final List<String> userPrompts = new ArrayList<>();
    private final List<Integer> budgets = new ArrayList<>();
    private final boolean firstReplyTruncated;

    ScriptedChatClient(String... repliesInOrder) {
        this(false, repliesInOrder);
    }

    ScriptedChatClient(boolean firstReplyTruncated, String... repliesInOrder) {
        this.firstReplyTruncated = firstReplyTruncated;
        replies.addAll(List.of(repliesInOrder));
    }

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        userPrompts.add(userPrompt);
        budgets.add(maxOutputTokens);
        var truncated = firstReplyTruncated && userPrompts.size() == 1;
        return new ChatReply(replies.poll(), MODEL, USAGE, truncated);
    }

    List<Integer> getBudgets() {
        return List.copyOf(budgets);
    }

    List<String> getUserPrompts() {
        return List.copyOf(userPrompts);
    }
}
