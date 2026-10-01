package com.assignment.research.prompt;

import java.util.Locale;

public enum AgentName {
    PLANNER,
    RESEARCHER,
    RECONCILER,
    SYNTHESIZER,
    CRITIC;

    public String getDirectoryName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
