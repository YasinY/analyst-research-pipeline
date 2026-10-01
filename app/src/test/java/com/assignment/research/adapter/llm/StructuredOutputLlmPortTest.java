package com.assignment.research.adapter.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.assignment.research.adapter.output.JSONMapperFactory;
import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.llm.MalformedLLMResponseException;
import com.assignment.research.planning.PlanOutput;
import org.junit.jupiter.api.Test;

class StructuredOutputLLMPortTest {

    private static final LLMRequest REQUEST = new LLMRequest("planner", "system", "user prompt", 256);
    private static final String VALID = "{\"interpretation\": \"ok\", \"subQuestions\": []}";
    private static final String GARBAGE = "Sorry, I got confused.";

    private final JSONResponseParser parser = new JSONResponseParser(JSONMapperFactory.create());

    @Test
    void cleanReplyIsReturnedAsOkWithoutASecondCall() {
        var chat = new ScriptedChatClient(VALID);

        var result = new StructuredOutputLLMPort(chat, parser).complete(REQUEST, PlanOutput.class);

        assertThat(result.getStatus()).isEqualTo(LLMCallStatus.OK);
        assertThat(result.getUsage().getTotalTokens()).isEqualTo(15);
        assertThat(chat.getUserPrompts()).hasSize(1);
    }

    @Test
    void malformedReplyTriggersOneRepairCallThatQuotesTheErrorAndTheBadReply() {
        var chat = new ScriptedChatClient(GARBAGE, VALID);

        var result = new StructuredOutputLLMPort(chat, parser).complete(REQUEST, PlanOutput.class);

        assertThat(result.getStatus()).isEqualTo(LLMCallStatus.REPAIRED);
        assertThat(result.getUsage().getTotalTokens()).isEqualTo(30);
        assertThat(chat.getUserPrompts()).hasSize(2);
        assertThat(chat.getUserPrompts().get(1)).startsWith("user prompt").contains(GARBAGE)
                .contains("no JSON object");
    }

    @Test
    void truncatedReplyIsRetriedWithDoubledBudgetInsteadOfARepairPrompt() {
        var chat = new ScriptedChatClient(true, "{\"interpretation\": \"cut off", VALID);

        var result = new StructuredOutputLLMPort(chat, parser).complete(REQUEST, PlanOutput.class);

        assertThat(result.getStatus()).isEqualTo(LLMCallStatus.REPAIRED);
        assertThat(chat.getBudgets()).containsExactly(256, 512);
        assertThat(chat.getUserPrompts().get(1)).isEqualTo("user prompt");
    }

    @Test
    void secondMalformedReplyFailsLoudlyWithTheRawText() {
        var chat = new ScriptedChatClient(GARBAGE, "still not json");

        assertThatThrownBy(() -> new StructuredOutputLLMPort(chat, parser).complete(REQUEST, PlanOutput.class))
                .isInstanceOf(MalformedLLMResponseException.class)
                .extracting(failure -> ((MalformedLLMResponseException) failure).getRawText())
                .isEqualTo("still not json");
    }
}
