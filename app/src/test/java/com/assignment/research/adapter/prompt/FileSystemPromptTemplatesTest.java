package com.assignment.research.adapter.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.prompt.AgentName;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class FileSystemPromptTemplatesTest {

    private static final Path PROMPTS = Path.of(System.getProperty("basedir", "app")).resolveSibling("data")
            .resolve("prompts");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");

    private final FileSystemPromptTemplates templates = new FileSystemPromptTemplates(PROMPTS);

    @Test
    void everyAgentHasASystemAndAUserPromptOnDisk() {
        for (var agent : AgentName.values()) {
            var template = templates.forAgent(agent);
            assertThat(template.getSystemPrompt()).as(agent + " system prompt").isNotBlank();
            assertThat(template.getUserPromptTemplate()).as(agent + " user prompt").isNotBlank();
        }
    }

    @Test
    void userPromptsOnlyUseTheVariablesTheAgentsSupply() {
        assertPlaceholders(AgentName.PLANNER, "query");
        assertPlaceholders(AgentName.RESEARCHER, "question", "sources");
        assertPlaceholders(AgentName.RECONCILER, "question", "claims");
        assertPlaceholders(AgentName.SYNTHESIZER, "query", "interpretation", "evidence", "weakEvidence", "gaps",
                "revision");
        assertPlaceholders(AgentName.CRITIC, "query", "draft", "evidence", "gaps");
    }

    private void assertPlaceholders(AgentName agent, String... expected) {
        var matcher = PLACEHOLDER.matcher(templates.forAgent(agent).getUserPromptTemplate());
        var found = new java.util.TreeSet<String>();
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        assertThat(found).as(agent + " placeholders").containsExactlyInAnyOrder(expected);
    }
}
