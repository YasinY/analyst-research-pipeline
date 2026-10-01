package com.assignment.research.reconciliation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class IntraGroupConflictSplitter {

    private IntraGroupConflictSplitter() {
    }

    public static ReconciliationOutput split(ReconciliationOutput output) {
        var groupsById = new LinkedHashMap<String, ClaimGroupOutput>();
        output.getGroups().forEach(group -> groupsById.putIfAbsent(group.getId(), group));
        var conflicts = new ArrayList<ConflictOutput>();

        for (var conflict : output.getConflicts()) {
            var distinctIds = conflict.getGroupIds().stream().distinct().toList();
            var single = distinctIds.size() == 1 ? groupsById.get(distinctIds.getFirst()) : null;
            if (single == null || single.getClaimIds().size() < ReconciliationConstants.MIN_GROUPS_IN_CONFLICT) {
                conflicts.add(conflict);
                continue;
            }
            var parts = splitIntoSingletons(single);
            groupsById.remove(single.getId());
            parts.forEach(part -> groupsById.put(part.getId(), part));
            conflicts.add(new ConflictOutput(parts.stream().map(ClaimGroupOutput::getId).toList(),
                    conflict.getDescription()));
        }
        return new ReconciliationOutput(List.copyOf(groupsById.values()), List.copyOf(conflicts));
    }

    private static List<ClaimGroupOutput> splitIntoSingletons(ClaimGroupOutput group) {
        var parts = new ArrayList<ClaimGroupOutput>();
        var number = ReconciliationConstants.FIRST_GROUP_NUMBER;
        for (var claimId : group.getClaimIds()) {
            var partId = ReconciliationConstants.SPLIT_GROUP_ID_FORMAT.formatted(group.getId(), number++);
            parts.add(new ClaimGroupOutput(partId, ReconciliationConstants.EMPTY_ASSERTION, List.of(claimId)));
        }
        return parts;
    }
}
