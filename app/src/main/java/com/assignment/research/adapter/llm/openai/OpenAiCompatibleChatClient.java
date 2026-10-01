package com.assignment.research.adapter.llm.openai;

import com.assignment.research.adapter.llm.ChatClient;
import com.assignment.research.adapter.llm.ChatReply;
import com.assignment.research.adapter.llm.HttpJSONPoster;
import com.assignment.research.adapter.llm.LLMAdapterConstants;
import com.assignment.research.llm.LLMUsage;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class OpenAiCompatibleChatClient implements ChatClient {

    private final HttpJSONPoster poster;
    private final String url;
    private final String apiKey;
    private final String model;

    @Override
    public ChatReply chat(String systemPrompt, String userPrompt, int maxOutputTokens) {
        var body = Map.of(
                OpenAiConstants.FIELD_MODEL, model,
                OpenAiConstants.FIELD_MAX_COMPLETION_TOKENS, maxOutputTokens,
                OpenAiConstants.FIELD_MESSAGES, List.of(
                        message(OpenAiConstants.ROLE_SYSTEM, systemPrompt),
                        message(OpenAiConstants.ROLE_USER, userPrompt)));
        var headers = apiKey.isBlank()
                ? Map.<String, String>of()
                : Map.of(LLMAdapterConstants.HEADER_AUTHORIZATION, LLMAdapterConstants.BEARER_PREFIX + apiKey);

        var response = poster.post(url, headers, body);
        var firstChoice = response.path(OpenAiConstants.FIELD_CHOICES).path(OpenAiConstants.FIRST_CHOICE);
        var text = HttpJSONPoster.requiredText(firstChoice, OpenAiConstants.FIELD_MESSAGE,
                OpenAiConstants.FIELD_CONTENT);
        var usage = new LLMUsage(
                HttpJSONPoster.intOrZero(response, OpenAiConstants.FIELD_USAGE, OpenAiConstants.FIELD_PROMPT_TOKENS),
                HttpJSONPoster.intOrZero(response, OpenAiConstants.FIELD_USAGE,
                        OpenAiConstants.FIELD_COMPLETION_TOKENS));
        var reportedModel = response.path(OpenAiConstants.FIELD_MODEL).asText(model);
        var truncated = OpenAiConstants.FINISH_REASON_LENGTH.equals(
                firstChoice.path(OpenAiConstants.FIELD_FINISH_REASON).asText());
        return new ChatReply(text, reportedModel, usage, truncated);
    }

    private static Map<String, String> message(String role, String content) {
        return Map.of(OpenAiConstants.FIELD_ROLE, role, OpenAiConstants.FIELD_CONTENT, content);
    }
}
