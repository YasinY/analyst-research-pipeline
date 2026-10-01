package com.assignment.research.bootstrap;

public final class BootstrapConstants {

    public static final String ENV_PROVIDER = "LLM_PROVIDER";
    public static final String DEFAULT_PROVIDER = "openai";
    public static final String ENV_DATA_DIR = "DATA_DIR";
    public static final String DEFAULT_DATA_DIR = "data";
    public static final String ENV_RUNS_DIR = "RUNS_DIR";
    public static final String DEFAULT_RUNS_DIR = "runs";
    public static final String ENV_CORPUS_FILE = "CORPUS_FILE";
    public static final String DEFAULT_CORPUS_FILE = "dry-bulk-shipping.json";
    public static final String PROMPTS_SUBDIRECTORY = "prompts";
    public static final String CORPUS_SUBDIRECTORY = "corpus";

    private BootstrapConstants() {
    }
}
