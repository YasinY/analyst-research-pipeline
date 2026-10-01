package com.assignment.research.adapter.llm.anthropic;

import com.assignment.research.adapter.llm.ChatClient;
import com.assignment.research.adapter.llm.ChatReply;
import com.assignment.research.adapter.llm.HttpJSONPoster;
import com.assignment.research.llm.LLMException;
import com.assignment.research.llm.LLMUsage;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AnthropicChatClient implements ChatClient {

    private final HttpJSONPoster poster;
    private final String url;
    private final String apiKey;
    private final String model;

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        var body = Map.of(
                AnthropicConstants.FIELD_MODEL, model,
                AnthropicConstants.FIELD_MAX_TOKENS, maxOutputTokens,
                AnthropicConstants.FIELD_SYSTEM, systemPrompt,
                AnthropicConstants.FIELD_THINKING, Map.of(AnthropicConstants.FIELD_TYPE,
                        AnthropicConstants.THINKING_BETWEEN_TOOLS_ONLY),
                AnthropicConstants.FIELD_MESSAGES, List.of(Map.of(
                        AnthropicConstants.FIELD_ROLE, AnthropicConstants.ROLE_USER,
                        AnthropicConstants.FIELD_CONTENT, userPrompt)));
        var headers = Map.of(
                AnthropicConstants.HEADER_API_KEY, apiKey,
                AnthropicConstants.HEADER_VERSION, AnthropicConstants.API_VERSION);

        var response = poster.post(url, headers, body);
        var text = extractText(response);
        var usage = new LLMUsage(
                HttpJSONPoster.intOrZero(response, AnthropicConstants.FIELD_USAGE,
                        AnthropicConstants.FIELD_INPUT_TOKENS),
                HttpJSONPoster.intOrZero(response, AnthropicConstants.FIELD_USAGE,
                        AnthropicConstants.FIELD_OUTPUT_TOKENS));
        var reportedModel = response.path(AnthropicConstants.FIELD_MODEL).asText(model);
        var truncated = AnthropicConstants.STOP_REASON_MAX_TOKENS.equals(
                response.path(AnthropicConstants.FIELD_STOP_REASON).asText());
        return new ChatReply(text, reportedModel, usage, truncated);
    }

    static String extractText(JsonNode response) {
        var blocks = response.path(AnthropicConstants.FIELD_CONTENT);
        var texts = new ArrayList<String>();
        var types = new ArrayList<String>();
        for (JsonNode block : blocks) {
            var type = block.path(AnthropicConstants.FIELD_TYPE).asText();
            types.add(type);
            if (AnthropicConstants.BLOCK_TYPE_TEXT.equals(type)) {
                texts.add(block.path(AnthropicConstants.FIELD_TEXT).asText());
            }
        }
        if (texts.isEmpty()) {
            throw new LLMException(AnthropicConstants.NO_TEXT_BLOCK.formatted(
                    response.path(AnthropicConstants.FIELD_STOP_REASON).asText(), types));
        }
        return String.join(AnthropicConstants.TEXT_BLOCK_SEPARATOR, texts);
    }
}
