package com.assignment.research.evidence;

import lombok.NonNull;
import lombok.Value;

@Value
public class SearchHit {

    @NonNull Source source;
    int score;
}
