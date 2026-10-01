package com.assignment.research.synthesis;

import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.reconciliation.EvidenceGroup;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class SynthesisPromptFormatter {

    private SynthesisPromptFormatter() {
    }

    public static String formatEvidence(List<EvidenceGroup> groups, Map<String, GroupConfidence> confidences) {
        return joinOrNone(groups.stream().map(group -> formatGroup(group, confidences.get(group.getId()))).toList());
    }

    public static String formatGaps(List<SubQuestion> gaps) {
        return joinOrNone(gaps.stream()
                .map(gap -> SynthesisConstants.GAP_LINE.formatted(gap.getId(), gap.getQuestion()))
                .toList());
    }

    public static String formatRevision(SynthesisInput input) {
        if (!input.isRevision()) {
            return SynthesisConstants.FIRST_DRAFT_NOTE;
        }
        var previous = SynthesisConstants.SECTION_FORMAT.formatted(SynthesisConstants.PREVIOUS_DRAFT_HEADING,
                formatDraft(input.getPreviousDraft().orElseThrow()));
        var findings = SynthesisConstants.SECTION_FORMAT.formatted(SynthesisConstants.FINDINGS_HEADING,
                formatFindings(input.getFindings()));
        return previous + SynthesisConstants.LINE_SEPARATOR + SynthesisConstants.LINE_SEPARATOR + findings;
    }

    public static String formatDraft(BriefingDraft draft) {
        return String.join(SynthesisConstants.LINE_SEPARATOR + SynthesisConstants.LINE_SEPARATOR,
                section(SynthesisConstants.SUMMARY_HEADING, draft.getSummary()),
                section(SynthesisConstants.KEY_FACTS_HEADING, draft.getKeyFacts()),
                section(SynthesisConstants.UNCERTAINTIES_HEADING, draft.getUncertainties()),
                SynthesisConstants.SECTION_FORMAT.formatted(SynthesisConstants.FOLLOW_UPS_HEADING,
                        joinOrNone(draft.getFollowUpQuestions().stream().map(SynthesisConstants.FOLLOW_UP_LINE::formatted).toList())));
    }

    private static String formatGroup(EvidenceGroup group, GroupConfidence confidence) {
        var line = String.format(Locale.ROOT, SynthesisConstants.EVIDENCE_LINE,
                group.getId(),
                confidence.getLevel(),
                confidence.getScore(),
                group.getAssertion(),
                group.getIndependentSourceCount(),
                group.getBestTier(),
                group.getNewestSourceDate(),
                group.getConflictStatus());
        if (group.getConflict().isEmpty()) {
            return line;
        }
        var conflictingIds = String.join(SynthesisConstants.GROUP_ID_SEPARATOR, group.getConflictingGroupIds());
        return line + SynthesisConstants.CONFLICT_SUFFIX.formatted(conflictingIds, group.getConflict().get());
    }

    private static String section(String heading, List<GroundedStatement> statements) {
        var lines = statements.stream()
                .map(statement -> SynthesisConstants.STATEMENT_LINE.formatted(statement.getText(),
                        String.join(SynthesisConstants.GROUP_ID_SEPARATOR, statement.getGroupIds())))
                .toList();
        return SynthesisConstants.SECTION_FORMAT.formatted(heading, joinOrNone(lines));
    }

    private static String formatFindings(List<CriticFinding> findings) {
        return joinOrNone(findings.stream()
                .map(finding -> SynthesisConstants.FINDING_LINE.formatted(finding.getSeverity(), finding.getType(),
                        finding.getQuotedText(), finding.getDetail()))
                .toList());
    }

    private static String joinOrNone(List<String> lines) {
        if (lines.isEmpty()) {
            return SynthesisConstants.NONE_PLACEHOLDER;
        }
        return lines.stream().collect(Collectors.joining(SynthesisConstants.LINE_SEPARATOR));
    }
}
