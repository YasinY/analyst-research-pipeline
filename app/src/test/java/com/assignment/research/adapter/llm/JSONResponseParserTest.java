package com.assignment.research.adapter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.critique.CritiqueOutput;
import com.assignment.research.critique.FindingType;
import com.assignment.research.planning.PlanOutput;
import org.junit.jupiter.api.Test;

class JSONResponseParserTest {

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
}
