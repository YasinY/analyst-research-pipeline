package com.assignment.research.adapter.llm.anthropic;

public final class AnthropicConstants {

    public static final String DEFAULT_URL = "https://api.anthropic.com/v1/messages";
    public static final String DEFAULT_MODEL = "claude-sonnet-5-5";
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
    public static final String FIELD_THINKING = "thinking";
    public static final String THINKING_BETWEEN_TOOLS_ONLY = "between_tools";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_TYPE = "type";
    public static final String BLOCK_TYPE_TEXT = "text";
    public static final String FIELD_STOP_REASON = "stop_reason";
    public static final String STOP_REASON_MAX_TOKENS = "max_tokens";
    public static final String NO_TEXT_BLOCK =
            "Anthropic reply contains no text block (stop_reason %s, block types %s)";
    public static final String TEXT_BLOCK_SEPARATOR = "\n";
    public static final String FIELD_USAGE = "usage";
    public static final String FIELD_INPUT_TOKENS = "input_tokens";
    public static final String FIELD_OUTPUT_TOKENS = "output_tokens";

    private AnthropicConstants() {
    }
}
