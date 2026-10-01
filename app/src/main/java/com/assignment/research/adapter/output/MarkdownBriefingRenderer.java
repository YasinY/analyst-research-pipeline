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
import java.util.Comparator;
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
        var sourcesById = state.getSources().stream()
                .collect(Collectors.toMap(Source::getId, Function.identity()));
        var confidenceById = state.getConfidences().stream()
                .collect(Collectors.toMap(GroupConfidence::getGroupId, Function.identity()));
        Function<List<EvidenceGroup>, List<String>> noDetails = cited -> List.of();
        Function<List<EvidenceGroup>, List<String>> evidenceNotes = cited -> evidenceNote(cited, confidenceById);
        var draft = result.getDraft();

        var out = new StringBuilder();
        header(out, state, result);
        section(out, OutputConstants.HEADING_SUMMARY, draft.getSummary(), groupsById, sourcesById, noDetails);
        section(out, OutputConstants.HEADING_KEY_FACTS, draft.getKeyFacts(), groupsById, sourcesById,
                evidenceNotes);
        section(out, OutputConstants.HEADING_UNCERTAINTIES, draft.getUncertainties(), groupsById, sourcesById,
                noDetails);
        gaps(out, result);
        confidence(out, result);
        followUps(out, draft.getFollowUpQuestions());
        openFindings(out, result.getOpenFindings());
        subQuestions(out, state, result);
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
            Map<String, EvidenceGroup> groups, Map<String, Source> sources,
            Function<List<EvidenceGroup>, List<String>> details) {
        heading(out, heading);
        if (statements.isEmpty()) {
            noneLine(out);
            return;
        }
        for (var statement : statements) {
            var cited = statement.getGroupIds().stream().map(groups::get).filter(Objects::nonNull).toList();
            bullet(out, OutputConstants.STATEMENT_LINE.formatted(statement.getText(), attribution(cited, sources)));
            details.apply(cited).forEach(detail -> line(out, detail));
        }
        blankLine(out);
    }

    private static String attribution(List<EvidenceGroup> cited, Map<String, Source> sources) {
        var publishers = cited.stream()
                .flatMap(group -> group.getIndependentSourceIds().stream())
                .map(sources::get)
                .filter(Objects::nonNull)
                .map(source -> OutputConstants.PUBLISHER_WITH_YEAR.formatted(source.getPublisher(),
                        source.getPublishedAt().getYear()))
                .distinct()
                .collect(Collectors.joining(OutputConstants.LIST_SEPARATOR));
        if (publishers.isEmpty()) {
            return OutputConstants.SOURCES_NOT_IDENTIFIED;
        }
        return OutputConstants.SOURCES_ATTRIBUTION.formatted(publishers);
    }

    private static List<String> evidenceNote(List<EvidenceGroup> cited, Map<String, GroupConfidence> confidences) {
        if (cited.isEmpty()) {
            return List.of();
        }
        var level = cited.stream()
                .map(group -> confidences.get(group.getId()))
                .max(Comparator.comparingDouble(GroupConfidence::getScore))
                .orElseThrow()
                .getLevel();
        var worstConflict = cited.stream()
                .map(EvidenceGroup::getConflictStatus)
                .max(Comparator.comparingInt(OutputConstants.CONFLICT_SEVERITY_ORDER::indexOf))
                .orElseThrow();
        return List.of(OutputConstants.EVIDENCE_NOTE.formatted(sourcesPhrase(cited),
                OutputConstants.CONFLICT_WORDS.get(worstConflict), level));
    }

    private static String sourcesPhrase(List<EvidenceGroup> cited) {
        var strongestTier = cited.stream().map(EvidenceGroup::getBestTier).min(Comparator.naturalOrder()).orElseThrow();
        var tier = OutputConstants.TIER_WORDS.get(strongestTier);
        var published = cited.stream().map(EvidenceGroup::getNewestSourceDate).max(Comparator.naturalOrder())
                .orElseThrow().format(OutputConstants.MONTH_YEAR_FORMAT);
        var count = (int) cited.stream().flatMap(group -> group.getIndependentSourceIds().stream()).distinct().count();
        if (count == OutputConstants.SINGLE_SOURCE_COUNT) {
            return OutputConstants.SINGLE_SOURCE_PHRASE.formatted(tier, published);
        }
        var countWord = OutputConstants.COUNT_WORDS.getOrDefault(count, String.valueOf(count));
        return OutputConstants.MULTIPLE_SOURCES_PHRASE.formatted(countWord, tier, published);
    }

    private static void subQuestions(StringBuilder out, BriefingState state, BriefingResult result) {
        var gapIds = result.getGaps().stream().map(SubQuestion::getId).collect(Collectors.toSet());
        heading(out, OutputConstants.HEADING_SUB_QUESTIONS);
        for (var question : state.getSubQuestions()) {
            var status = gapIds.contains(question.getId())
                    ? OutputConstants.COVERAGE_GAP
                    : OutputConstants.COVERAGE_OK;
            line(out, OutputConstants.SUB_QUESTION_LINE.formatted(question.getQuestion(), status));
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
        gaps.forEach(gap -> line(out, OutputConstants.GAP_LINE.formatted(gap.getQuestion())));
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
        findings.forEach(finding -> line(out, OutputConstants.FINDING_LINE.formatted(
                OutputConstants.SEVERITY_WORDS.get(finding.getSeverity()),
                OutputConstants.FINDING_TYPE_WORDS.get(finding.getType()), finding.getQuotedText(),
                finding.getDetail())));
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
        line(out, String.format(Locale.ROOT, OutputConstants.MEDIAN_SCORE_LINE,
                result.getConfidence().getMedianKeyFactScore()));
        keyFactGroups(out, draft.getKeyFacts());
        countLine(out, OutputConstants.DROPPED_STATEMENTS_LINE, draft.getDroppedStatements());
        countLine(out, OutputConstants.DEMOTED_KEY_FACTS_LINE, draft.getDemotedKeyFacts());
        failures(out, state.getFailures());
        blankLine(out);
        roleTable(out, trace);
    }

    private static void keyFactGroups(StringBuilder out, List<GroundedStatement> keyFacts) {
        var number = OutputConstants.FIRST_KEY_FACT_NUMBER;
        for (var keyFact : keyFacts) {
            line(out, keyFactGroupsLine(number, keyFact.getGroupIds()));
            number++;
        }
    }

    private static String keyFactGroupsLine(int number, List<String> groupIds) {
        if (groupIds.isEmpty()) {
            return OutputConstants.KEY_FACT_WITHOUT_GROUPS_LINE.formatted(number);
        }
        return OutputConstants.KEY_FACT_GROUPS_LINE.formatted(number,
                String.join(OutputConstants.LIST_SEPARATOR, groupIds));
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
