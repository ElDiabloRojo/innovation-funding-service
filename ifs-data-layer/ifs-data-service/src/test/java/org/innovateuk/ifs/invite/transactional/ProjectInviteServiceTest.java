package org.innovateuk.ifs.invite.transactional;

import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.domain.ProjectInvite;
import org.innovateuk.ifs.invite.mapper.ProjectInviteMapper;
import org.innovateuk.ifs.invite.repository.ProjectInviteRepository;
import org.innovateuk.ifs.invite.resource.ProjectInviteResource;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.core.transactional.ProjectService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static junit.framework.TestCase.assertEquals;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_INVITE_INVALID;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.builder.ProjectInviteBuilder.newProjectInvite;
import static org.innovateuk.ifs.invite.builder.ProjectInviteResourceBuilder.newProjectInviteResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mapstruct.factory.Mappers.getMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectInviteServiceTest extends BaseUnitTestMocksTest {

    @InjectMocks
    private ProjectInviteService projectInviteService = new ProjectInviteServiceImpl();

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private ProjectInviteRepository projectInviteRepositoryMock;

    @Mock
    private ProjectService projectServiceMock;

    @Mock
    private ProjectInviteMapper projectInviteMapperMock;

    @Mock
    private OrganisationRepository organisationRepositoryMock;

    @Mock
    private ProjectUserRepository projectUserRepositoryMock;

    @Test
    public void acceptProjectInvite_success() throws Exception {
        Project project = newProject().build();
        Organisation organisation = newOrganisation().build();
        User user = newUser().withEmailAddress("email@example.com").build();
        ProjectUser projectUser = newProjectUser().build();
        ProjectInvite projectInvite = newProjectInvite().withEmail(user.getEmail()).withHash("hash").withProject(project).withOrganisation(organisation).build();
        when(projectInviteRepositoryMock.getByHash(projectInvite.getHash())).thenReturn(projectInvite);
        when(userRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));
        when(projectInviteRepositoryMock.save(projectInvite)).thenReturn(projectInvite);
        when(projectServiceMock.addPartner(projectInvite.getTarget().getId(), user.getId(), projectInvite.getOrganisation().getId())).thenReturn(serviceSuccess(projectUser));
        ServiceResult<Void> result = projectInviteService.acceptProjectInvite(projectInvite.getHash(), user.getId());
        assertTrue(result.isSuccess());
    }


    @Test
    public void acceptProjectInvite_hashDoesNotExist() throws Exception {
        String hash = "hash";
        User user = newUser().withEmailAddress("email@example.com").build();
        when(projectInviteRepositoryMock.getByHash(hash)).thenReturn(null);
        when(userRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));
        ServiceResult<Void> result = projectInviteService.acceptProjectInvite(hash, user.getId());
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(ProjectInvite.class, hash)));
    }

    @Test
    public void acceptProjectInvite_userDoesNotExist() throws Exception {
        Long userId = 1L;
        ProjectInvite projectInvite = newProjectInvite().withEmail("email@example.com").withHash("hash").build();
        when(projectInviteRepositoryMock.getByHash(projectInvite.getHash())).thenReturn(projectInvite);
        when(userRepositoryMock.findById(userId)).thenReturn(Optional.empty());
        ServiceResult<Void> result = projectInviteService.acceptProjectInvite(projectInvite.getHash(), userId);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(User.class, userId)));
    }


    @Test
    public void checkUserExistsForInvite_success() throws Exception {
        User user = newUser().withEmailAddress("email@example.com").build();
        ProjectInvite projectInvite = newProjectInvite().withEmail(user.getEmail()).withHash("hash").build();
        when(projectInviteRepositoryMock.getByHash(projectInvite.getHash())).thenReturn(projectInvite);
        when(userRepositoryMock.findByEmail(projectInvite.getEmail())).thenReturn(of(user));
        ServiceResult<Boolean> result = projectInviteService.checkUserExistsForInvite(projectInvite.getHash());
        assertTrue(result.isSuccess());
        assertTrue(result.getSuccess());
    }

    @Test
    public void checkUserExistsForInvite_hashHashNotFound() throws Exception {
        String hash = "hash";
        when(projectInviteRepositoryMock.getByHash(hash)).thenReturn(null);
        ServiceResult<Boolean> result = projectInviteService.checkUserExistsForInvite(hash);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(ProjectInvite.class, hash)));
    }

    @Test
    public void checkUserExistsForInvite_hashNoUserFound() throws Exception {
        ProjectInvite projectInvite = newProjectInvite().withEmail("email@example.com").withHash("hash").build();
        when(projectInviteRepositoryMock.getByHash(projectInvite.getHash())).thenReturn(projectInvite);
        when(userRepositoryMock.findByEmail(projectInvite.getEmail())).thenReturn(empty());
        ServiceResult<Boolean> result = projectInviteService.checkUserExistsForInvite(projectInvite.getHash());
        assertTrue(result.isSuccess());
        assertFalse(result.getSuccess());
    }


    @Test
    public void saveProjectInvite_success() throws Exception {
        Organisation organisation = newOrganisation().build();
        when(organisationRepositoryMock.findDistinctByUsers(any(User.class))).thenReturn(singletonList(organisation));

        Project project = newProject().withName("project name").build();
        User user = newUser().
                withEmailAddress("email@example.com").
                build();
        ProjectInvite projectInvite = newProjectInvite().
                withProject(project).
                withOrganisation(organisation).
                withName("project name").
                withEmail(user.getEmail()).
                build();
        ProjectInviteResource projectInviteResource = getMapper(ProjectInviteMapper.class).mapToResource(projectInvite);
        when(userRepositoryMock.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(projectInviteMapperMock.mapToDomain(projectInviteResource)).thenReturn(projectInvite);
        ServiceResult<Void> result = projectInviteService.saveProjectInvite(projectInviteResource);
        assertTrue(result.isSuccess());
    }

    @Test
    public void saveProjectInvite_validationFailure() throws Exception {
        Organisation organisation = newOrganisation().build();
        Project project = newProject().withName("project name").build();
        User user = newUser().withEmailAddress("email@example.com").build();

        {
            ProjectInvite projectInviteNoName = newProjectInvite().withProject(project).withOrganisation(organisation).withEmail(user.getEmail()).build();
            ProjectInviteResource projectInviteNoNameResource = getMapper(ProjectInviteMapper.class).mapToResource(projectInviteNoName);
            when(projectInviteMapperMock.mapToDomain(projectInviteNoNameResource)).thenReturn(projectInviteNoName);
            ServiceResult<Void> result = projectInviteService.saveProjectInvite(projectInviteNoNameResource);
            assertTrue(result.isFailure());
            assertTrue(result.getFailure().is(PROJECT_INVITE_INVALID));
        }

        {
            ProjectInvite projectInviteNoEmail = newProjectInvite().withProject(project).withOrganisation(organisation).withName("project name").build();
            ProjectInviteResource projectInviteNoEmailResource = getMapper(ProjectInviteMapper.class).mapToResource(projectInviteNoEmail);
            when(projectInviteMapperMock.mapToDomain(projectInviteNoEmailResource)).thenReturn(projectInviteNoEmail);
            ServiceResult<Void> result = projectInviteService.saveProjectInvite(projectInviteNoEmailResource);
            assertTrue(result.isFailure());
            assertTrue(result.getFailure().is(PROJECT_INVITE_INVALID));
        }

        {
            ProjectInvite projectInviteNoOrganisation = newProjectInvite().withProject(project).withName("project name").withEmail(user.getEmail()).build();
            ProjectInviteResource projectInviteNoOrganisationResource = getMapper(ProjectInviteMapper.class).mapToResource(projectInviteNoOrganisation);
            when(projectInviteMapperMock.mapToDomain(projectInviteNoOrganisationResource)).thenReturn(projectInviteNoOrganisation);
            ServiceResult<Void> result = projectInviteService.saveProjectInvite(projectInviteNoOrganisationResource);
            assertTrue(result.isFailure());
            assertTrue(result.getFailure().is(PROJECT_INVITE_INVALID));
        }

        {
            ProjectInvite projectInviteNoProject = newProjectInvite().withOrganisation(organisation).withName("project name").withEmail(user.getEmail()).build();
            ProjectInviteResource projectInviteNoProjectResource = getMapper(ProjectInviteMapper.class).mapToResource(projectInviteNoProject);
            when(projectInviteMapperMock.mapToDomain(projectInviteNoProjectResource)).thenReturn(projectInviteNoProject);
            ServiceResult<Void> result = projectInviteService.saveProjectInvite(projectInviteNoProjectResource);
            assertTrue(result.isFailure());
            assertTrue(result.getFailure().is(PROJECT_INVITE_INVALID));
        }
    }

    @Test
    public void getInvitesByProject() throws Exception {

        ProjectResource projectResource = newProjectResource()
                .build();

        Organisation organisation = newOrganisation()
                .build();

        ProjectInviteResource projectInviteResource = newProjectInviteResource()
                .withProject(projectResource.getId())
                .withLeadOrganisation(organisation.getId())
                .build();

        ProjectInvite projectInvite = newProjectInvite()
                .build();

        when(projectInviteRepositoryMock.findByProjectId(projectResource.getId())).thenReturn(singletonList(projectInvite));
        when(projectInviteMapperMock.mapToResource(projectInvite)).thenReturn(projectInviteResource);
        when(organisationRepositoryMock.findById(projectInviteResource.getLeadOrganisationId())).thenReturn(Optional.of(organisation));
        when(projectServiceMock.getProjectById(projectResource.getId())).thenReturn(serviceSuccess(projectResource));

        ServiceResult<List<ProjectInviteResource>> invitesByProject = projectInviteService.getInvitesByProject(projectResource.getId());
        assertTrue(invitesByProject.isSuccess());
        assertEquals(singletonList(projectInviteResource), invitesByProject.getSuccess());
    }
}
