package com.assignment.research.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.llm.FakeLLMPort;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.FakePromptTemplates;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResearcherTest {

    private static final SubQuestion QUESTION = new SubQuestion("q2", "What drives supply?", List.of("fleet"));
    private static final int ROUND = 1;
    private static final Source SOURCE_A = Sources.tierA("src-a");
    private static final Source SOURCE_B = Sources.tierC("src-b");

    private final FakePromptTemplates prompts =
            new FakePromptTemplates("Question: {{question}}\nSources:\n{{sources}}");

    @Test
    void keepsClaimsFromKnownSourcesAndRejectsInventedSourceIds() {
        var output = new ResearchOutput(List.of(
                new ExtractedClaim("Fleet grew 3.1% in 2025.", "src-a"),
                new ExtractedClaim("Rates will double.", "src-b"),
                new ExtractedClaim("Made up.", "src-does-not-exist")));
        var llm = FakeLLMPort.returning(output);
        var researcher = new Researcher(llm, FakeSourceSearchPort.returning(SOURCE_A, SOURCE_B), prompts);

        var result = researcher.research(QUESTION, ROUND);

        assertThat(result.getClaims()).extracting(Claim::getId).containsExactly("q2-c1", "q2-c2");
        assertThat(result.getClaims()).extracting(Claim::getSubQuestionId).containsOnly("q2");
        assertThat(result.getRejectedSourceIds()).containsExactly("src-does-not-exist");
        assertThat(result.getConsultedSources()).containsExactly(SOURCE_A, SOURCE_B);
    }

    @Test
    void skipsLLMEntirelyWhenSearchFindsNothing() {
        var llm = FakeLLMPort.returning(new ResearchOutput(List.of()));
        var researcher = new Researcher(llm, FakeSourceSearchPort.empty(), prompts);

        var result = researcher.research(QUESTION, ROUND);

        assertThat(result.hasClaims()).isFalse();
        assertThat(result.getConsultedSources()).isEmpty();
        assertThat(llm.getLastRequest()).isNull();
    }

    @Test
    void dropsBlankAndDuplicateStatements() {
        var output = new ResearchOutput(List.of(
                new ExtractedClaim("Fleet grew 3.1% in 2025.", "src-a"),
                new ExtractedClaim("  Fleet grew 3.1% in 2025.  ", "src-a"),
                new ExtractedClaim("   ", "src-a")));
        var llm = FakeLLMPort.returning(output);
        var researcher = new Researcher(llm, FakeSourceSearchPort.returning(SOURCE_A), prompts);

        var result = researcher.research(QUESTION, ROUND);

        assertThat(result.getClaims()).hasSize(1);
    }

    @Test
    void labelsCallPerSubQuestionAndRoundAndEmbedsSourceExcerpts() {
        var llm = FakeLLMPort.returning(new ResearchOutput(List.of()));
        var researcher = new Researcher(llm, FakeSourceSearchPort.returning(SOURCE_A), prompts);

        researcher.research(QUESTION, 2);

        var request = llm.getLastRequest();
        assertThat(request.getLabel()).isEqualTo("researcher/q2/round2");
        assertThat(request.getUserPrompt()).contains(QUESTION.getQuestion()).contains("[sourceId: src-a]")
                .contains(SOURCE_A.getExcerpt());
    }
}
