package com.assignment.research.adapter.llm.anthropic;

import com.assignment.research.adapter.llm.ChatClient;
import com.assignment.research.adapter.llm.ChatReply;
import com.assignment.research.adapter.llm.HttpJsonPoster;
import com.assignment.research.llm.LlmUsage;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AnthropicChatClient implements ChatClient {

    private final HttpJsonPoster poster;
    private final String url;
    private final String apiKey;
    private final String model;

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        var body = Map.of(
                AnthropicConstants.FIELD_MODEL, model,
                AnthropicConstants.FIELD_MAX_TOKENS, maxOutputTokens,
                AnthropicConstants.FIELD_SYSTEM, systemPrompt,
                AnthropicConstants.FIELD_MESSAGES, List.of(Map.of(
                        AnthropicConstants.FIELD_ROLE, AnthropicConstants.ROLE_USER,
                        AnthropicConstants.FIELD_CONTENT, userPrompt)));
        var headers = Map.of(
                AnthropicConstants.HEADER_API_KEY, apiKey,
                AnthropicConstants.HEADER_VERSION, AnthropicConstants.API_VERSION);

        var response = poster.post(url, headers, body);
        var firstBlock = response.path(AnthropicConstants.FIELD_CONTENT).path(AnthropicConstants.FIRST_CONTENT_BLOCK);
        var text = HttpJsonPoster.requiredText(firstBlock, AnthropicConstants.FIELD_TEXT);
        var usage = new LlmUsage(
                HttpJsonPoster.intOrZero(response, AnthropicConstants.FIELD_USAGE,
                        AnthropicConstants.FIELD_INPUT_TOKENS),
                HttpJsonPoster.intOrZero(response, AnthropicConstants.FIELD_USAGE,
                        AnthropicConstants.FIELD_OUTPUT_TOKENS));
        var reportedModel = response.path(AnthropicConstants.FIELD_MODEL).asText(model);
        return new ChatReply(text, reportedModel, usage);
    }
}
