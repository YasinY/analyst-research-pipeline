package com.assignment.research.synthesis;

import java.util.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class GroundedStatement {

    @NonNull
    private final String text;
    @NonNull
    private final List<String> groupIds;

    public boolean isGrounded() {
        return !groupIds.isEmpty();
    }
}
