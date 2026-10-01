package com.assignment.research.bootstrap;

import com.assignment.research.adapter.llm.anthropic.AnthropicConstants;
import com.assignment.research.adapter.llm.openai.OpenAiConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LLMProvider {
    OPENAI(OpenAiConstants.ENV_API_KEY, OpenAiConstants.ENV_API_URL, OpenAiConstants.ENV_MODEL,
            OpenAiConstants.DEFAULT_URL, OpenAiConstants.DEFAULT_MODEL),
    ANTHROPIC(AnthropicConstants.ENV_API_KEY, AnthropicConstants.ENV_API_URL, AnthropicConstants.ENV_MODEL,
            AnthropicConstants.DEFAULT_URL, AnthropicConstants.DEFAULT_MODEL);

    private final String apiKeyVariable;
    private final String urlVariable;
    private final String modelVariable;
    private final String defaultUrl;
    private final String defaultModel;
}
