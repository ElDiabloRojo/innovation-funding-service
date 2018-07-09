package org.innovateuk.ifs.assessment.dashboard.populator;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.assessment.common.service.AssessmentService;
import org.innovateuk.ifs.assessment.dashboard.viewmodel.AssessorCompetitionDashboardApplicationViewModel;
import org.innovateuk.ifs.assessment.dashboard.viewmodel.AssessorCompetitionDashboardViewModel;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.resource.AssessmentTotalScoreResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.partitioningBy;
import static org.innovateuk.ifs.assessment.resource.AssessmentState.SUBMITTED;

/**
 * Build the model for the Assessor Competition Dashboard view.
 */
@Component
public class AssessorCompetitionDashboardModelPopulator {

    private CompetitionService competitionService;
    private AssessmentService assessmentService;
    private ApplicationService applicationService;
    private OrganisationService organisationService;
    private ProcessRoleService processRoleService;

    public AssessorCompetitionDashboardModelPopulator(CompetitionService competitionService,
                                                      AssessmentService assessmentService,
                                                      ApplicationService applicationService,
                                                      OrganisationService organisationService,
                                                      ProcessRoleService processRoleService) {
        this.competitionService = competitionService;
        this.assessmentService = assessmentService;
        this.applicationService = applicationService;
        this.organisationService = organisationService;
        this.processRoleService = processRoleService;
    }

    public AssessorCompetitionDashboardViewModel populateModel(Long competitionId, Long userId) {
        CompetitionResource competition = competitionService.getById(competitionId);
        ZonedDateTime acceptDeadline = competition.getAssessorAcceptsDate();
        ZonedDateTime submitDeadline = competition.getAssessorDeadlineDate();

        Map<Boolean, List<AssessorCompetitionDashboardApplicationViewModel>> applicationsPartitionedBySubmitted =
                getApplicationsPartitionedBySubmitted(userId, competitionId);
        List<AssessorCompetitionDashboardApplicationViewModel> submitted = applicationsPartitionedBySubmitted.get(TRUE);
        List<AssessorCompetitionDashboardApplicationViewModel> outstanding = applicationsPartitionedBySubmitted.get(FALSE);

        boolean submitVisible = outstanding.stream()
                .anyMatch(AssessorCompetitionDashboardApplicationViewModel::isReadyToSubmit);

        return new AssessorCompetitionDashboardViewModel(
                competition.getId(),
                competition.getName(),
                competition.getLeadTechnologistName(),
                acceptDeadline,
                submitDeadline,
                submitted,
                outstanding,
                submitVisible
        );
    }

    private Map<Boolean, List<AssessorCompetitionDashboardApplicationViewModel>> getApplicationsPartitionedBySubmitted(Long userId, Long competitionId) {
        return assessmentService.getByUserAndCompetition(userId, competitionId).stream()
                .collect(partitioningBy(this::isAssessmentSubmitted, mapping(this::createApplicationViewModel, Collectors.toList())));
    }

    private boolean isAssessmentSubmitted(AssessmentResource assessmentResource) {
        return SUBMITTED == assessmentResource.getAssessmentState();
    }

    private AssessorCompetitionDashboardApplicationViewModel createApplicationViewModel(AssessmentResource assessment) {
        ApplicationResource application = applicationService.getById(assessment.getApplication());
        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(application.getId());
        Optional<OrganisationResource> leadOrganisation = organisationService.getApplicationLeadOrganisation(userApplicationRoles);
        return new AssessorCompetitionDashboardApplicationViewModel(application.getId(),
                assessment.getId(),
                application.getName(),
                leadOrganisation.get().getName(),
                assessment.getAssessmentState(),
                getOverallScore(assessment),
                getRecommended(assessment));
    }

    private int getOverallScore(AssessmentResource assessmentResource) {
        switch (assessmentResource.getAssessmentState()) {
            case READY_TO_SUBMIT:
            case SUBMITTED:
                AssessmentTotalScoreResource assessmentTotalScore = assessmentService.getTotalScore(assessmentResource.getId());
                return assessmentTotalScore.getTotalScorePercentage();
            default:
                return 0;
        }
    }

    private Boolean getRecommended(AssessmentResource assessment) {
        return ofNullable(assessment.getFundingDecision())
                .map(fundingDecisionResource -> fundingDecisionResource.getFundingConfirmation()).orElse(null);
    }
}
