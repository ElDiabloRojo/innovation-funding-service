package org.innovateuk.ifs.application.forms.controller;

import org.innovateuk.ifs.AbstractApplicationMockMVCTest;
import org.innovateuk.ifs.applicant.builder.ApplicantSectionResourceBuilder;
import org.innovateuk.ifs.applicant.resource.ApplicantResource;
import org.innovateuk.ifs.applicant.service.ApplicantRestService;
import org.innovateuk.ifs.application.finance.view.ApplicationFinanceOverviewModelManager;
import org.innovateuk.ifs.application.finance.view.DefaultFinanceFormHandler;
import org.innovateuk.ifs.application.finance.view.FinanceViewHandlerProvider;
import org.innovateuk.ifs.application.finance.viewmodel.ApplicationFinanceOverviewViewModel;
import org.innovateuk.ifs.application.finance.viewmodel.FinanceViewModel;
import org.innovateuk.ifs.application.forms.saver.ApplicationSectionSaver;
import org.innovateuk.ifs.application.forms.service.ApplicationRedirectionService;
import org.innovateuk.ifs.application.overheads.OverheadFileSaver;
import org.innovateuk.ifs.application.populator.ApplicationNavigationPopulator;
import org.innovateuk.ifs.application.populator.forminput.FormInputViewModelGenerator;
import org.innovateuk.ifs.application.populator.section.AbstractSectionPopulator;
import org.innovateuk.ifs.application.populator.section.YourFinancesSectionPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.viewmodel.section.YourFinancesSectionViewModel;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.form.Form;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.form.resource.SectionType;
import org.innovateuk.ifs.user.resource.FinanceUtil;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.Role;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.applicant.builder.ApplicantQuestionResourceBuilder.newApplicantQuestionResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantResourceBuilder.newApplicantResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantSectionResourceBuilder.newApplicantSectionResource;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.*;
import static org.innovateuk.ifs.application.service.Futures.settable;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.error.Error.globalError;
import static org.innovateuk.ifs.commons.error.ValidationMessages.noErrors;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.form.builder.SectionResourceBuilder.newSectionResource;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.Silent.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationSectionControllerTest extends AbstractApplicationMockMVCTest<ApplicationSectionController> {

    @Mock
    private DefaultFinanceFormHandler defaultFinanceFormHandler;

    @Mock
    private FormInputViewModelGenerator formInputViewModelGenerator;

    @Mock
    private YourFinancesSectionPopulator yourFinancesSectionPopulator;

    @Mock
    private ApplicantRestService applicantRestService;

    @Mock
    private ApplicationNavigationPopulator applicationNavigationPopulator;

    @Mock
    private OverheadFileSaver overheadFileSaver;

    @Spy
    @InjectMocks
    @SuppressWarnings("unused")
    private ApplicationRedirectionService applicationRedirectionService;

    @Mock
    private ApplicationSectionSaver applicationSaver;

    @Mock
    private FinanceViewHandlerProvider financeViewHandlerProvider;

    @Mock
    private ApplicationFinanceOverviewModelManager applicationFinanceOverviewModelManager;

    @Mock
    private FinanceUtil financeUtil;

    @Mock
    @SuppressWarnings("unused")
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    private ApplicationResource application;
    private Long sectionId;
    private Long questionId;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
    private ApplicantSectionResourceBuilder sectionBuilder;
    private ApplicantResource applicantResource;

    @Override
    protected ApplicationSectionController supplyControllerUnderTest() {
        return new ApplicationSectionController();
    }

    @Before
    public void setUpData() {

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupApplicationResponses();
        this.setupFinances();
        this.setupInvites();
        this.setupQuestionStatus(applications.get(0));

        application = applications.get(0);
        sectionId = 1L;
        questionId = 1L;

        // save actions should always succeed.
        when(formInputResponseRestService.saveQuestionResponse(anyLong(), anyLong(), anyLong(), eq(""), anyBoolean())).thenReturn(restSuccess(new ValidationMessages(fieldError("value", "", "Please enter some text 123"))));
        when(formInputResponseRestService.saveQuestionResponse(anyLong(), anyLong(), anyLong(), anyString(), anyBoolean())).thenReturn(restSuccess(noErrors()));
        when(organisationRestService.getOrganisationById(anyLong())).thenReturn(restSuccess(organisations.get(0)));
        when(overheadFileSaver.handleOverheadFileRequest(any())).thenReturn(noErrors());
        when(overheadFileSaver.isOverheadFileRequest(any(HttpServletRequest.class))).thenCallRealMethod();
        when(financeViewHandlerProvider.getFinanceFormHandler(any(), anyLong())).thenReturn(defaultFinanceFormHandler);

        applicantResource = newApplicantResource().withProcessRole(processRoles.get(0)).withOrganisation(organisations.get(0)).build();
        when(applicantRestService.getQuestion(anyLong(), anyLong(), anyLong())).thenReturn(newApplicantQuestionResource().withApplication(application).withCompetition(competitionResource).withCurrentApplicant(applicantResource).withApplicants(asList(applicantResource)).withQuestion(questionResources.values().iterator().next()).withCurrentUser(loggedInUser).build());
        sectionBuilder = newApplicantSectionResource().withApplication(application).withCompetition(competitionResource).withCurrentApplicant(applicantResource).withApplicants(asList(applicantResource)).withSection(newSectionResource().withType(SectionType.FINANCE).build()).withCurrentUser(loggedInUser);
        when(applicantRestService.getSection(anyLong(), anyLong(), anyLong())).thenReturn(sectionBuilder.build());
        when(formInputViewModelGenerator.fromQuestion(any(), any())).thenReturn(Collections.emptyList());
        when(formInputViewModelGenerator.fromSection(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(yourFinancesSectionPopulator.populate(any(), any(), any(), any(), any(), any(), any())).thenReturn(new YourFinancesSectionViewModel(null, null, null, false, Optional.empty(), false));

        ApplicationFinanceOverviewViewModel financeOverviewViewModel = new ApplicationFinanceOverviewViewModel();
        when(applicationFinanceOverviewModelManager.getFinanceDetailsViewModel(competitionResource.getId(), application.getId())).thenReturn(financeOverviewViewModel);

        FinanceViewModel financeViewModel = new FinanceViewModel();
        financeViewModel.setOrganisationGrantClaimPercentage(76);

        when(defaultFinanceModelManager.getFinanceViewModel(anyLong(), anyList(), anyLong(), any(Form.class), anyLong())).thenReturn(financeViewModel);
        Map<SectionType, AbstractSectionPopulator> sectionPopulators = mock(Map.class);
        when(sectionPopulators.get(any())).thenReturn(yourFinancesSectionPopulator);
        ReflectionTestUtils.setField(controller, "sectionPopulators", sectionPopulators);

        when(applicationSaver.saveApplicationForm(any(ApplicationResource.class), any(ApplicationForm.class), anyLong(), anyLong(), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(new ValidationMessages());
    }

    @Test
    public void applicationFormWithOpenSection() throws Exception {

        Long currentSectionId = sectionResources.get(2).getId();

        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        when(sectionService.getAllByCompetitionId(anyLong())).thenReturn(sectionResources);
        MvcResult result = mockMvc.perform(get("/application/1/form/section/" + currentSectionId).header("referer", "/application/1"))
                .andExpect(view().name("application-form"))
                .andReturn();

        Object viewModelResult = result.getModelAndView().getModelMap().get("model");
        assertEquals(YourFinancesSectionViewModel.class, viewModelResult.getClass());

        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(Model.class), any(SectionResource.class), any(Optional.class), any(Boolean.class));
    }

    @Test
    public void applicationFormWithOpenSection_whenTraversedFromSummaryPage() throws Exception {
        Long currentSectionId = sectionResources.get(2).getId();

        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        when(sectionService.getAllByCompetitionId(anyLong())).thenReturn(sectionResources);
        mockMvc.perform(get("/application/1/form/section/" + currentSectionId).header("referer", "/application/1/summary"))
                .andExpect(view().name("application-form"));

        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(Model.class), any(SectionResource.class), any(Optional.class), any(Boolean.class));
    }

    @Test
    public void applicationFormWithOpenSection_financeSection() throws Exception {
        Long currentSectionId = sectionResources.get(6).getId();
        MvcResult result = mockMvc.perform(get("/application/1/form/section/" + currentSectionId))
                .andExpect(view().name("application-form"))
                .andReturn();

        Object viewModelResult = result.getModelAndView().getModelMap().get("model");
        assertEquals(YourFinancesSectionViewModel.class, viewModelResult.getClass());

        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(Model.class), any(SectionResource.class), any(Optional.class), any(Boolean.class));
    }

    @Test
    public void applicationFormSubmit() throws Exception {

        LocalDate futureDate = LocalDate.now().plusDays(1);

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("application.startDate", futureDate.format(FORMATTER))
                        .param("application.startDate.year", Integer.toString(futureDate.getYear()))
                        .param("application.startDate.dayOfMonth", Integer.toString(futureDate.getDayOfMonth()))
                        .param("application.startDate.monthValue", Integer.toString(futureDate.getMonthValue()))
                        .param("application.name", "New Application Title")
                        .param("application.durationInMonths", "12")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void applicationFormSubmit_notRequestingFunding() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), "1")
                        .param(NOT_REQUESTING_FUNDING, "1")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void applicationFormSubmit_requestingFunding() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), "1")
                        .param(REQUESTING_FUNDING, "1")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void applicationFormSubmit_markSectionInComplete() throws Exception {

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param(MARK_SECTION_AS_INCOMPLETE, String.valueOf(sectionId))

        )
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void applicationFormSubmit_markAsComplete() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param(MARK_AS_COMPLETE, "12")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/" + sectionId + "**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void applicationFormSubmit_markAsIncomplete() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param(MARK_AS_INCOMPLETE, "3")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/" + sectionId + "**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void applicationFormSubmit_givesNoValidationErrorsIfNoQuestionIsEmptyOnSectionSubmit() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "Question 1 Response")
                        .param("formInput[2]", "Question 2 Response")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection());
    }

    // See INFUND-1222 - not checking empty values on save now (only on mark as complete).
    @Test
    public void applicationFormSubmit_givesNoValidationErrorsIfQuestionIsEmptyOnSectionSubmit() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "")
                        .param("formInput[2]", "Question 2 Response")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    public void applicationFormSubmit_notAllowedMarkAsComplete() throws Exception {
        // Question should not be marked as complete, since the input is not valid.

        when(applicationSaver.saveApplicationForm(any(ApplicationResource.class), any(ApplicationForm.class), anyLong(), anyLong(), any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(new ValidationMessages(globalError("please.enter.some.text")));

        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "")
                        .param(MARK_AS_COMPLETE, "1")
        ).andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeErrorCount("form", 1))
                .andExpect(model().hasErrors());
        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(Model.class), any(SectionResource.class), any(Optional.class), any(Boolean.class));
    }

    @Test
    public void applicationFormSubmit_assignQuestion() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "Question 1 Response")
                        .param("formInput[2]", "Question 2 Response")
                        .param("formInput[3]", "Question 3 Response")
                        .param("submit-section", "Save")
                        .param("assign_question", questionId + "_" + loggedInUser.getId())
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME));
    }

    @Test
    public void redirectToSection_unique() throws Exception {
        SectionResource financeSection = newSectionResource().withType(SectionType.FINANCE).build();
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.FINANCE))
                .thenReturn(asList(financeSection));

        mockMvc.perform(
                get("/application/{applicationId}/form/{sectionType}", application.getId(), SectionType.FINANCE))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/" + application.getId() + "/form/section/" + financeSection.getId()));

    }

    @Test
    public void redirectToSection_notUnique() throws Exception {
        SectionResource financeSection = newSectionResource().withType(SectionType.FINANCE).build();
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.FINANCE))
                .thenReturn(asList(financeSection, newSectionResource().build()));

        mockMvc.perform(
                get("/application/{applicationId}/form/{sectionType}", application.getId(), SectionType.FINANCE))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/" + application.getId()));

    }

    @Test
    public void redirectToSection_missing() throws Exception {
        when(sectionService.getSectionsForCompetitionByType(competitionResource.getId(), SectionType.FINANCE))
                .thenReturn(asList());

        mockMvc.perform(
                get("/application/{applicationId}/form/{sectionType}", application.getId(), SectionType.FINANCE))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/" + application.getId()));
    }

    @Test
    public void applicationFormSubmit_overheadFileSaverIsCalledOnFormSubmit() throws Exception {
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), "1")
                        .param(MARK_SECTION_AS_COMPLETE, String.valueOf("1"))
        ).andExpect(status().is3xxRedirection());
    }

    @Test
    public void applicationFormWithOpenSectionForApplicant() throws Exception {

        Long currentSectionId = sectionResources.get(2).getId();
        ApplicationResource application = newApplicationResource().build();
        Role role = Role.COLLABORATOR;
        ProcessRoleResource processRole = newProcessRoleResource().withOrganisation(2L).withRole(role).withUserId(4L).build();

        when(applicationService.getById(1L)).thenReturn(application);
        when(userRestService.findProcessRole(application.getId())).thenReturn(restSuccess(asList(processRole)));
        when(applicantRestService.getSection(processRole.getUser(), application.getId(), currentSectionId)).thenReturn(sectionBuilder.build());

        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        when(sectionService.getAllByCompetitionId(anyLong())).thenReturn(sectionResources);
        MvcResult result = mockMvc.perform(get("/application/1/form/section/" + currentSectionId + "/2").header("referer", "/application/1"))
                .andExpect(view().name("application-form"))
                .andReturn();

        Object viewModelResult = result.getModelAndView().getModelMap().get("model");
        assertEquals(YourFinancesSectionViewModel.class, viewModelResult.getClass());

        verify(applicationNavigationPopulator).addAppropriateBackURLToModel(any(Long.class), any(Model.class), any(SectionResource.class), any(Optional.class), any(Boolean.class));
    }

    @Test
    public void applicationFormWithOpenSectionForInternal() throws Exception {

        setLoggedInUser(newUserResource().withRoleGlobal(Role.COMP_ADMIN).build());

        Long currentSectionId = sectionResources.get(7).getId();
        ApplicationResource application = newApplicationResource().build();
        ProcessRoleResource applicantProcessRole = newProcessRoleResource().withOrganisation(2L).withRole(Role.APPLICANT).build();
        ProcessRoleResource assessorProcessRole = newProcessRoleResource().withRole(Role.ASSESSOR).build();
        ProcessRoleResource collabProcessRole = newProcessRoleResource().withOrganisation(2L).withRole(Role.COLLABORATOR).build();

        when(sectionService.getById(anyLong())).thenReturn(sectionResources.get(7));
        when(userRestService.findProcessRole(application.getId())).thenReturn(restSuccess(new ArrayList<ProcessRoleResource>(asList(applicantProcessRole, assessorProcessRole, collabProcessRole))));
        when(applicantRestService.getSection(applicantProcessRole.getUser(), application.getId(), currentSectionId)).thenReturn(sectionBuilder.build());
        when(financeUtil.isUsingJesFinances(any(CompetitionResource.class), anyLong())).thenReturn(false);

        mockMvc.perform(get("/application/{applicationId}/form/section/{sectionId}/{applicantOrganisationId}", application.getId(), currentSectionId, applicantProcessRole.getOrganisationId()))
                .andExpect(status().is3xxRedirection());

        verify(sectionService).getById(anyLong());
        verify(userRestService).findProcessRole(application.getId());
        verify(applicantRestService).getSection(applicantProcessRole.getUser(), application.getId(), currentSectionId);
    }
}