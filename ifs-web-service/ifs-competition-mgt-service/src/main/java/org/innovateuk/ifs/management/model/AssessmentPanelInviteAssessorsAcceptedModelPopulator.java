package org.innovateuk.ifs.management.model;

import org.innovateuk.ifs.assessment.service.ReviewPanelInviteRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.invite.resource.AssessorInviteOverviewPageResource;
import org.innovateuk.ifs.invite.resource.AssessorInviteOverviewResource;
import org.innovateuk.ifs.management.viewmodel.InviteAssessorsAcceptedViewModel;
import org.innovateuk.ifs.management.viewmodel.OverviewAssessorRowViewModel;
import org.innovateuk.ifs.management.viewmodel.PaginationViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.invite.resource.ParticipantStatusResource.ACCEPTED;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * Build the model for the Invite assessors 'Accepted' view.
 */
@Component
public class AssessmentPanelInviteAssessorsAcceptedModelPopulator extends AssessmentPanelInviteAssessorsModelPopulator<InviteAssessorsAcceptedViewModel> {

    @Autowired
    private ReviewPanelInviteRestService reviewPanelInviteRestService;

    @Autowired
    private CompetitionRestService competitionsRestService;

    public InviteAssessorsAcceptedViewModel populateModel(long competitionId,
                                                               int page,
                                                               String originQuery) {
        CompetitionResource competition = competitionsRestService
                .getCompetitionById(competitionId)
                .getSuccess();

        InviteAssessorsAcceptedViewModel model = super.populateModel(competition);

        AssessorInviteOverviewPageResource pageResource = reviewPanelInviteRestService.getInvitationOverview(
                competition.getId(),
                page,
                singletonList(ACCEPTED))
                .getSuccess();

        List<OverviewAssessorRowViewModel> assessors = simpleMap(pageResource.getContent(), this::getRowViewModel);

        model.setAssessors(assessors);
        model.setPagination(new PaginationViewModel(pageResource, originQuery));
        model.setOriginQuery(originQuery);

        return model;
    }

    private OverviewAssessorRowViewModel getRowViewModel(AssessorInviteOverviewResource assessorInviteOverviewResource) {
        return new OverviewAssessorRowViewModel(
                assessorInviteOverviewResource.getId(),
                assessorInviteOverviewResource.getName(),
                assessorInviteOverviewResource.getInnovationAreas(),
                assessorInviteOverviewResource.isCompliant(),
                assessorInviteOverviewResource.getBusinessType(),
                assessorInviteOverviewResource.getStatus(),
                assessorInviteOverviewResource.getDetails(),
                assessorInviteOverviewResource.getInviteId());
    }

    @Override
    protected InviteAssessorsAcceptedViewModel createModel() { return new InviteAssessorsAcceptedViewModel(); }
}
