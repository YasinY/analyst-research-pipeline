package com.assignment.research.reconciliation;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceTier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EvidenceGroupAssembler {

    private EvidenceGroupAssembler() {
    }

    public static Reconciliation assemble(
            String subQuestionId, List<Claim> claims, List<Source> sources, ReconciliationOutput output) {
        var claimsById = claims.stream().collect(Collectors.toMap(Claim::getId, Function.identity()));
        var sourcesById = sources.stream().collect(Collectors.toMap(Source::getId, Function.identity()));

        var drafts = assignClaimsToGroups(claimsById.keySet(), output.getGroups()).stream()
                .map(group -> draft(group, claimsById, sourcesById))
                .toList();
        var draftsByModelId = drafts.stream().collect(Collectors.toMap(GroupDraft::getModelGroupId, Function.identity()));
        var conflictsByModelId = indexConflicts(output.getConflicts(), draftsByModelId.keySet());
        var finalIds = assignFinalIds(subQuestionId, drafts);

        var groups = drafts.stream()
                .map(draft -> toGroup(subQuestionId, draft, conflictsByModelId, draftsByModelId, finalIds))
                .toList();
        return new Reconciliation(subQuestionId, groups);
    }

    private static List<ClaimGroupOutput> assignClaimsToGroups(
            Set<String> knownClaimIds, List<ClaimGroupOutput> modelGroups) {
        var assigned = new LinkedHashSet<String>();
        var groups = new ArrayList<ClaimGroupOutput>();
        for (var group : modelGroups) {
            var accepted = group.getClaimIds().stream().filter(knownClaimIds::contains).filter(assigned::add).toList();
            if (accepted.isEmpty()) {
                continue;
            }
            groups.add(new ClaimGroupOutput(group.getId(), group.getAssertion(), accepted));
        }
        knownClaimIds.stream()
                .filter(assigned::add)
                .forEach(claimId -> groups.add(new ClaimGroupOutput(claimId, "", List.of(claimId))));
        return groups;
    }

    private static GroupDraft draft(ClaimGroupOutput group, Map<String, Claim> claimsById,
            Map<String, Source> sourcesById) {
        var groupSources = group.getClaimIds().stream()
                .map(claimsById::get)
                .map(Claim::getSourceId)
                .distinct()
                .map(sourcesById::get)
                .filter(Objects::nonNull)
                .toList();
        var independent = independentSources(groupSources);
        var assertion = group.getAssertion().isBlank()
                ? claimsById.get(group.getClaimIds().getFirst()).getStatement()
                : group.getAssertion();
        var bestTier = independent.stream().map(Source::getTier).min(Comparator.naturalOrder()).orElse(SourceTier.C);
        var newestDate = independent.stream().map(Source::getPublishedAt).max(Comparator.naturalOrder())
                .orElse(LocalDate.MIN);
        return new GroupDraft(group.getId(), assertion, group.getClaimIds(),
                independent.stream().map(Source::getId).toList(), bestTier, newestDate);
    }

    private static List<Source> independentSources(List<Source> groupSources) {
        var presentIds = groupSources.stream().map(Source::getId).collect(Collectors.toSet());
        return groupSources.stream()
                .filter(source -> source.getCitedSource().map(cited -> !presentIds.contains(cited)).orElse(true))
                .toList();
    }

    private static Map<String, List<ConflictOutput>> indexConflicts(
            List<ConflictOutput> conflicts, Set<String> knownGroupIds) {
        var index = new HashMap<String, List<ConflictOutput>>();
        for (var conflict : conflicts) {
            var involved = conflict.getGroupIds().stream().filter(knownGroupIds::contains).distinct().toList();
            if (involved.size() < ReconciliationConstants.MIN_GROUPS_IN_CONFLICT) {
                continue;
            }
            var normalized = new ConflictOutput(involved, conflict.getDescription());
            involved.forEach(groupId -> index.computeIfAbsent(groupId, key -> new ArrayList<>()).add(normalized));
        }
        return index;
    }

    private static Map<String, String> assignFinalIds(String subQuestionId, List<GroupDraft> drafts) {
        var finalIds = new LinkedHashMap<String, String>();
        var number = ReconciliationConstants.FIRST_GROUP_NUMBER;
        for (var draft : drafts) {
            finalIds.put(draft.getModelGroupId(), ReconciliationConstants.GROUP_ID_FORMAT.formatted(subQuestionId, number++));
        }
        return finalIds;
    }

    private static EvidenceGroup toGroup(String subQuestionId, GroupDraft draft,
            Map<String, List<ConflictOutput>> conflictsByModelId, Map<String, GroupDraft> draftsByModelId,
            Map<String, String> finalIds) {
        var modelGroupId = draft.getModelGroupId();
        var conflicts = conflictsByModelId.getOrDefault(modelGroupId, List.of());
        var conflictingIds = conflicts.stream()
                .flatMap(conflict -> conflict.getGroupIds().stream())
                .filter(otherId -> !otherId.equals(modelGroupId))
                .distinct()
                .map(finalIds::get)
                .toList();
        var description = conflicts.isEmpty() ? null : conflicts.getFirst().getDescription();
        return new EvidenceGroup(
                finalIds.get(modelGroupId),
                subQuestionId,
                draft.getAssertion(),
                draft.getClaimIds(),
                draft.getIndependentSourceIds(),
                draft.getBestTier(),
                draft.getNewestDate(),
                resolveStatus(draft, conflicts, draftsByModelId),
                description,
                conflictingIds);
    }

    private static ConflictStatus resolveStatus(GroupDraft draft, List<ConflictOutput> conflicts,
            Map<String, GroupDraft> draftsByModelId) {
        var status = ConflictStatus.NONE;
        for (var conflict : conflicts) {
            var othersNewest = conflict.getGroupIds().stream()
                    .filter(otherId -> !otherId.equals(draft.getModelGroupId()))
                    .map(draftsByModelId::get)
                    .map(GroupDraft::getNewestDate)
                    .max(Comparator.naturalOrder())
                    .orElse(LocalDate.MIN);
            status = status.worseOf(statusAgainst(draft.getNewestDate(), othersNewest));
        }
        return status;
    }

    private static ConflictStatus statusAgainst(LocalDate own, LocalDate othersNewest) {
        if (own.minusYears(ReconciliationConstants.RECENCY_THRESHOLD_YEARS).isAfter(othersNewest)) {
            return ConflictStatus.RESOLVED_BY_RECENCY;
        }
        if (othersNewest.minusYears(ReconciliationConstants.RECENCY_THRESHOLD_YEARS).isAfter(own)) {
            return ConflictStatus.SUPERSEDED;
        }
        return ConflictStatus.OPEN;
    }
}
