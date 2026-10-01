package com.assignment.research.critique;

import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.synthesis.SynthesisPromptFormatter;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Critic {

    private final LLMPort llm;
    private final PromptTemplates prompts;

    public Critique critique(CritiqueInput input, int round, int pass) {
        var confidences = input.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, Function.identity()));
        var output = llm.complete(buildRequest(input, confidences, round, pass), CritiqueOutput.class).getValue();
        return CritiqueAssembler.assemble(input.getDraft(), output, confidences.keySet());
    }

    private static String previousFindings(CritiqueInput input) {
        if (input.getPreviousFindings().isEmpty()) {
            return CritiqueConstants.NO_PREVIOUS_FINDINGS;
        }
        return SynthesisPromptFormatter.formatFindings(input.getPreviousFindings());
    }

    private LLMRequest buildRequest(CritiqueInput input, Map<String, GroupConfidence> confidences, int round,
            int pass) {
        var template = prompts.forAgent(AgentName.CRITIC);
        var userPrompt = template.renderUserPrompt(Map.of(
                CritiqueConstants.QUERY_VARIABLE, input.getQuery().getText(),
                CritiqueConstants.DRAFT_VARIABLE, SynthesisPromptFormatter.formatDraft(input.getDraft()),
                CritiqueConstants.EVIDENCE_VARIABLE,
                SynthesisPromptFormatter.formatEvidence(input.getGroups(), confidences),
                CritiqueConstants.GAPS_VARIABLE, SynthesisPromptFormatter.formatGaps(input.getGaps()),
                CritiqueConstants.PREVIOUS_FINDINGS_VARIABLE, previousFindings(input)));
        var label = CritiqueConstants.TRACE_LABEL_FORMAT.formatted(round, pass);
        return new LLMRequest(label, template.getSystemPrompt(), userPrompt, CritiqueConstants.MAX_OUTPUT_TOKENS);
    }
}
