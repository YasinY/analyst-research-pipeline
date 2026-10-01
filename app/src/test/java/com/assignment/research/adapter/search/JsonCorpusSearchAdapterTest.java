package com.assignment.research.adapter.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.adapter.output.JsonMapperFactory;
import com.assignment.research.evidence.SearchHit;
import com.assignment.research.evidence.SourceTier;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonCorpusSearchAdapterTest {

    private static final Path CORPUS = Path.of(System.getProperty("basedir", "app")).resolveSibling("data")
            .resolve("corpus").resolve("dry-bulk-shipping.json");
    private static final int MAX_RESULTS = 5;

    private final JsonCorpusSearchAdapter search = JsonCorpusSearchAdapter.load(CORPUS, JsonMapperFactory.create());

    @Test
    void corpusLoadsWithFixedTiersDerivedFromSourceType() {
        assertThat(search.size()).isGreaterThanOrEqualTo(14);
        var hits = search.search(List.of("forum", "imo"), MAX_RESULTS);
        assertThat(hits.getFirst().getSource().getTier()).isEqualTo(SourceTier.C);
    }

    @Test
    void fleetGrowthQueryReturnsBothConflictingSourcesAndTheDerivativeArticle() {
        var ids = search.search(List.of("fleet growth", "orderbook", "supply"), MAX_RESULTS).stream()
                .map(hit -> hit.getSource().getId())
                .toList();

        assertThat(ids).contains("src-fleet-stats-2026", "src-broker-fleet-2026", "src-tradepress-fleet-2026");
    }

    @Test
    void irrelevantContainerArticleRanksBelowDryBulkSourcesButIsStillFound() {
        var hits = search.search(List.of("fleet growth", "freight rates", "dry bulk"), MAX_RESULTS);

        assertThat(hits).extracting(hit -> hit.getSource().getId()).contains("src-container-2026");
        assertThat(hits.getFirst().getSource().getId()).isNotEqualTo("src-container-2026");
    }

    @Test
    void regulationQueryOnlyFindsTheLowQualityForumThread() {
        var hits = search.search(List.of("regulation", "emissions", "imo"), MAX_RESULTS);

        assertThat(hits).extracting(SearchHit::getSource).extracting(source -> source.getTier())
                .containsOnly(SourceTier.C);
    }

    @Test
    void unrelatedTopicFindsNothing() {
        assertThat(search.search(List.of("pharmaceutical", "biotech"), MAX_RESULTS)).isEmpty();
    }
}
