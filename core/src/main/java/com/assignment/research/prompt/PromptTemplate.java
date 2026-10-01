package com.assignment.research.prompt;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.Value;

@Value
public class PromptTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");

    @NonNull
    private final String systemPrompt;
    @NonNull
    private final String userPromptTemplate;

    public String renderUserPrompt(Map<String, String> variables) {
        var matcher = PLACEHOLDER.matcher(userPromptTemplate);
        var rendered = new StringBuilder();
        while (matcher.find()) {
            var name = matcher.group(1);
            var value = variables.get(name);
            if (value == null) {
                throw new MissingPromptVariableException(name);
            }
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }
}
