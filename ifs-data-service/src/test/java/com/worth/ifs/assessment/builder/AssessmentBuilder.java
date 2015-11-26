package com.worth.ifs.assessment.builder;

import com.worth.ifs.BaseBuilder;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.assessment.domain.Recommendation;

import java.util.List;
import java.util.function.BiConsumer;

import static com.worth.ifs.BuilderAmendFunctions.setField;
import static com.worth.ifs.BuilderAmendFunctions.uniqueIds;
import static java.util.Collections.emptyList;

public class AssessmentBuilder extends BaseBuilder<Recommendation, AssessmentBuilder> {

    private AssessmentBuilder(List<BiConsumer<Integer, Recommendation>> multiActions) {
        super(multiActions);
    }

    public static AssessmentBuilder newAssessment() {
        return new AssessmentBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected AssessmentBuilder createNewBuilderWithActions(List<BiConsumer<Integer, Recommendation>> actions) {
        return new AssessmentBuilder(actions);
    }

    @Override
    protected Recommendation createInitial() {
        return new Recommendation();
    }

    public AssessmentBuilder withId(Long... ids) {
        return withArray((id, assessment) -> setField("id", id, assessment), ids);
    }

    public AssessmentBuilder withApplication(Application... applications) {
        return withArray((application, assessment) -> setField("application", application, assessment), applications);
    }

    public AssessmentBuilder withProcessState(String... processStates) {
        return withArray((processState, assessment) -> assessment.setProcessStatus(processState), processStates);
    }

}
