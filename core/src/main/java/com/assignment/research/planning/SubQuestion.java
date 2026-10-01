package com.assignment.research.planning;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class SubQuestion {

    @NonNull String id;
    @NonNull String question;
    @NonNull List<String> searchKeywords;
}
