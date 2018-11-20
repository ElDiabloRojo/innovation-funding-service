package org.innovateuk.ifs.project.projectdetails.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.AddressTypeResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.resource.ProjectInviteResource;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.service.OrganisationAddressRestService;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.builder.PartnerOrganisationResourceBuilder;
import org.innovateuk.ifs.project.constant.ProjectActivityStates;
import org.innovateuk.ifs.project.projectdetails.form.PartnerProjectLocationForm;
import org.innovateuk.ifs.project.projectdetails.form.ProjectDetailsAddressForm;
import org.innovateuk.ifs.project.projectdetails.form.ProjectDetailsStartDateForm;
import org.innovateuk.ifs.project.projectdetails.viewmodel.PartnerProjectLocationViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectDetailsAddressViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectDetailsStartDateViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectDetailsViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectUserInviteModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.SelectFinanceContactViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.SelectProjectManagerViewModel;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.service.PartnerOrganisationRestService;
import org.innovateuk.ifs.project.status.populator.SetupStatusViewModelPopulator;
import org.innovateuk.ifs.project.status.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.projectdetails.ProjectDetailsService;
import org.innovateuk.ifs.status.StatusService;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.innovateuk.ifs.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.address.builder.AddressTypeResourceBuilder.newAddressTypeResource;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.ADD_NEW;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.PROJECT;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.REGISTERED;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.name;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_PARTNER_PROJECT_LOCATION_CANNOT_BE_CHANGED_ONCE_MONITORING_OFFICER_HAS_BEEN_ASSIGNED;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.invite.builder.ProjectInviteResourceBuilder.newProjectInviteResource;
import static org.innovateuk.ifs.invite.constant.InviteStatus.CREATED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.organisation.builder.OrganisationAddressResourceBuilder.newOrganisationAddressResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.project.AddressLookupBaseController.FORM_ATTR_NAME;
import static org.innovateuk.ifs.project.builder.ProjectPartnerStatusResourceBuilder.newProjectPartnerStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectTeamStatusResourceBuilder.newProjectTeamStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectUserInviteStatus.PENDING;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.PARTNER;
import static org.innovateuk.ifs.user.resource.Role.PROJECT_MANAGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(MockitoJUnitRunner.class)
public class ProjectDetailsControllerTest extends BaseControllerMockMVCTest<ProjectDetailsController> {
    private static final String SAVE_FC = "save-fc";
    private static final String INVITE_FC = "invite-fc";
    private static final String SAVE_PM = "save-pm";
    private static final String INVITE_PM = "invite-pm";
    private static final String RESEND_FC_INVITE = "resend-fc-invite";
    private static final String RESEND_PM_INVITE = "resend-pm-invite";

    @Mock
    private SetupStatusViewModelPopulator setupStatusViewModelPopulatorMock;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private ProjectService projectService;

    @Mock
    private PartnerOrganisationRestService partnerOrganisationRestService;

    @Mock
    private StatusService statusService;

    @Mock
    private OrganisationRestService organisationRestService;

    @Mock
    private ProjectDetailsService projectDetailsService;

    @Mock
    private UserService userService;

    @Mock
    private UserRestService userRestService;

    @Mock
    private OrganisationAddressRestService organisationAddressRestService;

    @Override
    protected ProjectDetailsController supplyControllerUnderTest() {
        return new ProjectDetailsController();
    }

    @Test
    public void testProjectDetails() throws Exception {
        Long projectId = 20L;

        boolean partnerProjectLocationRequired = true;
        CompetitionResource competitionResource = newCompetitionResource()
                .withLocationPerPartner(partnerProjectLocationRequired)
                .build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withIsLeadPartner(true)
                        .withMonitoringOfficerStatus(ProjectActivityStates.NOT_STARTED)
                        .withSpendProfileStatus(ProjectActivityStates.PENDING)
                        .withGrantOfferStatus(ProjectActivityStates.NOT_REQUIRED).build()).
                build();

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        List<PartnerOrganisationResource> partnerOrganisationResourceList = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource().build(3);
        when(partnerOrganisationRestService.getProjectPartnerOrganisations(projectId)).thenReturn(restSuccess(partnerOrganisationResourceList));

        when(statusService.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(teamStatus);
        when(setupStatusViewModelPopulatorMock.checkLeadPartnerProjectDetailsProcessCompleted(teamStatus, partnerProjectLocationRequired)).thenReturn(true);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        MvcResult result = mockMvc.perform(get("/project/{id}/details", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        ProjectDetailsViewModel model = (ProjectDetailsViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(applicationResource, model.getApp());
        assertEquals(competitionResource, model.getCompetition());
        assertEquals(project, model.getProject());
        assertEquals(singletonList(leadOrganisation), model.getOrganisations());
        assertEquals(null, model.getProjectManager());
        assertTrue(model.isAllProjectDetailsFinanceContactsAndProjectLocationsAssigned());
        assertTrue(model.isUserLeadPartner());
        assertFalse(model.isMonitoringOfficerAssigned());
        assertTrue(model.isSpendProfileGenerated());
        assertFalse(model.isReadOnly());
        assertFalse(model.isGrantOfferLetterGenerated());
    }

    @Test
    public void testProjectDetailsReadOnlyView() throws Exception {
        Long projectId = 20L;

        boolean partnerProjectLocationRequired = true;
        CompetitionResource competitionResource = newCompetitionResource()
                .withLocationPerPartner(partnerProjectLocationRequired)
                .build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withIsLeadPartner(true)
                        .withMonitoringOfficerStatus(ProjectActivityStates.NOT_STARTED)
                        .build()).
                build();

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        List<PartnerOrganisationResource> partnerOrganisationResourceList = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource().build(3);
        when(partnerOrganisationRestService.getProjectPartnerOrganisations(projectId)).thenReturn(restSuccess(partnerOrganisationResourceList));

        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        when(statusService.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(teamStatus);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        MvcResult result = mockMvc.perform(get("/project/{id}/readonly", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andReturn();

        ProjectDetailsViewModel model = (ProjectDetailsViewModel) result.getModelAndView().getModel().get("model");
        Boolean readOnlyView = (Boolean) result.getModelAndView().getModel().get("readOnlyView");

        assertEquals(applicationResource, model.getApp());
        assertEquals(competitionResource, model.getCompetition());
        assertEquals(project, model.getProject());
        assertEquals(singletonList(leadOrganisation), model.getOrganisations());
        assertEquals(null, model.getProjectManager());
        assertTrue(model.isUserLeadPartner());
        assertFalse(model.isSpendProfileGenerated());
        assertFalse(model.isMonitoringOfficerAssigned());
        assertTrue(model.isReadOnly());
        assertTrue(model.isGrantOfferLetterGenerated());
    }

    @Test
    public void testProjectDetailsProjectManager() throws Exception {
        Long projectId = 20L;

        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        List<ProjectInviteResource> invitedUsers = newProjectInviteResource().build(2);

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(projectId)).thenReturn(project);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(leadOrganisation);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(invitedUsers));
        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        List<ProjectUserInviteModel> users = new ArrayList<>();

        List<ProjectUserInviteModel> invites = invitedUsers.stream()
                .filter(invite -> leadOrganisation.getId().equals(invite.getOrganisation()))
                .map(invite -> new ProjectUserInviteModel(PENDING, invite.getName() + " (Pending)", projectId))
                .collect(toList());

        SelectProjectManagerViewModel viewModel = new SelectProjectManagerViewModel(users, invites, project, 1L, applicationResource, competitionResource, false);

        mockMvc.perform(get("/project/{id}/details/project-manager", projectId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name("project/project-manager"));
    }

    @Test
    public void testProjectDetailsSetProjectManager() throws Exception {
        Long projectId = 20L;
        Long projectManagerUserId = 80L;

        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), projectManagerUserId).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(2);

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectDetailsService.updateProjectManager(projectId, projectManagerUserId)).thenReturn(serviceSuccess());

        ProcessRoleResource processRoleResource = new ProcessRoleResource();
        processRoleResource.setUser(projectManagerUserId);
        when(userService.getLeadPartnerOrganisationProcessRoles(applicationResource)).thenReturn(singletonList(processRoleResource));

        when(projectDetailsService.updateProjectManager(projectId, projectManagerUserId)).thenReturn(serviceSuccess());


        mockMvc.perform(post("/project/{id}/details/project-manager", projectId)
                .param(SAVE_PM, INVITE_FC)
                .param("projectManager", projectManagerUserId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/project/" + projectId + "/details"));

    }

    @Test
    public void testProjectManagerAndAddressCannotBeChangedWhenGOLAlreadyGenerated() throws Exception {
        Long projectId = 20L;

        boolean partnerProjectLocationRequired = true;
        CompetitionResource competitionResource = newCompetitionResource()
                .withLocationPerPartner(partnerProjectLocationRequired)
                .build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().withIsLeadPartner(true).withSpendProfileStatus(ProjectActivityStates.COMPLETE).build()).
                build();

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        List<PartnerOrganisationResource> partnerOrganisationResourceList = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource().build(3);
        when(partnerOrganisationRestService.getProjectPartnerOrganisations(projectId)).thenReturn(restSuccess(partnerOrganisationResourceList));
        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        when(statusService.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(teamStatus);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        MvcResult result = mockMvc.perform(get("/project/{id}/details", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        ProjectDetailsViewModel model = (ProjectDetailsViewModel) result.getModelAndView().getModel().get("model");
        assertTrue(model.isGrantOfferLetterGenerated());
    }

    @Test
    public void testViewStartDate() throws Exception {
        ApplicationResource applicationResource = newApplicationResource().build();

        ProjectResource project = newProjectResource().
                withApplication(applicationResource).
                with(name("My Project")).
                withDuration(4L).
                withTargetStartDate(LocalDate.now().withDayOfMonth(5)).
                withDuration(4L).
                build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();
        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PARTNER).
                build(1);

        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);

        MvcResult result = mockMvc.perform(get("/project/{id}/details/start-date", project.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("project/details-start-date"))
                .andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();
        ProjectDetailsStartDateViewModel viewModel = (ProjectDetailsStartDateViewModel) model.get("model");

        assertEquals(project.getId(), viewModel.getProjectId());
        assertEquals(project.getApplication(), (long) viewModel.getApplicationId());
        assertEquals(project.getName(), viewModel.getProjectName());
        assertEquals(project.getDurationInMonths(), Long.valueOf(viewModel.getProjectDurationInMonths()));

        ProjectDetailsStartDateForm form = (ProjectDetailsStartDateForm) model.get(FORM_ATTR_NAME);
        assertEquals(project.getTargetStartDate().withDayOfMonth(1), form.getProjectStartDate());
    }

    @Test
    public void testUpdateStartDate() throws Exception {
        ApplicationResource applicationResource = newApplicationResource().build();
        ProjectResource projectResource = newProjectResource().withApplication(applicationResource).build();

        when(projectService.getById(projectResource.getId())).thenReturn(projectResource);
        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectDetailsService.updateProjectStartDate(projectResource.getId(), LocalDate.of(2017, 6, 3))).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{id}/details/start-date", projectResource.getId()).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("projectStartDate", "projectStartDate").
                param("projectStartDate.dayOfMonth", "3").
                param("projectStartDate.monthValue", "6").
                param("projectStartDate.year", "2017"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/project/" + projectResource.getId() + "/details"))
                .andReturn();

    }

    @Test
    public void testUpdateFinanceContact() throws Exception {

        long competitionId = 1L;
        long applicationId = 1L;
        long projectId = 1L;
        long organisationId = 1L;
        long loggedInUserId = 1L;
        long invitedUserId = 2L;

        UserResource financeContactUserResource = newUserResource().withId(invitedUserId).withFirstName("First").withLastName("Last").withEmail("test@test.com").build();


        String invitedUserName = "First Last";
        String invitedUserEmail = "test@test.com";

        CompetitionResource competitionResource = newCompetitionResource().withId(competitionId).build();
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetition(competitionId).build();
        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();

        ProjectInviteResource projectInviteResource = new ProjectInviteResource(invitedUserName, invitedUserEmail, projectId);
        projectInviteResource.setUser(invitedUserId);
        projectInviteResource.setOrganisation(organisationId);
        projectInviteResource.setApplicationId(applicationId);
        projectInviteResource.setLeadOrganisationId(leadOrganisation.getId());

        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.updateFinanceContact(new ProjectOrganisationCompositeId(projectId, organisationId), invitedUserId)).thenReturn(serviceSuccess());
        when(userRestService.retrieveUserById(invitedUserId)).thenReturn(restSuccess(financeContactUserResource));
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(leadOrganisation);
        when(applicationService.getById(applicationId)).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(userService.getOrganisationProcessRoles(applicationResource, organisationId)).thenReturn(emptyList());
        when(projectDetailsService.saveProjectInvite(projectInviteResource)).thenReturn(serviceSuccess());
        when(projectDetailsService.inviteFinanceContact(projectId, projectInviteResource)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(SAVE_FC, INVITE_FC).
                param("organisation", "1").
                param("financeContact", "2")).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + projectId + "/details")).
                andReturn();

        verify(projectDetailsService).updateFinanceContact(new ProjectOrganisationCompositeId(projectId, organisationId), invitedUserId);
    }

    @Test
    public void testViewPartnerProjectLocation() throws Exception {

        long projectId = 1L;
        long organisationId = 2L;

        ProjectResource projectResource = newProjectResource()
                .withId(projectId)
                .withName("Project 1")
                .build();

        PartnerOrganisationResource partnerOrganisation = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource()
                .withPostcode("TW14 9QG")
                .build();
        when(partnerOrganisationRestService.getPartnerOrganisation(projectId, organisationId)).thenReturn(restSuccess(partnerOrganisation));
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);
        when(projectService.getById(projectId)).thenReturn(projectResource);

        MvcResult result = mockMvc.perform(get("/project/{projectId}/organisation/{organisationId}/partner-project-location", projectId, organisationId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/partner-project-location"))
                .andReturn();

        PartnerProjectLocationViewModel model = (PartnerProjectLocationViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(projectId, model.getProjectId());
        assertEquals("Project 1", model.getProjectName());
        assertEquals(organisationId, model.getOrganisationId());

        PartnerProjectLocationForm form = (PartnerProjectLocationForm) result.getModelAndView().getModel().get(FORM_ATTR_NAME);
        assertEquals("TW14 9QG", form.getPostcode());

    }

    @Test
    public void testUpdatePartnerProjectLocationWhenUpdateFails() throws Exception {

        long projectId = 1L;
        long organisationId = 2L;
        String postcode = "UB7 8QF";

        ProjectResource projectResource = newProjectResource()
                .withId(projectId)
                .withName("Project 1")
                .build();

        when(projectDetailsService.updatePartnerProjectLocation(projectId, organisationId, postcode))
                .thenReturn(serviceFailure(PROJECT_SETUP_PARTNER_PROJECT_LOCATION_CANNOT_BE_CHANGED_ONCE_MONITORING_OFFICER_HAS_BEEN_ASSIGNED));
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);
        when(projectService.getById(projectId)).thenReturn(projectResource);

        MvcResult result = mockMvc.perform(post("/project/{projectId}/organisation/{organisationId}/partner-project-location", projectId, organisationId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("postcode", postcode)).
                andExpect(status().isOk()).
                andExpect(view().name("project/partner-project-location")).
                andReturn();

        PartnerProjectLocationForm form = (PartnerProjectLocationForm) result.getModelAndView().getModel().get(FORM_ATTR_NAME);
        assertEquals(new PartnerProjectLocationForm(postcode), form);
    }

    @Test
    public void testUpdatePartnerProjectLocationSuccess() throws Exception {

        long projectId = 1L;
        long organisationId = 2L;
        String postcode = "UB7 8QF";

        when(projectDetailsService.updatePartnerProjectLocation(projectId, organisationId, postcode))
                .thenReturn(serviceSuccess());

        MvcResult result = mockMvc.perform(post("/project/{projectId}/organisation/{organisationId}/partner-project-location", projectId, organisationId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("postcode", postcode)).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + projectId + "/details")).
                andReturn();

        PartnerProjectLocationForm form = (PartnerProjectLocationForm) result.getModelAndView().getModel().get(FORM_ATTR_NAME);
        assertEquals(new PartnerProjectLocationForm(postcode), form);

        verify(projectService, never()).userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId());
        verify(projectService, never()).getById(projectId);
    }

    @Test
    public void testInviteSelfToProjectManager() throws Exception {

        long loggedInUserId = 1L;
        long projectId = 4L;
        long organisationId = 4L;
        long applicationId = 16L;

        UserResource loggedInUser = newUserResource().withId(loggedInUserId).withFirstName("Steve").withLastName("Smith").withEmail("Steve.Smith@empire.com").build();
        setLoggedInUser(loggedInUser);

        String invitedUserName = "Steve Smith";
        String invitedUserEmail = "Steve.Smith@empire.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource organisationResource = newOrganisationResource().withId(organisationId).build();

        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.isUserLeadPartner(projectResource.getId(), loggedInUser.getId())).thenReturn(false);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(organisationResource);

        mockMvc.perform(post("/project/{id}/details/project-manager", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(INVITE_PM, INVITE_PM).
                param("name", invitedUserName).
                param("inviteEmail", invitedUserEmail)
        ).
                andExpect(status().is3xxRedirection()).
                andExpect(view().name("redirect:/project/" + projectId + "/details")).
                andReturn();

        verify(projectDetailsService, never()).saveProjectInvite(any(ProjectInviteResource.class));
        verify(projectDetailsService, never()).inviteProjectManager(Mockito.anyLong(), Mockito.any(ProjectInviteResource.class));
    }

    @Test
    public void testInviteSelfToFinanceContact() throws Exception {

        long loggedInUserId = 1L;
        long projectId = 4L;
        long organisationId = 21L;
        long applicationId = 16L;

        UserResource loggedInUser = newUserResource().withId(loggedInUserId).withFirstName("Steve").withLastName("Smith").withEmail("Steve.Smith@empire.com").build();
        setLoggedInUser(loggedInUser);

        String invitedUserName = "Steve Smith";
        String invitedUserEmail = "Steve.Smith@empire.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetition(competitionResource.getId()).build();

        List<ProjectInviteResource> existingInvites = newProjectInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(INVITE_FC, INVITE_FC).
                param("name", invitedUserName).
                param("inviteEmail", invitedUserEmail).
                param("organisation", organisationId + "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/finance-contact")).
                andReturn();

        verify(projectDetailsService, never()).saveProjectInvite(any(ProjectInviteResource.class));
        verify(projectDetailsService, never()).inviteFinanceContact(Mockito.anyLong(), Mockito.any(ProjectInviteResource.class));
    }

    @Test
    public void testInviteFinanceContactFails() throws Exception {
        long competitionId = 1L;
        long applicationId = 16L;
        long projectId = 4L;
        long organisationId = 21L;
        long loggedInUserId = 1L;
        long invitedUserId = 2L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        UserResource financeContactUserResource = newUserResource().withId(invitedUserId).withFirstName("First").withLastName("Last").withEmail("test@test.com").build();
        CompetitionResource competitionResource = newCompetitionResource().withId(competitionId).build();
        ApplicationResource applicationResource = newApplicationResource().withId(applicationId).withCompetition(competitionId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        ProjectInviteResource createdInvite = newProjectInviteResource().withId()
                .withProject(projectId).withName(invitedUserName)
                .withEmail(invitedUserEmail)
                .withOrganisation(organisationId)
                .withLeadOrganisation(leadOrganisation.getId()).build();

        createdInvite.setOrganisation(organisationId);
        createdInvite.setApplicationId(projectResource.getApplication());
        createdInvite.setApplicationId(applicationId);

        List<ProjectInviteResource> existingInvites = newProjectInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(userService.findUserByEmail(invitedUserEmail)).thenReturn(Optional.empty());
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(leadOrganisation);
        when(projectDetailsService.saveProjectInvite(createdInvite)).thenReturn(serviceSuccess());
        when(projectDetailsService.inviteFinanceContact(projectId, createdInvite)).thenReturn(serviceSuccess());
        when(projectDetailsService.updateFinanceContact(new ProjectOrganisationCompositeId(projectId, organisationId), invitedUserId)).thenReturn(serviceSuccess());
        when(userRestService.retrieveUserById(invitedUserId)).thenReturn(restSuccess(financeContactUserResource));
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(projectDetailsService.inviteFinanceContact(projectId, existingInvites.get(1))).thenReturn(serviceSuccess());
        when(organisationRestService.getOrganisationById(organisationId)).thenReturn(restSuccess(leadOrganisation));
        when(projectDetailsService.saveProjectInvite(any())).thenReturn(serviceSuccess());
        when(competitionRestService.getCompetitionById(competitionId)).thenReturn(restSuccess(competitionResource));
        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);

        InviteStatus testStatus = CREATED;

        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param(INVITE_FC, INVITE_FC).
                param("userId", invitedUserId + "").
                param("name", invitedUserName).
                param("email", invitedUserEmail).
                param("financeContact", "-1").
                param("inviteStatus", testStatus.toString()).
                param("organisation", organisationId + "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/finance-contact")).
                andReturn();
    }

    @Test
    public void testAddressTypeValidation() throws Exception {
        ApplicationResource applicationResource = newApplicationResource().build();
        ProjectResource project = newProjectResource().withApplication(applicationResource).build();
        OrganisationResource organisationResource = newOrganisationResource().build();

        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(organisationResource);
        when(organisationRestService.getOrganisationById(organisationResource.getId())).thenReturn(restSuccess(organisationResource));

        mockMvc.perform(post("/project/{id}/details/project-address", project.getId()).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("addressType", "")).
                andExpect(status().isOk()).
                andExpect(view().name("project/details-address")).
                andExpect(model().hasErrors()).
                andExpect(model().attributeHasFieldErrors("form", "addressType")).
                andReturn();
    }

    @Test
    public void testViewAddress() throws Exception {
        OrganisationResource organisationResource = newOrganisationResource().build();
        AddressResource addressResource = newAddressResource().build();
        AddressTypeResource addressTypeResource = newAddressTypeResource().withId((long) REGISTERED.getOrdinal()).withName(REGISTERED.name()).build();
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().withAddressType(addressTypeResource).withAddress(addressResource).build();
        organisationResource.setAddresses(Collections.singletonList(organisationAddressResource));
        ApplicationResource applicationResource = newApplicationResource().build();
        ProjectResource project = newProjectResource().withApplication(applicationResource).withAddress(addressResource).build();

        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(organisationResource);
        when(organisationRestService.getOrganisationById(organisationResource.getId())).thenReturn(restSuccess(organisationResource));
        when(organisationAddressRestService.findByOrganisationIdAndAddressId(organisationResource.getId(), project.getAddress().getId())).thenReturn(restSuccess(organisationAddressResource));

        MvcResult result = mockMvc.perform(get("/project/{id}/details/project-address", project.getId())).
                andExpect(status().isOk()).
                andExpect(view().name("project/details-address")).
                andExpect(model().hasNoErrors()).
                andReturn();

        Map<String, Object> model = result.getModelAndView().getModel();

        ProjectDetailsAddressViewModel viewModel = (ProjectDetailsAddressViewModel) model.get("model");
        assertEquals(project.getId(), viewModel.getProjectId());
        assertEquals(project.getName(), viewModel.getProjectName());
        assertEquals(project.getApplication(), (long) viewModel.getApplicationId());
        assertNull(viewModel.getOperatingAddress());
        assertEquals(addressResource, viewModel.getRegisteredAddress());
        assertNull(viewModel.getProjectAddress());

        ProjectDetailsAddressForm form = (ProjectDetailsAddressForm) model.get(FORM_ATTR_NAME);
        assertEquals(OrganisationAddressType.valueOf(organisationAddressResource.getAddressType().getName()), form.getAddressType());
    }

    @Test
    public void testUpdateProjectAddressToBeSameAsRegistered() throws Exception {
        OrganisationResource leadOrganisation = newOrganisationResource().build();
        AddressResource addressResource = newAddressResource().build();
        AddressTypeResource addressTypeResource = newAddressTypeResource().withId((long) REGISTERED.getOrdinal()).withName(REGISTERED.name()).build();
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().withAddressType(addressTypeResource).withAddress(addressResource).build();
        leadOrganisation.setAddresses(Collections.singletonList(organisationAddressResource));
        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withApplication(applicationResource).withAddress(addressResource).build();

        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(projectDetailsService.updateAddress(leadOrganisation.getId(), project.getId(), REGISTERED, addressResource)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{id}/details/project-address", project.getId()).
                contentType(MediaType.APPLICATION_FORM_URLENCODED).
                param("addressType", REGISTERED.name())).
                andExpect(status().is3xxRedirection()).
                andExpect(model().attributeDoesNotExist("readOnlyView")).
                andExpect(redirectedUrl("/project/" + project.getId() + "/details")).
                andReturn();
    }

    @Test
    public void testUpdateProjectAddressAddNewManually() throws Exception {
        OrganisationResource leadOrganisation = newOrganisationResource().build();

        AddressResource addressResource = newAddressResource().
                withId().
                withAddressLine1("Address Line 1").
                withAddressLine2().
                withAddressLine3().
                withTown("Sheffield").
                withCounty().
                withPostcode("S1 2LB").
                build();

        AddressTypeResource addressTypeResource = newAddressTypeResource().withId((long) REGISTERED.getOrdinal()).withName(REGISTERED.name()).build();
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().withAddressType(addressTypeResource).withAddress(addressResource).build();
        leadOrganisation.setAddresses(Collections.singletonList(organisationAddressResource));
        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withApplication(applicationResource).withAddress(addressResource).build();

        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(projectDetailsService.updateAddress(leadOrganisation.getId(), project.getId(), PROJECT, addressResource)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{id}/details/project-address", project.getId()).
                contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("addressType", ADD_NEW.name())
                .param("manualEntry", "true")
                .param("addressForm.postcodeInput", "S101LB")
                .param("addressForm.selectedPostcode.addressLine1", addressResource.getAddressLine1())
                .param("addressForm.selectedPostcode.town", addressResource.getTown())
                .param("addressForm.selectedPostcode.postcode", addressResource.getPostcode()))
                .andExpect(redirectedUrl("/project/" + project.getId() + "/details"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();
    }

    @Test
    public void testSearchAddressFailsWithFieldErrorOnEmpty() throws Exception {
        OrganisationResource leadOrganisation = newOrganisationResource().build();
        AddressResource addressResource = newAddressResource().withPostcode("S1 2LB").withAddressLine1("Address Line 1").withTown("Sheffield").build();
        addressResource.setId(null);
        AddressTypeResource addressTypeResource = newAddressTypeResource().withId((long) REGISTERED.getOrdinal()).withName(REGISTERED.name()).build();
        OrganisationAddressResource organisationAddressResource = newOrganisationAddressResource().withAddressType(addressTypeResource).withAddress(addressResource).build();
        leadOrganisation.setAddresses(Collections.singletonList(organisationAddressResource));
        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withApplication(applicationResource).withAddress(addressResource).build();

        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);

        mockMvc.perform(post("/project/{id}/details/project-address", project.getId()).
                contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("addressType", ADD_NEW.name())
                .param("search-address", "")
                .param("addressForm.postcodeInput", "")).
                andExpect(model().hasErrors()).
                andExpect(model().attributeDoesNotExist("readOnlyView")).
                andExpect(model().attributeHasFieldErrors("form", "addressForm.postcodeInput")).
                andReturn();
    }

    @Test
    public void testFinanceContactInviteNotYetAccepted() throws Exception {

        long applicationId = 16L;
        long projectId = 4L;
        long organisationId = 21L;
        long loggedInUserId = 1L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).withId(applicationId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        List<ProjectInviteResource> existingInvites = newProjectInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(CREATED)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);

        MvcResult result = mockMvc.perform(get("/project/{id}/details/finance-contact", projectId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("organisation", String.valueOf(organisationId)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/finance-contact"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        SelectFinanceContactViewModel model = (SelectFinanceContactViewModel) result.getModelAndView().getModel().get("model");

        assertEquals("PENDING", model.getInvitedUsers().get(0).getStatus());
    }

    @Test
    public void testFinanceContactInviteAcceptedByInviteeSoNoLongerInInvitesList() throws Exception {

        long applicationId = 16L;
        long projectId = 4L;
        long organisationId = 21L;
        long loggedInUserId = 1L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        ProjectResource projectResource = newProjectResource().withId(projectId).withApplication(applicationId).build();
        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).withId(applicationId).build();

        List<ProjectUserResource> availableUsers = newProjectUserResource().
                withUser(loggedInUser.getId(), loggedInUserId).
                withOrganisation(organisationId).
                withRole(PARTNER).
                build(2);

        List<ProjectInviteResource> existingInvites = newProjectInviteResource().withId(2L)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(OPENED)
                .withLeadOrganisation(leadOrganisation.getId()).build(2);

        when(applicationService.getById(projectResource.getApplication())).thenReturn(applicationResource);
        when(projectService.getById(projectId)).thenReturn(projectResource);
        when(projectService.getProjectUsersForProject(projectId)).thenReturn(availableUsers);
        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        when(projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())).thenReturn(true);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));

        MvcResult result = mockMvc.perform(get("/project/{id}/details/finance-contact", projectId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("organisation", String.valueOf(organisationId)))
                .andExpect(status().isOk())
                .andExpect(view().name("project/finance-contact"))
                .andExpect(model().attributeDoesNotExist("readOnlyView"))
                .andReturn();

        SelectFinanceContactViewModel model = (SelectFinanceContactViewModel) result.getModelAndView().getModel().get("model");

        assertTrue(model.getInvitedUsers().isEmpty());
    }

    @Test
    public void testFinanceContactResend() throws Exception {
        long projectId = 4L;
        long organisationId = 21L;
        long inviteId = 3L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();

        List<ProjectInviteResource> existingInvites = newProjectInviteResource().withId(inviteId)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(OPENED)
                .withLeadOrganisation(leadOrganisation.getId()).build(1);


        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        mockMvc.perform(post("/project/{id}/details/finance-contact", projectId)
                .param(RESEND_FC_INVITE, "3")
                .param("organisation", "21"))
                .andExpect(status().is3xxRedirection());
        verify(projectDetailsService).getInvitesByProject(projectId);
    }

    @Test
    public void testProjectManagerResend() throws Exception {
        long projectId = 4L;
        long organisationId = 21L;
        long inviteId = 12L;

        String invitedUserName = "test";
        String invitedUserEmail = "test@test.com";

        OrganisationResource leadOrganisation = newOrganisationResource().withName("Lead Organisation").build();

        List<ProjectInviteResource> existingInvites = newProjectInviteResource().withId(inviteId)
                .withProject(projectId).withName("exist test", invitedUserName)
                .withEmail("existing@test.com", invitedUserEmail)
                .withOrganisation(organisationId)
                .withStatus(OPENED)
                .withLeadOrganisation(leadOrganisation.getId()).build(1);


        when(projectDetailsService.getInvitesByProject(projectId)).thenReturn(serviceSuccess(existingInvites));
        mockMvc.perform(post("/project/{id}/details/project-manager", projectId)
                .param(RESEND_PM_INVITE, "12"))
                .andExpect(status().is3xxRedirection());
        verify(projectDetailsService).getInvitesByProject(projectId);
    }

    @Test
    public void testViewProjectDetailsInReadOnly() throws Exception {
        Long projectId = 15L;

        boolean partnerProjectLocationRequired = true;
        CompetitionResource competitionResource = newCompetitionResource()
                .withLocationPerPartner(partnerProjectLocationRequired)
                .build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competitionResource.getId()).build();
        ProjectResource project = newProjectResource().withId(projectId).build();

        OrganisationResource leadOrganisation = newOrganisationResource().build();

        List<ProjectUserResource> projectManagerProjectUsers = newProjectUserResource().
                withUser(loggedInUser.getId()).
                withOrganisation(leadOrganisation.getId()).
                withRole(PROJECT_MANAGER).
                build(1);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withIsLeadPartner(true)
                        .withMonitoringOfficerStatus(ProjectActivityStates.NOT_STARTED)
                        .build()).
                build();

        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(competitionRestService.getCompetitionById(competitionResource.getId())).thenReturn(restSuccess(competitionResource));
        when(projectService.getById(project.getId())).thenReturn(project);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectManagerProjectUsers);
        when(projectService.getLeadOrganisation(project.getId())).thenReturn(leadOrganisation);
        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));
        List<PartnerOrganisationResource> partnerOrganisationResourceList = PartnerOrganisationResourceBuilder.newPartnerOrganisationResource().build(3);
        when(partnerOrganisationRestService.getProjectPartnerOrganisations(projectId)).thenReturn(restSuccess(partnerOrganisationResourceList));
        when(projectService.isUserLeadPartner(projectId, loggedInUser.getId())).thenReturn(true);
        when(statusService.getProjectTeamStatus(projectId, Optional.empty())).thenReturn(teamStatus);

        when(organisationRestService.getOrganisationById(leadOrganisation.getId())).thenReturn(restSuccess(leadOrganisation));

        MvcResult result = mockMvc.perform(get("/project/{id}/readonly", projectId))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andReturn();

        ProjectDetailsViewModel model = (ProjectDetailsViewModel) result.getModelAndView().getModel().get("model");
        assertEquals(applicationResource, model.getApp());
        assertEquals(competitionResource, model.getCompetition());
        assertEquals(project, model.getProject());
        assertEquals(projectManagerProjectUsers.get(0), model.getProjectManager());
        assertFalse(model.isSpendProfileGenerated());
        assertFalse(model.isMonitoringOfficerAssigned());
        assertTrue(model.isReadOnly());
        assertTrue(model.isGrantOfferLetterGenerated());
    }
}

