package com.assignment.research.adapter.cli;

import java.util.Arrays;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        var exitCode = CliApplication.standard().run(Arrays.asList(args), System.getenv(), System.out, System.err);
        if (exitCode != CliConstants.EXIT_OK) {
            System.exit(exitCode);
        }
    }
}
