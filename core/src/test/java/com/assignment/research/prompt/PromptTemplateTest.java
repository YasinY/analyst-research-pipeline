package com.assignment.research.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptTemplateTest {

    @Test
    void replacesEveryPlaceholderIncludingRepeatsAndWhitespace() {
        var template = new PromptTemplate("system", "Query: {{query}} / again: {{ query }} / {{other}}");

        var rendered = template.renderUserPrompt(Map.of("query", "dry bulk", "other", "x"));

        assertThat(rendered).isEqualTo("Query: dry bulk / again: dry bulk / x");
    }

    @Test
    void failsLoudlyWhenAVariableIsMissing() {
        var template = new PromptTemplate("system", "Query: {{query}}");

        assertThatThrownBy(() -> template.renderUserPrompt(Map.of()))
                .isInstanceOf(MissingPromptVariableException.class)
                .hasMessageContaining("query");
    }

    @Test
    void keepsReplacementValuesWithRegexSpecialCharactersIntact() {
        var template = new PromptTemplate("system", "{{value}}");

        var rendered = template.renderUserPrompt(Map.of("value", "$1 and \\backslash"));

        assertThat(rendered).isEqualTo("$1 and \\backslash");
    }
}
