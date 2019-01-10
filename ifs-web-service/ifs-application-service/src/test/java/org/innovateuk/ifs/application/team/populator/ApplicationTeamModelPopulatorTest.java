package org.innovateuk.ifs.application.team.populator;

import org.innovateuk.ifs.BaseUnitTest;
import org.innovateuk.ifs.applicant.resource.ApplicantQuestionResource;
import org.innovateuk.ifs.applicant.resource.ApplicantResource;
import org.innovateuk.ifs.applicant.service.ApplicantRestService;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.team.viewmodel.ApplicationTeamApplicantRowViewModel;
import org.innovateuk.ifs.application.team.viewmodel.ApplicationTeamOrganisationRowViewModel;
import org.innovateuk.ifs.application.team.viewmodel.ApplicationTeamViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.innovateuk.ifs.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.applicant.builder.ApplicantQuestionResourceBuilder.newApplicantQuestionResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantQuestionStatusResourceBuilder.newApplicantQuestionStatusResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantResourceBuilder.newApplicantResource;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.QuestionStatusResourceBuilder.newQuestionStatusResource;
import static org.innovateuk.ifs.application.resource.ApplicationState.CREATED;
import static org.innovateuk.ifs.application.resource.ApplicationState.OPEN;
import static org.innovateuk.ifs.commons.BaseIntegrationTest.setLoggedInUser;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CollaborationLevel.SINGLE_OR_COLLABORATIVE;
import static org.innovateuk.ifs.form.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.invite.builder.ApplicationInviteResourceBuilder.newApplicationInviteResource;
import static org.innovateuk.ifs.invite.builder.InviteOrganisationResourceBuilder.newInviteOrganisationResource;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.SENT;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.APPLICATION_TEAM;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleToMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ApplicationTeamModelPopulatorTest extends BaseUnitTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private InviteRestService inviteRestService;

    @Mock
    private UserService userService;
    
    @Mock
    private UserRestService userRestService;

    @Mock
    private ApplicantRestService applicantRestService;

    @Mock
    private QuestionRestService questionRestService;

    @InjectMocks
    private ApplicationTeamModelPopulator applicationTeamModelPopulator = new ApplicationTeamModelPopulator();

    @Test
    public void populateModel_loggedInUserIsLead() {
        Map<String, OrganisationResource> organisationsMap = setupOrganisationResources();
        ApplicationResource applicationResource = setupApplicationResource(organisationsMap);
        Map<String, UserResource> usersMap = setupUserResources();
        Map<String, InviteOrganisationResource> inviteOrganisationsMap = setupOrganisationInvitesWithInviteForLeadOrg(
                applicationResource.getId(), usersMap, organisationsMap);
        UserResource leadApplicant = setupLeadApplicant(applicationResource, usersMap);
        ApplicantQuestionResource applicantQuestion = setupApplicantQuestion(applicationResource.getId(), leadApplicant.getId());

        Long orgIdEmpire = organisationsMap.get("Empire Ltd").getId();
        Long orgIdLudlow = organisationsMap.get("Ludlow").getId();
        Long orgIdEggs = organisationsMap.get("EGGS").getId();
        Long inviteOrgIdEmpire = inviteOrganisationsMap.get("Empire Ltd").getId();
        Long inviteOrgIdLudlow = inviteOrganisationsMap.get("Ludlow").getId();
        Long inviteOrgIdEggs = inviteOrganisationsMap.get("EGGS").getId();
        Long questionId = applicantQuestion.getQuestion().getId();

        List<ApplicationTeamOrganisationRowViewModel> expectedOrganisations = asList(
                new ApplicationTeamOrganisationRowViewModel(orgIdEmpire, inviteOrgIdEmpire, "Empire Ltd", "Business", true, asList(
                        new ApplicationTeamApplicantRowViewModel("Steve Smith", "steve.smith@empire.com", true, false),
                        new ApplicationTeamApplicantRowViewModel("Paul Davidson", "paul.davidson@empire.com", false, false)
                ), true),
                new ApplicationTeamOrganisationRowViewModel(orgIdEggs, inviteOrgIdEggs, "EGGS", "Business", false, singletonList(
                        new ApplicationTeamApplicantRowViewModel("Paul Tom", "paul.tom@egg.com", false, false)
                ), true),
                new ApplicationTeamOrganisationRowViewModel(orgIdLudlow, inviteOrgIdLudlow, "Ludlow", "Academic", false, asList(
                        new ApplicationTeamApplicantRowViewModel("Jessica Doe", "jessica.doe@ludlow.com", false, false),
                        new ApplicationTeamApplicantRowViewModel("Ryan Dell", "ryan.dell@ludlow.com", false, true)
                ), true)
        );


        ApplicationTeamViewModel expectedViewModel = new ApplicationTeamViewModel(
                applicationResource.getId(),
                1L,
                "Application name",
                expectedOrganisations,
                true,
                false,
                false,
                false,
                true,
                false,
                SINGLE_OR_COLLABORATIVE
        );

        when(applicantRestService.getQuestion(leadApplicant.getId(), applicationResource.getId(), questionId)).thenReturn(applicantQuestion);

        ApplicationTeamViewModel applicationTeamViewModel = applicationTeamModelPopulator.populateModel
                (applicationResource.getId(), leadApplicant.getId(), questionId);

        assertEquals(expectedViewModel, applicationTeamViewModel);

        InOrder inOrder = inOrder(applicationService, userService, userRestService, applicantRestService, inviteRestService);
        inOrder.verify(applicationService).getById(applicationResource.getId());
        inOrder.verify(userService).getLeadApplicantProcessRoleOrNull(applicationResource.getId());
        inOrder.verify(userRestService).retrieveUserById(leadApplicant.getId());
        inOrder.verify(applicantRestService).getQuestion(leadApplicant.getId(), applicationResource.getId(), questionId);
        inOrder.verify(applicationService).getLeadOrganisation(applicationResource.getId());
        inOrder.verify(inviteRestService).getInvitesByApplication(applicationResource.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void populateModel_loggedInUserIsNonLead() {
        Map<String, OrganisationResource> organisationsMap = setupOrganisationResources();
        ApplicationResource applicationResource = setupApplicationResource(organisationsMap);
        Map<String, UserResource> usersMap = setupUserResources();
        Map<String, InviteOrganisationResource> inviteOrganisationsMap = setupOrganisationInvitesWithInviteForLeadOrg(
                applicationResource.getId(), usersMap, organisationsMap);
        UserResource leadApplicant = setupLeadApplicant(applicationResource, usersMap);
        ApplicantQuestionResource applicantQuestion = setupApplicantQuestion(applicationResource.getId(), leadApplicant.getId());

        Long orgIdEmpire = organisationsMap.get("Empire Ltd").getId();
        Long orgIdLudlow = organisationsMap.get("Ludlow").getId();
        Long orgIdEggs = organisationsMap.get("EGGS").getId();
        Long inviteOrgIdEmpire = inviteOrganisationsMap.get("Empire Ltd").getId();
        Long inviteOrgIdLudlow = inviteOrganisationsMap.get("Ludlow").getId();
        Long inviteOrgIdEggs = inviteOrganisationsMap.get("EGGS").getId();
        Long questionId = applicantQuestion.getQuestion().getId();
        Long userId = usersMap.get("jessica.doe@ludlow.com").getId();

        List<ApplicationTeamOrganisationRowViewModel> expectedOrganisations = asList(
                new ApplicationTeamOrganisationRowViewModel(orgIdEmpire, inviteOrgIdEmpire, "Empire Ltd", "Business", true, asList(
                        new ApplicationTeamApplicantRowViewModel("Steve Smith", "steve.smith@empire.com", true, false),
                        new ApplicationTeamApplicantRowViewModel("Paul Davidson", "paul.davidson@empire.com", false, false)
                ), false),
                new ApplicationTeamOrganisationRowViewModel(orgIdEggs, inviteOrgIdEggs, "EGGS", "Business", false, singletonList(
                        new ApplicationTeamApplicantRowViewModel("Paul Tom", "paul.tom@egg.com", false, false)
                ), false),
                new ApplicationTeamOrganisationRowViewModel(orgIdLudlow, inviteOrgIdLudlow, "Ludlow", "Acedemic", false, asList(
                        new ApplicationTeamApplicantRowViewModel("Jessica Doe", "jessica.doe@ludlow.com", false, false),
                        new ApplicationTeamApplicantRowViewModel("Ryan Dell", "ryan.dell@ludlow.com", false, true)
                ), true)
        );

        ApplicationTeamViewModel expectedViewModel = new ApplicationTeamViewModel(
                applicationResource.getId(),
                1L,
                "Application name",
                expectedOrganisations,
                false,
                false,
                false,
                false,
                false,
                false,
                SINGLE_OR_COLLABORATIVE
        );

        when(applicantRestService.getQuestion(userId, applicationResource.getId(), questionId)).thenReturn(applicantQuestion);

        ApplicationTeamViewModel applicationTeamViewModel = applicationTeamModelPopulator.populateModel
                (applicationResource.getId(), userId, questionId);

        assertEquals(expectedViewModel, applicationTeamViewModel);

        InOrder inOrder = inOrder(applicationService, userService, userRestService, applicantRestService, inviteRestService);
        inOrder.verify(applicationService).getById(applicationResource.getId());
        inOrder.verify(userService).getLeadApplicantProcessRoleOrNull(applicationResource.getId());
        inOrder.verify(userRestService).retrieveUserById(leadApplicant.getId());
        inOrder.verify(applicantRestService).getQuestion(userId, applicationResource.getId(), questionId);
        inOrder.verify(applicationService).getLeadOrganisation(applicationResource.getId());
        inOrder.verify(inviteRestService).getInvitesByApplication(applicationResource.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void populateModel_leadOrgHasNoInvites() {
        Map<String, OrganisationResource> organisationsMap = setupOrganisationResources();
        ApplicationResource applicationResource = setupApplicationResource(organisationsMap);
        Map<String, UserResource> usersMap = setupUserResources();
        Map<String, InviteOrganisationResource> inviteOrganisationsMap = setupOrganisationInvitesWithoutInvitesForLeadOrg(
                applicationResource.getId(), usersMap, organisationsMap);
        UserResource leadApplicant = setupLeadApplicant(applicationResource, usersMap);
        ApplicantQuestionResource applicantQuestion = setupApplicantQuestion(applicationResource.getId(), leadApplicant.getId());

        Long orgIdEmpire = organisationsMap.get("Empire Ltd").getId();
        Long orgIdLudlow = organisationsMap.get("Ludlow").getId();
        Long orgIdEggs = organisationsMap.get("EGGS").getId();
        // No InviteOrganisation exists for Empire Ltd
        Long inviteOrgIdEmpire = null;
        Long inviteOrgIdLudlow = inviteOrganisationsMap.get("Ludlow").getId();
        Long inviteOrgIdEggs = inviteOrganisationsMap.get("EGGS").getId();
        Long questionId = applicantQuestion.getQuestion().getId();
        Long userId = usersMap.get("jessica.doe@ludlow.com").getId();

        List<ApplicationTeamOrganisationRowViewModel> expectedOrganisations = asList(
                new ApplicationTeamOrganisationRowViewModel(orgIdEmpire, inviteOrgIdEmpire, "Empire Ltd", "Business", true, singletonList(
                        new ApplicationTeamApplicantRowViewModel("Steve Smith", "steve.smith@empire.com", true, false)
                ), false),
                new ApplicationTeamOrganisationRowViewModel(orgIdEggs, inviteOrgIdEggs, "EGGS", "Business", false, singletonList(
                        new ApplicationTeamApplicantRowViewModel("Paul Tom", "paul.tom@egg.com", false, false)
                ), false),
                new ApplicationTeamOrganisationRowViewModel(orgIdLudlow, inviteOrgIdLudlow, "Ludlow", "Acedemic", false, asList(
                        new ApplicationTeamApplicantRowViewModel("Jessica Doe", "jessica.doe@ludlow.com", false, false),
                        new ApplicationTeamApplicantRowViewModel("Ryan Dell", "ryan.dell@ludlow.com", false, true)
                ), true)
        );

        ApplicationTeamViewModel expectedViewModel = new ApplicationTeamViewModel(
                applicationResource.getId(),
                1L,
                "Application name",
                expectedOrganisations,
                false,
                false,
                false,
                false,
                false,
                false,
                SINGLE_OR_COLLABORATIVE
        );

        when(applicantRestService.getQuestion(userId, applicationResource.getId(), questionId)).thenReturn(applicantQuestion);

        ApplicationTeamViewModel applicationTeamViewModel = applicationTeamModelPopulator.populateModel
                (applicationResource.getId(), userId, questionId);

        assertEquals(expectedViewModel, applicationTeamViewModel);

        InOrder inOrder = inOrder(applicationService, userService, userRestService, applicantRestService, inviteRestService);
        inOrder.verify(applicationService).getById(applicationResource.getId());
        inOrder.verify(userService).getLeadApplicantProcessRoleOrNull(applicationResource.getId());
        inOrder.verify(userRestService).retrieveUserById(leadApplicant.getId());
        inOrder.verify(applicantRestService).getQuestion(userId, applicationResource.getId(), questionId);
        inOrder.verify(applicationService).getLeadOrganisation(applicationResource.getId());
        inOrder.verify(inviteRestService).getInvitesByApplication(applicationResource.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void getApplicationTeam_organisationUnconfirmed() {
        Map<String, OrganisationResource> organisationsMap = setupOrganisationResources();
        ApplicationResource applicationResource = setupApplicationResource(organisationsMap);
        Map<String, UserResource> usersMap = setupUserResources();
        Map<String, InviteOrganisationResource> inviteOrganisationsMap = setupOrganisationInvitesWithAnUnconfirmedOrganisation(
                applicationResource.getId(), usersMap, organisationsMap);
        UserResource leadApplicant = setupLeadApplicant(applicationResource, usersMap);
        ApplicantQuestionResource applicantQuestion = setupApplicantQuestion(applicationResource.getId(), leadApplicant.getId());

        Long orgIdEmpire = organisationsMap.get("Empire Ltd").getId();
        // No Organisation exists for Ludlow
        Long orgIdLudlow = null;
        Long orgIdEggs = organisationsMap.get("EGGS").getId();
        Long inviteOrgIdEmpire = inviteOrganisationsMap.get("Empire Ltd").getId();
        Long inviteOrgIdLudlow = inviteOrganisationsMap.get("Ludlow").getId();
        Long inviteOrgIdEggs = inviteOrganisationsMap.get("EGGS").getId();
        Long questionId = applicantQuestion.getQuestion().getId();

        List<ApplicationTeamOrganisationRowViewModel> expectedOrganisations = asList(
                new ApplicationTeamOrganisationRowViewModel(orgIdEmpire, inviteOrgIdEmpire, "Empire Ltd", "Business", true, asList(
                        new ApplicationTeamApplicantRowViewModel("Steve Smith", "steve.smith@empire.com", true, false),
                        new ApplicationTeamApplicantRowViewModel("Paul Davidson", "paul.davidson@empire.com", false, false)
                ), true),
                new ApplicationTeamOrganisationRowViewModel(orgIdEggs, inviteOrgIdEggs, "EGGS", "Business", false, singletonList(
                        new ApplicationTeamApplicantRowViewModel("Paul Tom", "paul.tom@egg.com", false, false)
                ), true),
                new ApplicationTeamOrganisationRowViewModel(orgIdLudlow, inviteOrgIdLudlow, "Ludlow", "Acedemic", false, asList(
                        new ApplicationTeamApplicantRowViewModel("Jessica Doe", "jessica.doe@ludlow.com", false, true),
                        new ApplicationTeamApplicantRowViewModel("Ryan Dell", "ryan.dell@ludlow.com", false, true)
                ), true)
        );

        ApplicationTeamViewModel expectedViewModel = new ApplicationTeamViewModel(
                applicationResource.getId(),
                1L,
                "Application name",
                expectedOrganisations,
                true,
                false,
                false,
                false,
                true,
                false,
                SINGLE_OR_COLLABORATIVE
        );

        when(applicantRestService.getQuestion(leadApplicant.getId(), applicationResource.getId(), questionId)).thenReturn(applicantQuestion);

        ApplicationTeamViewModel applicationTeamViewModel = applicationTeamModelPopulator.populateModel
                (applicationResource.getId(), leadApplicant.getId(), questionId);

        assertEquals(expectedViewModel, applicationTeamViewModel);

        InOrder inOrder = inOrder(applicationService, userService, userRestService, applicantRestService, inviteRestService);
        inOrder.verify(applicationService).getById(applicationResource.getId());
        inOrder.verify(userService).getLeadApplicantProcessRoleOrNull(applicationResource.getId());
        inOrder.verify(userRestService).retrieveUserById(leadApplicant.getId());
        inOrder.verify(applicantRestService).getQuestion(leadApplicant.getId(), applicationResource.getId(), questionId);
        inOrder.verify(applicationService).getLeadOrganisation(applicationResource.getId());
        inOrder.verify(inviteRestService).getInvitesByApplication(applicationResource.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void getApplicationTeam_leadApplicantHasOptionToBeginTheApplication() {
        Map<String, OrganisationResource> organisationsMap = setupOrganisationResources();
        ApplicationResource applicationResource = setupApplicationResource(organisationsMap, CREATED);
        Map<String, UserResource> usersMap = setupUserResources();
        setupOrganisationInvitesWithInviteForLeadOrg(applicationResource.getId(), usersMap, organisationsMap);
        UserResource leadApplicant = setupLeadApplicant(applicationResource, usersMap);
        ApplicantQuestionResource applicantQuestion = setupApplicantQuestion(applicationResource.getId(), leadApplicant.getId());

        Long questionId = applicantQuestion.getQuestion().getId();

        setLoggedInUser(leadApplicant);

        when(applicantRestService.getQuestion(leadApplicant.getId(), applicationResource.getId(), questionId)).thenReturn(applicantQuestion);

        ApplicationTeamViewModel applicationTeamViewModel = applicationTeamModelPopulator.populateModel
                (applicationResource.getId(), leadApplicant.getId(), questionId);

        assertTrue(applicationTeamViewModel.isApplicationCanBegin());

        InOrder inOrder = inOrder(applicationService, userService, userRestService, applicantRestService, inviteRestService);
        inOrder.verify(applicationService).getById(applicationResource.getId());
        inOrder.verify(userService).getLeadApplicantProcessRoleOrNull(applicationResource.getId());
        inOrder.verify(userRestService).retrieveUserById(leadApplicant.getId());
        inOrder.verify(applicantRestService).getQuestion(leadApplicant.getId(), applicationResource.getId(), questionId);
        inOrder.verify(applicationService).getLeadOrganisation(applicationResource.getId());
        inOrder.verify(inviteRestService).getInvitesByApplication(applicationResource.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void getApplicationTeam_nonLeadApplicantHasNoOptionToBeginTheApplication() {
        Map<String, OrganisationResource> organisationsMap = setupOrganisationResources();
        ApplicationResource applicationResource = setupApplicationResource(organisationsMap, CREATED);
        Map<String, UserResource> usersMap = setupUserResources();
        setupOrganisationInvitesWithInviteForLeadOrg(applicationResource.getId(), usersMap, organisationsMap);
        UserResource leadApplicant = setupLeadApplicant(applicationResource, usersMap);
        ApplicantQuestionResource applicantQuestion = setupApplicantQuestion(applicationResource.getId(), leadApplicant.getId());

        Long questionId = applicantQuestion.getQuestion().getId();
        Long userId = usersMap.get("jessica.doe@ludlow.com").getId();

        when(applicantRestService.getQuestion(userId, applicationResource.getId(), questionId)).thenReturn(applicantQuestion);

        ApplicationTeamViewModel applicationTeamViewModel = applicationTeamModelPopulator.populateModel
                (applicationResource.getId(), userId, questionId);

        assertFalse(applicationTeamViewModel.isApplicationCanBegin());

        InOrder inOrder = inOrder(applicationService, userService, userRestService, applicantRestService, inviteRestService);
        inOrder.verify(applicationService).getById(applicationResource.getId());
        inOrder.verify(userService).getLeadApplicantProcessRoleOrNull(applicationResource.getId());
        inOrder.verify(userRestService).retrieveUserById(leadApplicant.getId());
        inOrder.verify(applicantRestService).getQuestion(userId, applicationResource.getId(), questionId);
        inOrder.verify(applicationService).getLeadOrganisation(applicationResource.getId());
        inOrder.verify(inviteRestService).getInvitesByApplication(applicationResource.getId());
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void populateSummaryModel_userIsLead() {
        Map<String, OrganisationResource> organisationsMap = setupOrganisationResources();
        ApplicationResource applicationResource = setupApplicationResource(organisationsMap);
        Map<String, UserResource> usersMap = setupUserResources();
        Map<String, InviteOrganisationResource> inviteOrganisationsMap = setupOrganisationInvitesWithInviteForLeadOrg(
                applicationResource.getId(), usersMap, organisationsMap);
        UserResource leadApplicant = setupLeadApplicant(applicationResource, usersMap);
        ApplicantQuestionResource applicantQuestion = setupApplicantQuestion(applicationResource.getId(), leadApplicant.getId());
        CompetitionResource competition = newCompetitionResource().build();

        Long orgIdEmpire = organisationsMap.get("Empire Ltd").getId();
        Long orgIdLudlow = organisationsMap.get("Ludlow").getId();
        Long orgIdEggs = organisationsMap.get("EGGS").getId();
        Long inviteOrgIdEmpire = inviteOrganisationsMap.get("Empire Ltd").getId();
        Long inviteOrgIdLudlow = inviteOrganisationsMap.get("Ludlow").getId();
        Long inviteOrgIdEggs = inviteOrganisationsMap.get("EGGS").getId();
        QuestionResource question = applicantQuestion.getQuestion();

        List<ApplicationTeamOrganisationRowViewModel> expectedOrganisations = asList(
                new ApplicationTeamOrganisationRowViewModel(orgIdEmpire, inviteOrgIdEmpire, "Empire Ltd", "Business", true, asList(
                        new ApplicationTeamApplicantRowViewModel("Steve Smith", "steve.smith@empire.com", true, false),
                        new ApplicationTeamApplicantRowViewModel("Paul Davidson", "paul.davidson@empire.com", false, false)
                ), true),
                new ApplicationTeamOrganisationRowViewModel(orgIdEggs, inviteOrgIdEggs, "EGGS", "Business", false, singletonList(
                        new ApplicationTeamApplicantRowViewModel("Paul Tom", "paul.tom@egg.com", false, false)
                ), true),
                new ApplicationTeamOrganisationRowViewModel(orgIdLudlow, inviteOrgIdLudlow, "Ludlow", "Academic", false, asList(
                        new ApplicationTeamApplicantRowViewModel("Jessica Doe", "jessica.doe@ludlow.com", false, false),
                        new ApplicationTeamApplicantRowViewModel("Ryan Dell", "ryan.dell@ludlow.com", false, true)
                ), true)
        );


        ApplicationTeamViewModel expectedViewModel = new ApplicationTeamViewModel(
                applicationResource.getId(),
                1L,
                "Application name",
                expectedOrganisations,
                true,
                false,
                false,
                false,
                true,
                false,
                SINGLE_OR_COLLABORATIVE
        );

        expectedViewModel.setSummary(true);

        when(questionRestService.getQuestionByCompetitionIdAndQuestionSetupType(competition.getId(), APPLICATION_TEAM))
                .thenReturn(restSuccess(question));
        when(applicantRestService.getQuestion(leadApplicant.getId(), applicationResource.getId(), question.getId())).thenReturn(applicantQuestion);

        ApplicationTeamViewModel applicationTeamViewModel = applicationTeamModelPopulator.populateSummaryModel
                (applicationResource.getId(), leadApplicant.getId(), competition.getId());

        assertEquals(expectedViewModel, applicationTeamViewModel);

        InOrder inOrder = inOrder(questionRestService, applicationService, userService, userRestService, applicantRestService, inviteRestService);
        inOrder.verify(questionRestService).getQuestionByCompetitionIdAndQuestionSetupType(competition.getId(), APPLICATION_TEAM);
        inOrder.verify(applicationService).getById(applicationResource.getId());
        inOrder.verify(userService).getLeadApplicantProcessRoleOrNull(applicationResource.getId());
        inOrder.verify(userRestService).retrieveUserById(leadApplicant.getId());
        inOrder.verify(applicantRestService).getQuestion(leadApplicant.getId(), applicationResource.getId(), question.getId());
        inOrder.verify(applicationService).getLeadOrganisation(applicationResource.getId());
        inOrder.verify(inviteRestService).getInvitesByApplication(applicationResource.getId());
        inOrder.verifyNoMoreInteractions();
    }

    private ApplicationResource setupApplicationResource(Map<String, OrganisationResource> organisationsMap) {
        return setupApplicationResource(organisationsMap, OPEN);
    }

    private ApplicationResource setupApplicationResource(Map<String, OrganisationResource> organisationsMap, ApplicationState applicationState) {
        ApplicationResource applicationResource = newApplicationResource()
                .withName("Application name")
                .withApplicationState(applicationState)
                .withCompetitionStatus(CompetitionStatus.OPEN)
                .withCollaborationLevel(SINGLE_OR_COLLABORATIVE)
                .build();

        when(applicationService.getById(applicationResource.getId())).thenReturn(applicationResource);
        when(applicationService.getLeadOrganisation(applicationResource.getId())).thenReturn(organisationsMap.get("Empire Ltd"));
        return applicationResource;
    }

    private Map<String, UserResource> setupUserResources() {
        List<UserResource> userResources = newUserResource()
                .withFirstName("Jessica", "Paul", "Steve", "Paul")
                .withLastName("Doe", "Tom", "Smith", "Davidson")
                .withEmail("jessica.doe@ludlow.com", "paul.tom@egg.com", "steve.smith@empire.com", "paul.davidson@empire.com")
                .build(4);

        return simpleToMap(userResources, UserResource::getEmail);
    }

    private UserResource setupLeadApplicant(ApplicationResource applicationResource, Map<String, UserResource> usersMap) {
        UserResource leadApplicant = usersMap.get("steve.smith@empire.com");
        when(userService.getLeadApplicantProcessRoleOrNull(applicationResource.getId())).thenReturn(newProcessRoleResource()
                .withUser(leadApplicant)
                .build());
        when(userRestService.retrieveUserById(leadApplicant.getId())).thenReturn(restSuccess(leadApplicant));
        return leadApplicant;
    }

    private Map<String, OrganisationResource> setupOrganisationResources() {
        List<OrganisationResource> organisationResources = newOrganisationResource()
                .withName("Ludlow", "EGGS", "Empire Ltd")
                .build(3);

        return simpleToMap(organisationResources, OrganisationResource::getName);
    }

    private Map<String, InviteOrganisationResource> setupOrganisationInvitesWithInviteForLeadOrg(long applicationId,
                                                                                                 Map<String, UserResource> usersMap,
                                                                                                 Map<String, OrganisationResource> organisationsMap) {
        UserResource user1 = usersMap.get("jessica.doe@ludlow.com");
        UserResource user2 = usersMap.get("paul.tom@egg.com");
        UserResource user3 = usersMap.get("paul.davidson@empire.com");

        List<ApplicationInviteResource> invitesOrg1 = newApplicationInviteResource()
                .withUsers(user1.getId(), null)
                .withNameConfirmed(user1.getName(), null)
                .withName("Jess Doe", "Ryan Dell")
                .withEmail(user1.getEmail(), "ryan.dell@ludlow.com")
                .withStatus(OPENED, SENT)
                .build(2);

        List<ApplicationInviteResource> invitesOrg2 = newApplicationInviteResource()
                .withUsers(user2.getId())
                .withNameConfirmed(user2.getName())
                .withName(user2.getName())
                .withEmail(user2.getEmail())
                .withStatus(OPENED)
                .build(1);

        List<ApplicationInviteResource> invitesOrg3 = newApplicationInviteResource()
                .withUsers(user3.getId())
                .withNameConfirmed(user3.getName())
                .withName(user3.getName())
                .withEmail(user3.getEmail())
                .withStatus(OPENED)
                .build(1);

        OrganisationResource org1 = organisationsMap.get("Ludlow");
        OrganisationResource org2 = organisationsMap.get("EGGS");
        OrganisationResource org3 = organisationsMap.get("Empire Ltd");

        List<InviteOrganisationResource> inviteOrganisationResources = newInviteOrganisationResource()
                .withOrganisation(org1.getId(), org2.getId(), org3.getId())
                .withOrganisationName(org1.getName(), org2.getName(), org3.getName())
                .withOrganisationNameConfirmed(org1.getName(), org2.getName(), org3.getName())
                .withInviteResources(invitesOrg1, invitesOrg2, invitesOrg3)
                .build(3);

        when(inviteRestService.getInvitesByApplication(applicationId)).thenReturn(restSuccess(inviteOrganisationResources));
        return simpleToMap(inviteOrganisationResources, InviteOrganisationResource::getOrganisationName);
    }

    private Map<String, InviteOrganisationResource> setupOrganisationInvitesWithoutInvitesForLeadOrg(long applicationId,
                                                                                                     Map<String, UserResource> usersMap,
                                                                                                     Map<String, OrganisationResource> organisationsMap) {
        UserResource user1 = usersMap.get("jessica.doe@ludlow.com");
        UserResource user2 = usersMap.get("paul.tom@egg.com");

        List<ApplicationInviteResource> invitesOrg1 = newApplicationInviteResource()
                .withUsers(user1.getId(), null)
                .withNameConfirmed(user1.getName(), null)
                .withName("Jess Doe", "Ryan Dell")
                .withEmail(user1.getEmail(), "ryan.dell@ludlow.com")
                .withStatus(OPENED, SENT)
                .build(2);

        List<ApplicationInviteResource> invitesOrg2 = newApplicationInviteResource()
                .withUsers(user2.getId())
                .withNameConfirmed(user2.getName())
                .withName(user2.getName())
                .withEmail(user2.getEmail())
                .withStatus(OPENED)
                .build(1);

        OrganisationResource org1 = organisationsMap.get("Ludlow");
        OrganisationResource org2 = organisationsMap.get("EGGS");

        List<InviteOrganisationResource> inviteOrganisationResources = newInviteOrganisationResource()
                .withOrganisation(org1.getId(), org2.getId())
                .withOrganisationName(org1.getName(), org2.getName())
                .withOrganisationNameConfirmed(org1.getName(), org2.getName())
                .withInviteResources(invitesOrg1, invitesOrg2)
                .build(2);

        when(inviteRestService.getInvitesByApplication(applicationId)).thenReturn(restSuccess(inviteOrganisationResources));
        return simpleToMap(inviteOrganisationResources, InviteOrganisationResource::getOrganisationName);
    }

    private Map<String, InviteOrganisationResource> setupOrganisationInvitesWithAnUnconfirmedOrganisation(long applicationId,
                                                                                                          Map<String, UserResource> usersMap,
                                                                                                          Map<String, OrganisationResource> organisationsMap) {
        UserResource user1 = usersMap.get("jessica.doe@ludlow.com");
        UserResource user2 = usersMap.get("paul.tom@egg.com");
        UserResource user3 = usersMap.get("paul.davidson@empire.com");

        List<ApplicationInviteResource> invitesOrg1 = newApplicationInviteResource()
                .withUsers(user1.getId(), null)
                .withNameConfirmed(user1.getName(), null)
                .withName("Jess Doe", "Ryan Dell")
                .withEmail(user1.getEmail(), "ryan.dell@ludlow.com")
                .withStatus(SENT, SENT)
                .build(2);

        List<ApplicationInviteResource> invitesOrg2 = newApplicationInviteResource()
                .withUsers(user2.getId())
                .withNameConfirmed(user2.getName())
                .withName(user2.getName())
                .withEmail(user2.getEmail())
                .withStatus(OPENED)
                .build(1);

        List<ApplicationInviteResource> invitesOrg3 = newApplicationInviteResource()
                .withUsers(user3.getId())
                .withNameConfirmed(user3.getName())
                .withName(user3.getName())
                .withEmail(user3.getEmail())
                .withStatus(OPENED)
                .build(1);

        OrganisationResource org2 = organisationsMap.get("EGGS");
        OrganisationResource org3 = organisationsMap.get("Empire Ltd");

        List<InviteOrganisationResource> inviteOrganisationResources = newInviteOrganisationResource()
                .withOrganisation(null, org2.getId(), org3.getId())
                .withOrganisationName("Ludlow", org2.getName(), org3.getName())
                .withOrganisationNameConfirmed(null, org2.getName(), org3.getName())
                .withInviteResources(invitesOrg1, invitesOrg2, invitesOrg3)
                .build(3);

        when(inviteRestService.getInvitesByApplication(applicationId)).thenReturn(restSuccess(inviteOrganisationResources));
        return simpleToMap(inviteOrganisationResources, InviteOrganisationResource::getOrganisationName);
    }

    private ApplicantQuestionResource setupApplicantQuestion(long applicationId, long userId) {
        QuestionResource question = newQuestionResource().build();
        ApplicantResource applicant = newApplicantResource().build();

        ApplicantQuestionResource applicantQuestion = newApplicantQuestionResource()
                .withApplication(newApplicationResource().withId(applicationId).build())
                .withCurrentApplicant(applicant)
                .withCurrentUser(newUserResource().withId(userId).build())
                .withQuestion(question)
                .withApplicantQuestionStatuses(newApplicantQuestionStatusResource()
                        .withStatus(newQuestionStatusResource().build())
                        .withAssignedBy(applicant)
                        .withAssignee(newApplicantResource().build())
                        .withMarkedAsCompleteBy(newApplicantResource().build())
                        .build(3))
                .build();

        return applicantQuestion;
    }
}