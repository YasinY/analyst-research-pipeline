package com.assignment.research.adapter.llm;

public interface ChatClient {

    ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens);
}
