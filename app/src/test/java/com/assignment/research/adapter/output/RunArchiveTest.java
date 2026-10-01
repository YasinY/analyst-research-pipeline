package com.assignment.research.adapter.output;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RunArchiveTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-20T10:15:30.123Z"), ZoneOffset.UTC);
    private static final String FIRST_FOLDER = "2026-05-20T10-15-30-123";
    private static final String SECOND_FOLDER = "2026-05-20T10-15-30-123-2";

    @TempDir
    private Path runsRoot;

    @Test
    void twoArchivesStartedAtTheSameInstantGetSeparateFolders() {
        var first = newArchive();
        var second = newArchive();

        assertThat(first.getDirectory()).isNotEqualTo(second.getDirectory());
        assertThat(first.getDirectory().getFileName()).hasToString(FIRST_FOLDER);
        assertThat(second.getDirectory().getFileName()).hasToString(SECOND_FOLDER);
        assertThat(second.getDirectory().resolve(OutputConstants.CALLS_DIRECTORY)).isDirectory();
        assertThat(second.getDirectory().resolve(OutputConstants.SNAPSHOTS_DIRECTORY)).isDirectory();
    }

    private RunArchive newArchive() {
        return new RunArchive(runsRoot, FIXED_CLOCK, JSONMapperFactory.create(),
                new PrintStream(OutputStream.nullOutputStream()));
    }
}
