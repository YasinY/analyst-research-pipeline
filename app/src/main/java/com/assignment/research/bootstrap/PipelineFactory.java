package com.assignment.research.bootstrap;

import com.assignment.research.adapter.llm.ChatClient;
import com.assignment.research.adapter.llm.HttpJSONPoster;
import com.assignment.research.adapter.llm.JSONResponseParser;
import com.assignment.research.adapter.llm.RetryingChatClient;
import com.assignment.research.adapter.llm.StructuredOutputLLMPort;
import com.assignment.research.adapter.llm.anthropic.AnthropicChatClient;
import com.assignment.research.adapter.llm.openai.OpenAiCompatibleChatClient;
import com.assignment.research.adapter.prompt.FileSystemPromptTemplates;
import com.assignment.research.adapter.search.JSONCorpusSearchAdapter;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.pipeline.BriefingOrchestrator;
import com.assignment.research.pipeline.ProduceBriefingUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class PipelineFactory {

    private final AppConfig config;
    private final ObjectMapper mapper;
    private final Clock clock;

    public ProduceBriefingUseCase createUseCase() {
        var search = JSONCorpusSearchAdapter.load(config.getCorpusPath(), mapper);
        var prompts = new FileSystemPromptTemplates(config.getPromptsDirectory());
        return new BriefingOrchestrator(createLLMPort(), search, prompts, clock);
    }

    public LLMPort createLLMPort() {
        var chat = new RetryingChatClient(createChatClient());
        return new StructuredOutputLLMPort(chat, new JSONResponseParser(mapper));
    }

    private ChatClient createChatClient() {
        var poster = new HttpJSONPoster(mapper);
        return switch (config.getProvider()) {
            case OPENAI -> new OpenAiCompatibleChatClient(poster, config.getApiUrl(), config.getApiKey(),
                    config.getModel());
            case ANTHROPIC -> new AnthropicChatClient(poster, config.getApiUrl(), config.getApiKey(),
                    config.getModel());
        };
    }
}
