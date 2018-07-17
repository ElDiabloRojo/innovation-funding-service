package org.innovateuk.ifs.application.feedback.populator;

import org.innovateuk.ifs.application.common.populator.ApplicationFinanceSummaryViewModelPopulator;
import org.innovateuk.ifs.application.common.populator.ApplicationFundingBreakdownViewModelPopulator;
import org.innovateuk.ifs.application.common.viewmodel.ApplicationFinanceSummaryViewModel;
import org.innovateuk.ifs.application.common.viewmodel.ApplicationFundingBreakdownViewModel;
import org.innovateuk.ifs.application.feedback.viewmodel.ApplicationFeedbackViewModel;
import org.innovateuk.ifs.application.feedback.viewmodel.InterviewFeedbackViewModel;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.finance.view.OrganisationApplicationFinanceOverviewImpl;
import org.innovateuk.ifs.application.populator.AbstractApplicationModelPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.*;
import org.innovateuk.ifs.assessment.resource.ApplicationAssessmentAggregateResource;
import org.innovateuk.ifs.assessment.service.AssessmentRestService;
import org.innovateuk.ifs.assessment.service.AssessorFormInputResponseRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.interview.service.InterviewAssignmentRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.origin.ApplicationSummaryOrigin;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.origin.BackLinkUtil.buildBackUrl;
import static org.innovateuk.ifs.origin.BackLinkUtil.buildOriginQueryString;

@Component
public class ApplicationFeedbackViewModelPopulator extends AbstractApplicationModelPopulator {

    private OrganisationRestService organisationRestService;
    private OrganisationService organisationService;
    private ApplicationService applicationService;
    private CompetitionService competitionService;
    private UserService userService;
    private FinanceService financeService;
    private FileEntryRestService fileEntryRestService;
    private ApplicationFinanceSummaryViewModelPopulator applicationFinanceSummaryViewModelPopulator;
    private ApplicationFundingBreakdownViewModelPopulator applicationFundingBreakdownViewModelPopulator;
    private AssessmentRestService assessmentRestService;
    private SectionService sectionService;
    private AssessorFormInputResponseRestService assessorFormInputResponseRestService;
    private InterviewAssignmentRestService interviewAssignmentRestService;
    private InterviewFeedbackViewModelPopulator interviewFeedbackViewModelPopulator;
    private ProjectService projectService;

    public ApplicationFeedbackViewModelPopulator(OrganisationRestService organisationRestService,
                                                 ApplicationService applicationService,
                                                 CompetitionService competitionService,
                                                 OrganisationService organisationService,
                                                 UserService userService,
                                                 FileEntryRestService fileEntryRestService,
                                                 FinanceService financeService,
                                                 AssessmentRestService assessmentRestService,
                                                 SectionService sectionService,
                                                 QuestionService questionService,
                                                 AssessorFormInputResponseRestService assessorFormInputResponseRestService,
                                                 ApplicationFinanceSummaryViewModelPopulator applicationFinanceSummaryViewModelPopulator,
                                                 ApplicationFundingBreakdownViewModelPopulator applicationFundingBreakdownViewModelPopulator,
                                                 InterviewFeedbackViewModelPopulator interviewFeedbackViewModelPopulator,
                                                 InterviewAssignmentRestService interviewAssignmentRestService,
                                                 ProjectService projectService) {
        super(sectionService, questionService);
        this.organisationRestService = organisationRestService;
        this.applicationService = applicationService;
        this.competitionService = competitionService;
        this.organisationService = organisationService;
        this.userService = userService;
        this.fileEntryRestService = fileEntryRestService;
        this.financeService = financeService;
        this.assessmentRestService = assessmentRestService;
        this.sectionService = sectionService;
        this.assessorFormInputResponseRestService = assessorFormInputResponseRestService;
        this.applicationFinanceSummaryViewModelPopulator = applicationFinanceSummaryViewModelPopulator;
        this.applicationFundingBreakdownViewModelPopulator = applicationFundingBreakdownViewModelPopulator;
        this.interviewFeedbackViewModelPopulator = interviewFeedbackViewModelPopulator;
        this.interviewAssignmentRestService = interviewAssignmentRestService;
        this.projectService = projectService;
    }

    public ApplicationFeedbackViewModel populate(long applicationId, UserResource user, MultiValueMap<String, String> queryParams, String origin) {

        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());

        ProcessRoleResource leadApplicantUser = userService.getLeadApplicantProcessRoleOrNull(applicationId);
        OrganisationResource leadOrganisation = organisationService.getOrganisationById(leadApplicantUser.getOrganisationId());
        List<OrganisationResource> partners = organisationRestService.getOrganisationsByApplicationId(applicationId).getSuccess();


        OrganisationApplicationFinanceOverviewImpl organisationFinanceOverview = new OrganisationApplicationFinanceOverviewImpl(
                financeService,
                fileEntryRestService,
                applicationId
        );

        List<String> feedback = assessmentRestService.getApplicationFeedback(applicationId).getSuccess().getFeedback();

        SectionResource financeSection = sectionService.getFinanceSection(application.getCompetition());
        final boolean hasFinanceSection = financeSection != null;

        ApplicationAssessmentAggregateResource scores = assessorFormInputResponseRestService.getApplicationAssessmentAggregate(applicationId).getSuccess();

        ApplicationFinanceSummaryViewModel applicationFinanceSummaryViewModel = applicationFinanceSummaryViewModelPopulator.populate(applicationId, user);
        ApplicationFundingBreakdownViewModel applicationFundingBreakdownViewModel = applicationFundingBreakdownViewModelPopulator.populate(applicationId);

        final InterviewFeedbackViewModel interviewFeedbackViewModel;
        if (interviewAssignmentRestService.isAssignedToInterview(applicationId).getSuccess()) {
            interviewFeedbackViewModel = interviewFeedbackViewModelPopulator.populate(applicationId, user, competition.getCompetitionStatus().isFeedbackReleased());
        } else {
            interviewFeedbackViewModel = null;
        }

        ProjectResource project = projectService.getByApplicationId(applicationId);
        boolean projectWithdrawn = (project != null && project.isWithdrawn());

        queryParams.put("competitionId", asList(String.valueOf(application.getCompetition())));
        queryParams.put("applicationId", asList(String.valueOf(application.getId())));

        return new ApplicationFeedbackViewModel(
                application,
                competition,
                leadOrganisation,
                partners,
                organisationFinanceOverview.getTotalFundingSought(),
                feedback,
                hasFinanceSection,
                getSections(competition.getId()),
                getSectionQuestions(competition.getId()),
                scores,
                applicationFinanceSummaryViewModel,
                applicationFundingBreakdownViewModel,
                interviewFeedbackViewModel,
                projectWithdrawn,
                ApplicationSummaryOrigin.valueOf(origin),
                buildOriginQueryString(ApplicationSummaryOrigin.valueOf(origin), queryParams),
                buildBackUrl(ApplicationSummaryOrigin.valueOf(origin), queryParams, "competitionId", "projectId")
        );
    }
}
