package com.assignment.research.reconciliation;

import static org.assertj.core.api.Assertions.assertThat;

import com.assignment.research.evidence.Claim;
import com.assignment.research.evidence.Sources;
import com.assignment.research.llm.FakeLLMPort;
import com.assignment.research.planning.SubQuestion;
import com.assignment.research.prompt.FakePromptTemplates;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReconcilerTest {

    private static final SubQuestion QUESTION = new SubQuestion("q1", "How fast does the fleet grow?", List.of("fleet"));
    private static final int ROUND = 1;

    private final FakePromptTemplates prompts = new FakePromptTemplates("{{questions}}\n{{claims}}");

    @Test
    void returnsEmptyReconciliationWithoutClaimsAndWithoutLLMCall() {
        var llm = FakeLLMPort.returning(ReconciliationOutput.empty());

        var result = new Reconciler(llm, prompts).reconcile(List.of(QUESTION), List.of(), List.of(), ROUND);

        assertThat(result.getGroups()).isEmpty();
        assertThat(llm.getLastRequest()).isNull();
    }

    @Test
    void wrapsASingleClaimWithoutAskingTheModel() {
        var llm = FakeLLMPort.returning(ReconciliationOutput.empty());
        var claim = new Claim("q1-c1", "q1", "Fleet grew 3.1%.", "src-a");

        var result = new Reconciler(llm, prompts).reconcile(List.of(QUESTION), List.of(claim), List.of(Sources.tierA("src-a")),
                ROUND);

        assertThat(result.getGroups()).hasSize(1);
        assertThat(result.getGroups().getFirst().getAssertion()).isEqualTo("Fleet grew 3.1%.");
        assertThat(llm.getLastRequest()).isNull();
    }

    @Test
    void sendsClaimLinesWithSourceMetadataAndLabelsTheCall() {
        var output = new ReconciliationOutput(
                List.of(new ClaimGroupOutput("g1", "Fleet growth around 3%.", List.of("q1-c1", "q1-c2"))), List.of());
        var llm = FakeLLMPort.returning(output);
        var claims = List.of(new Claim("q1-c1", "q1", "Fleet grew 3.1%.", "src-a"),
                new Claim("q1-c2", "q1", "Fleet grew 2.9%.", "src-b"));
        var sources = List.of(Sources.tierA("src-a"), Sources.tierB("src-b", Sources.RECENT));

        var result = new Reconciler(llm, prompts).reconcile(List.of(QUESTION), claims, sources, 2);

        assertThat(result.getGroups()).hasSize(1);
        var request = llm.getLastRequest();
        assertThat(request.getLabel()).isEqualTo("reconciler/round2");
        assertThat(request.getUserPrompt()).contains("[q1] How fast does the fleet grow?")
                .contains("[q1-c1] (q1; source src-a, Fictional Statistics Office");
    }
}
