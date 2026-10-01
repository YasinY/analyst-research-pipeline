package com.assignment.research.pipeline;

public enum StopReason {
    APPROVED,
    ROUND_LIMIT_REACHED,
    REWRITE_LIMIT_REACHED,
    CALL_BUDGET_EXHAUSTED,
    NO_NEW_EVIDENCE,
    AGENT_FAILURE
}
