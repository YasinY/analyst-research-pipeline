package com.assignment.research.adapter.llm.anthropic;

import com.assignment.research.adapter.llm.CacheablePrompt;
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

    private static final Map<String, String> EPHEMERAL_CACHE =
            Map.of(AnthropicConstants.FIELD_TYPE, AnthropicConstants.CACHE_TYPE_EPHEMERAL);

    private final HttpJSONPoster poster;
    private final String url;
    private final String apiKey;
    private final String model;

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        var body = Map.of(
                AnthropicConstants.FIELD_MODEL, model,
                AnthropicConstants.FIELD_MAX_TOKENS, maxOutputTokens,
                AnthropicConstants.FIELD_SYSTEM, List.of(cachedTextBlock(systemPrompt)),
                AnthropicConstants.FIELD_THINKING, Map.of(AnthropicConstants.FIELD_TYPE,
                        AnthropicConstants.THINKING_BETWEEN_TOOLS_ONLY),
                AnthropicConstants.FIELD_MESSAGES, List.of(Map.of(
                        AnthropicConstants.FIELD_ROLE, AnthropicConstants.ROLE_USER,
                        AnthropicConstants.FIELD_CONTENT, userContent(userPrompt))));
        var headers = Map.of(
                AnthropicConstants.HEADER_API_KEY, apiKey,
                AnthropicConstants.HEADER_VERSION, AnthropicConstants.API_VERSION);

        var response = poster.post(url, headers, body);
        var text = extractText(response);
        var reportedModel = response.path(AnthropicConstants.FIELD_MODEL).asText(model);
        var truncated = AnthropicConstants.STOP_REASON_MAX_TOKENS.equals(
                response.path(AnthropicConstants.FIELD_STOP_REASON).asText());
        return new ChatReply(text, reportedModel, usage(response), truncated);
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

    private static LLMUsage usage(JsonNode response) {
        var uncached = usageField(response, AnthropicConstants.FIELD_INPUT_TOKENS);
        var cacheWrites = usageField(response, AnthropicConstants.FIELD_CACHE_CREATION_INPUT_TOKENS);
        var cacheReads = usageField(response, AnthropicConstants.FIELD_CACHE_READ_INPUT_TOKENS);
        return new LLMUsage(uncached + cacheWrites + cacheReads,
                usageField(response, AnthropicConstants.FIELD_OUTPUT_TOKENS), cacheReads);
    }

    private static int usageField(JsonNode response, String field) {
        return HttpJSONPoster.intOrZero(response, AnthropicConstants.FIELD_USAGE, field);
    }

    private static List<Map<String, Object>> userContent(String userPrompt) {
        var prompt = CacheablePrompt.of(userPrompt);
        var blocks = new ArrayList<Map<String, Object>>();
        prompt.getStablePrefix().ifPresent(prefix -> blocks.add(cachedTextBlock(prefix)));
        blocks.add(textBlock(prompt.getRemainder()));
        return blocks;
    }

    private static Map<String, Object> textBlock(String text) {
        return Map.of(AnthropicConstants.FIELD_TYPE, AnthropicConstants.BLOCK_TYPE_TEXT,
                AnthropicConstants.FIELD_TEXT, text);
    }

    private static Map<String, Object> cachedTextBlock(String text) {
        return Map.of(AnthropicConstants.FIELD_TYPE, AnthropicConstants.BLOCK_TYPE_TEXT,
                AnthropicConstants.FIELD_TEXT, text,
                AnthropicConstants.FIELD_CACHE_CONTROL, EPHEMERAL_CACHE);
    }
}
