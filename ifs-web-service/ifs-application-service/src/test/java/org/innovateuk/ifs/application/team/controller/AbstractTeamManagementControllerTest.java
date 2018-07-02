package org.innovateuk.ifs.application.team.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.team.form.ApplicantInviteForm;
import org.innovateuk.ifs.application.team.form.ApplicationTeamUpdateForm;
import org.innovateuk.ifs.application.team.service.AbstractTeamManagementService;
import org.innovateuk.ifs.application.team.viewmodel.ApplicationTeamManagementViewModel;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.invite.resource.InviteResultsResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionType.APPLICATION_TEAM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AbstractTeamManagementControllerTest extends BaseControllerMockMVCTest<AbstractTeamManagementController> {

    private long testApplicationId = 1L;
    private long testOrganisationId = 2L;

    @Mock
    private TestTeamManagementService testTeamManagementService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private QuestionRestService questionRestService;

    private static final Long COMPETITION_ID = 36L;

    protected AbstractTeamManagementController supplyControllerUnderTest() {
       return new TestTeamManagementController();
    }

    @Test
    public void getUpdateOrganisation_shouldReturnSuccessViewAndViewModelWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());

        mockMvc.perform(get("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId))
                .andExpect(status().isOk())
                .andExpect(view().name("application-team/edit-org"))
                .andExpect(model().attribute("model", createAViewModel())).andReturn();
    }

    @Test
    @Ignore("This test is ignored due to the bug described in IFS-2598. The offending code is likely to be rewritten/removed as part of the person-to-org decoupling work IFS-3513, but if it is not then this bug should be fixed and test reinstated.")
    public void getUpdateOrganisation_shouldReturnNotFoundWhenOrganisationIsInvalid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(false);

        mockMvc.perform(get("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("404"));

        verify(testTeamManagementService, never()).createViewModel(anyLong(), anyLong(), any());
    }

    @Test
    public void addStagedInvite_shouldReturnSuccessViewWithStagedInviteWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());

        MvcResult result = mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
        .param("addStagedInvite", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-team/edit-org"))
                .andExpect(model().attribute("model", createAViewModel())).andReturn();

        ApplicationTeamUpdateForm model = (ApplicationTeamUpdateForm)result.getModelAndView().getModel().get("form");

        assertNotNull(model.getStagedInvite());
    }

    @Test
    public void addStagedInvite_shouldReturnNotFoundWhenOrganisationIsInvalid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(false);

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
        .param("addStagedInvite", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("404"));

        verify(testTeamManagementService, never()).createViewModel(anyLong(), anyLong(), any());
    }

    @Test
    public void removeStagedInvite_shouldReturnSuccessViewAndRemoveStagedInviteWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("removeStagedInvite", "true")
                .param("stagedInvite.email", "an email")
                .param("stagedInvite.name", "a name"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(format("redirect:/application/%s/team/update/invited/%s", testApplicationId, testOrganisationId)));
    }

    @Test
    public void removeStagedInvite_shouldReturnNotFoundWhenOrganisationIsInvalid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(false);

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("removeStagedInvite", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("404"));

        verify(testTeamManagementService, never()).createViewModel(anyLong(), anyLong(), any());
    }

    @Test
    public void inviteApplicant_shouldReturnSuccessViewWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.executeStagedInvite(anyLong(), anyLong(), any())).thenReturn(serviceSuccess(new InviteResultsResource()));

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("executeStagedInvite", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(format("redirect:/application/%s/team/update/invited/%s", testApplicationId, testOrganisationId)));
    }

    @Test
    public void inviteApplicant_shouldInviteApplicantWhenTheFormIsValidAndTheOrganisationIsValid() throws Exception {
        String validName = "valid name";
        String validEmail = "valid@email.com";

        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.executeStagedInvite(anyLong(), anyLong(), any())).thenReturn(serviceSuccess(new InviteResultsResource()));

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("executeStagedInvite", "true")
                .param("stagedInvite.name", validName)
                .param("stagedInvite.email", validEmail))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(format("redirect:/application/%s/team/update/invited/%s", testApplicationId, testOrganisationId)));


        ArgumentCaptor<ApplicationTeamUpdateForm> captor = ArgumentCaptor.forClass(ApplicationTeamUpdateForm.class);

        verify(testTeamManagementService, times(1)).executeStagedInvite(anyLong(), anyLong(), captor.capture());
        ApplicantInviteForm inviteResource = captor.getValue().getStagedInvite();

        assertEquals(validEmail, inviteResource.getEmail());
        assertEquals(validName, inviteResource.getName());
    }

    @Test
    public void inviteApplicant_shouldNotInviteApplicantWhenTheFormIsInvalidAndTheOrganisationIsValid() throws Exception {
        String invalidName = "";
        String invalidEmail = "invalidemail";

        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.executeStagedInvite(anyLong(), anyLong(), any())).thenReturn(serviceSuccess(new InviteResultsResource()));

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("executeStagedInvite", "true")
                .param("stagedInvite.name", invalidName)
                .param("stagedInvite.name", invalidEmail))
                .andExpect(status().isOk())
                .andExpect(view().name("application-team/edit-org"))
                .andExpect(model().attribute("model", createAViewModel())).andReturn();

        verify(testTeamManagementService, never()).executeStagedInvite(anyLong(), anyLong(), any());

    }

    @Test
    public void inviteApplicant_shouldReturnNotFoundWhenOrganisationIsInvalid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(false);

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("executeStagedInvite", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("404"));

        verify(testTeamManagementService, never()).createViewModel(anyLong(), anyLong(), any());
    }

    @Test
    public void removeApplicant_shouldReturnSuccessViewWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.removeInvite(3L)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("removeInvite", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-team/edit-org"))
                .andExpect(model().attribute("model", createAViewModel())).andReturn();
    }

    @Test
    public void removeApplicant_shouldReturnRedirectWhenOrganisationIsInvalidAfterRemoval() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true, false);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.removeInvite(3L)).thenReturn(serviceSuccess());

        setupApplicationResource();

        QuestionResource applicationTeamQuestion = new QuestionResource();
        when(questionRestService.getQuestionByCompetitionIdAndCompetitionSetupQuestionType(COMPETITION_ID, APPLICATION_TEAM))
                .thenReturn(restSuccess(applicationTeamQuestion));

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}",
                testApplicationId, testOrganisationId)
                .param("removeInvite", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(format("redirect:/application/%s/form/question/%s", testApplicationId,
                        applicationTeamQuestion.getId())));
    }

    @Test
    public void removeApplicant_shouldReturnRedirectWithOldApplicantMenuWhenOrganisationIsInvalidAfterRemoval() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true, false);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.removeInvite(3L)).thenReturn(serviceSuccess());

        setupApplicationResource();
        when(questionRestService.getQuestionByCompetitionIdAndCompetitionSetupQuestionType(COMPETITION_ID, APPLICATION_TEAM))
                .thenReturn(restFailure(notFoundError(QuestionResource.class, COMPETITION_ID, APPLICATION_TEAM)));

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("removeInvite", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(format("redirect:/application/%s/team", testApplicationId)));
    }

    @Test
    public void confirmDeleteInviteOrganisation_shouldReturnSuccessViewWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());

        mockMvc.perform(get("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("confirmDeleteOrganisation", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("application-team/edit-org"))
                .andExpect(model().attribute("model", createAViewModel()));
    }

    @Test
    @Ignore
    public void confirmDeleteInviteOrganisation_shouldReturnNotFoundWhenOrganisationIsInvalid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(false);

        mockMvc.perform(get("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("confirmDeleteOrganisation", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("404"));

        verify(testTeamManagementService, never()).createViewModel(anyLong(), anyLong(), any());
    }

    @Test
    public void deleteOrganisation_shouldReturnSuccessViewWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.getInviteIds(same(testApplicationId), same(testOrganisationId))).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(testTeamManagementService.removeInvite(anyLong())).thenReturn(serviceSuccess());

        setupApplicationResource();
        QuestionResource applicationTeamQuestion = new QuestionResource();
        when(questionRestService.getQuestionByCompetitionIdAndCompetitionSetupQuestionType(COMPETITION_ID, APPLICATION_TEAM))
                .thenReturn(restSuccess(applicationTeamQuestion));

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}",
                testApplicationId, testOrganisationId)
                .param("deleteOrganisation", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(format("redirect:/application/%s/form/question/%s", testApplicationId,
                        applicationTeamQuestion.getId())));
    }

    @Test
    public void deleteOrganisation_shouldReturnSuccessViewWithOldApplicantMenuWhenOrganisationIsValid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(true);
        when(testTeamManagementService.createViewModel(anyLong(), anyLong(), any())).thenReturn(createAViewModel());
        when(testTeamManagementService.getInviteIds(same(testApplicationId), same(testOrganisationId))).thenReturn(Arrays.asList(1L, 2L, 3L));
        when(testTeamManagementService.removeInvite(anyLong())).thenReturn(serviceSuccess());

        setupApplicationResource();
        when(questionRestService.getQuestionByCompetitionIdAndCompetitionSetupQuestionType(COMPETITION_ID, APPLICATION_TEAM))
                .thenReturn(restFailure(notFoundError(QuestionResource.class, COMPETITION_ID, APPLICATION_TEAM)));

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("deleteOrganisation", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(format("redirect:/application/%s/team", testApplicationId)));
    }

    @Test
    public void deleteOrganisation_shouldReturnNotFoundWhenOrganisationIsInvalid() throws Exception {
        when(testTeamManagementService.applicationAndOrganisationIdCombinationIsValid(same(testApplicationId), same(testOrganisationId))).thenReturn(false);

        mockMvc.perform(post("/application/{applicationId}/team/update/invited/{organisationId}", testApplicationId, testOrganisationId)
                .param("deleteOrganisation", "true"))
                .andExpect(status().is4xxClientError())
                .andExpect(view().name("404"));

        verify(testTeamManagementService, never()).createViewModel(anyLong(), anyLong(), any());
    }

    @Service
    public class TestTeamManagementService extends AbstractTeamManagementService {
        @Override
        public boolean applicationAndOrganisationIdCombinationIsValid(Long applicationId, Long organisationId) {
            return false;
        }

        @Override
        public ApplicationTeamManagementViewModel createViewModel(long applicationId, long organisationId, UserResource loggedInUser) {
            return null;
        }

        @Override
        public ServiceResult<InviteResultsResource> executeStagedInvite(long applicationId, long organisationId, ApplicationTeamUpdateForm form) {
            return null;
        }

        @Override
        public List<Long> getInviteIds(long applicationId, long organisationId) {
            return null;
        }
    }

    @RequestMapping("/application/{applicationId}/team/update/invited/{organisationId}")
    public class TestTeamManagementController extends AbstractTeamManagementController<TestTeamManagementService> {
        @Override
        protected String getMappingFormatString(long applicationId, long organisationid) {
            return format("/application/%s/team/update/invited/%s", applicationId, organisationid);
        }
    }

    private ApplicationResource setupApplicationResource() {
        ApplicationResource applicationResource = newApplicationResource()
                .withCompetition(COMPETITION_ID)
                .build();

        when(applicationService.getById(applicationResource.getId())).thenReturn(applicationResource);
        return applicationResource;
    }

    private static ApplicationTeamManagementViewModel createAViewModel() {
        return new ApplicationTeamManagementViewModel(1L,
                2L,
                "application name",
                3L,
                4L,
                "organisation name",
                true,
                true,
                emptyList(),
                true);
    }
}