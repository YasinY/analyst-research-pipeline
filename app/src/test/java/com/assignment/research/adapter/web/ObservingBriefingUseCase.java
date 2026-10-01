package com.assignment.research.adapter.web;

import com.assignment.research.pipeline.BriefingResult;
import com.assignment.research.pipeline.PipelineObserver;
import com.assignment.research.pipeline.PipelineStep;
import com.assignment.research.pipeline.ProduceBriefingUseCase;
import com.assignment.research.query.AnalystQuery;

public final class ObservingBriefingUseCase implements ProduceBriefingUseCase {

    @Override
    public BriefingResult produce(AnalystQuery query, PipelineObserver observer) {
        var result = BriefingFixtures.result();
        observer.onTrace(BriefingFixtures.plannerEntry());
        observer.onStep(PipelineStep.PLAN, result.getFinalState());
        observer.onTrace(BriefingFixtures.failedCriticEntry());
        observer.onStep(PipelineStep.FINISH, result.getFinalState());
        return result;
    }
}
