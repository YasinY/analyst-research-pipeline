package com.assignment.research.adapter.output;

import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.evidence.Source;
import com.assignment.research.pipeline.AgentFailure;
import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.reconciliation.EvidenceGroup;
import com.assignment.research.synthesis.GroundedStatement;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MarkdownBriefingRenderer {

    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String NEWLINE = "\n";
    private static final String BULLET = "- ";
    private static final String NONE = "_None._";
    private static final String EVIDENCE_NOTE =
            "  - evidence: %d independent source(s), best tier %s, newest %s, conflict %s, confidence %s (%.2f)";
    private static final String GROUP_REF = " [%s]";
    private static final String FINDING_LINE = "- **%s / %s** on \"%s\": %s";
    private static final String SOURCE_LINE = "- `%s` %s, %s (%s, tier %s), %s%s";
    private static final String CITES_SUFFIX = ", cites `%s`";
    private static final String FAILURE_LINE = "- round %d, %s: %s";
    private static final String COVERAGE_OK = " (adequate evidence found)";
    private static final String COVERAGE_GAP = " (no adequate evidence found)";

    private final Clock clock;

    public String render(BriefingResult result) {
        var state = result.getFinalState();
        var groupsById = state.getGroups().stream()
                .collect(Collectors.toMap(EvidenceGroup::getId, Function.identity()));
        var confidenceById = state.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, Function.identity()));
        var draft = result.getDraft();

        var out = new StringBuilder();
        out.append("# Analyst briefing").append(NEWLINE).append(NEWLINE);
        out.append("> **Query:** ").append(state.getQuery().getText()).append(NEWLINE);
        out.append("> **Generated:** ").append(LocalDateTime.now(clock).format(GENERATED_FORMAT)).append(NEWLINE);
        out.append("> **Overall confidence:** ").append(result.getConfidence().getLevel()).append(NEWLINE);
        out.append("> **Scope as understood by the system:** ").append(state.getInterpretation().orElse(""))
                .append(NEWLINE).append(NEWLINE);

        subQuestions(out, state, result);
        section(out, "Summary", draft.getSummary(), groupsById, confidenceById, false);
        section(out, "Key facts", draft.getKeyFacts(), groupsById, confidenceById, true);
        section(out, "Identified uncertainties", draft.getUncertainties(), groupsById, confidenceById, false);
        gaps(out, result);
        confidence(out, result);
        openFindings(out, result.getOpenFindings());
        followUps(out, draft.getFollowUpQuestions());
        howProduced(out, result);
        sources(out, state);
        return out.toString();
    }

    private static void section(StringBuilder out, String heading, List<GroundedStatement> statements,
            Map<String, EvidenceGroup> groups, Map<String, GroupConfidence> confidences, boolean withEvidence) {
        out.append("## ").append(heading).append(NEWLINE).append(NEWLINE);
        if (statements.isEmpty()) {
            out.append(NONE).append(NEWLINE).append(NEWLINE);
            return;
        }
        for (var statement : statements) {
            out.append(BULLET).append(statement.getText());
            statement.getGroupIds().forEach(id -> out.append(GROUP_REF.formatted(id)));
            out.append(NEWLINE);
            if (withEvidence) {
                statement.getGroupIds().stream().map(groups::get).filter(group -> group != null)
                        .forEach(group -> out.append(evidenceNote(group, confidences.get(group.getId())))
                                .append(NEWLINE));
            }
        }
        out.append(NEWLINE);
    }

    private static String evidenceNote(EvidenceGroup group, GroupConfidence confidence) {
        return String.format(Locale.ROOT, EVIDENCE_NOTE, group.getIndependentSourceCount(), group.getBestTier(),
                group.getNewestSourceDate(), group.getConflictStatus(), confidence.getLevel(), confidence.getScore());
    }

    private static void subQuestions(StringBuilder out, BriefingState state, BriefingResult result) {
        var gapIds = result.getGaps().stream().map(gap -> gap.getId()).collect(Collectors.toSet());
        out.append("## Sub-questions investigated").append(NEWLINE).append(NEWLINE);
        for (var question : state.getSubQuestions()) {
            var status = gapIds.contains(question.getId()) ? COVERAGE_GAP : COVERAGE_OK;
            out.append(BULLET).append(question.getId()).append(": ").append(question.getQuestion()).append(status)
                    .append(NEWLINE);
        }
        out.append(NEWLINE);
    }

    private static void gaps(StringBuilder out, BriefingResult result) {
        if (result.getGaps().isEmpty()) {
            return;
        }
        out.append("### Sub-questions without adequate evidence").append(NEWLINE).append(NEWLINE);
        result.getGaps().forEach(gap -> out.append(BULLET).append(gap.getId()).append(": ")
                .append(gap.getQuestion()).append(NEWLINE));
        out.append(NEWLINE);
    }

    private static void confidence(StringBuilder out, BriefingResult result) {
        var confidence = result.getConfidence();
        out.append("## Confidence: ").append(confidence.getLevel()).append(NEWLINE).append(NEWLINE);
        out.append("Derived from the run record, not asserted by a model:").append(NEWLINE).append(NEWLINE);
        confidence.getReasons().forEach(reason -> out.append(BULLET).append(reason).append(NEWLINE));
        out.append(NEWLINE);
    }

    private static void openFindings(StringBuilder out, List<CriticFinding> findings) {
        out.append("## Open review findings").append(NEWLINE).append(NEWLINE);
        if (findings.isEmpty()) {
            out.append("_The independent reviewer approved the final draft._").append(NEWLINE).append(NEWLINE);
            return;
        }
        findings.forEach(finding -> out.append(FINDING_LINE.formatted(finding.getSeverity(), finding.getType(),
                finding.getQuotedText(), finding.getDetail())).append(NEWLINE));
        out.append(NEWLINE);
    }

    private static void followUps(StringBuilder out, List<String> questions) {
        out.append("## Suggested follow-up questions").append(NEWLINE).append(NEWLINE);
        if (questions.isEmpty()) {
            out.append(NONE).append(NEWLINE).append(NEWLINE);
            return;
        }
        questions.forEach(question -> out.append(BULLET).append(question).append(NEWLINE));
        out.append(NEWLINE);
    }

    private static void howProduced(StringBuilder out, BriefingResult result) {
        var state = result.getFinalState();
        out.append("## How this briefing was produced").append(NEWLINE).append(NEWLINE);
        out.append(BULLET).append("Stop reason: ").append(result.getStopDecision().getReason()).append(". ")
                .append(result.getStopDecision().getExplanation()).append(NEWLINE);
        out.append(BULLET).append("Research rounds: ").append(state.getRound()).append(NEWLINE);
        out.append(BULLET).append("Model calls: ").append(result.getTrace().size()).append(", tokens in: ")
                .append(result.getUsage().getInputTokens()).append(", tokens out: ")
                .append(result.getUsage().getOutputTokens()).append(NEWLINE);
        out.append(BULLET).append("Claims extracted: ").append(state.getClaims().size())
                .append(", evidence groups: ").append(state.getGroups().size()).append(NEWLINE);
        if (!result.getDraft().getDroppedStatements().isEmpty()) {
            out.append(BULLET).append("Statements removed for lacking evidence: ")
                    .append(result.getDraft().getDroppedStatements().size()).append(NEWLINE);
        }
        if (!result.getDraft().getDemotedKeyFacts().isEmpty()) {
            out.append(BULLET).append("Key facts demoted to uncertainties for weak evidence: ")
                    .append(result.getDraft().getDemotedKeyFacts().size()).append(NEWLINE);
        }
        failures(out, state.getFailures());
        out.append(NEWLINE);
    }

    private static void failures(StringBuilder out, List<AgentFailure> failures) {
        if (failures.isEmpty()) {
            return;
        }
        out.append(BULLET).append("Degraded steps:").append(NEWLINE);
        failures.forEach(failure -> out.append("  ").append(FAILURE_LINE.formatted(failure.getRound(),
                failure.getAgent(), failure.getMessage())).append(NEWLINE));
    }

    private static void sources(StringBuilder out, BriefingState state) {
        out.append("## Sources consulted").append(NEWLINE).append(NEWLINE);
        out.append("_All sources are part of a fictional mock corpus built for this exercise._").append(NEWLINE)
                .append(NEWLINE);
        for (Source source : state.getSources()) {
            var cites = source.getCitedSource().map(CITES_SUFFIX::formatted).orElse("");
            out.append(SOURCE_LINE.formatted(source.getId(), source.getTitle(), source.getPublisher(),
                    source.getType(), source.getTier(), source.getPublishedAt(), cites)).append(NEWLINE);
        }
        out.append(NEWLINE);
    }
}
