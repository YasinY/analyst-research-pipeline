package com.assignment.research.synthesis;

import com.assignment.research.confidence.ConfidenceLevel;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.llm.LlmPort;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Synthesizer {

    private static final ConfidenceLevel KEY_FACT_MINIMUM = ConfidenceLevel.MEDIUM;

    private final LlmPort llm;
    private final PromptTemplates prompts;

    public BriefingDraft synthesize(SynthesisInput input, int round) {
        var confidences = input.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, Function.identity()));
        var output = llm.complete(buildRequest(input, confidences, round), SynthesisOutput.class).getValue();
        return BriefingDraftAssembler.assemble(output, confidences);
    }

    private LlmRequest buildRequest(SynthesisInput input, Map<String, GroupConfidence> confidences, int round) {
        var template = prompts.forAgent(AgentName.SYNTHESIZER);
        var eligible = groupsAtLeast(input.getGroups(), confidences, true);
        var weak = groupsAtLeast(input.getGroups(), confidences, false);
        var userPrompt = template.renderUserPrompt(Map.of(
                SynthesisConstants.QUERY_VARIABLE, input.getQuery().getText(),
                SynthesisConstants.INTERPRETATION_VARIABLE, input.getInterpretation(),
                SynthesisConstants.EVIDENCE_VARIABLE, SynthesisPromptFormatter.formatEvidence(eligible, confidences),
                SynthesisConstants.WEAK_EVIDENCE_VARIABLE, SynthesisPromptFormatter.formatEvidence(weak, confidences),
                SynthesisConstants.GAPS_VARIABLE, SynthesisPromptFormatter.formatGaps(input.getGaps()),
                SynthesisConstants.REVISION_VARIABLE, SynthesisPromptFormatter.formatRevision(input)));
        var labelFormat = input.isRevision()
                ? SynthesisConstants.REVISION_LABEL_FORMAT
                : SynthesisConstants.FIRST_DRAFT_LABEL_FORMAT;
        return new LlmRequest(labelFormat.formatted(round), template.getSystemPrompt(), userPrompt,
                SynthesisConstants.MAX_OUTPUT_TOKENS);
    }

    private static List<EvidenceGroup> groupsAtLeast(List<EvidenceGroup> groups,
            Map<String, GroupConfidence> confidences, boolean eligible) {
        return groups.stream()
                .filter(group -> confidences.containsKey(group.getId()))
                .filter(group -> confidences.get(group.getId()).getLevel().isAtLeast(KEY_FACT_MINIMUM) == eligible)
                .toList();
    }
}
