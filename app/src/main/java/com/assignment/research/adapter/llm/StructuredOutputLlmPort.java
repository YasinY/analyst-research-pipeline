package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LlmCallStatus;
import com.assignment.research.llm.LlmPort;
import com.assignment.research.llm.LlmRequest;
import com.assignment.research.llm.LlmResult;
import com.assignment.research.llm.MalformedLlmResponseException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class StructuredOutputLlmPort implements LlmPort {

    private final ChatClient chat;
    private final JsonResponseParser parser;

    @Override
    public <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType) {
        var systemPrompt = request.getSystemPrompt();
        var maxTokens = request.getMaxOutputTokens();
        var first = chat.chat(systemPrompt, request.getUserPrompt(), maxTokens);
        try {
            var value = parser.parse(first.getText(), responseType);
            return new LlmResult<>(value, first.getText(), first.getModel(), first.getUsage(), LlmCallStatus.OK);
        } catch (ResponseParseException firstFailure) {
            return repair(request, responseType, first, firstFailure);
        }
    }

    private <T> LlmResult<T> repair(LlmRequest request, Class<T> responseType, ChatReply first,
            ResponseParseException firstFailure) {
        var repairPrompt = request.getUserPrompt()
                + LlmAdapterConstants.REPAIR_INSTRUCTION.formatted(firstFailure.getMessage(), first.getText());
        var second = chat.chat(request.getSystemPrompt(), repairPrompt, request.getMaxOutputTokens());
        var usage = first.getUsage().plus(second.getUsage());
        try {
            var value = parser.parse(second.getText(), responseType);
            return new LlmResult<>(value, second.getText(), second.getModel(), usage, LlmCallStatus.REPAIRED);
        } catch (ResponseParseException secondFailure) {
            throw new MalformedLlmResponseException(
                    LlmAdapterConstants.MALFORMED_AFTER_REPAIR.formatted(secondFailure.getMessage()),
                    second.getText());
        }
    }
}
