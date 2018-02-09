package org.innovateuk.ifs.assessment.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.resource.AssessmentInterviewPanelInviteResource;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;

/**
 * Builder for {@link AssessmentInterviewPanelInviteResource}
 */
public class AssessmentInterviewPanelInviteResourceBuilder extends BaseBuilder<AssessmentInterviewPanelInviteResource, AssessmentInterviewPanelInviteResourceBuilder> {

    private AssessmentInterviewPanelInviteResourceBuilder(List<BiConsumer<Integer, AssessmentInterviewPanelInviteResource>> multiActions) {
        super(multiActions);
    }

    public static AssessmentInterviewPanelInviteResourceBuilder newAssessmentInterviewPanelInviteResource() {
        return new AssessmentInterviewPanelInviteResourceBuilder(emptyList());
    }

    public AssessmentInterviewPanelInviteResourceBuilder withIds(Long... ids) {
        return withArray(BaseBuilderAmendFunctions::setId, ids);
    }

    public AssessmentInterviewPanelInviteResourceBuilder withCompetitionName(String... competitionNames) {
        return withArraySetFieldByReflection("competitionName", competitionNames);
    }

    public AssessmentInterviewPanelInviteResourceBuilder withCompetitionId(Long... ids) {
        return withArraySetFieldByReflection("competitionId", ids);
    }

    public AssessmentInterviewPanelInviteResourceBuilder withUserId(Long... ids) {
        return withArraySetFieldByReflection("userId", ids);
    }

    public AssessmentInterviewPanelInviteResourceBuilder withEmail(String... emails) {
        return withArraySetFieldByReflection("email", emails);
    }

    public AssessmentInterviewPanelInviteResourceBuilder withInviteHash(String... hashes) {
        return withArraySetFieldByReflection("hash", hashes);
    }

    public AssessmentInterviewPanelInviteResourceBuilder withStatus(InviteStatus... statuses) {
        return withArraySetFieldByReflection("status", statuses);
    }

    public AssessmentInterviewPanelInviteResourceBuilder withInterviewDate(ZonedDateTime... interviewDates) {
        return withArraySetFieldByReflection("interviewDate", interviewDates);
    }

    @Override
    protected AssessmentInterviewPanelInviteResourceBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessmentInterviewPanelInviteResource>> actions) {
        return new AssessmentInterviewPanelInviteResourceBuilder(actions);
    }

    @Override
    protected AssessmentInterviewPanelInviteResource createInitial() {
        return new AssessmentInterviewPanelInviteResource();
    }
}