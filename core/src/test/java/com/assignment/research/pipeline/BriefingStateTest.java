package com.assignment.research.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.planning.SubQuestion;
import com.assignment.research.query.AnalystQuery;
import java.util.List;
import org.junit.jupiter.api.Test;

class BriefingStateTest {

    private static final AnalystQuery QUERY = new AnalystQuery("dry bulk shipping risk drivers");

    @Test
    void initialStateStartsInRoundOneWithoutContent() {
        var state = BriefingState.initial(QUERY);

        assertThat(state.getRound()).isEqualTo(1);
        assertThat(state.getSubQuestions()).isEmpty();
        assertThat(state.getClaims()).isEmpty();
    }

    @Test
    void withSubQuestionsAppendsAndLeavesOriginalUntouched() {
        var initial = BriefingState.initial(QUERY);
        var question = new SubQuestion("q1", "What drives demand?", List.of("demand", "iron ore"));

        var updated = initial.withSubQuestions(List.of(question));

        assertThat(initial.getSubQuestions()).isEmpty();
        assertThat(updated.getSubQuestions()).containsExactly(question);
    }
}
