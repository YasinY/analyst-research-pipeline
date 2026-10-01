package com.assignment.research.evidence;

import com.assignment.research.llm.LLMPort;
import com.assignment.research.llm.LLMRequest;
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

    private final LLMPort llm;
    private final SourceSearchPort search;
    private final PromptTemplates prompts;

    public ResearchResult research(SubQuestion question, int round, int firstClaimNumber) {
        var sources = search.search(question.getSearchKeywords(), EvidenceConstants.MAX_SEARCH_HITS).stream()
                .map(SearchHit::getSource)
                .toList();
        if (sources.isEmpty()) {
            return ResearchResult.withoutEvidence(question.getId());
        }

        var output = llm.complete(buildRequest(question, sources, round), ResearchOutput.class).getValue();
        return toResult(question.getId(), sources, output.getClaims(), firstClaimNumber);
    }

    private LLMRequest buildRequest(SubQuestion question, List<Source> sources, int round) {
        var template = prompts.forAgent(AgentName.RESEARCHER);
        var userPrompt = template.renderUserPrompt(Map.of(
                EvidenceConstants.QUESTION_VARIABLE, question.getQuestion(),
                EvidenceConstants.SOURCES_VARIABLE, SourcePromptFormatter.format(sources)));
        var label = EvidenceConstants.TRACE_LABEL_FORMAT.formatted(question.getId(), round);
        return new LLMRequest(label, template.getSystemPrompt(), userPrompt,
                EvidenceConstants.MAX_OUTPUT_TOKENS);
    }

    private static ResearchResult toResult(String subQuestionId, List<Source> sources, List<ExtractedClaim> extracted,
            int firstClaimNumber) {
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
            var claimNumber = firstClaimNumber + claims.size();
            var claimId = EvidenceConstants.CLAIM_ID_FORMAT.formatted(subQuestionId, claimNumber);
            claims.add(new Claim(claimId, subQuestionId, statement, candidate.getSourceId()));
        }
        return new ResearchResult(subQuestionId, sources, List.copyOf(claims), List.copyOf(rejectedSourceIds));
    }

    private static String statementKey(String sourceId, String statement) {
        return sourceId + EvidenceConstants.STATEMENT_KEY_SEPARATOR + statement;
    }
}
