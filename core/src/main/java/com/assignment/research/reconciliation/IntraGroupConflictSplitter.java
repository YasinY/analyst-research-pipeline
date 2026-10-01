package com.assignment.research.reconciliation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class IntraGroupConflictSplitter {

    private IntraGroupConflictSplitter() {
    }

    public static ReconciliationOutput split(ReconciliationOutput output) {
        var groupsById = new LinkedHashMap<String, ClaimGroupOutput>();
        output.getGroups().forEach(group -> groupsById.putIfAbsent(group.getId(), group));
        var partIdsBySplitId = new HashMap<String, List<String>>();

        for (var conflict : output.getConflicts()) {
            var splittable = splittableGroup(conflict, groupsById);
            if (splittable.isEmpty()) {
                continue;
            }
            var group = splittable.get();
            var parts = splitIntoSingletons(group);
            groupsById.remove(group.getId());
            parts.forEach(part -> groupsById.put(part.getId(), part));
            partIdsBySplitId.put(group.getId(), parts.stream().map(ClaimGroupOutput::getId).toList());
        }

        var conflicts = output.getConflicts().stream()
                .map(conflict -> referToParts(conflict, partIdsBySplitId))
                .toList();
        return new ReconciliationOutput(List.copyOf(groupsById.values()), conflicts);
    }

    private static Optional<ClaimGroupOutput> splittableGroup(ConflictOutput conflict,
            Map<String, ClaimGroupOutput> groupsById) {
        var distinctIds = conflict.getGroupIds().stream().distinct().toList();
        if (distinctIds.size() != ReconciliationConstants.SELF_CONFLICT_GROUP_COUNT) {
            return Optional.empty();
        }
        return Optional.ofNullable(groupsById.get(distinctIds.getFirst()))
                .filter(group -> group.getClaimIds().size() >= ReconciliationConstants.MIN_CLAIMS_TO_SPLIT);
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

    private static ConflictOutput referToParts(ConflictOutput conflict, Map<String, List<String>> partIdsBySplitId) {
        var groupIds = conflict.getGroupIds();
        if (groupIds.stream().noneMatch(partIdsBySplitId::containsKey)) {
            return conflict;
        }
        var rewrittenIds = groupIds.stream()
                .flatMap(groupId -> partIdsBySplitId.getOrDefault(groupId, List.of(groupId)).stream())
                .distinct()
                .toList();
        return new ConflictOutput(rewrittenIds, conflict.getDescription());
    }
}
