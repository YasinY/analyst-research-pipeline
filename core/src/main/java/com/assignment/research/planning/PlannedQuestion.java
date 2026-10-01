package com.assignment.research.planning;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class PlannedQuestion {

    @NonNull
    private final String question;
    @NonNull
    private final List<String> searchKeywords;
}
