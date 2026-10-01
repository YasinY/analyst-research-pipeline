package com.assignment.research.reconciliation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConflictStatusTest {

    @Test
    void worseOfKeepsTheMoreSevereStatusInEitherOrder() {
        assertThat(ConflictStatus.OPEN.worseOf(ConflictStatus.NONE)).isEqualTo(ConflictStatus.OPEN);
        assertThat(ConflictStatus.NONE.worseOf(ConflictStatus.SUPERSEDED)).isEqualTo(ConflictStatus.SUPERSEDED);
    }
}
