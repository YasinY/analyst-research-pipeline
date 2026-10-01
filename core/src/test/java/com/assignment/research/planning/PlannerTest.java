package com.assignment.research.planning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.llm.FakeLLMPort;
import com.assignment.research.prompt.FakePromptTemplates;
import com.assignment.research.query.AnalystQuery;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class PlannerTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk shipping market");
    private static final String INTERPRETATION = "market overview with risk focus";

    private final FakePromptTemplates prompts = FakePromptTemplates.withQueryPlaceholder();

    @Test
    void assignsSequentialIdsAndNormalizesKeywords() {
        var output = new PlanOutput(INTERPRETATION, List.of(
                new PlannedQuestion("What drives demand?", List.of(" Iron Ore", "coal", "iron ore", "")),
                new PlannedQuestion("What drives supply?", List.of("fleet growth"))));
        var llm = FakeLLMPort.returning(output);

        var plan = new Planner(llm, prompts).plan(QUERY);

        assertThat(plan.getInterpretation()).isEqualTo(INTERPRETATION);
        assertThat(plan.getSubQuestions()).extracting(SubQuestion::getId).containsExactly("q1", "q2");
        assertThat(plan.getSubQuestions().getFirst().getSearchKeywords()).containsExactly("iron ore", "coal");
    }

    @Test
    void sendsQueryTextInsideRenderedUserPrompt() {
        var llm = FakeLLMPort.returning(new PlanOutput(INTERPRETATION,
                List.of(new PlannedQuestion("Q", List.of("k")))));

        new Planner(llm, prompts).plan(QUERY);

        var request = llm.getLastRequest();
        assertThat(request.getLabel()).isEqualTo("planner");
        assertThat(request.getSystemPrompt()).isEqualTo(FakePromptTemplates.SYSTEM_PROMPT);
        assertThat(request.getUserPrompt()).contains(QUERY.getText());
    }

    @Test
    void truncatesToAtMostFiveSubQuestions() {
        var planned = IntStream.rangeClosed(1, 8)
                .mapToObj(number -> new PlannedQuestion("Question " + number, List.of("k" + number)))
                .toList();
        var llm = FakeLLMPort.returning(new PlanOutput(INTERPRETATION, planned));

        var plan = new Planner(llm, prompts).plan(QUERY);

        assertThat(plan.getSubQuestions()).hasSize(5);
    }

    @Test
    void derivesFallbackKeywordsFromQuestionWhenModelGivesNone() {
        var llm = FakeLLMPort.returning(new PlanOutput(INTERPRETATION,
                List.of(new PlannedQuestion("How do freight rates react to fleet growth?", List.of()))));

        var plan = new Planner(llm, prompts).plan(QUERY);

        assertThat(plan.getSubQuestions().getFirst().getSearchKeywords())
                .containsExactly("freight", "rates", "react", "fleet", "growth");
    }

    @Test
    void rejectsPlanWithoutUsableSubQuestions() {
        var llm = FakeLLMPort.returning(new PlanOutput(INTERPRETATION,
                List.of(new PlannedQuestion("   ", List.of("k")))));

        assertThatThrownBy(() -> new Planner(llm, prompts).plan(QUERY)).isInstanceOf(EmptyPlanException.class);
    }
}
