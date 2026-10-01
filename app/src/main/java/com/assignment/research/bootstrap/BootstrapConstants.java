package com.assignment.research.bootstrap;

public final class BootstrapConstants {

    public static final String ENV_PROVIDER = "LLM_PROVIDER";
    public static final String DEFAULT_PROVIDER = "anthropic";
    public static final String DEFAULT_API_KEY = "";
    public static final String ENV_DATA_DIR = "DATA_DIR";
    public static final String DEFAULT_DATA_DIR = "data";
    public static final String ENV_RUNS_DIR = "RUNS_DIR";
    public static final String DEFAULT_RUNS_DIR = "runs";
    public static final String ENV_CORPUS_FILE = "CORPUS_FILE";
    public static final String DEFAULT_CORPUS_FILE = "dry-bulk-shipping.json";
    public static final String ENV_PORT = "PORT";
    public static final int MIN_PORT = 1;
    public static final int MAX_PORT = 65535;
    public static final String PROMPTS_SUBDIRECTORY = "prompts";
    public static final String CORPUS_SUBDIRECTORY = "corpus";
    public static final String ABORTED_MESSAGE =
            "Run aborted: the %s step failed and no briefing could be produced. Cause: %s";

    private BootstrapConstants() {
    }
}
