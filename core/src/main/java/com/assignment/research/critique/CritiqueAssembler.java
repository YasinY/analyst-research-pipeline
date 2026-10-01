package com.assignment.research.critique;

import com.assignment.research.planning.Keywords;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.synthesis.GroundedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CritiqueAssembler {

    private CritiqueAssembler() {
    }

    public static Critique assemble(BriefingDraft draft, CritiqueOutput output, Set<String> knownGroupIds) {
        var findings = new ArrayList<>(mechanicalFindings(draft));
        output.getFindings().stream()
                .filter(finding -> !finding.getDetail().isBlank())
                .map(finding -> toFinding(finding, knownGroupIds))
                .forEach(findings::add);
        return new Critique(List.copyOf(findings));
    }

    public static List<CriticFinding> mechanicalFindings(BriefingDraft draft) {
        return Stream.concat(draft.getSummary().stream(), draft.getKeyFacts().stream())
                .filter(statement -> !statement.isGrounded())
                .map(CritiqueAssembler::ungroundedFinding)
                .toList();
    }

    private static CriticFinding ungroundedFinding(GroundedStatement statement) {
        return new CriticFinding(FindingType.UNSUPPORTED, FindingSeverity.MAJOR, statement.getText(),
                CritiqueConstants.UNGROUNDED_STATEMENT_DETAIL, List.of(), List.of());
    }

    private static CriticFinding toFinding(FindingOutput output, Set<String> knownGroupIds) {
        var groupIds = output.getGroupIds().stream().filter(knownGroupIds::contains).distinct().toList();
        var keywords = output.getType() == FindingType.MISSING_EVIDENCE
                ? Keywords.normalizeOrDerive(output.getSuggestedKeywords(), output.getDetail())
                : List.<String>of();
        return new CriticFinding(output.getType(), output.getSeverity(), output.getQuotedText().strip(),
                output.getDetail().strip(), groupIds, keywords);
    }
}
