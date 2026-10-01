package com.assignment.research.synthesis;

import com.assignment.research.confidence.ConfidenceConstants;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Synthesizer {

    private final LLMPort llm;
    private final PromptTemplates prompts;

    public BriefingDraft synthesize(SynthesisInput input, int round) {
        var confidences = input.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, Function.identity()));
        var output = llm.complete(buildRequest(input, confidences, round), SynthesisOutput.class).getValue();
        return BriefingDraftAssembler.assemble(output, confidences);
    }

    private LLMRequest buildRequest(SynthesisInput input, Map<String, GroupConfidence> confidences, int round) {
        var template = prompts.forAgent(AgentName.SYNTHESIZER);
        var eligible = eligibleGroups(input.getGroups(), confidences);
        var weak = weakGroups(input.getGroups(), confidences);
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
        return new LLMRequest(labelFormat.formatted(round), template.getSystemPrompt(), userPrompt,
                SynthesisConstants.MAX_OUTPUT_TOKENS);
    }

    private static List<EvidenceGroup> eligibleGroups(List<EvidenceGroup> groups,
            Map<String, GroupConfidence> confidences) {
        return scoredGroups(groups, confidences)
                .filter(group -> hasAdequateEvidence(confidences.get(group.getId())))
                .toList();
    }

    private static List<EvidenceGroup> weakGroups(List<EvidenceGroup> groups,
            Map<String, GroupConfidence> confidences) {
        return scoredGroups(groups, confidences)
                .filter(group -> !hasAdequateEvidence(confidences.get(group.getId())))
                .toList();
    }

    private static Stream<EvidenceGroup> scoredGroups(List<EvidenceGroup> groups,
            Map<String, GroupConfidence> confidences) {
        return groups.stream().filter(group -> confidences.containsKey(group.getId()));
    }

    private static boolean hasAdequateEvidence(GroupConfidence confidence) {
        return confidence.getLevel().isAtLeast(ConfidenceConstants.ADEQUATE_EVIDENCE);
    }
}
