package com.assignment.research.adapter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.output.JsonMapperFactory;
import com.assignment.research.llm.LlmCallStatus;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.llm.MalformedLlmResponseException;
import com.assignment.research.planning.PlanOutput;
import org.junit.jupiter.api.Test;

class StructuredOutputLlmPortTest {

    private static final LlmRequest REQUEST = new LlmRequest("planner", "system", "user prompt", 256);
    private static final String VALID = "{\"interpretation\": \"ok\", \"subQuestions\": []}";
    private static final String GARBAGE = "Sorry, I got confused.";

    private final JsonResponseParser parser = new JsonResponseParser(JsonMapperFactory.create());

    @Test
    void cleanReplyIsReturnedAsOkWithoutASecondCall() {
        var chat = new ScriptedChatClient(VALID);

        var result = new StructuredOutputLlmPort(chat, parser).complete(REQUEST, PlanOutput.class);

        assertThat(result.getStatus()).isEqualTo(LlmCallStatus.OK);
        assertThat(result.getUsage().getTotalTokens()).isEqualTo(15);
        assertThat(chat.getUserPrompts()).hasSize(1);
    }

    @Test
    void malformedReplyTriggersOneRepairCallThatQuotesTheErrorAndTheBadReply() {
        var chat = new ScriptedChatClient(GARBAGE, VALID);

        var result = new StructuredOutputLlmPort(chat, parser).complete(REQUEST, PlanOutput.class);

        assertThat(result.getStatus()).isEqualTo(LlmCallStatus.REPAIRED);
        assertThat(result.getUsage().getTotalTokens()).isEqualTo(30);
        assertThat(chat.getUserPrompts()).hasSize(2);
        assertThat(chat.getUserPrompts().get(1)).startsWith("user prompt").contains(GARBAGE)
                .contains("no JSON object");
    }

    @Test
    void secondMalformedReplyFailsLoudlyWithTheRawText() {
        var chat = new ScriptedChatClient(GARBAGE, "still not json");

        assertThatThrownBy(() -> new StructuredOutputLlmPort(chat, parser).complete(REQUEST, PlanOutput.class))
                .isInstanceOf(MalformedLlmResponseException.class)
                .extracting(failure -> ((MalformedLlmResponseException) failure).getRawText())
                .isEqualTo("still not json");
    }
}
