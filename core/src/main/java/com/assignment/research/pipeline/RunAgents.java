package com.assignment.research.pipeline;

import com.assignment.research.confidence.ConfidenceCalculator;
import com.assignment.research.critique.Critic;
import com.assignment.research.evidence.Researcher;
import com.assignment.research.evidence.SourceSearchPort;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.planning.Planner;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.reconciliation.Reconciler;
import com.assignment.research.synthesis.Synthesizer;
import java.time.Clock;
import lombok.Getter;

@Getter
final class RunAgents {

    private final Planner planner;
    private final Researcher researcher;
    private final Reconciler reconciler;
    private final ConfidenceCalculator confidenceCalculator;
    private final Synthesizer synthesizer;
    private final Critic critic;

    RunAgents(LLMPort llm, SourceSearchPort search, PromptTemplates prompts, Clock clock) {
        this.planner = new Planner(llm, prompts);
        this.researcher = new Researcher(llm, search, prompts);
        this.reconciler = new Reconciler(llm, prompts);
        this.confidenceCalculator = new ConfidenceCalculator(clock);
        this.synthesizer = new Synthesizer(llm, prompts);
        this.critic = new Critic(llm, prompts);
    }
}
