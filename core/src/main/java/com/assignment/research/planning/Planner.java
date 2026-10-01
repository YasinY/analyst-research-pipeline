package com.assignment.research.planning;

import com.assignment.research.llm.LlmPort;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.prompt.AgentName;
import com.assignment.research.prompt.PromptTemplates;
import com.assignment.research.query.AnalystQuery;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Planner {

    private static final String TRACE_LABEL = "planner";
    private static final String QUERY_VARIABLE = "query";
    private static final String SUB_QUESTION_ID_FORMAT = "q%d";
    private static final int FIRST_ID = 1;
    private static final int MAX_SUB_QUESTIONS = 5;
    private static final int MAX_OUTPUT_TOKENS = 1024;
    private static final int MIN_FALLBACK_KEYWORD_LENGTH = 4;
    private static final Pattern WORD_SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");

    private final LlmPort llm;
    private final PromptTemplates prompts;

    public Plan plan(AnalystQuery query) {
        var template = prompts.forAgent(AgentName.PLANNER);
        var userPrompt = template.renderUserPrompt(Map.of(QUERY_VARIABLE, query.getText()));
        var request = new LlmRequest(TRACE_LABEL, template.getSystemPrompt(), userPrompt, MAX_OUTPUT_TOKENS);

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
                .limit(MAX_SUB_QUESTIONS)
                .toList();
        return IntStream.range(0, usable.size())
                .mapToObj(index -> toSubQuestion(index + FIRST_ID, usable.get(index)))
                .toList();
    }

    private SubQuestion toSubQuestion(int number, PlannedQuestion planned) {
        var id = String.format(SUB_QUESTION_ID_FORMAT, number);
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
        return WORD_SEPARATOR.splitAsStream(question.toLowerCase(Locale.ROOT))
                .filter(word -> word.length() >= MIN_FALLBACK_KEYWORD_LENGTH)
                .distinct()
                .toList();
    }
}
