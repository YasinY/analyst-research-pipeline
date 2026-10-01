package com.assignment.research.adapter.cli;

public final class CliConstants {

    public static final String QUERY_FLAG = "--query";
    public static final String SERVE_FLAG = "--serve";
    public static final String QUERY_WORD_SEPARATOR = " ";
    public static final String USAGE = """
            Usage: java -jar app/target/research-pipeline.jar --query "<analyst question>"
                   java -jar app/target/research-pipeline.jar --serve      (web UI on http://%1$s:%2$d)

            Environment:
              LLM_PROVIDER        anthropic (default) or openai
              ANTHROPIC_API_KEY   key for the Anthropic Messages API
              ANTHROPIC_MODEL     default claude-sonnet-5-5
              OPENAI_API_KEY      key for the OpenAI-compatible endpoint (empty allowed for local servers)
              OPENAI_API_URL      default https://api.openai.com/v1/chat/completions
              OPENAI_MODEL        default gpt-5.4-mini
              DATA_DIR            default ./data (prompts and corpus)
              RUNS_DIR            default ./runs (one folder per run)
              PORT                web UI port for --serve, default %2$d
            """;
    public static final int EXIT_OK = 0;
    public static final int EXIT_ABORTED = 1;
    public static final int EXIT_USAGE = 2;

    private CliConstants() {
    }
}
