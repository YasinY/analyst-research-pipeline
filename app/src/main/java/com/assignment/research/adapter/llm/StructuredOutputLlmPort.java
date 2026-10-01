package com.assignment.research.adapter.llm;

import com.assignment.research.llm.LLMCallStatus;
import com.assignment.research.llm.LLMPort;
import com.assignment.research.llm.LLMRequest;
import com.assignment.research.llm.LLMResult;
import com.assignment.research.llm.MalformedLLMResponseException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class StructuredOutputLLMPort implements LLMPort {

    private final ChatClient chat;
    private final JSONResponseParser parser;

    @Override
    public <T> LLMResult<T> complete(LLMRequest request, Class<T> responseType) {
        var systemPrompt = request.getSystemPrompt();
        var maxTokens = request.getMaxOutputTokens();
        var first = chat.chat(systemPrompt, request.getUserPrompt(), maxTokens);
        try {
            var value = parser.parse(first.getText(), responseType);
            return new LLMResult<>(value, first.getText(), first.getModel(), first.getUsage(), LLMCallStatus.OK);
        } catch (ResponseParseException firstFailure) {
            if (first.isTruncated()) {
                return retryWithLargerBudget(request, responseType, first);
            }
            return repair(request, responseType, first, firstFailure);
        }
    }

    private <T> LLMResult<T> retryWithLargerBudget(LLMRequest request, Class<T> responseType, ChatReply first) {
        var budget = request.getMaxOutputTokens() * LLMAdapterConstants.TRUNCATION_BUDGET_FACTOR;
        var second = chat.chat(request.getSystemPrompt(), request.getUserPrompt(), budget);
        var usage = first.getUsage().plus(second.getUsage());
        try {
            var value = parser.parse(second.getText(), responseType);
            return new LLMResult<>(value, second.getText(), second.getModel(), usage, LLMCallStatus.REPAIRED);
        } catch (ResponseParseException secondFailure) {
            throw new MalformedLLMResponseException(
                    LLMAdapterConstants.MALFORMED_AFTER_TRUNCATION.formatted(budget, secondFailure.getMessage()),
                    second.getText());
        }
    }

    private <T> LLMResult<T> repair(LLMRequest request, Class<T> responseType, ChatReply first,
            ResponseParseException firstFailure) {
        var repairPrompt = request.getUserPrompt()
                + LLMAdapterConstants.REPAIR_INSTRUCTION.formatted(firstFailure.getMessage(), first.getText());
        var second = chat.chat(request.getSystemPrompt(), repairPrompt, request.getMaxOutputTokens());
        var usage = first.getUsage().plus(second.getUsage());
        try {
            var value = parser.parse(second.getText(), responseType);
            return new LLMResult<>(value, second.getText(), second.getModel(), usage, LLMCallStatus.REPAIRED);
        } catch (ResponseParseException secondFailure) {
            throw new MalformedLLMResponseException(
                    LLMAdapterConstants.MALFORMED_AFTER_REPAIR.formatted(secondFailure.getMessage()),
                    second.getText());
        }
    }
}
