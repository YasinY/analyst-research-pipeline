package com.assignment.research.adapter.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.evidence.SearchHit;
import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceTier;
import com.assignment.research.evidence.SourceType;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class JSONCorpusSearchAdapterTest {

    private static final Path CORPUS = Path.of(System.getProperty("basedir", "app")).resolveSibling("data")
            .resolve("corpus").resolve("dry-bulk-shipping.json");
    private static final int MAX_RESULTS = 5;
    private static final Source REPORT_ONLY = new Source("src-report", "Quarterly market report", "Broker",
            SourceType.BROKER_NOTE, LocalDate.of(2026, 1, 15), null, List.of("freight"),
            "Analysts report weaker freight demand.");
    private static final Source OLDER_QUOTED = new Source("src-older", "\"Freight\" outlook", "Broker",
            SourceType.BROKER_NOTE, LocalDate.of(2025, 6, 1), null, List.of("freight"), "Older freight view.");
    private static final Source NEWER_QUOTED = new Source("src-newer", "\"Freight\" update", "Broker",
            SourceType.BROKER_NOTE, LocalDate.of(2026, 2, 1), null, List.of("freight"), "Newer freight view.");
    private static final Path MISSING_CORPUS = CORPUS.resolveSibling("does-not-exist.json");
    private static final int SINGLE_RESULT = 1;

    private final JSONCorpusSearchAdapter search = JSONCorpusSearchAdapter.load(CORPUS, JSONMapperFactory.create());

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
    void multiWordKeywordMatchesWhenAllItsWordsAppearEvenIfNotAdjacent() {
        var hits = search.search(List.of("dry bulk shipping risk drivers"), MAX_RESULTS);

        assertThat(hits).extracting(hit -> hit.getSource().getId()).contains("src-risk-report-2026");
    }

    @Test
    void unrelatedTopicFindsNothing() {
        assertThat(search.search(List.of("pharmaceutical", "biotech"), MAX_RESULTS)).isEmpty();
    }

    @Test
    void keywordMatchesWholeWordsOnlyNotSubstringsOfLongerWords() {
        var reportOnly = new JSONCorpusSearchAdapter(List.of(REPORT_ONLY));

        assertThat(reportOnly.search(List.of("port"), MAX_RESULTS)).isEmpty();
        assertThat(reportOnly.search(List.of("report"), MAX_RESULTS)).hasSize(1);
        assertThat(reportOnly.search(List.of("weaker freight"), MAX_RESULTS)).hasSize(1);
        assertThat(reportOnly.search(List.of("demand weaker"), MAX_RESULTS)).hasSize(1);
    }

    @Test
    void keywordsWithoutAnyWordCharactersOrWithOnlyShortUnmatchedWordsFindNothing() {
        var reportOnly = new JSONCorpusSearchAdapter(List.of(REPORT_ONLY));

        assertThat(reportOnly.search(List.of("", "--"), MAX_RESULTS)).isEmpty();
        assertThat(reportOnly.search(List.of("an of"), MAX_RESULTS)).isEmpty();
    }

    @Test
    void equalScoresAreOrderedNewestFirstAndCutAtMaxResults() {
        var adapter = new JSONCorpusSearchAdapter(List.of(OLDER_QUOTED, NEWER_QUOTED));

        assertThat(adapter.size()).isEqualTo(2);
        assertThat(adapter.search(List.of("freight"), MAX_RESULTS)).extracting(hit -> hit.getSource().getId())
                .containsExactly(NEWER_QUOTED.getId(), OLDER_QUOTED.getId());
        assertThat(adapter.search(List.of("freight"), SINGLE_RESULT)).extracting(hit -> hit.getSource().getId())
                .containsExactly(NEWER_QUOTED.getId());
    }

    @Test
    void loadingAMissingCorpusFailsNamingTheFile() {
        assertThatThrownBy(() -> JSONCorpusSearchAdapter.load(MISSING_CORPUS, JSONMapperFactory.create()))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining(MISSING_CORPUS.getFileName().toString());
    }
}
