package org.innovateuk.ifs.assessment.overview.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.innovateuk.ifs.AbstractApplicationMockMVCTest;
import org.innovateuk.ifs.applicant.resource.ApplicantQuestionResource;
import org.innovateuk.ifs.applicant.resource.ApplicantResource;
import org.innovateuk.ifs.applicant.resource.ApplicantSectionResource;
import org.innovateuk.ifs.applicant.service.ApplicantRestService;
import org.innovateuk.ifs.application.finance.view.FinanceModelManager;
import org.innovateuk.ifs.application.finance.view.OrganisationFinanceOverview;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.application.populator.ApplicationNavigationPopulator;
import org.innovateuk.ifs.application.populator.forminput.FormInputViewModelGenerator;
import org.innovateuk.ifs.application.populator.section.YourProjectCostsSectionPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseFileEntryResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.service.SectionRestService;
import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;
import org.innovateuk.ifs.application.viewmodel.section.DefaultProjectCostSection;
import org.innovateuk.ifs.application.viewmodel.section.DefaultYourProjectCostsSectionViewModel;
import org.innovateuk.ifs.application.viewmodel.section.JesYourProjectCostsSectionViewModel;
import org.innovateuk.ifs.assessment.common.service.AssessmentService;
import org.innovateuk.ifs.assessment.overview.form.AssessmentOverviewForm;
import org.innovateuk.ifs.assessment.overview.populator.AssessmentDetailedFinancesModelPopulator;
import org.innovateuk.ifs.assessment.overview.populator.AssessmentFinancesSummaryModelPopulator;
import org.innovateuk.ifs.assessment.overview.populator.AssessmentOverviewModelPopulator;
import org.innovateuk.ifs.assessment.overview.viewmodel.*;
import org.innovateuk.ifs.assessment.resource.AssessmentRejectOutcomeValue;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.assessment.resource.AssessorFormInputResponseResource;
import org.innovateuk.ifs.assessment.service.AssessorFormInputResponseRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.form.resource.*;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.innovateuk.ifs.applicant.builder.ApplicantFormInputResourceBuilder.newApplicantFormInputResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantQuestionResourceBuilder.newApplicantQuestionResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantResourceBuilder.newApplicantResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantSectionResourceBuilder.newApplicantSectionResource;
import static org.innovateuk.ifs.application.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static org.innovateuk.ifs.assessment.builder.AssessmentResourceBuilder.newAssessmentResource;
import static org.innovateuk.ifs.assessment.builder.AssessorFormInputResponseResourceBuilder.newAssessorFormInputResponseResource;
import static org.innovateuk.ifs.assessment.resource.AssessmentRejectOutcomeValue.CONFLICT_OF_INTEREST;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.ASSESSMENT_REJECTION_FAILED;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.finance.resource.OrganisationSize.MEDIUM;
import static org.innovateuk.ifs.finance.builder.OrganisationFinanceOverviewBuilder.newOrganisationFinanceOverviewBuilder;
import static org.innovateuk.ifs.form.builder.FormInputResourceBuilder.newFormInputResource;
import static org.innovateuk.ifs.form.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.form.builder.SectionResourceBuilder.newSectionResource;
import static org.innovateuk.ifs.form.resource.FormInputScope.ASSESSMENT;
import static org.innovateuk.ifs.form.resource.FormInputType.*;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.innovateuk.ifs.util.CollectionFunctions.combineLists;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.Silent.class)
@TestPropertySource(locations = "classpath:application.properties")
public class AssessmentOverviewControllerTest extends AbstractApplicationMockMVCTest<AssessmentOverviewController> {

    private static final long APPLICATION_ID = 1L;

    private List<SectionResource> sections;
    private QuestionResource questionApplicationDetails;
    private QuestionResource questionScope;
    private QuestionResource questionBusinessOpportunity;
    private QuestionResource questionPotentialMarket;
    private FormInputResource potentialMarketFileEntryFormInput;
    private CompetitionResource competition;
    private AssessmentResource assessment;

    @Mock
    private AssessmentService assessmentService;

    @Mock
    private ApplicantRestService applicantRestService;

    @Mock
    private ApplicationNavigationPopulator navigationPopulator;

    @Mock
    private FormInputViewModelGenerator formInputViewModelGenerator;

    @Mock
    private AssessorFormInputResponseRestService assessorFormInputResponseRestService;

    @Mock
    private SectionRestService sectionRestService;

    @Spy
    @InjectMocks
    private AssessmentOverviewModelPopulator assessmentOverviewModelPopulator;

    @Spy
    @InjectMocks
    private AssessmentFinancesSummaryModelPopulator assessmentFinancesSummaryModelPopulator;

    @Spy
    @InjectMocks
    private AssessmentDetailedFinancesModelPopulator assessmentDetailedFinancesModelPopulator;

    @Spy
    @InjectMocks
    private YourProjectCostsSectionPopulator yourProjectCostsSectionPopulator;

    @Override
    protected AssessmentOverviewController supplyControllerUnderTest() {
        return new AssessmentOverviewController();
    }

    @Before
    public void setup() {
        super.setup();

        competition = newCompetitionResource()
                .withAssessorAcceptsDate(ZonedDateTime.now().minusDays(2))
                .withAssessorDeadlineDate(ZonedDateTime.now().plusDays(4))
                .withName("Super creative competition name")
                .build();

        assessment = newAssessmentResource()
                .withApplication(APPLICATION_ID)
                .withCompetition(competition.getId())
                .withApplicationName("Using natural gas to heat homes")
                .build();

        questionApplicationDetails = newQuestionResource()
                .withShortName("Application Details")
                .build();

        questionScope = newQuestionResource()
                .withShortName("Scope")
                .build();

        questionBusinessOpportunity = newQuestionResource()
                .withShortName("Business opportunity")
                .withAssessorMaximumScore(10)
                .build();

        questionPotentialMarket = newQuestionResource()
                .withShortName("Potential market")
                .withAssessorMaximumScore(15)
                .build();

        List<QuestionResource> questions = asList(questionApplicationDetails, questionScope, questionBusinessOpportunity, questionPotentialMarket);

        @SuppressWarnings("unchecked") List<Long>[] questionIds = new List[] { asList(questionApplicationDetails.getId(), questionScope.getId()),
                asList(questionBusinessOpportunity.getId(), questionPotentialMarket.getId()),
                emptyList() };

        sections = newSectionResource()
                .withName("Project details", "Application questions", "Finances")
                .withQuestions(questionIds)
                .withAssessorGuidanceDescription("These do not need scoring.",
                        "Each question should be given a score.",
                        "Each partner is required to submit their own finances.")
                .build(3);

        potentialMarketFileEntryFormInput = newFormInputResource()
                .build();

        List<FormInputResource> assessorFormInputsScope = newFormInputResource()
                .withType(TEXTAREA, ASSESSOR_APPLICATION_IN_SCOPE)
                .withQuestion(questionScope.getId())
                .build(2);

        List<FormInputResource> assessorFormInputsBusinessOpportunity = newFormInputResource()
                .withType(TEXTAREA, ASSESSOR_SCORE)
                .withQuestion(questionBusinessOpportunity.getId())
                .build(2);

        List<FormInputResource> assessorFormInputsPotentialMarket = newFormInputResource()
                .withType(TEXTAREA, ASSESSOR_SCORE)
                .withQuestion(questionPotentialMarket.getId())
                .build(2);

        List<FormInputResource> assessorFormInputs = combineLists(assessorFormInputsScope,
                assessorFormInputsBusinessOpportunity, assessorFormInputsPotentialMarket);

        List<FormInputResponseResource> applicantResponses = newFormInputResponseResource()
                .withFormInputs(potentialMarketFileEntryFormInput.getId())
                .withFileEntry(1L)
                .withQuestion(questionPotentialMarket.getId())
                .withFileName("Project-plan.pdf")
                .withFilesizeBytes(112640L)
                .build(1);

        AssessorFormInputResponseResource assessorResponsesScope = newAssessorFormInputResponseResource()
                .withAssessment(assessment.getId())
                .withQuestion(questionScope.getId())
                .withFormInput(assessorFormInputsScope.get(1).getId())
                .withValue("true")
                .build();

        List<AssessorFormInputResponseResource> assessorResponsesBusinessOpportunity = newAssessorFormInputResponseResource()
                .withAssessment(assessment.getId())
                .withQuestion(questionBusinessOpportunity.getId())
                .withFormInput(assessorFormInputsBusinessOpportunity.get(0).getId(),
                        assessorFormInputsBusinessOpportunity.get(1).getId())
                .withValue("Text response", "7")
                .build(2);

        AssessorFormInputResponseResource assessorResponsesPotentialMarket = newAssessorFormInputResponseResource()
                .withAssessment(assessment.getId())
                .withQuestion(questionPotentialMarket.getId())
                .withFormInput(assessorFormInputsPotentialMarket.get(0).getId())
                .withValue("Text response")
                .build();

        List<AssessorFormInputResponseResource> assessorResponses = combineLists(combineLists(assessorResponsesScope,
                assessorResponsesBusinessOpportunity), assessorResponsesPotentialMarket);

        when(assessmentService.getById(assessment.getId())).thenReturn(assessment);
        when(competitionRestService.getCompetitionById(competition.getId())).thenReturn(restSuccess(competition));
        when(sectionRestService.getByCompetitionIdVisibleForAssessment(competition.getId())).thenReturn(restSuccess(sections));
        when(questionRestService.findByCompetition(competition.getId())).thenReturn(restSuccess(questions));
        when(formInputRestService.getByCompetitionIdAndScope(competition.getId(), ASSESSMENT)).thenReturn(restSuccess(assessorFormInputs));
        when(assessorFormInputResponseRestService.getAllAssessorFormInputResponses(assessment.getId())).thenReturn(restSuccess(assessorResponses));
        when(formInputResponseRestService.getResponsesByApplicationId(APPLICATION_ID)).thenReturn(restSuccess(applicantResponses));
    }

    @Test
    public void getOverview() throws Exception {

        List<AssessmentOverviewSectionViewModel> expectedSections = asList(
                new AssessmentOverviewSectionViewModel(sections.get(0).getId(),
                        "Project details",
                        "These do not need scoring.",
                        asList(
                                new AssessmentOverviewQuestionViewModel(
                                        questionApplicationDetails.getId(),
                                        questionApplicationDetails.getShortName(),
                                        questionApplicationDetails.getQuestionNumber(),
                                        questionApplicationDetails.getAssessorMaximumScore(),
                                        false,
                                        false,
                                        null,
                                        null),
                                new AssessmentOverviewQuestionViewModel(
                                        questionScope.getId(),
                                        questionScope.getShortName(),
                                        questionScope.getQuestionNumber(),
                                        questionScope.getAssessorMaximumScore(),
                                        true,
                                        false,
                                        TRUE,
                                        null)
                        ),
                        false
                ),
                new AssessmentOverviewSectionViewModel(sections.get(1).getId(),
                        "Application questions",
                        "Each question should be given a score.",
                        asList(
                                new AssessmentOverviewQuestionViewModel(
                                        questionBusinessOpportunity.getId(),
                                        questionBusinessOpportunity.getShortName(),
                                        questionBusinessOpportunity.getQuestionNumber(),
                                        questionBusinessOpportunity.getAssessorMaximumScore(),
                                        true,
                                        true,
                                        null,
                                        "7"),
                                new AssessmentOverviewQuestionViewModel(
                                        questionPotentialMarket.getId(),
                                        questionPotentialMarket.getShortName(),
                                        questionPotentialMarket.getQuestionNumber(),
                                        questionPotentialMarket.getAssessorMaximumScore(),
                                        true,
                                        false,
                                        null,
                                        null)
                        ),
                        false
                ),
                new AssessmentOverviewSectionViewModel((sections.get(2).getId()),
                        "Finances",
                        "Each partner is required to submit their own finances.",
                        emptyList(),
                        true
                )
        );

        List<AssessmentOverviewAppendixViewModel> expectedAppendices = singletonList(
                new AssessmentOverviewAppendixViewModel(
                        potentialMarketFileEntryFormInput.getId(),
                        "Potential market",
                        "Project-plan.pdf",
                        "110 KB")
        );

        AssessmentOverviewViewModel expectedViewModel = new AssessmentOverviewViewModel(
                assessment.getId(),
                APPLICATION_ID,
                "Using natural gas to heat homes",
                competition.getId(),
                "Super creative competition name",
                50L,
                3L,
                expectedSections,
                expectedAppendices
        );

        mockMvc.perform(get("/" + assessment.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("model"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(view().name("assessment/application-overview"));

        InOrder inOrder = inOrder(assessmentService, competitionRestService, sectionRestService, questionRestService,
                formInputRestService, assessorFormInputResponseRestService, formInputResponseRestService);
        inOrder.verify(assessmentService).getById(assessment.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(questionRestService).findByCompetition(competition.getId());
        inOrder.verify(sectionRestService).getByCompetitionIdVisibleForAssessment(competition.getId());
        inOrder.verify(formInputRestService).getByCompetitionIdAndScope(competition.getId(), ASSESSMENT);
        inOrder.verify(assessorFormInputResponseRestService).getAllAssessorFormInputResponses(assessment.getId());
        inOrder.verify(formInputResponseRestService).getResponsesByApplicationId(APPLICATION_ID);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void getFinancesSummary() throws Exception {
        setupCompetition();
        setupApplicationWithRoles();

        ZonedDateTime now = ZonedDateTime.now();

        CompetitionResource competitionResource = newCompetitionResource()
                .withAssessorAcceptsDate(now.minusDays(2))
                .withAssessorDeadlineDate(now.plusDays(4))
                .build();

        ApplicationResource applicationResource = applications.get(0);

        AssessmentResource assessmentResource = newAssessmentResource()
                .withApplication(applicationResource.getId())
                .withApplicationName("Application name")
                .withCompetition(competitionResource.getId())
                .build();

        ProcessRoleResource assessorRole = newProcessRoleResource().withUser(assessor).build();

        SortedSet<OrganisationResource> orgSet = setupOrganisations();
        List<ApplicationFinanceResource> appFinanceList = setupFinances(applicationResource, orgSet);
        OrganisationFinanceOverview organisationFinanceOverview = newOrganisationFinanceOverviewBuilder()
                .withApplicationId(applicationResource.getId())
                .withOrganisationFinances(appFinanceList)
                .build();

        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(assessmentService.getById(assessmentResource.getId())).thenReturn(assessmentResource);
        when(userRestService.findProcessRole(applicationResource.getId())).thenReturn(restSuccess(asList(assessorRole)));
        when(organisationService.getApplicationOrganisations(asList(assessorRole))).thenReturn(orgSet);
        when(organisationService.getApplicationLeadOrganisation(asList(assessorRole))).thenReturn(Optional.ofNullable(newOrganisationResource().build()));


        AssessmentFinancesSummaryViewModel expectedViewModel = new AssessmentFinancesSummaryViewModel(
                assessmentResource.getId(), applicationResource.getId(), "Application name", 3, 50);

        mockMvc.perform(get("/{assessmentId}/finances", assessmentResource.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attribute("applicationOrganisations", orgSet))
                .andExpect(model().attribute("organisationFinances", organisationFinanceOverview.getFinancesByOrganisation()))
                .andExpect(model().attribute("financeTotal", organisationFinanceOverview.getTotal()))
                .andExpect(view().name("assessment/application-finances-summary"));
    }

    @Test
    public void getDetailedFinancesBusiness() throws Exception {
        setupCompetition();
        setupApplicationWithRoles();
        ApplicationResource applicationResource = applications.get(0);
        AssessmentResource assessmentResource = setUpAssessmentResource(applicationResource.getId());

        SortedSet<OrganisationResource> orgSet = setupOrganisations();
        OrganisationResource organisation = orgSet.first();
        List<ApplicationFinanceResource> appFinanceList = setupFinances(applicationResource, orgSet);
        OrganisationFinanceOverview organisationFinanceOverview = newOrganisationFinanceOverviewBuilder()
                .withApplicationId(applicationResource.getId())
                .withOrganisationFinances(appFinanceList)
                .build();

        ApplicantResource applicant = newApplicantResource().withProcessRole(processRoles.get(0)).withOrganisation(organisations.get(0)).build();
        QuestionResource costQuestion = newQuestionResource().withType(QuestionType.COST).build();
        ApplicantSectionResource costSection = newApplicantSectionResource()
                .withApplicantQuestions(newApplicantQuestionResource().withQuestion(costQuestion).build(1))
                .build();

        ApplicantSectionResource section = newApplicantSectionResource()
                .withApplication(applicationResource)
                .withCompetition(competitionResource)
                .withCurrentApplicant(applicant)
                .withApplicants(asList(applicant))
                .withSection(newSectionResource()
                        .withType(SectionType.FINANCE).build())
                .withApplicantQuestions(
                        newApplicantQuestionResource()
                                .withQuestion(newQuestionResource()
                                        .withType(QuestionType.GENERAL).build())
                                .build(1))
                .withApplicantChildrenSections(asList(costSection))
                .withCurrentUser(loggedInUser)
                .build();

        AssessmentDetailedFinancesViewModel expectedViewModel = new AssessmentDetailedFinancesViewModel(
                assessmentResource.getId(), applicationResource.getId(), "Application name", "finance");

        ApplicationForm form = new ApplicationForm();
        AbstractFormInputViewModel formInputViewModel = mock(AbstractFormInputViewModel.class);
        FinanceModelManager financeModelManager = mock(FinanceModelManager.class);

        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(assessmentService.getById(assessmentResource.getId())).thenReturn(assessmentResource);
        when(userRestService.findProcessRole(assessmentResource.getApplication())).thenReturn(restSuccess(application1ProcessRoles));
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.PROJECT_COST_FINANCES)).thenReturn(Arrays.asList(sectionResources.get(7)));
        when(applicantRestService.getSection(application1ProcessRoles.get(0).getUser(), applicationResource.getId(), sectionResources.get(7).getId())).thenReturn(section);
        when(formInputViewModelGenerator.fromSection(section, costSection, form, true)).thenReturn(asList(formInputViewModel));
        when(financeViewHandlerProvider.getFinanceModelManager(section.getCurrentApplicant().getOrganisation().getOrganisationType())).thenReturn(financeModelManager);

        MvcResult result = mockMvc.perform(get("/{assessmentId}/detailed-finances/organisation/{organisationId}", assessmentResource.getId(), organisation.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attribute("applicationOrganisation", organisations.get(0)))
                .andExpect(model().attribute("organisationFinances", organisationFinanceOverview.getFinancesByOrganisation()))
                .andExpect(view().name("assessment/application-detailed-finances"))
                .andReturn();

        DefaultYourProjectCostsSectionViewModel resultCostsViewModel = (DefaultYourProjectCostsSectionViewModel) result.getModelAndView().getModel().get("detailedCostings");
        assertThat(resultCostsViewModel.isSection(), equalTo(true));
        assertThat(resultCostsViewModel.isComplete(), equalTo(false));
        assertThat(resultCostsViewModel.getDefaultProjectCostSections().size(), equalTo(1));
        DefaultProjectCostSection costSectionViewModel = resultCostsViewModel.getDefaultProjectCostSections().get(0);
        assertThat(costSectionViewModel.getApplicantResource(), equalTo(section));
        assertThat(costSectionViewModel.getApplicantSection(), equalTo(costSection));
        assertThat(costSectionViewModel.getCostViews(), equalTo(asList(formInputViewModel)));
    }

    @Test
    public void getDetailedFinancesAcademic() throws Exception {
        setupCompetition();
        setupApplicationWithRoles();
        ApplicationResource applicationResource = applications.get(0);
        AssessmentResource assessmentResource = setUpAssessmentResource(applicationResource.getId());
        FormInputResource fileUpload = newFormInputResource().withType(FormInputType.FINANCE_UPLOAD).build();

        SortedSet<OrganisationResource> orgSet = setupOrganisations();
        OrganisationResource organisation = orgSet.first();
        List<ApplicationFinanceResource> appFinanceList = setupFinances(applicationResource, orgSet);
        OrganisationFinanceOverview organisationFinanceOverview = newOrganisationFinanceOverviewBuilder()
                .withApplicationId(applicationResource.getId())
                .withOrganisationFinances(appFinanceList)
                .build();

        ApplicantResource applicant = newApplicantResource().withProcessRole(processRoles.get(0)).withOrganisation(organisations.get(1)).build();
        QuestionResource costQuestion = newQuestionResource().withType(QuestionType.COST).build();
        ApplicantQuestionResource costApplicantQuestion = newApplicantQuestionResource()
                .withQuestion(costQuestion)
                .withApplicantFormInputs(
                        newApplicantFormInputResource()
                                .withFormInput(fileUpload)
                                .build(1))
                .build();
        ApplicantSectionResource costSection = newApplicantSectionResource()
                .withApplicantQuestions(asList(costApplicantQuestion))
                .build();

        ApplicantSectionResource section = newApplicantSectionResource()
                .withApplication(applicationResource)
                .withCompetition(competitionResource)
                .withCurrentApplicant(applicant)
                .withApplicants(asList(applicant))
                .withSection(newSectionResource()
                        .withType(SectionType.FINANCE).build())
                .withApplicantQuestions(newApplicantQuestionResource()
                        .withQuestion(newQuestionResource()
                                .withType(QuestionType.GENERAL)
                                .build())
                        .withApplicantFormInputs(Collections.emptyList())
                        .build(1))
                .withApplicantChildrenSections(asList(costSection))
                .withCurrentUser(loggedInUser)
                .build();

        AssessmentDetailedFinancesViewModel expectedViewModel = new AssessmentDetailedFinancesViewModel(
                assessmentResource.getId(), applicationResource.getId(), "Application name", "finance");

        FinanceModelManager financeModelManager = mock(FinanceModelManager.class);

        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(assessmentService.getById(assessmentResource.getId())).thenReturn(assessmentResource);
        when(userRestService.findProcessRole(assessmentResource.getApplication())).thenReturn(restSuccess(application1ProcessRoles));
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.PROJECT_COST_FINANCES)).thenReturn(Arrays.asList(sectionResources.get(7)));
        when(applicantRestService.getSection(application1ProcessRoles.get(0).getUser(), applicationResource.getId(), sectionResources.get(7).getId())).thenReturn(section);
        when(financeViewHandlerProvider.getFinanceModelManager(section.getCurrentApplicant().getOrganisation().getOrganisationType())).thenReturn(financeModelManager);

        MvcResult result = mockMvc.perform(get("/{assessmentId}/detailed-finances/organisation/{organisationId}", assessmentResource.getId(), organisation.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(model().attribute("applicationOrganisation", organisations.get(0)))
                .andExpect(model().attribute("organisationFinances", organisationFinanceOverview.getFinancesByOrganisation()))
                .andExpect(view().name("assessment/application-detailed-finances"))
                .andReturn();

        JesYourProjectCostsSectionViewModel viewModel = (JesYourProjectCostsSectionViewModel) result.getModelAndView().getModel().get("detailedCostings");

        assertThat(viewModel.isSection(), equalTo(true));
        assertThat(viewModel.isComplete(), equalTo(false));
        assertThat(viewModel.getFinanceUploadFormInput(), equalTo(fileUpload));
        assertThat(viewModel.getFinanceUploadQuestion(), equalTo(costQuestion));
    }

    @Test
    public void rejectInvitation() throws Exception {
        Long assessmentId = 1L;
        Long competitionId = 2L;
        AssessmentRejectOutcomeValue reason = CONFLICT_OF_INTEREST;
        String comment = String.join(" ", nCopies(100, "comment"));

        AssessmentResource assessment = newAssessmentResource()
                .with(id(assessmentId))
                .withCompetition(competitionId)
                .build();

        when(assessmentService.getRejectableById(assessmentId)).thenReturn(assessment);
        when(assessmentService.rejectInvitation(assessmentId, reason, comment)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/{assessmentId}/reject", assessmentId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason.name())
                .param("rejectComment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(format("/assessor/dashboard/competition/%s", competitionId)))
                .andReturn();

        InOrder inOrder = inOrder(assessmentService);
        inOrder.verify(assessmentService).getRejectableById(assessmentId);
        inOrder.verify(assessmentService).rejectInvitation(assessmentId, reason, comment);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvitation_noReason() throws Exception {
        String comment = String.join(" ", nCopies(100, "comment"));

        when(assessmentService.getRejectableById(assessment.getId())).thenReturn(assessment);

        AssessmentOverviewForm expectedForm = new AssessmentOverviewForm();
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/reject", assessment.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", "")
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "rejectReason"))
                .andExpect(view().name("assessment/application-overview"))
                .andReturn();

        AssessmentOverviewForm form = (AssessmentOverviewForm) result.getModelAndView().getModel().get("form");

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("rejectReason"));
        assertEquals("Please enter a reason.", bindingResult.getFieldError("rejectReason").getDefaultMessage());

        InOrder inOrder = inOrder(assessmentService, competitionRestService, sectionRestService, questionRestService,
                formInputRestService, assessorFormInputResponseRestService, formInputResponseRestService);
        inOrder.verify(assessmentService).getById(assessment.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(questionRestService).findByCompetition(competition.getId());
        inOrder.verify(sectionRestService).getByCompetitionIdVisibleForAssessment(competition.getId());
        inOrder.verify(formInputRestService).getByCompetitionIdAndScope(competition.getId(), ASSESSMENT);
        inOrder.verify(assessorFormInputResponseRestService).getAllAssessorFormInputResponses(assessment.getId());
        inOrder.verify(formInputResponseRestService).getResponsesByApplicationId(APPLICATION_ID);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvitation_exceedsCharacterSizeLimit() throws Exception {
        AssessmentRejectOutcomeValue reason = CONFLICT_OF_INTEREST;
        String comment = RandomStringUtils.random(5001);

        when(assessmentService.getRejectableById(assessment.getId())).thenReturn(assessment);

        AssessmentOverviewForm expectedForm = new AssessmentOverviewForm();
        expectedForm.setRejectReason(reason);
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/reject", assessment.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason.name())
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "rejectComment"))
                .andExpect(view().name("assessment/application-overview"))
                .andReturn();

        AssessmentOverviewForm form = (AssessmentOverviewForm) result.getModelAndView().getModel().get("form");

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("rejectComment"));
        assertEquals("This field cannot contain more than {1} characters.", bindingResult.getFieldError("rejectComment").getDefaultMessage());
        assertEquals(5000, bindingResult.getFieldError("rejectComment").getArguments()[1]);

        InOrder inOrder = inOrder(assessmentService, competitionRestService, sectionRestService, questionRestService,
                formInputRestService, assessorFormInputResponseRestService, formInputResponseRestService);
        inOrder.verify(assessmentService).getById(assessment.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(questionRestService).findByCompetition(competition.getId());
        inOrder.verify(sectionRestService).getByCompetitionIdVisibleForAssessment(competition.getId());
        inOrder.verify(formInputRestService).getByCompetitionIdAndScope(competition.getId(), ASSESSMENT);
        inOrder.verify(assessorFormInputResponseRestService).getAllAssessorFormInputResponses(assessment.getId());
        inOrder.verify(formInputResponseRestService).getResponsesByApplicationId(APPLICATION_ID);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvitation_exceedsWordLimit() throws Exception {
        AssessmentRejectOutcomeValue reason = CONFLICT_OF_INTEREST;
        String comment = String.join(" ", nCopies(101, "comment"));

        when(assessmentService.getRejectableById(assessment.getId())).thenReturn(assessment);

        AssessmentOverviewForm expectedForm = new AssessmentOverviewForm();
        expectedForm.setRejectReason(reason);
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/reject", assessment.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason.name())
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "rejectComment"))
                .andExpect(view().name("assessment/application-overview"))
                .andReturn();

        AssessmentOverviewForm form = (AssessmentOverviewForm) result.getModelAndView().getModel().get("form");

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("rejectComment"));
        assertEquals("Maximum word count exceeded. Please reduce your word count to {1}.", bindingResult.getFieldError("rejectComment").getDefaultMessage());
        assertEquals(100, bindingResult.getFieldError("rejectComment").getArguments()[1]);

        InOrder inOrder = inOrder(assessmentService, competitionRestService, sectionRestService, questionRestService,
                formInputRestService, assessorFormInputResponseRestService, formInputResponseRestService);
        inOrder.verify(assessmentService).getById(assessment.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(questionRestService).findByCompetition(competition.getId());
        inOrder.verify(sectionRestService).getByCompetitionIdVisibleForAssessment(competition.getId());
        inOrder.verify(formInputRestService).getByCompetitionIdAndScope(competition.getId(), ASSESSMENT);
        inOrder.verify(assessorFormInputResponseRestService).getAllAssessorFormInputResponses(assessment.getId());
        inOrder.verify(formInputResponseRestService).getResponsesByApplicationId(APPLICATION_ID);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rejectInvitation_eventNotAccepted() throws Exception {
        AssessmentRejectOutcomeValue reason = CONFLICT_OF_INTEREST;
        String comment = String.join(" ", nCopies(100, "comment"));

        when(assessmentService.getRejectableById(assessment.getId())).thenReturn(assessment);
        when(assessmentService.rejectInvitation(assessment.getId(), reason, comment)).thenReturn(serviceFailure(ASSESSMENT_REJECTION_FAILED));

        AssessmentOverviewForm expectedForm = new AssessmentOverviewForm();
        expectedForm.setRejectReason(reason);
        expectedForm.setRejectComment(comment);

        MvcResult result = mockMvc.perform(post("/{assessmentId}/reject", assessment.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("rejectReason", reason.name())
                .param("rejectComment", comment))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form"))
                .andExpect(view().name("assessment/application-overview"))
                .andReturn();

        AssessmentOverviewForm form = (AssessmentOverviewForm) result.getModelAndView().getModel().get("form");

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(1, bindingResult.getGlobalErrorCount());
        assertEquals(0, bindingResult.getFieldErrorCount());
        assertEquals(ASSESSMENT_REJECTION_FAILED.name(), bindingResult.getGlobalError().getCode());

        InOrder inOrder = inOrder(assessmentService, competitionRestService, sectionRestService, questionRestService,
                formInputRestService, assessorFormInputResponseRestService, formInputResponseRestService);
        inOrder.verify(assessmentService).getRejectableById(assessment.getId());
        inOrder.verify(assessmentService).rejectInvitation(assessment.getId(), reason, comment);
        inOrder.verify(assessmentService).getById(assessment.getId());
        inOrder.verify(competitionRestService).getCompetitionById(competition.getId());
        inOrder.verify(questionRestService).findByCompetition(competition.getId());
        inOrder.verify(sectionRestService).getByCompetitionIdVisibleForAssessment(competition.getId());
        inOrder.verify(formInputRestService).getByCompetitionIdAndScope(competition.getId(), ASSESSMENT);
        inOrder.verify(assessorFormInputResponseRestService).getAllAssessorFormInputResponses(assessment.getId());
        inOrder.verify(formInputResponseRestService).getResponsesByApplicationId(APPLICATION_ID);
    }

    @Test
    public void downloadAppendix() throws Exception {
        long assessmentId = 1L;
        long applicationId = 2L;
        long formInputId = 3L;

        UserResource assessor = getLoggedInUser();

        ProcessRoleResource assessorRole = newProcessRoleResource().withUser(assessor).build();
        ByteArrayResource fileContents = new ByteArrayResource("The returned file data".getBytes());
        FileEntryResource fileEntry = newFileEntryResource().withMediaType("text/hello").withFilesizeBytes(1234L).build();
        FormInputResponseFileEntryResource formInputResponseFileEntry =
                new FormInputResponseFileEntryResource(fileEntry, formInputId, applicationId, assessorRole.getId());


        when(userRestService.findProcessRole(applicationId)).thenReturn(restSuccess(asList(assessorRole)));
        when(formInputResponseRestService.getFile(formInputId,
                applicationId,
                assessorRole.getId()))
                .thenReturn(restSuccess(fileContents));
        when(formInputResponseRestService.getFileDetails(formInputId, applicationId, assessorRole.getId()))
                .thenReturn(restSuccess(formInputResponseFileEntry));

        mockMvc.perform(get("/{assessmentId}/application/{applicationId}/formInput/{formInputId}/download/filename.pdf",
                assessmentId,
                applicationId,
                formInputId))
                .andExpect(status().isOk())
                .andExpect(content().string("The returned file data"))
                .andExpect(header().string("Content-Type", "text/hello"))
                .andExpect(header().longValue("Content-Length", "The returned file data".length()));

        verify(userRestService).findProcessRole(applicationId);
        verify(formInputResponseRestService).getFile(formInputId, applicationId, assessorRole.getId());
        verify(formInputResponseRestService).getFileDetails(formInputId, applicationId, assessorRole.getId());
    }

    private List<ApplicationFinanceResource> setupFinances(ApplicationResource app, SortedSet<OrganisationResource> orgSet) {
        List<OrganisationResource> orgList = orgSet.stream().collect(Collectors.toList());
        List<ApplicationFinanceResource> appFinanceList = new ArrayList<>();
        appFinanceList.add(new ApplicationFinanceResource(1L, orgList.get(0).getId(), app.getId(),MEDIUM, ""));
        appFinanceList.add(new ApplicationFinanceResource(2L, orgList.get(1).getId(), app.getId(), MEDIUM, ""));

        when(financeService.getApplicationFinanceTotals(app.getId())).thenReturn(appFinanceList);

        when(applicationFinanceRestService.getResearchParticipationPercentage(anyLong())).thenReturn(restSuccess(0.0));
        when(financeViewHandlerProvider.getFinanceFormHandler(1L)).thenReturn(defaultFinanceFormHandler);
        when(financeViewHandlerProvider.getFinanceModelManager(1L)).thenReturn(defaultFinanceModelManager);

        return appFinanceList;
    }

    private SortedSet<OrganisationResource> setupOrganisations() {
        OrganisationResource org1 = newOrganisationResource().withId(1L).withName("Empire Ltd").build();
        OrganisationResource org2 = newOrganisationResource().withId(2L).withName("Ludlow").build();
        Comparator<OrganisationResource> compareById = Comparator.comparingLong(OrganisationResource::getId);
        SortedSet<OrganisationResource> orgSet = new TreeSet<>(compareById);
        orgSet.add(org1);
        orgSet.add(org2);

        return orgSet;
    }

    private AssessmentResource setUpAssessmentResource(long applicationId) {
        return newAssessmentResource()
                .withApplication(applicationId)
                .withApplicationName("Application name")
                .withCompetition(competitionResource.getId())
                .build();
    }
}
