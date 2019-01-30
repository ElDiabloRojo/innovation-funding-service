package org.innovateuk.ifs.project.core.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.project.builder.PartnerOrganisationResourceBuilder.newPartnerOrganisationResource;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.core.domain.ProjectUserRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class PartnerOrganisationPermissionRulesTest extends BasePermissionRulesTest<PartnerOrganisationPermissionRules> {

    @Override
    protected PartnerOrganisationPermissionRules supplyPermissionRulesUnderTest() {
        return new PartnerOrganisationPermissionRules();
    }

    @Test
    public void testInternalUsersCanViewPartnerOrgs() {

        UserResource user = newUserResource().withRolesGlobal(singletonList(Role.COMP_ADMIN)).build();

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource().build();

        assertTrue(rules.internalUsersCanViewPartnerOrganisations(partnerOrg, user));
    }

    @Test
    public void testExternalUsersCannotViewPartnerOrgs() {

        UserResource user = newUserResource().build();

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource().build();

        assertFalse(rules.internalUsersCanViewPartnerOrganisations(partnerOrg, user));
    }

    @Test
    public void testPartnersCannotViewOtherPartnerOrganisations() {

        long projectId = 1L;
        long organisationId = 2L;
        UserResource user = newUserResource().build();

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource()
                .withProject(projectId)
                .withOrganisation(organisationId)
                .build();
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(projectId, user.getId(), organisationId, PROJECT_PARTNER)).thenReturn(null);

        assertFalse(rules.partnersCanViewTheirOwnPartnerOrganisation(partnerOrg, user));
    }

    @Test
    public void testPartnersCanViewTheirOwnPartnerOrganisation() {

        long projectId = 1L;
        long organisationId = 2L;
        UserResource user = newUserResource().build();

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource()
                .withProject(projectId)
                .withOrganisation(organisationId)
                .build();
        ProjectUser projectUser = newProjectUser()
                .build();
        when(projectUserRepositoryMock.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(projectId, user.getId(), organisationId, PROJECT_PARTNER)).thenReturn(projectUser);

        assertTrue(rules.partnersCanViewTheirOwnPartnerOrganisation(partnerOrg, user));
    }

    @Test
    public void testPartnersCanView() {

        UserResource user = newUserResource().withRolesGlobal(singletonList(Role.COMP_ADMIN)).build();
        Project project = newProject().build();
        ProjectUser projectUser = newProjectUser().withProject(project).build();

        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(singletonList(projectUser));

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource().withProject(project.getId()).build();

        assertTrue(rules.partnersOnProjectCanView(partnerOrg, user));
    }

    @Test
    public void testNonPartnersCannotView() {

        UserResource user = newUserResource().withRolesGlobal(singletonList(Role.COMP_ADMIN)).build();
        Project project = newProject().build();

        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(emptyList());

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource().withProject(project.getId()).build();

        assertFalse(rules.partnersOnProjectCanView(partnerOrg, user));
    }

    @Test
    public void testInternalUserCanView() {

        UserResource user = newUserResource().withRolesGlobal(singletonList(Role.COMP_ADMIN)).build();
        Project project = newProject().build();

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource().withProject(project.getId()).build();

        assertTrue(rules.internalUsersCanView(partnerOrg, user));
    }

    @Test
    public void testExternalUsersCannotView() {

        UserResource user = newUserResource().withRolesGlobal(singletonList(Role.PARTNER)).build();
        Project project = newProject().build();

        PartnerOrganisationResource partnerOrg = newPartnerOrganisationResource().withProject(project.getId()).build();

        assertFalse(rules.internalUsersCanView(partnerOrg, user));
    }
}
