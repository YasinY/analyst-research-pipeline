package com.assignment.research.llm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScriptedLlmPort implements LlmPort {

    private static final String MODEL = "scripted-model";
    private static final String RAW = "{}";
    private static final LlmUsage USAGE_PER_CALL = new LlmUsage(100, 50);

    private final Map<String, Deque<Object>> responsesByLabelPrefix = new LinkedHashMap<>();
    private final List<LlmRequest> requests = new ArrayList<>();

    public ScriptedLlmPort on(String labelPrefix, Object... responsesInOrder) {
        var queue = responsesByLabelPrefix.computeIfAbsent(labelPrefix, key -> new ArrayDeque<>());
        for (var response : responsesInOrder) {
            queue.add(response);
        }
        return this;
    }

    @Override
    public <T> LlmResult<T> complete(LlmRequest request, Class<T> responseType) {
        requests.add(request);
        var response = nextResponse(request.getLabel());
        if (response instanceof LlmException failure) {
            throw failure;
        }
        return new LlmResult<>(responseType.cast(response), RAW, MODEL, USAGE_PER_CALL, LlmCallStatus.OK);
    }

    public List<LlmRequest> getRequests() {
        return List.copyOf(requests);
    }

    public List<String> getLabels() {
        return requests.stream().map(LlmRequest::getLabel).toList();
    }

    private Object nextResponse(String label) {
        var queue = responsesByLabelPrefix.entrySet().stream()
                .filter(entry -> label.startsWith(entry.getKey()))
                .max((left, right) -> Integer.compare(left.getKey().length(), right.getKey().length()))
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalStateException("no scripted response for label " + label));
        if (queue.size() > 1) {
            return queue.poll();
        }
        return queue.peek();
    }
}
