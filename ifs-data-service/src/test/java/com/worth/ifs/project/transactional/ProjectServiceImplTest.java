package com.worth.ifs.project.transactional;

import com.worth.ifs.BaseServiceUnitTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.project.domain.Project;
import com.worth.ifs.project.domain.ProjectUser;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.user.builder.ProcessRoleBuilder;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.ProcessRole;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.domain.User;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static com.worth.ifs.LambdaMatcher.createLambdaMatcher;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.commons.error.CommonErrors.notFoundError;
import static com.worth.ifs.commons.error.CommonFailureKeys.*;
import static com.worth.ifs.project.builder.ProjectBuilder.newProject;
import static com.worth.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static com.worth.ifs.project.builder.ProjectUserBuilder.newProjectUser;
import static com.worth.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.builder.UserBuilder.newUser;
import static com.worth.ifs.user.resource.UserRoleType.*;
import static com.worth.ifs.util.CollectionFunctions.simpleFilter;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProjectServiceImplTest extends BaseServiceUnitTest<ProjectService> {

    @Test
    public void testCreateProjectFromApplication() {

        Application application = newApplication().
                withId(123L).
                withName("My Application").
                withDurationInMonths(5L).
                withStartDate(LocalDate.of(2017, 3, 2)).
                build();

        ProjectResource newProjectResource = newProjectResource().build();

        User leadApplicant = newUser().build();
        User collaborator1 = newUser().build();
        User collaborator2 = newUser().build();

        Organisation leadOrganisation = newOrganisation().build();
        Organisation collaboratorOrganisation1 = newOrganisation().build();
        Organisation collaboratorOrganisation2 = newOrganisation().build();

        ProcessRoleBuilder processRoleBuilder = newProcessRole().
                withApplication(application);

        @SuppressWarnings("unused")
        ProcessRole leadApplicantProcessRole = processRoleBuilder.
                withRole(LEADAPPLICANT).
                withUser(leadApplicant).
                withOrganisation(collaboratorOrganisation1).
                build();

        @SuppressWarnings("unused")
        ProcessRole collaborator1ProcessRole = processRoleBuilder.
                withRole(COLLABORATOR).
                withUser(collaborator1).
                withOrganisation(leadOrganisation).
                build();

        @SuppressWarnings("unused")
        ProcessRole collaborator2ProcessRole = processRoleBuilder.
                withRole(COLLABORATOR).
                withUser(collaborator2).
                withOrganisation(collaboratorOrganisation2).
                build();

        Role partnerRole = newRole().withType(PARTNER).build();

        when(applicationRepositoryMock.findOne(123L)).thenReturn(application);

        Project savedProject = newProject().build();

        when(roleRepositoryMock.findByName(PARTNER.getName())).thenReturn(singletonList(partnerRole));

        Project newProjectExpectations = createProjectExpectationsFromOriginalApplication(application);
        when(projectRepositoryMock.save(newProjectExpectations)).thenReturn(savedProject);

        when(projectMapperMock.mapToResource(savedProject)).thenReturn(newProjectResource);

        ServiceResult<ProjectResource> project = service.createProjectFromApplication(123L);
        assertTrue(project.isSuccess());
        assertEquals(newProjectResource, project.getSuccessObject());
    }

    @Test
    public void testUpdateProjectStartDate() {

        LocalDate now = LocalDate.now();
        LocalDate validDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1).plusMonths(1);

        Project existingProject = newProject().build();
        assertNull(existingProject.getTargetStartDate());

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, validDate);
        assertTrue(updateResult.isSuccess());

        verify(projectRepositoryMock).findOne(123L);
        assertEquals(validDate, existingProject.getTargetStartDate());
    }

    @Test
    public void testUpdateProjectStartDateButProjectDoesntExist() {

        LocalDate now = LocalDate.now();
        LocalDate validDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1).plusMonths(1);

        when(projectRepositoryMock.findOne(123L)).thenReturn(null);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, validDate);
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(notFoundError(Project.class, 123L)));
    }

    @Test
    public void testUpdateProjectStartDateButStartDateDoesntBeginOnFirstDayOfMonth() {

        LocalDate now = LocalDate.now();
        LocalDate dateNotOnFirstDayOfMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 2).plusMonths(1);

        Project existingProject = newProject().build();
        assertNull(existingProject.getTargetStartDate());

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, dateNotOnFirstDayOfMonth);
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_DATE_MUST_START_ON_FIRST_DAY_OF_MONTH));

        verify(projectRepositoryMock, never()).findOne(123L);
        assertNull(existingProject.getTargetStartDate());
    }

    @Test
    public void testUpdateProjectStartDateButStartDateNotInFuture() {

        LocalDate now = LocalDate.now();
        LocalDate pastDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1).minusMonths(1);

        Project existingProject = newProject().build();
        assertNull(existingProject.getTargetStartDate());

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);

        ServiceResult<Void> updateResult = service.updateProjectStartDate(123L, pastDate);
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_DATE_MUST_BE_IN_THE_FUTURE));

        verify(projectRepositoryMock, never()).findOne(123L);
        assertNull(existingProject.getTargetStartDate());
    }

    @Test
    public void testUpdateFinanceContact() {

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withid(7L).build();

        Role partnerRole = newRole().withType(PARTNER).build();
        newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(partnerRole).build();

        Role financeContactRole = newRole().withType(FINANCE_CONTACT).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);
        when(roleRepositoryMock.findByName(FINANCE_CONTACT.getName())).thenReturn(singletonList(financeContactRole));

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, 7L);

        assertTrue(updateResult.isSuccess());

        List<ProjectUser> foundFinanceContacts = simpleFilter(project.getProjectUsers(), projectUser ->
                projectUser.getOrganisation().equals(organisation) &&
                        projectUser.getUser().equals(user) &&
                        projectUser.getProject().equals(project) &&
                        projectUser.getRole().equals(financeContactRole));

        assertEquals(1, foundFinanceContacts.size());
    }

    @Test
    public void testUpdateFinanceContactWithExistingFinanceContactChosenForSameOrganisation() {

        Role partnerRole = newRole().withType(PARTNER).build();
        Role financeContactRole = newRole().withType(FINANCE_CONTACT).build();

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();

        User newFinanceContactUser = newUser().withid(7L).build();
        newProjectUser().withOrganisation(organisation).withUser(newFinanceContactUser).withProject(project).withRole(partnerRole).build();

        User existingFinanceContactUser = newUser().withid(9999L).build();
        newProjectUser().withOrganisation(organisation).withUser(existingFinanceContactUser).withProject(project).withRole(partnerRole).build();
        newProjectUser().withOrganisation(organisation).withUser(existingFinanceContactUser).withProject(project).withRole(financeContactRole).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);
        when(roleRepositoryMock.findByName(FINANCE_CONTACT.getName())).thenReturn(singletonList(financeContactRole));

        List<ProjectUser> existingFinanceContactForOrganisation = simpleFilter(project.getProjectUsers(), projectUser ->
                projectUser.getOrganisation().equals(organisation) &&
                        projectUser.getProject().equals(project) &&
                        projectUser.getRole().equals(financeContactRole));

        assertEquals(1, existingFinanceContactForOrganisation.size());

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, 7L);

        assertTrue(updateResult.isSuccess());

        List<ProjectUser> foundFinanceContacts = simpleFilter(project.getProjectUsers(), projectUser ->
                projectUser.getOrganisation().equals(organisation) &&
                        projectUser.getUser().equals(newFinanceContactUser) &&
                        projectUser.getProject().equals(project) &&
                        projectUser.getRole().equals(financeContactRole));

        assertEquals(1, foundFinanceContacts.size());
    }

    @Test
    public void testUpdateFinanceContactButUserIsNotExistingPartner() {

        Role projectManagerRole = newRole().withType(PROJECT_MANAGER).build();

        Project project = newProject().withId(123L).build();
        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withid(7L).build();
        newProjectUser().withOrganisation(organisation).withUser(user).withProject(project).withRole(projectManagerRole).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, 7L);

        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_PARTNER_ON_THE_PROJECT_FOR_THE_ORGANISATION));

        verify(processRoleRepositoryMock, never()).save(isA(ProcessRole.class));
    }

    @Test
    public void testUpdateFinanceContactWhenNotPresentOnTheProject() {

        long userIdForUserNotOnProject = 6L;

        Role partnerRole = newRole().withType(PARTNER).build();

        Project existingProject = newProject().withId(123L).build();
        Project anotherProject = newProject().withId(9999L).build();

        when(projectRepositoryMock.findOne(123L)).thenReturn(existingProject);
        
        Organisation organisation = newOrganisation().withId(5L).build();
        User user = newUser().withid(7L).build();
        newProjectUser().withOrganisation(organisation).withUser(user).withProject(anotherProject).withRole(partnerRole).build();

        ServiceResult<Void> updateResult = service.updateFinanceContact(123L, 5L, userIdForUserNotOnProject);
        
        assertTrue(updateResult.isFailure());
        assertTrue(updateResult.getFailure().is(PROJECT_SETUP_FINANCE_CONTACT_MUST_BE_A_USER_ON_THE_PROJECT_FOR_THE_ORGANISATION));
    }

    private Project createProjectExpectationsFromOriginalApplication(Application application) {

        assertFalse(application.getProcessRoles().isEmpty());

        return createLambdaMatcher(project -> {
            assertEquals(application.getId(), project.getId());
            assertEquals(application.getName(), project.getName());
            assertEquals(application.getDurationInMonths(), project.getDurationInMonths());
            assertEquals(application.getStartDate(), project.getTargetStartDate());
            assertNull(project.getProjectManager());
            assertNull(project.getAddress());

            List<ProcessRole> collaborativeRoles = simpleFilter(application.getProcessRoles(), ProcessRole::isLeadApplicantOrCollaborator);

            assertEquals(collaborativeRoles.size(), project.getProjectUsers().size());

            collaborativeRoles.forEach(processRole -> {

                List<ProjectUser> matchingProjectUser = simpleFilter(project.getProjectUsers(), projectUser ->
                        projectUser.getOrganisation().equals(processRole.getOrganisation()) &&
                           projectUser.getUser().equals(processRole.getUser()));

                assertEquals(1, matchingProjectUser.size());
                assertEquals(PARTNER.getName(), matchingProjectUser.get(0).getRole().getName());
                assertEquals(project, matchingProjectUser.get(0).getProject());
            });
        });
    }

    private ProcessRole createProcessRoleExpectations(ProcessRole expectedProcessRole) {

        return createLambdaMatcher(processRole -> {
            assertEquals(expectedProcessRole.getApplication().getId(), processRole.getApplication().getId());
            assertEquals(expectedProcessRole.getOrganisation().getId(), processRole.getOrganisation().getId());
            assertEquals(expectedProcessRole.getRole().getId(), processRole.getRole().getId());
            assertEquals(expectedProcessRole.getUser().getId(), processRole.getUser().getId());
        });
    }

    @Override
    protected ProjectService supplyServiceUnderTest() {
        return new ProjectServiceImpl();
    }
}
