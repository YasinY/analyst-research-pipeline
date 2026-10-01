package com.assignment.research.prompt;

public class MissingPromptVariableException extends RuntimeException {

    public MissingPromptVariableException(String variableName) {
        super("prompt template references variable '" + variableName + "' but no value was supplied");
    }
}
