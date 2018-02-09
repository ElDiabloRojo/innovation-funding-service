package org.innovateuk.ifs.assessment.review.workflow.actions;

import org.innovateuk.ifs.assessment.review.domain.AssessmentReview;
import org.innovateuk.ifs.assessment.review.domain.AssessmentReviewRejectOutcome;
import org.innovateuk.ifs.assessment.review.resource.AssessmentReviewEvent;
import org.innovateuk.ifs.assessment.review.resource.AssessmentReviewState;
import org.innovateuk.ifs.assessment.workflow.configuration.AssessmentWorkflow;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * The {@code AssessmentReviewRejectAction} handles the reject event for an {@code AssessmentReview}
 * For more info see {@link AssessmentWorkflow}
 */
@Component
public class AssessmentReviewRejectAction implements Action<AssessmentReviewState, AssessmentReviewEvent> {
    @Override
    public void execute(StateContext<AssessmentReviewState, AssessmentReviewEvent> context) {
        AssessmentReview invite = (AssessmentReview) context.getMessageHeader("target");
        AssessmentReviewRejectOutcome rejectOutcome = (AssessmentReviewRejectOutcome) context.getMessageHeader("rejection");
        invite.setRejection(rejectOutcome);
    }
}