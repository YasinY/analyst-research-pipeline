package com.assignment.research.reconciliation;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Source;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ClaimPromptFormatter {

    private ClaimPromptFormatter() {
    }

    public static String format(List<Claim> claims, List<Source> sources) {
        var sourcesById = sources.stream().collect(Collectors.toMap(Source::getId, Function.identity()));
        return claims.stream()
                .map(claim -> formatOne(claim, sourcesById))
                .collect(Collectors.joining(ReconciliationConstants.LINE_SEPARATOR));
    }

    private static String formatOne(Claim claim, Map<String, Source> sourcesById) {
        var source = sourcesById.get(claim.getSourceId());
        var publisher = source == null ? ReconciliationConstants.UNKNOWN : source.getPublisher();
        var type = source == null ? ReconciliationConstants.UNKNOWN : source.getType().toString();
        var published = source == null ? ReconciliationConstants.UNKNOWN : source.getPublishedAt().toString();
        return ReconciliationConstants.CLAIM_LINE.formatted(claim.getId(), claim.getSourceId(), publisher, type,
                published, claim.getStatement());
    }
}
