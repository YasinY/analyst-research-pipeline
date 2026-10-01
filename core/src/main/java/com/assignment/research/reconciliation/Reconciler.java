package com.assignment.research.reconciliation;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Source;
import com.assignment.research.llm.LlmPort;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Reconciler {

    private static final String TRACE_LABEL_FORMAT = "reconciler/%s/round%d";
    private static final String QUESTION_VARIABLE = "question";
    private static final String CLAIMS_VARIABLE = "claims";
    private static final int MIN_CLAIMS_WORTH_COMPARING = 2;
    private static final int MAX_OUTPUT_TOKENS = 2048;

    private final LlmPort llm;
    private final PromptTemplates prompts;

    public Reconciliation reconcile(SubQuestion question, List<Claim> claims, List<Source> sources, int round) {
        var subQuestionId = question.getId();
        if (claims.isEmpty()) {
            return Reconciliation.empty(subQuestionId);
        }
        if (claims.size() < MIN_CLAIMS_WORTH_COMPARING) {
            return EvidenceGroupAssembler.assemble(subQuestionId, claims, sources, ReconciliationOutput.empty());
        }
        var output = llm.complete(buildRequest(question, claims, sources, round), ReconciliationOutput.class)
                .getValue();
        return EvidenceGroupAssembler.assemble(subQuestionId, claims, sources, output);
    }

    private LlmRequest buildRequest(SubQuestion question, List<Claim> claims, List<Source> sources, int round) {
        var template = prompts.forAgent(AgentName.RECONCILER);
        var userPrompt = template.renderUserPrompt(Map.of(
                QUESTION_VARIABLE, question.getQuestion(),
                CLAIMS_VARIABLE, ClaimPromptFormatter.format(claims, sources)));
        var label = TRACE_LABEL_FORMAT.formatted(question.getId(), round);
        return new LlmRequest(label, template.getSystemPrompt(), userPrompt, MAX_OUTPUT_TOKENS);
    }
}
