package com.assignment.research.evidence;

import lombok.NonNull;
import lombok.Value;

@Value
public class SearchHit {

    @NonNull
    private final Source source;
    private final int score;
}
