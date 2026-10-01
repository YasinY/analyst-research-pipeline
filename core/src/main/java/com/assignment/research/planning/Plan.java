package com.assignment.research.planning;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class Plan {

    @NonNull
    private final String interpretation;
    @NonNull
    private final List<SubQuestion> subQuestions;
}
