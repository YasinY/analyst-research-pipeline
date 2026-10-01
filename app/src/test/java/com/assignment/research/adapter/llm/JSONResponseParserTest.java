package com.assignment.research.adapter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.critique.CritiqueOutput;
import com.assignment.research.critique.FindingType;
import com.assignment.research.planning.PlanOutput;
import org.junit.jupiter.api.Test;

class JSONResponseParserTest {

    private static final String ANY_OBJECT = "{}";
    private static final String ROOT_CAUSE = "root cause detail";
    private static final String WRAPPER = "wrapper";
    private static final String SELF_CAUSED = "self caused detail";

    private final JSONResponseParser parser = new JSONResponseParser(JSONMapperFactory.create());

    @Test
    void parsesLombokValueClassesWrappedInProseAndCodeFences() {
        var raw = """
                Sure, here is the plan:
                ```json
                {"interpretation": "overview", "subQuestions": [{"question": "Q1", "searchKeywords": ["a", "b"]}],
                 "extraField": 1}
                ```
                """;

        var plan = parser.parse(raw, PlanOutput.class);

        assertThat(plan.getInterpretation()).isEqualTo("overview");
        assertThat(plan.getSubQuestions()).hasSize(1);
    }

    @Test
    void acceptsEnumsCaseInsensitively() {
        var raw = """
                {"findings": [{"type": "overstated_certainty", "severity": "major", "quotedText": "x",
                 "detail": "d", "groupIds": [], "suggestedKeywords": []}]}
                """;

        var critique = parser.parse(raw, CritiqueOutput.class);

        assertThat(critique.getFindings().getFirst().getType()).isEqualTo(FindingType.OVERSTATED_CERTAINTY);
    }

    @Test
    void rejectsMissingRequiredFieldsWithAUsefulMessage() {
        assertThatThrownBy(() -> parser.parse("{\"interpretation\": \"only this\"}", PlanOutput.class))
                .isInstanceOf(ResponseParseException.class)
                .hasMessageContaining("subQuestions");
    }

    @Test
    void rejectsRepliesWithoutAnyJsonObject() {
        assertThatThrownBy(() -> parser.parse("I cannot help with that.", PlanOutput.class))
                .isInstanceOf(ResponseParseException.class)
                .hasMessageContaining("no JSON object");
    }

    @Test
    void rejectsRepliesWithAnOpeningBraceButNoClosingOne() {
        assertThatThrownBy(() -> parser.parse("{\"interpretation\": \"cut", PlanOutput.class))
                .isInstanceOf(ResponseParseException.class)
                .hasMessageContaining("no JSON object");
    }

    @Test
    void reportsTheMessageOfTheInnermostCause() {
        var failure = new IllegalStateException(WRAPPER, new IllegalArgumentException(ROOT_CAUSE));

        assertThatThrownBy(() -> parserFailingWith(failure).parse(ANY_OBJECT, PlanOutput.class))
                .isInstanceOf(ResponseParseException.class)
                .hasMessageContaining(ROOT_CAUSE)
                .hasCause(failure);
    }

    @Test
    void reportsTheExceptionTypeWhenTheInnermostCauseHasNoMessage() {
        var failure = new IllegalStateException(WRAPPER, new UnsupportedOperationException());

        assertThatThrownBy(() -> parserFailingWith(failure).parse(ANY_OBJECT, PlanOutput.class))
                .hasMessageContaining(UnsupportedOperationException.class.getSimpleName());
    }

    @Test
    void stopsWalkingTheCauseChainAtASelfReferencingCause() {
        var failure = new SelfCausedException(SELF_CAUSED);

        assertThatThrownBy(() -> parserFailingWith(failure).parse(ANY_OBJECT, PlanOutput.class))
                .hasMessageContaining(SELF_CAUSED);
    }

    private static JSONResponseParser parserFailingWith(RuntimeException failure) {
        return new JSONResponseParser(new ThrowingObjectMapper(failure));
    }
}
