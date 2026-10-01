package dev.yazici.research;

import static org.assertj.core.api.Assertions.assertThat;

import dev.yazici.research.domain.AnalystQuery;
import dev.yazici.research.domain.BriefingState;
import dev.yazici.research.domain.SubQuestion;
import java.util.List;
import org.junit.jupiter.api.Test;

class BriefingStateTest {

    private static final AnalystQuery QUERY = new AnalystQuery("dry bulk shipping risk drivers");

    @Test
    void initialStateStartsInRoundOneWithoutContent() {
        var state = BriefingState.initial(QUERY);

        assertThat(state.round()).isEqualTo(1);
        assertThat(state.subQuestions()).isEmpty();
        assertThat(state.claims()).isEmpty();
    }

    @Test
    void withSubQuestionsAppendsAndLeavesOriginalUntouched() {
        var initial = BriefingState.initial(QUERY);
        var question = new SubQuestion("q1", "What drives demand?", List.of("demand", "iron ore"));

        var updated = initial.withSubQuestions(List.of(question));

        assertThat(initial.subQuestions()).isEmpty();
        assertThat(updated.subQuestions()).containsExactly(question);
    }
}
