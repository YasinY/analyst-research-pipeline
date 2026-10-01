package com.assignment.research.trace;

import java.util.LinkedHashMap;
import java.util.List;

public final class TraceStatistics {

    private TraceStatistics() {
    }

    public static List<RoleStatistics> byRole(List<TraceEntry> entries) {
        var statisticsByRole = new LinkedHashMap<String, RoleStatistics>();
        for (var entry : entries) {
            var role = roleOf(entry.getLabel());
            var current = statisticsByRole.getOrDefault(role, RoleStatistics.empty(role));
            statisticsByRole.put(role, current.plus(entry));
        }
        return List.copyOf(statisticsByRole.values());
    }

    public static String roleOf(String label) {
        var separator = label.indexOf(TraceConstants.ROLE_SEPARATOR);
        if (separator == TraceConstants.NO_SEPARATOR) {
            return label;
        }
        return label.substring(TraceConstants.LABEL_START, separator);
    }
}
