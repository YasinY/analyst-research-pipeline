package com.assignment.research.planning;

import com.assignment.research.llm.LlmPort;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.query.AnalystQuery;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Planner {

    private final LlmPort llm;
    private final PromptTemplates prompts;

    public Plan plan(AnalystQuery query) {
        var template = prompts.forAgent(AgentName.PLANNER);
        var userPrompt = template.renderUserPrompt(Map.of(PlanningConstants.QUERY_VARIABLE, query.getText()));
        var request = new LlmRequest(PlanningConstants.TRACE_LABEL, template.getSystemPrompt(), userPrompt,
                PlanningConstants.MAX_OUTPUT_TOKENS);

        var output = llm.complete(request, PlanOutput.class).getValue();
        var subQuestions = toSubQuestions(output.getSubQuestions());
        if (subQuestions.isEmpty()) {
            throw new EmptyPlanException();
        }
        return new Plan(output.getInterpretation(), subQuestions);
    }

    private List<SubQuestion> toSubQuestions(List<PlannedQuestion> planned) {
        var usable = planned.stream()
                .filter(question -> !question.getQuestion().isBlank())
                .limit(PlanningConstants.MAX_SUB_QUESTIONS)
                .toList();
        return IntStream.range(0, usable.size())
                .mapToObj(index -> toSubQuestion(index + PlanningConstants.FIRST_ID, usable.get(index)))
                .toList();
    }

    private SubQuestion toSubQuestion(int number, PlannedQuestion planned) {
        var id = PlanningConstants.SUB_QUESTION_ID_FORMAT.formatted(number);
        var question = planned.getQuestion().strip();
        var keywords = normalizeKeywords(planned.getSearchKeywords());
        if (keywords.isEmpty()) {
            keywords = fallbackKeywords(question);
        }
        return new SubQuestion(id, question, keywords);
    }

    private static List<String> normalizeKeywords(List<String> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.strip().toLowerCase(Locale.ROOT))
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .toList();
    }

    private static List<String> fallbackKeywords(String question) {
        return PlanningConstants.WORD_SEPARATOR.splitAsStream(question.toLowerCase(Locale.ROOT))
                .filter(word -> word.length() >= PlanningConstants.MIN_FALLBACK_KEYWORD_LENGTH)
                .distinct()
                .toList();
    }
}
