package org.innovateuk.ifs.project.core.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.finance.builder.ApplicationFinanceBuilder;
import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.fundingdecision.domain.FundingDecisionStatus;
import org.innovateuk.ifs.invite.domain.ProjectInvite;
import org.innovateuk.ifs.invite.repository.ProjectInviteRepository;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.domain.OrganisationType;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.project.core.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.mapper.ProjectMapper;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.core.workflow.configuration.ProjectWorkflowHandler;
import org.innovateuk.ifs.project.financechecks.domain.CostCategoryType;
import org.innovateuk.ifs.project.financechecks.transactional.FinanceChecksGenerator;
import org.innovateuk.ifs.project.financechecks.workflow.financechecks.configuration.EligibilityWorkflowHandler;
import org.innovateuk.ifs.project.financechecks.workflow.financechecks.configuration.ViabilityWorkflowHandler;
import org.innovateuk.ifs.project.grantofferletter.configuration.workflow.GrantOfferLetterWorkflowHandler;
import org.innovateuk.ifs.project.projectdetails.workflow.configuration.ProjectDetailsWorkflowHandler;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.spendprofile.configuration.workflow.SpendProfileWorkflowHandler;
import org.innovateuk.ifs.project.spendprofile.transactional.CostCategoryTypeStrategy;
import org.innovateuk.ifs.security.LoggedInUserSupplier;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.ProcessRoleRepository;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.innovateuk.ifs.LambdaMatcher.createLambdaMatcher;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.commons.error.CommonErrors.badRequestError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_CANNOT_BE_WITHDRAWN;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.builder.ProjectInviteBuilder.newProjectInvite;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_FINANCE_CONTACT;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.organisation.builder.OrganisationTypeBuilder.newOrganisationType;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.core.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.financecheck.builder.CostCategoryBuilder.newCostCategory;
import static org.innovateuk.ifs.project.financecheck.builder.CostCategoryGroupBuilder.newCostCategoryGroup;
import static org.innovateuk.ifs.project.financecheck.builder.CostCategoryTypeBuilder.newCostCategoryType;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProjectServiceImplTest extends BaseServiceUnitTest<ProjectService> {

    @Mock
    private CostCategoryTypeStrategy costCategoryTypeStrategyMock;

    @Mock
    private FinanceChecksGenerator financeChecksGeneratorMock;

    @Mock
    private ApplicationRepository applicationRepositoryMock;

    @Mock
    private OrganisationRepository organisationRepositoryMock;

    @Mock
    private LoggedInUserSupplier loggedInUserSupplierMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private ProjectDetailsWorkflowHandler projectDetailsWorkflowHandlerMock;

    @Mock
    private ViabilityWorkflowHandler viabilityWorkflowHandlerMock;

    @Mock
    private EligibilityWorkflowHandler eligibilityWorkflowHandlerMock;

    @Mock
    private GrantOfferLetterWorkflowHandler golWorkflowHandlerMock;

    @Mock
    private ProjectWorkflowHandler projectWorkflowHandlerMock;

    @Mock
    private ProjectMapper projectMapperMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private ProcessRoleRepository processRoleRepositoryMock;

    @Mock
    private ProjectUserRepository projectUserRepositoryMock;

    @Mock
    private ProjectInviteRepository projectInviteRepositoryMock;


    @Mock
    private SpendProfileWorkflowHandler spendProfileWorkflowHandlerMock;

    private Long applicationId = 456L;
    private Long userId = 7L;

    private Application application;
    private List<ApplicationFinance> applicationFinances;
    private Organisation organisation;
    private User user;
    private User u;
    private ProcessRole leadApplicantProcessRole;
    private ProjectUser leadPartnerProjectUser;
    private List<PartnerOrganisation> po;
    private List<ProjectUser> pu;
    private Organisation o;
    private Project p;

    @Before
    public void setUp() {

        organisation = newOrganisation().
                withOrganisationType(OrganisationTypeEnum.BUSINESS).
                build();

        user = newUser().
                withId(userId).
                build();

        leadApplicantProcessRole = newProcessRole().
                withOrganisationId(organisation.getId()).
                withRole(Role.LEADAPPLICANT).
                withUser(user).
                build();

        leadPartnerProjectUser = newProjectUser().
                withOrganisation(organisation).
                withRole(PROJECT_PARTNER).
                withUser(user).
                build();

        ApplicationFinance applicationFinance = ApplicationFinanceBuilder.newApplicationFinance()
                .withApplication(application)
                .withOrganisation(organisation)
                .withWorkPostcode("UB7 8QF")
                .build();
        applicationFinances = singletonList(applicationFinance);

        application = newApplication().
                withId(applicationId).
                withProcessRoles(leadApplicantProcessRole).
                withName("My Application").
                withDurationInMonths(5L).
                withStartDate(LocalDate.of(2017, 3, 2)).
                withFundingDecision(FundingDecisionStatus.FUNDED).
                withApplicationFinancesList(applicationFinances).
                build();

        OrganisationType businessOrganisationType = newOrganisationType().withOrganisationType(OrganisationTypeEnum.BUSINESS).build();
        o = organisation;
        o.setOrganisationType(businessOrganisationType);

        po = newPartnerOrganisation().
                withOrganisation(o).
                withLeadOrganisation(TRUE).
                build(1);

        u = newUser().
                withEmailAddress("a@b.com").
                withFirstName("A").
                withLastName("B").
                build();

        pu = newProjectUser().
                withRole(PROJECT_FINANCE_CONTACT).
                withUser(u).
                withOrganisation(o).
                withInvite(newProjectInvite().
                        build()).
                build(1);

        p = newProject().
                withProjectUsers(pu).
                withApplication(application).
                withPartnerOrganisations(po).
                withDateSubmitted(ZonedDateTime.now()).
                withOtherDocumentsApproved(ApprovalType.APPROVED).
                withSpendProfileSubmittedDate(ZonedDateTime.now()).
                build();

        when(applicationRepositoryMock.findOne(applicationId)).thenReturn(application);
        when(organisationRepositoryMock.findOne(organisation.getId())).thenReturn(organisation);
        when(loggedInUserSupplierMock.get()).thenReturn(newUser().build());
    }

    @Test
    public void testCreateProjectFromApplication() {

        ProjectResource newProjectResource = newProjectResource().build();

        PartnerOrganisation savedProjectPartnerOrganisation = newPartnerOrganisation().
                withOrganisation(organisation).
                withLeadOrganisation(true).
                build();

        Project savedProject = newProject().
                withId(newProjectResource.getId()).
                withApplication(application).
                withProjectUsers(asList(leadPartnerProjectUser, newProjectUser().build())).
                withPartnerOrganisations(singletonList(savedProjectPartnerOrganisation)).
                build();

        Project newProjectExpectations = createProjectExpectationsFromOriginalApplication();
        when(projectRepositoryMock.save(newProjectExpectations)).thenReturn(savedProject);

        CostCategoryType costCategoryTypeForOrganisation = newCostCategoryType().
                withCostCategoryGroup(newCostCategoryGroup().
                        withCostCategories(newCostCategory().withName("Cat1", "Cat2").build(2)).
                        build()).
                build();

        when(costCategoryTypeStrategyMock.getOrCreateCostCategoryTypeForSpendProfile(savedProject.getId(),
                organisation.getId())).thenReturn(serviceSuccess(costCategoryTypeForOrganisation));

        when(financeChecksGeneratorMock.createMvpFinanceChecksFigures(savedProject, organisation, costCategoryTypeForOrganisation)).thenReturn(serviceSuccess());
        when(financeChecksGeneratorMock.createFinanceChecksFigures(savedProject, organisation)).thenReturn(serviceSuccess());

        when(projectDetailsWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);
        when(viabilityWorkflowHandlerMock.projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser)).thenReturn(true);
        when(eligibilityWorkflowHandlerMock.projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser)).thenReturn(true);
        when(golWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);
        when(projectWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);
        when(spendProfileWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);

        when(projectMapperMock.mapToResource(savedProject)).thenReturn(newProjectResource);

        ServiceResult<ProjectResource> project = service.createProjectFromApplication(applicationId);
        assertTrue(project.isSuccess());
        assertEquals(newProjectResource, project.getSuccess());

        verify(costCategoryTypeStrategyMock).getOrCreateCostCategoryTypeForSpendProfile(savedProject.getId(), organisation.getId());
        verify(financeChecksGeneratorMock).createMvpFinanceChecksFigures(savedProject, organisation, costCategoryTypeForOrganisation);
        verify(financeChecksGeneratorMock).createFinanceChecksFigures(savedProject, organisation);

        verify(projectDetailsWorkflowHandlerMock).projectCreated(savedProject, leadPartnerProjectUser);
        verify(viabilityWorkflowHandlerMock).projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser);
        verify(eligibilityWorkflowHandlerMock).projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser);
        verify(golWorkflowHandlerMock).projectCreated(savedProject, leadPartnerProjectUser);
        verify(projectWorkflowHandlerMock).projectCreated(savedProject, leadPartnerProjectUser);
        verify(projectMapperMock).mapToResource(savedProject);
    }

    @Test
    public void testCreateProjectFromApplicationAlreadyExists() {

        ProjectResource existingProjectResource = newProjectResource().build();
        Project existingProject = newProject().withApplication(application).build();

        when(projectRepositoryMock.findOneByApplicationId(applicationId)).thenReturn(existingProject);
        when(projectMapperMock.mapToResource(existingProject)).thenReturn(existingProjectResource);

        ServiceResult<ProjectResource> project = service.createProjectFromApplication(applicationId);
        assertTrue(project.isSuccess());
        assertEquals(existingProjectResource, project.getSuccess());

        verify(projectRepositoryMock).findOneByApplicationId(applicationId);
        verify(projectMapperMock).mapToResource(existingProject);

        verify(costCategoryTypeStrategyMock, never()).getOrCreateCostCategoryTypeForSpendProfile(any(Long.class), any(Long.class));
        verify(financeChecksGeneratorMock, never()).createMvpFinanceChecksFigures(any(Project.class), any(Organisation.class), any(CostCategoryType.class));
        verify(financeChecksGeneratorMock, never()).createFinanceChecksFigures(any(Project.class), any(Organisation.class));
        verify(projectDetailsWorkflowHandlerMock, never()).projectCreated(any(Project.class), any(ProjectUser.class));
        verify(golWorkflowHandlerMock, never()).projectCreated(any(Project.class), any(ProjectUser.class));
        verify(projectWorkflowHandlerMock, never()).projectCreated(any(Project.class), any(ProjectUser.class));
    }

    @Test
    public void testWithdrawProject() {
        Long projectId = 123L;
        Long userId = 456L;
        Project project = newProject().withId(projectId).build();
        UserResource loggedInUser = newUserResource()
                .withRolesGlobal(singletonList(Role.IFS_ADMINISTRATOR))
                .withId(userId)
                .build();
        User user = newUser()
                .withId(userId)
                .build();
        setLoggedInUser(loggedInUser);
        when(userRepositoryMock.findOne(userId)).thenReturn(user);
        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(projectWorkflowHandlerMock.projectWithdrawn(eq(project), any())).thenReturn(true);

        ServiceResult<Void> result = service.withdrawProject(projectId);
        assertTrue(result.isSuccess());

        verify(projectRepositoryMock).findOne(projectId);
        verify(userRepositoryMock).findOne(userId);
        verify(projectWorkflowHandlerMock).projectWithdrawn(eq(project), any());
    }

    @Test
    public void testWithdrawProjectFails() {
        Long projectId = 321L;
        Long userId = 987L;
        Project project = newProject().withId(projectId).build();
        UserResource loggedInUser = newUserResource()
                .withRolesGlobal(singletonList(Role.IFS_ADMINISTRATOR))
                .withId(userId)
                .build();
        User user = newUser()
                .withId(userId)
                .build();
        when(userRepositoryMock.findOne(userId)).thenReturn(user);
        setLoggedInUser(loggedInUser);
        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(projectWorkflowHandlerMock.projectWithdrawn(eq(project), any())).thenReturn(false);

        ServiceResult<Void> result = service.withdrawProject(projectId);
        assertTrue(result.isFailure());
        assertEquals(PROJECT_CANNOT_BE_WITHDRAWN.getErrorKey(), result.getErrors().get(0).getErrorKey());
        verify(projectRepositoryMock).findOne(projectId);
        verify(userRepositoryMock).findOne(userId);
        verify(projectWorkflowHandlerMock).projectWithdrawn(eq(project), any());
    }

    @Test
    public void testWithdrawProjectCannotFindIdFails() {
        Long projectId = 456L;
        Project project = newProject().withId(projectId).build();
        UserResource user = newUserResource()
                .withRolesGlobal(singletonList(Role.IFS_ADMINISTRATOR))
                .build();
        setLoggedInUser(user);
        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);
        when(projectWorkflowHandlerMock.projectWithdrawn(eq(project), any())).thenReturn(false);

        ServiceResult<Void> result = service.withdrawProject(projectId);
        assertTrue(result.isFailure());
        verify(projectRepositoryMock).findOne(projectId);
        verifyZeroInteractions(projectWorkflowHandlerMock);
    }

    @Test
    public void testFindByUserIdReturnsOnlyDistinctProjects() {

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withId(7L).build();

        ProjectUser projectUserWithPartnerRole = newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(PROJECT_PARTNER).build();
        ProjectUser projectUserWithFinanceRole = newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(PROJECT_FINANCE_CONTACT).build();

        List<ProjectUser> projectUserRecords = asList(projectUserWithPartnerRole, projectUserWithFinanceRole);

        ProjectResource projectResource = newProjectResource().withId(project.getId()).build();

        when(projectUserRepositoryMock.findByUserId(user.getId())).thenReturn(projectUserRecords);

        when(projectMapperMock.mapToResource(project)).thenReturn(projectResource);

        ServiceResult<List<ProjectResource>> result = service.findByUserId(user.getId());

        assertTrue(result.isSuccess());

        assertEquals(result.getSuccess().size(), 1L);
    }

    @Test
    public void testAddPartnerOrganisationNotOnProject(){
        Organisation organisationNotOnProject = newOrganisation().build();
        when(projectRepositoryMock.findOne(p.getId())).thenReturn(p);
        when(organisationRepositoryMock.findOne(o.getId())).thenReturn(o);
        when(organisationRepositoryMock.findOne(organisationNotOnProject.getId())).thenReturn(organisationNotOnProject);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        // Method under test
        ServiceResult<ProjectUser> shouldFail = service.addPartner(p.getId(), u.getId(), organisationNotOnProject.getId());
        // Expectations
        assertTrue(shouldFail.isFailure());
        assertTrue(shouldFail.getFailure().is(badRequestError("project does not contain organisation")));
        verifyZeroInteractions(processRoleRepositoryMock);
    }

    @Test
    public void testAddPartnerPartnerAlreadyExists(){
        when(projectRepositoryMock.findOne(p.getId())).thenReturn(p);
        when(organisationRepositoryMock.findOne(o.getId())).thenReturn(o);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);

        setLoggedInUser(newUserResource().withId(u.getId()).build());

        // Method under test
        ServiceResult<ProjectUser> shouldFail = service.addPartner(p.getId(), u.getId(), o.getId());
        // Expectations
        verifyZeroInteractions(projectUserRepositoryMock);
        assertTrue(shouldFail.isSuccess());
    }

    @Test
    public void testAddPartner(){
        User newUser = newUser().build();
        when(projectRepositoryMock.findOne(p.getId())).thenReturn(p);
        when(organisationRepositoryMock.findOne(o.getId())).thenReturn(o);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(userRepositoryMock.findOne(newUser.getId())).thenReturn(u);
        List<ProjectInvite> projectInvites = newProjectInvite().withUser(user).build(1);
        projectInvites.get(0).open();
        when(projectInviteRepositoryMock.findByProjectId(p.getId())).thenReturn(projectInvites);

        // Method under test
        ServiceResult<ProjectUser> shouldSucceed = service.addPartner(p.getId(), newUser.getId(), o.getId());
        // Expectations
        assertTrue(shouldSucceed.isSuccess());
        verify(processRoleRepositoryMock).save(any(ProcessRole.class));
    }

    @Test
    public void testCreateProjectsFromFundingDecisions() {

        ProjectResource newProjectResource = newProjectResource().build();

        PartnerOrganisation savedProjectPartnerOrganisation = newPartnerOrganisation().
                withOrganisation(organisation).
                withLeadOrganisation(true).
                build();

        Project savedProject = newProject().
                withId(newProjectResource.getId()).
                withApplication(application).
                withProjectUsers(asList(leadPartnerProjectUser, newProjectUser().build())).
                withPartnerOrganisations(singletonList(savedProjectPartnerOrganisation)).
                build();

        Project newProjectExpectations = createProjectExpectationsFromOriginalApplication();
        when(projectRepositoryMock.save(newProjectExpectations)).thenReturn(savedProject);

        CostCategoryType costCategoryTypeForOrganisation = newCostCategoryType().
                withCostCategoryGroup(newCostCategoryGroup().
                        withCostCategories(newCostCategory().withName("Cat1", "Cat2").build(2)).
                        build()).
                build();

        when(costCategoryTypeStrategyMock.getOrCreateCostCategoryTypeForSpendProfile(savedProject.getId(),
                organisation.getId())).thenReturn(serviceSuccess(costCategoryTypeForOrganisation));

        when(financeChecksGeneratorMock.createMvpFinanceChecksFigures(savedProject, organisation, costCategoryTypeForOrganisation)).thenReturn(serviceSuccess());
        when(financeChecksGeneratorMock.createFinanceChecksFigures(savedProject, organisation)).thenReturn(serviceSuccess());

        when(projectDetailsWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);
        when(viabilityWorkflowHandlerMock.projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser)).thenReturn(true);
        when(eligibilityWorkflowHandlerMock.projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser)).thenReturn(true);
        when(golWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);
        when(projectWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);
        when(spendProfileWorkflowHandlerMock.projectCreated(savedProject, leadPartnerProjectUser)).thenReturn(true);

        when(projectMapperMock.mapToResource(savedProject)).thenReturn(newProjectResource);

        Map<Long, FundingDecision> fundingDecisions = new HashMap<>();
        fundingDecisions.put(applicationId, FundingDecision.FUNDED);
        ServiceResult<Void> project = service.createProjectsFromFundingDecisions(fundingDecisions);
        assertTrue(project.isSuccess());

        verify(costCategoryTypeStrategyMock).getOrCreateCostCategoryTypeForSpendProfile(savedProject.getId(), organisation.getId());
        verify(financeChecksGeneratorMock).createMvpFinanceChecksFigures(savedProject, organisation, costCategoryTypeForOrganisation);
        verify(financeChecksGeneratorMock).createFinanceChecksFigures(savedProject, organisation);
        verify(projectDetailsWorkflowHandlerMock).projectCreated(savedProject, leadPartnerProjectUser);
        verify(viabilityWorkflowHandlerMock).projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser);
        verify(eligibilityWorkflowHandlerMock).projectCreated(savedProjectPartnerOrganisation, leadPartnerProjectUser);
        verify(golWorkflowHandlerMock).projectCreated(savedProject, leadPartnerProjectUser);
        verify(projectWorkflowHandlerMock).projectCreated(savedProject, leadPartnerProjectUser);
        verify(projectMapperMock).mapToResource(savedProject);
    }

    @Test
    public void testCreateProjectsFromFundingDecisionsSaveFails() throws Exception {

        Project newProjectExpectations = createProjectExpectationsFromOriginalApplication();
        when(projectRepositoryMock.save(newProjectExpectations)).thenThrow(new DataIntegrityViolationException("dummy constraint violation"));

        Map<Long, FundingDecision> fundingDecisions = new HashMap<>();
        fundingDecisions.put(applicationId, FundingDecision.FUNDED);
        try {
            service.createProjectsFromFundingDecisions(fundingDecisions);
            assertThat("Service failed to throw expected exception.", false);
        } catch (Exception e) {
            assertEquals(e.getCause().getCause().getCause().getMessage(),"dummy constraint violation");
        }
    }

    private Project createProjectExpectationsFromOriginalApplication() {

        assertFalse(application.getProcessRoles().isEmpty());

        return createLambdaMatcher(project -> {
            assertEquals(application.getName(), project.getName());
            assertEquals(application.getDurationInMonths(), project.getDurationInMonths());
            assertEquals(application.getStartDate(), project.getTargetStartDate());
            assertFalse(project.getProjectUsers().isEmpty());
            assertNull(project.getAddress());

            List<ProcessRole> collaborativeRoles = simpleFilter(application.getProcessRoles(), ProcessRole::isLeadApplicantOrCollaborator);

            assertEquals(collaborativeRoles.size(), project.getProjectUsers().size());

            collaborativeRoles.forEach(processRole -> {

                List<ProjectUser> matchingProjectUser = simpleFilter(project.getProjectUsers(), projectUser ->
                        projectUser.getOrganisation().getId().equals(processRole.getOrganisationId()) &&
                                projectUser.getUser().equals(processRole.getUser()));

                assertEquals(1, matchingProjectUser.size());
                assertEquals(Role.PARTNER.getName(), matchingProjectUser.get(0).getRole().getName());
                assertEquals(project, matchingProjectUser.get(0).getProcess());
            });

            List<PartnerOrganisation> partnerOrganisations = project.getPartnerOrganisations();
            assertEquals(1, partnerOrganisations.size());

            PartnerOrganisation partnerOrganisation = partnerOrganisations.get(0);
            assertEquals(project, partnerOrganisation.getProject());
            assertEquals(organisation, partnerOrganisation.getOrganisation());
            assertEquals("UB7 8QF", partnerOrganisation.getPostcode());
            assertTrue(partnerOrganisation.isLeadOrganisation());
        });
    }

    @Override
    protected ProjectService supplyServiceUnderTest() {
        return new ProjectServiceImpl();
    }
}
