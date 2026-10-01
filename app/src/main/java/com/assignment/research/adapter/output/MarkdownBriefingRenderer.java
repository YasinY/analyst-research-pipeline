package com.assignment.research.adapter.output;

import com.assignment.research.adapter.pricing.CostEstimator;
import com.assignment.research.confidence.GroupConfidence;
import com.assignment.research.critique.CriticFinding;
import com.assignment.research.evidence.Source;
import com.assignment.research.pipeline.AgentFailure;
import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.BriefingState;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.reconciliation.EvidenceGroup;
import com.assignment.research.synthesis.GroundedStatement;
import com.assignment.research.trace.TraceEntry;
import com.assignment.research.trace.TraceStatistics;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MarkdownBriefingRenderer {

    private final Clock clock;
    private final CostEstimator costEstimator;

    public MarkdownBriefingRenderer(Clock clock) {
        this(clock, CostEstimator.free());
    }

    public MarkdownBriefingRenderer(Clock clock, CostEstimator costEstimator) {
        this.clock = clock;
        this.costEstimator = costEstimator;
    }

    public String render(BriefingResult result) {
        var state = result.getFinalState();
        var groupsById = state.getGroups().stream()
                .collect(Collectors.toMap(EvidenceGroup::getId, Function.identity()));
        var confidenceById = state.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, Function.identity()));
        var draft = result.getDraft();

        var out = new StringBuilder();
        header(out, state, result);
        subQuestions(out, state, result);
        section(out, OutputConstants.HEADING_SUMMARY, draft.getSummary(), groupsById, confidenceById, false);
        section(out, OutputConstants.HEADING_KEY_FACTS, draft.getKeyFacts(), groupsById, confidenceById, true);
        section(out, OutputConstants.HEADING_UNCERTAINTIES, draft.getUncertainties(), groupsById, confidenceById,
                false);
        gaps(out, result);
        confidence(out, result);
        openFindings(out, result.getOpenFindings());
        followUps(out, draft.getFollowUpQuestions());
        howProduced(out, result);
        sources(out, state);
        return out.toString();
    }

    private void header(StringBuilder out, BriefingState state, BriefingResult result) {
        line(out, OutputConstants.BRIEFING_TITLE);
        blankLine(out);
        line(out, OutputConstants.QUERY_LINE.formatted(state.getQuery().getText()));
        line(out, OutputConstants.GENERATED_LINE.formatted(
                LocalDateTime.now(clock).format(OutputConstants.GENERATED_FORMAT)));
        line(out, OutputConstants.OVERALL_CONFIDENCE_LINE.formatted(result.getConfidence().getLevel()));
        line(out, OutputConstants.SCOPE_LINE.formatted(state.getInterpretation().orElse(OutputConstants.EMPTY)));
        blankLine(out);
    }

    private static void section(StringBuilder out, String heading, List<GroundedStatement> statements,
            Map<String, EvidenceGroup> groups, Map<String, GroupConfidence> confidences, boolean withEvidence) {
        heading(out, heading);
        if (statements.isEmpty()) {
            noneLine(out);
            return;
        }
        for (var statement : statements) {
            var groupRefs = statement.getGroupIds().stream().map(OutputConstants.GROUP_REF::formatted)
                    .collect(Collectors.joining());
            bullet(out, statement.getText() + groupRefs);
            if (withEvidence) {
                statement.getGroupIds().stream().map(groups::get).filter(Objects::nonNull)
                        .forEach(group -> line(out, evidenceNote(group, confidences.get(group.getId()))));
            }
        }
        blankLine(out);
    }

    private static String evidenceNote(EvidenceGroup group, GroupConfidence confidence) {
        return String.format(Locale.ROOT, OutputConstants.EVIDENCE_NOTE, group.getIndependentSourceCount(),
                group.getBestTier(), group.getNewestSourceDate(), group.getConflictStatus(), confidence.getLevel(),
                confidence.getScore());
    }

    private static void subQuestions(StringBuilder out, BriefingState state, BriefingResult result) {
        var gapIds = result.getGaps().stream().map(SubQuestion::getId).collect(Collectors.toSet());
        heading(out, OutputConstants.HEADING_SUB_QUESTIONS);
        for (var question : state.getSubQuestions()) {
            var status = gapIds.contains(question.getId())
                    ? OutputConstants.COVERAGE_GAP
                    : OutputConstants.COVERAGE_OK;
            line(out, OutputConstants.SUB_QUESTION_LINE.formatted(question.getId(), question.getQuestion(), status));
        }
        blankLine(out);
    }

    private static void gaps(StringBuilder out, BriefingResult result) {
        var gaps = result.getGaps();
        if (gaps.isEmpty()) {
            return;
        }
        line(out, OutputConstants.GAPS_HEADING);
        blankLine(out);
        gaps.forEach(gap -> line(out, OutputConstants.GAP_LINE.formatted(gap.getId(), gap.getQuestion())));
        blankLine(out);
    }

    private static void confidence(StringBuilder out, BriefingResult result) {
        var confidence = result.getConfidence();
        heading(out, OutputConstants.HEADING_CONFIDENCE.formatted(confidence.getLevel()));
        line(out, OutputConstants.CONFIDENCE_INTRO);
        blankLine(out);
        confidence.getReasons().forEach(reason -> bullet(out, reason));
        blankLine(out);
    }

    private static void openFindings(StringBuilder out, List<CriticFinding> findings) {
        heading(out, OutputConstants.HEADING_OPEN_FINDINGS);
        if (findings.isEmpty()) {
            line(out, OutputConstants.REVIEWER_APPROVED);
            blankLine(out);
            return;
        }
        findings.forEach(finding -> line(out, OutputConstants.FINDING_LINE.formatted(finding.getSeverity(),
                finding.getType(), finding.getQuotedText(), finding.getDetail())));
        blankLine(out);
    }

    private static void followUps(StringBuilder out, List<String> questions) {
        heading(out, OutputConstants.HEADING_FOLLOW_UPS);
        if (questions.isEmpty()) {
            noneLine(out);
            return;
        }
        questions.forEach(question -> bullet(out, question));
        blankLine(out);
    }

    private void howProduced(StringBuilder out, BriefingResult result) {
        var state = result.getFinalState();
        var stopDecision = result.getStopDecision();
        var usage = result.getUsage();
        var draft = result.getDraft();
        heading(out, OutputConstants.HEADING_HOW_PRODUCED);
        line(out, OutputConstants.STOP_REASON_LINE.formatted(stopDecision.getReason(),
                stopDecision.getExplanation()));
        line(out, OutputConstants.ROUNDS_LINE.formatted(state.getRound()));
        var trace = result.getTrace();
        line(out, OutputConstants.MODEL_CALLS_LINE.formatted(trace.size(), usage.getInputTokens(),
                usage.getCachedInputTokens(), usage.getOutputTokens()));
        cost(out, trace);
        line(out, OutputConstants.CLAIMS_LINE.formatted(state.getClaims().size(), state.getGroups().size()));
        countLine(out, OutputConstants.DROPPED_STATEMENTS_LINE, draft.getDroppedStatements());
        countLine(out, OutputConstants.DEMOTED_KEY_FACTS_LINE, draft.getDemotedKeyFacts());
        failures(out, state.getFailures());
        blankLine(out);
        roleTable(out, trace);
    }

    private void cost(StringBuilder out, List<TraceEntry> trace) {
        if (costEstimator.isFree()) {
            return;
        }
        var cost = costEstimator.estimateRun(trace);
        line(out, String.format(Locale.ROOT, OutputConstants.COST_LINE, cost.getTotal(), cost.getFreshInputCost(),
                cost.getCachedInputCost(), cost.getOutputCost()));
    }

    private static void roleTable(StringBuilder out, List<TraceEntry> trace) {
        if (trace.isEmpty()) {
            return;
        }
        line(out, OutputConstants.ROLE_TABLE_HEADER);
        line(out, OutputConstants.ROLE_TABLE_DIVIDER);
        for (var role : TraceStatistics.byRole(trace)) {
            var usage = role.getUsage();
            var seconds = role.getDuration().toMillis() / OutputConstants.MILLIS_PER_SECOND;
            line(out, String.format(Locale.ROOT, OutputConstants.ROLE_TABLE_ROW, role.getRole(), role.getCalls(),
                    usage.getInputTokens(), usage.getCachedInputTokens(), usage.getOutputTokens(), seconds));
        }
        blankLine(out);
    }

    private static void countLine(StringBuilder out, String format, List<String> items) {
        if (items.isEmpty()) {
            return;
        }
        line(out, format.formatted(items.size()));
    }

    private static void failures(StringBuilder out, List<AgentFailure> failures) {
        if (failures.isEmpty()) {
            return;
        }
        line(out, OutputConstants.DEGRADED_STEPS_LINE);
        failures.forEach(failure -> line(out, OutputConstants.FAILURE_LINE.formatted(failure.getRound(),
                failure.getAgent(), failure.getMessage())));
    }

    private static void sources(StringBuilder out, BriefingState state) {
        heading(out, OutputConstants.HEADING_SOURCES);
        line(out, OutputConstants.SOURCES_DISCLAIMER);
        blankLine(out);
        for (Source source : state.getSources()) {
            var cites = source.getCitedSource().map(OutputConstants.CITES_SUFFIX::formatted)
                    .orElse(OutputConstants.EMPTY);
            line(out, OutputConstants.SOURCE_LINE.formatted(source.getId(), source.getTitle(), source.getPublisher(),
                    source.getType(), source.getTier(), source.getPublishedAt(), cites));
        }
        blankLine(out);
    }

    private static void heading(StringBuilder out, String title) {
        line(out, OutputConstants.SECTION_HEADING.formatted(title));
        blankLine(out);
    }

    private static void noneLine(StringBuilder out) {
        line(out, OutputConstants.NONE_PLACEHOLDER);
        blankLine(out);
    }

    private static void bullet(StringBuilder out, String text) {
        line(out, OutputConstants.BULLET + text);
    }

    private static void line(StringBuilder out, String text) {
        out.append(text).append(OutputConstants.NEWLINE);
    }

    private static void blankLine(StringBuilder out) {
        out.append(OutputConstants.NEWLINE);
    }
}
