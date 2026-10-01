package com.assignment.research.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AnalystQueryTest {

    private static final String PADDED_TEXT = "  Overview of the dry bulk market  ";
    private static final String STRIPPED_TEXT = "Overview of the dry bulk market";
    private static final String BLANK_TEXT = "   ";

    @Test
    void stripsSurroundingWhitespace() {
        assertThat(new AnalystQuery(PADDED_TEXT).getText()).isEqualTo(STRIPPED_TEXT);
    }

    @Test
    void rejectsBlankText() {
        assertThatThrownBy(() -> new AnalystQuery(BLANK_TEXT)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullText() {
        assertThatThrownBy(() -> new AnalystQuery(null)).isInstanceOf(NullPointerException.class);
    }
}
