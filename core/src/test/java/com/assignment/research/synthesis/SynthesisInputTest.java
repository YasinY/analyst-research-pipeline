package com.assignment.research.synthesis;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.query.AnalystQuery;
import java.util.List;
import org.junit.jupiter.api.Test;

class SynthesisInputTest {

    private static final AnalystQuery QUERY = new AnalystQuery("Overview of the dry bulk market");
    private static final String INTERPRETATION = "overview";
    private static final BriefingDraft DRAFT =
            new BriefingDraft(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

    @Test
    void previousDraftWithoutFindingsIsNoRevision() {
        var input = SynthesisInput.firstDraft(QUERY, INTERPRETATION, List.of(), List.of(), List.of())
                .revisedWith(DRAFT, List.of());

        assertThat(input.isRevision()).isFalse();
        assertThat(input.getPreviousDraft()).contains(DRAFT);
    }
}
