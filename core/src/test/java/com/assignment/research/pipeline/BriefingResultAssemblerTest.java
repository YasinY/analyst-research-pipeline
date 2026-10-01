package com.assignment.research.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.query.AnalystQuery;
import com.assignment.research.synthesis.BriefingDraft;
import com.assignment.research.trace.InMemoryTraceSink;
import java.util.List;
import org.junit.jupiter.api.Test;

class BriefingResultAssemblerTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk shipping market");
    private static final BriefingDraft DRAFT =
            new BriefingDraft(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

    @Test
    void assemblingAResultWithoutAStopDecisionIsRejected() {
        var unfinished = BriefingState.initial(QUERY).toBuilder().draft(DRAFT).build();

        assertThatThrownBy(() -> BriefingResultAssembler.assemble(unfinished, new InMemoryTraceSink()))
                .isInstanceOf(IllegalStateException.class);
    }
}
