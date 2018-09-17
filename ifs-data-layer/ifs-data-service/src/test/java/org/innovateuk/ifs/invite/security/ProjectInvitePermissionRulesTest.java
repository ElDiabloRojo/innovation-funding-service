package org.innovateuk.ifs.invite.security;


import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.invite.resource.ProjectInviteResource;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectProcess;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectProcessRepository;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.invite.builder.ProjectInviteResourceBuilder.newProjectInviteResource;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectProcessBuilder.newProjectProcess;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ProjectInvitePermissionRulesTest extends BasePermissionRulesTest<ProjectInvitePermissionRules> {

    private Project project;
    private Organisation organisationOne;
    private Organisation organisationTwo;
    private UserResource userOnProjectForOrganisationOne;
    private UserResource userOnProjectForOrganisationTwo;
    private UserResource userNotOnProject;
    private ProjectUser projectUserForUserOnOgranisationOne;
    private ProjectUser projectUserForUserOnOgranisationTwo;
    private ProjectInviteResource projectInviteResourceForOrganisationOne;
    private ProjectInviteResource projectInviteResourceForOrganisationTwo;
    private ProjectProcess projectProcess;

    @Mock
    private ProjectProcessRepository projectProcessRepositoryMock;

    @Override
    protected ProjectInvitePermissionRules supplyPermissionRulesUnderTest() {
        return new ProjectInvitePermissionRules();
    }

    @Before
    public void setup() {
        project = newProject().build();
        organisationOne = newOrganisation().build();
        organisationTwo = newOrganisation().build();
        userOnProjectForOrganisationOne = newUserResource().build();
        userOnProjectForOrganisationTwo = newUserResource().build();
        userNotOnProject = newUserResource().build();

        projectUserForUserOnOgranisationOne = newProjectUser()
                .withOrganisation(organisationOne)
                .withProject(project)
                .withUser(newUser().withId(userOnProjectForOrganisationOne.getId()).build())
                .build();
        projectUserForUserOnOgranisationTwo = newProjectUser()
                .withOrganisation(organisationTwo)
                .withProject(project)
                .withUser(newUser().withId(userOnProjectForOrganisationTwo.getId()).build())
                .build();
        projectInviteResourceForOrganisationOne = newProjectInviteResource()
                .withProject(project.getId())
                .withOrganisation(organisationOne.getId())
                .build();
        projectInviteResourceForOrganisationTwo = newProjectInviteResource()
                .withProject(project.getId())
                .withOrganisation(organisationTwo.getId())
                .build();
        projectProcess = newProjectProcess()
                .withProject(project)
                .withActivityState(ProjectState.SETUP)
                .build();

        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), userOnProjectForOrganisationOne.getId(), PROJECT_PARTNER)).thenReturn(asList(projectUserForUserOnOgranisationOne));
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), userOnProjectForOrganisationTwo.getId(), PROJECT_PARTNER)).thenReturn(asList(projectUserForUserOnOgranisationTwo));
        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), userNotOnProject.getId(), PROJECT_PARTNER)).thenReturn(emptyList());
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(project.getId(), userOnProjectForOrganisationOne.getId(), organisationOne.getId(), PROJECT_PARTNER)).thenReturn(projectUserForUserOnOgranisationOne);
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(project.getId(), userOnProjectForOrganisationTwo.getId(), organisationTwo.getId(), PROJECT_PARTNER)).thenReturn(projectUserForUserOnOgranisationTwo);
        when(projectProcessRepositoryMock.findOneByTargetId(project.getId())).thenReturn(projectProcess);
    }

    @Test
    public void testPartnersOnProjectCanSaveInvite() {
        assertTrue(rules.partnersOnProjectCanSaveInvite(projectInviteResourceForOrganisationOne, userOnProjectForOrganisationOne));
        assertTrue(rules.partnersOnProjectCanSaveInvite(projectInviteResourceForOrganisationTwo, userOnProjectForOrganisationTwo));
        assertFalse(rules.partnersOnProjectCanSaveInvite(projectInviteResourceForOrganisationOne, userOnProjectForOrganisationTwo));
        assertFalse(rules.partnersOnProjectCanSaveInvite(projectInviteResourceForOrganisationOne, userNotOnProject));
    }

    @Test
    public void testPartnersOnProjectCanSendInvite() {
        assertTrue(rules.partnersOnProjectCanSendInvite(projectInviteResourceForOrganisationOne, userOnProjectForOrganisationOne));
        assertTrue(rules.partnersOnProjectCanSendInvite(projectInviteResourceForOrganisationTwo, userOnProjectForOrganisationTwo));
        assertFalse(rules.partnersOnProjectCanSendInvite(projectInviteResourceForOrganisationOne, userOnProjectForOrganisationTwo));
        assertFalse(rules.partnersOnProjectCanSendInvite(projectInviteResourceForOrganisationOne, userNotOnProject));

    }

    @Test
    public void testPartnersOnProjectCanViewInvite(){
        assertTrue(rules.partnersOnProjectCanViewInvite(projectInviteResourceForOrganisationOne, userOnProjectForOrganisationOne));
        assertTrue(rules.partnersOnProjectCanViewInvite(projectInviteResourceForOrganisationOne, userOnProjectForOrganisationTwo));
        assertFalse(rules.partnersOnProjectCanViewInvite(projectInviteResourceForOrganisationOne, userNotOnProject));
    }
}