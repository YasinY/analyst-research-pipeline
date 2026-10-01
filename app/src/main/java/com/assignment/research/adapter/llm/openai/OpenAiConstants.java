package com.assignment.research.adapter.llm.openai;

public final class OpenAiConstants {

    public static final String DEFAULT_URL = "https://api.openai.com/v1/chat/completions";
    public static final String DEFAULT_MODEL = "gpt-5.4-mini";
    public static final String ENV_API_KEY = "OPENAI_API_KEY";
    public static final String ENV_API_URL = "OPENAI_API_URL";
    public static final String ENV_MODEL = "OPENAI_MODEL";

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String FIELD_MODEL = "model";
    public static final String FIELD_MESSAGES = "messages";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_MAX_COMPLETION_TOKENS = "max_completion_tokens";
    public static final String FIELD_CHOICES = "choices";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_FINISH_REASON = "finish_reason";
    public static final String FINISH_REASON_LENGTH = "length";
    public static final String FIELD_USAGE = "usage";
    public static final String FIELD_PROMPT_TOKENS = "prompt_tokens";
    public static final String FIELD_COMPLETION_TOKENS = "completion_tokens";
    public static final int FIRST_CHOICE = 0;

    private OpenAiConstants() {
    }
}
