package com.assignment.research.adapter.search;

import java.util.regex.Pattern;

public final class SearchConstants {

    public static final String FIELD_SEPARATOR = " ";
    public static final String WORD_DELIMITER = " ";
    public static final Pattern WORD_SEPARATOR = Pattern.compile("[^\\p{L}\\p{N}]+");
    public static final int NO_MATCH = 0;
    public static final String CORPUS_READ_FAILURE = "cannot read corpus %s";

    private SearchConstants() {
    }
}
