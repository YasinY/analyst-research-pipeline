package com.assignment.research.reconciliation;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Source;
import com.assignment.research.evidence.SourceTier;
import com.assignment.research.evidence.Sources;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvidenceGroupAssemblerTest {

    private static final String SUB_QUESTION = "q1";

    private static Claim claim(String id, String sourceId) {
        return new Claim(id, SUB_QUESTION, "statement " + id, sourceId);
    }

    @Test
    void collapsesDerivativeSourcesWhenTheOriginalIsPresent() {
        var original = Sources.tierA("src-a");
        var copy = Sources.derivativeOf("src-news", "src-a");
        var claims = List.of(claim("c1", "src-a"), claim("c2", "src-news"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Fleet grew 3.1%.", List.of("c1", "c2"))), List.of());

        var result = EvidenceGroupAssembler.assemble(claims, List.of(original, copy), output);

        var group = result.getGroups().getFirst();
        assertThat(group.getId()).isEqualTo("g-c1");
        assertThat(group.getSubQuestionIds()).containsExactly("q1");
        assertThat(group.getIndependentSourceIds()).containsExactly("src-a");
        assertThat(group.getBestTier()).isEqualTo(SourceTier.A);
        assertThat(group.getConflictStatus()).isEqualTo(ConflictStatus.NONE);
    }

    @Test
    void countsIndependentSourcesAndPicksBestTierAndNewestDate() {
        List<Source> sources = List.of(Sources.tierC("src-blog"), Sources.tierB("src-b", Sources.STALE));
        var claims = List.of(claim("c1", "src-blog"), claim("c2", "src-b"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Same fact.", List.of("c1", "c2"))), List.of());

        var group = EvidenceGroupAssembler.assemble(claims, sources, output).getGroups().getFirst();

        assertThat(group.getIndependentSourceCount()).isEqualTo(2);
        assertThat(group.getBestTier()).isEqualTo(SourceTier.B);
        assertThat(group.getNewestSourceDate()).isEqualTo(Sources.RECENT);
    }

    @Test
    void marksConflictBetweenContemporarySourcesAsOpenOnBothSides() {
        var sources = List.of(Sources.tierA("src-a"), Sources.tierB("src-b", Sources.RECENT));
        var claims = List.of(claim("c1", "src-a"), claim("c2", "src-b"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Growth 3.1%.", List.of("c1")),
                        new ClaimGroupOutput("g2", "Growth 2.4%.", List.of("c2"))),
                List.of(new ConflictOutput(List.of("g1", "g2"), "3.1% vs 2.4% for 2026")));

        var groups = EvidenceGroupAssembler.assemble(claims, sources, output).getGroups();

        assertThat(groups).extracting(EvidenceGroup::getConflictStatus)
                .containsExactly(ConflictStatus.OPEN, ConflictStatus.OPEN);
        assertThat(groups.getFirst().getConflictingGroupIds()).containsExactly("g-c2");
        assertThat(groups.getFirst().getConflict()).contains("3.1% vs 2.4% for 2026");
    }

    @Test
    void resolvesConflictByRecencyWhenOneSideIsYearsNewer() {
        var sources = List.of(Sources.tierA("src-old", Sources.STALE), Sources.tierB("src-new", Sources.RECENT));
        var claims = List.of(claim("c1", "src-old"), claim("c2", "src-new"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "China imports rising.", List.of("c1")),
                        new ClaimGroupOutput("g2", "China imports falling.", List.of("c2"))),
                List.of(new ConflictOutput(List.of("g1", "g2"), "rising vs falling")));

        var groups = EvidenceGroupAssembler.assemble(claims, sources, output).getGroups();

        assertThat(groups).extracting(EvidenceGroup::getConflictStatus)
                .containsExactly(ConflictStatus.SUPERSEDED, ConflictStatus.RESOLVED_BY_RECENCY);
    }

    @Test
    void ignoresUnknownClaimIdsAndGivesUnassignedClaimsTheirOwnGroup() {
        var sources = List.of(Sources.tierA("src-a"), Sources.tierC("src-c"));
        var claims = List.of(claim("c1", "src-a"), claim("c2", "src-c"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Known.", List.of("c1", "c-invented"))), List.of());

        var groups = EvidenceGroupAssembler.assemble(claims, sources, output).getGroups();

        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).getClaimIds()).containsExactly("c1");
        assertThat(groups.get(1).getClaimIds()).containsExactly("c2");
        assertThat(groups.get(1).getAssertion()).isEqualTo("statement c2");
    }

    @Test
    void derivesGroupIdFromSmallestClaimIdRegardlessOfModelIdOrOrder() {
        var sources = List.of(Sources.tierA("src-a"), Sources.tierC("src-c"));
        var claims = List.of(claim("q1-c1", "src-a"), claim("q1-c2", "src-c"), claim("q2-c1", "src-a"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g7", "Rising.", List.of("q2-c1", "q1-c2")),
                        new ClaimGroupOutput("g3", "Falling.", List.of("q1-c1"))),
                List.of(new ConflictOutput(List.of("g7", "g3"), "rising vs falling")));

        var groups = EvidenceGroupAssembler.assemble(claims, sources, output).getGroups();

        assertThat(groups).extracting(EvidenceGroup::getId).containsExactly("g-q1-c2", "g-q1-c1");
        assertThat(groups.getFirst().getConflictingGroupIds()).containsExactly("g-q1-c1");
    }

    @Test
    void fallsBackToAllSourcesWhenEverySourceCitesAnotherInTheSameGroup() {
        var first = Sources.derivativeOf("src-x", "src-y");
        var second = Sources.derivativeOf("src-y", "src-x");
        var other = Sources.tierA("src-a");
        var claims = List.of(claim("c1", "src-x"), claim("c2", "src-y"), claim("c3", "src-a"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Circular.", List.of("c1", "c2")),
                        new ClaimGroupOutput("g2", "Independent.", List.of("c3"))),
                List.of(new ConflictOutput(List.of("g1", "g2"), "circular vs independent")));

        var groups = EvidenceGroupAssembler.assemble(claims, List.of(first, second, other), output).getGroups();

        var circular = groups.getFirst();
        assertThat(circular.getIndependentSourceIds()).containsExactly("src-x", "src-y");
        assertThat(circular.getBestTier()).isEqualTo(first.getTier());
        assertThat(circular.getNewestSourceDate()).isEqualTo(Sources.RECENT);
        assertThat(circular.getConflictStatus()).isEqualTo(ConflictStatus.OPEN);
    }

    @Test
    void groupWithoutKnownSourcesGetsUnknownDateAndConflictStatusDoesNotThrow() {
        var sources = List.of(Sources.tierA("src-a"));
        var claims = List.of(claim("c1", "src-missing"), claim("c2", "src-a"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Orphan.", List.of("c1")),
                        new ClaimGroupOutput("g2", "Known.", List.of("c2"))),
                List.of(new ConflictOutput(List.of("g1", "g2"), "orphan vs known")));

        var groups = EvidenceGroupAssembler.assemble(claims, sources, output).getGroups();

        assertThat(groups.getFirst().getNewestSourceDate()).isEqualTo(LocalDate.EPOCH);
        assertThat(groups).extracting(EvidenceGroup::getConflictStatus)
                .containsExactly(ConflictStatus.SUPERSEDED, ConflictStatus.RESOLVED_BY_RECENCY);
    }

    @Test
    void keepsAClaimOnlyInTheFirstGroupThatListsIt() {
        var sources = List.of(Sources.tierA("src-a"));
        var claims = List.of(claim("c1", "src-a"));
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "First.", List.of("c1")),
                        new ClaimGroupOutput("g2", "Second.", List.of("c1"))),
                List.of());

        var groups = EvidenceGroupAssembler.assemble(claims, sources, output).getGroups();

        assertThat(groups).hasSize(1);
        assertThat(groups.getFirst().getAssertion()).isEqualTo("First.");
    }
}
