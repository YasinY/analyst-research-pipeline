package com.assignment.research.planning;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class SubQuestion {

    @NonNull
    private final String id;
    @NonNull
    private final String question;
    @NonNull
    private final List<String> searchKeywords;
}
