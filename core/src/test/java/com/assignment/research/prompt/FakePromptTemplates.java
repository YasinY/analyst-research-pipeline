package com.assignment.research.prompt;

public final class FakePromptTemplates implements PromptTemplates {

    public static final String SYSTEM_PROMPT = "fake system prompt";
    public static final String USER_TEMPLATE = "agent input: {{query}}";

    private final PromptTemplate template;

    public FakePromptTemplates(String userPromptTemplate) {
        this.template = new PromptTemplate(SYSTEM_PROMPT, userPromptTemplate);
    }

    public static FakePromptTemplates withQueryPlaceholder() {
        return new FakePromptTemplates(USER_TEMPLATE);
    }

    @Override
    public PromptTemplate forAgent(AgentName agent) {
        return template;
    }
}
