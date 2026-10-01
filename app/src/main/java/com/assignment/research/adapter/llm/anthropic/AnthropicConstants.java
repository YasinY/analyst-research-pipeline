package com.assignment.research.adapter.llm.anthropic;

public final class AnthropicConstants {

    public static final String DEFAULT_URL = "https://api.anthropic.com/v1/messages";
    public static final String DEFAULT_MODEL = "claude-haiku-4-5";
    public static final String ENV_API_KEY = "ANTHROPIC_API_KEY";
    public static final String ENV_API_URL = "ANTHROPIC_API_URL";
    public static final String ENV_MODEL = "ANTHROPIC_MODEL";

    public static final String HEADER_API_KEY = "x-api-key";
    public static final String HEADER_VERSION = "anthropic-version";
    public static final String API_VERSION = "2023-06-01";
    public static final String ROLE_USER = "user";
    public static final String FIELD_MODEL = "model";
    public static final String FIELD_MAX_TOKENS = "max_tokens";
    public static final String FIELD_SYSTEM = "system";
    public static final String FIELD_MESSAGES = "messages";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_USAGE = "usage";
    public static final String FIELD_INPUT_TOKENS = "input_tokens";
    public static final String FIELD_OUTPUT_TOKENS = "output_tokens";
    public static final int FIRST_CONTENT_BLOCK = 0;

    private AnthropicConstants() {
    }
}
