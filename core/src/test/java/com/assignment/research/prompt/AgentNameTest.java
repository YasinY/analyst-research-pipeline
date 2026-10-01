package com.assignment.research.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AgentNameTest {

    private static final String SYNTHESIZER_DIRECTORY = "synthesizer";

    @Test
    void directoryNameIsTheLowerCaseConstantName() {
        assertThat(AgentName.SYNTHESIZER.getDirectoryName()).isEqualTo(SYNTHESIZER_DIRECTORY);
    }
}
