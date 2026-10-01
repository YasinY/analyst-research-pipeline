package com.assignment.research.reconciliation;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Source;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Reconciler {

    private final LLMPort llm;
    private final PromptTemplates prompts;

    public Reconciliation reconcile(List<SubQuestion> questions, List<Claim> claims, List<Source> sources,
            int round) {
        if (claims.isEmpty()) {
            return Reconciliation.empty();
        }
        if (claims.size() < ReconciliationConstants.MIN_CLAIMS_WORTH_COMPARING) {
            return EvidenceGroupAssembler.assemble(claims, sources, ReconciliationOutput.empty());
        }
        var output = llm.complete(buildRequest(questions, claims, sources, round), ReconciliationOutput.class)
                .getValue();
        return EvidenceGroupAssembler.assemble(claims, sources, output);
    }

    private LLMRequest buildRequest(List<SubQuestion> questions, List<Claim> claims, List<Source> sources,
            int round) {
        var template = prompts.forAgent(AgentName.RECONCILER);
        var userPrompt = template.renderUserPrompt(Map.of(
                ReconciliationConstants.QUESTIONS_VARIABLE, ClaimPromptFormatter.formatQuestions(questions),
                ReconciliationConstants.CLAIMS_VARIABLE, ClaimPromptFormatter.formatClaims(claims, sources)));
        var label = ReconciliationConstants.TRACE_LABEL_FORMAT.formatted(round);
        return new LLMRequest(label, template.getSystemPrompt(), userPrompt,
                ReconciliationConstants.MAX_OUTPUT_TOKENS);
    }
}
