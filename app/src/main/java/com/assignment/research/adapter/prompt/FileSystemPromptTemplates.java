package com.assignment.research.adapter.prompt;

import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplate;
import com.assignment.research.prompt.PromptTemplates;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FileSystemPromptTemplates implements PromptTemplates {

    private static final String SYSTEM_FILE = "system.md";
    private static final String USER_FILE = "user.md";

    private final Path promptsDirectory;

    @Override
    public PromptTemplate forAgent(AgentName agent) {
        var directory = promptsDirectory.resolve(agent.getDirectoryName());
        return new PromptTemplate(read(directory.resolve(SYSTEM_FILE)), read(directory.resolve(USER_FILE)));
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("cannot read prompt file " + file.toAbsolutePath(), failure);
        }
    }
}
