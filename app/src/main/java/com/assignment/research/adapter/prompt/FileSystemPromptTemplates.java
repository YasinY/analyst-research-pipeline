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

    private final Path promptsDirectory;

    @Override
    public PromptTemplate forAgent(AgentName agent) {
        var directory = promptsDirectory.resolve(agent.getDirectoryName());
        return new PromptTemplate(read(directory.resolve(PromptFileConstants.SYSTEM_FILE)),
                read(directory.resolve(PromptFileConstants.USER_FILE)));
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException(PromptFileConstants.READ_FAILURE.formatted(file.toAbsolutePath()),
                    failure);
        }
    }
}
