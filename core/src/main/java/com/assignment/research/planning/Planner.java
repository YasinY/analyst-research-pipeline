package com.assignment.research.planning;

import com.assignment.research.llm.LLMPort;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.query.AnalystQuery;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Planner {

    private final LLMPort llm;
    private final PromptTemplates prompts;

    public Plan plan(AnalystQuery query) {
        var template = prompts.forAgent(AgentName.PLANNER);
        var userPrompt = template.renderUserPrompt(Map.of(PlanningConstants.QUERY_VARIABLE, query.getText()));
        var request = new LLMRequest(PlanningConstants.TRACE_LABEL, template.getSystemPrompt(), userPrompt,
                PlanningConstants.MAX_OUTPUT_TOKENS);

        var output = llm.complete(request, PlanOutput.class).getValue();
        var subQuestions = toSubQuestions(output.getSubQuestions());
        if (subQuestions.isEmpty()) {
            throw new EmptyPlanException();
        }
        return new Plan(output.getInterpretation(), subQuestions);
    }

    private static List<SubQuestion> toSubQuestions(List<PlannedQuestion> planned) {
        var usable = planned.stream()
                .filter(question -> !question.getQuestion().isBlank())
                .limit(PlanningConstants.MAX_SUB_QUESTIONS)
                .toList();
        return IntStream.range(0, usable.size())
                .mapToObj(index -> toSubQuestion(index + PlanningConstants.FIRST_ID, usable.get(index)))
                .toList();
    }

    private static SubQuestion toSubQuestion(int number, PlannedQuestion planned) {
        var id = PlanningConstants.SUB_QUESTION_ID_FORMAT.formatted(number);
        var question = planned.getQuestion().strip();
        var keywords = Keywords.normalizeOrDerive(planned.getSearchKeywords(), question);
        return new SubQuestion(id, question, keywords);
    }
}
