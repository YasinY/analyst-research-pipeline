package com.assignment.research.synthesis;

import com.assignment.research.confidence.ConfidenceConstants;
import com.assignment.research.confidence.GroupConfidence;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BriefingDraftAssembler {

    private BriefingDraftAssembler() {
    }

    public static BriefingDraft assemble(SynthesisOutput output, Map<String, GroupConfidence> confidences) {
        var dropped = new ArrayList<String>();
        var demoted = new ArrayList<String>();
        var uncertainties = new ArrayList<GroundedStatement>();
        var keyFacts = new ArrayList<GroundedStatement>();

        for (var candidate : output.getKeyFacts()) {
            var statement = withKnownGroups(candidate, confidences);
            if (!statement.isGrounded()) {
                dropped.add(statement.getText());
                continue;
            }
            if (!restsOnEligibleGroup(statement, confidences)) {
                demoted.add(statement.getText());
                uncertainties.add(statement);
                continue;
            }
            keyFacts.add(statement);
        }
        output.getUncertainties().stream().map(candidate -> withKnownGroups(candidate, confidences))
                .forEach(uncertainties::add);

        var summary = output.getSummary().stream().map(candidate -> withKnownGroups(candidate, confidences)).toList();
        var followUps = output.getFollowUpQuestions().stream()
                .map(String::strip)
                .filter(question -> !question.isEmpty())
                .distinct()
                .toList();
        return new BriefingDraft(summary, List.copyOf(keyFacts), List.copyOf(uncertainties), followUps,
                List.copyOf(demoted), List.copyOf(dropped));
    }

    private static GroundedStatement withKnownGroups(GroundedStatement statement,
            Map<String, GroupConfidence> confidences) {
        var known = statement.getGroupIds().stream().filter(confidences::containsKey).distinct().toList();
        return new GroundedStatement(statement.getText().strip(), known);
    }

    private static boolean restsOnEligibleGroup(GroundedStatement statement, Map<String, GroupConfidence> confidences) {
        return statement.getGroupIds().stream()
                .map(confidences::get)
                .anyMatch(confidence -> confidence.getLevel().isAtLeast(ConfidenceConstants.ADEQUATE_EVIDENCE));
    }
}
