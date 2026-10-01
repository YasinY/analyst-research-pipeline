package com.assignment.research.evidence;

import static com.assignment.research.evidence.EvidenceConstants.CLAIM_ID_FORMAT;
import static com.assignment.research.evidence.EvidenceConstants.FIRST_CLAIM_NUMBER;
import static com.assignment.research.evidence.EvidenceConstants.MAX_OUTPUT_TOKENS;
import static com.assignment.research.evidence.EvidenceConstants.MAX_SEARCH_HITS;
import static com.assignment.research.evidence.EvidenceConstants.QUESTION_VARIABLE;
import static com.assignment.research.evidence.EvidenceConstants.SOURCES_VARIABLE;
import static com.assignment.research.evidence.EvidenceConstants.STATEMENT_KEY_SEPARATOR;
import static com.assignment.research.evidence.EvidenceConstants.TRACE_LABEL_FORMAT;

import com.assignment.research.llm.LlmPort;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Researcher {

    private final LlmPort llm;
    private final SourceSearchPort search;
    private final PromptTemplates prompts;

    public ResearchResult research(SubQuestion question, int round) {
        var sources = search.search(question.getSearchKeywords(), MAX_SEARCH_HITS).stream()
                .map(SearchHit::getSource)
                .toList();
        if (sources.isEmpty()) {
            return ResearchResult.withoutEvidence(question.getId());
        }

        var output = llm.complete(buildRequest(question, sources, round), ResearchOutput.class).getValue();
        return toResult(question.getId(), sources, output.getClaims());
    }

    private LlmRequest buildRequest(SubQuestion question, List<Source> sources, int round) {
        var template = prompts.forAgent(AgentName.RESEARCHER);
        var userPrompt = template.renderUserPrompt(Map.of(
                QUESTION_VARIABLE, question.getQuestion(),
                SOURCES_VARIABLE, SourcePromptFormatter.format(sources)));
        var label = TRACE_LABEL_FORMAT.formatted(question.getId(), round);
        return new LlmRequest(label, template.getSystemPrompt(), userPrompt, MAX_OUTPUT_TOKENS);
    }

    private static ResearchResult toResult(String subQuestionId, List<Source> sources, List<ExtractedClaim> extracted) {
        var knownSourceIds = sources.stream().map(Source::getId).collect(Collectors.toSet());
        var rejectedSourceIds = new LinkedHashSet<String>();
        var claims = new ArrayList<Claim>();
        var seenStatements = new LinkedHashSet<String>();

        for (var candidate : extracted) {
            if (!knownSourceIds.contains(candidate.getSourceId())) {
                rejectedSourceIds.add(candidate.getSourceId());
                continue;
            }
            var statement = candidate.getStatement().strip();
            if (statement.isEmpty() || !seenStatements.add(statementKey(candidate.getSourceId(), statement))) {
                continue;
            }
            var claimId = CLAIM_ID_FORMAT.formatted(subQuestionId, claims.size() + FIRST_CLAIM_NUMBER);
            claims.add(new Claim(claimId, subQuestionId, statement, candidate.getSourceId()));
        }
        return new ResearchResult(subQuestionId, sources, List.copyOf(claims), List.copyOf(rejectedSourceIds));
    }

    private static String statementKey(String sourceId, String statement) {
        return sourceId + STATEMENT_KEY_SEPARATOR + statement;
    }
}
