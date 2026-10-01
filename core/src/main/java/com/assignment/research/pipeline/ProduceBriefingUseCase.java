package com.assignment.research.pipeline;

import com.assignment.research.query.AnalystQuery;

public interface ProduceBriefingUseCase {

    BriefingResult produce(AnalystQuery query, PipelineObserver observer);
}
