package com.assignment.research.planning;

public class EmptyPlanException extends RuntimeException {

    public EmptyPlanException() {
        super("planner returned no usable sub-questions");
    }
}
