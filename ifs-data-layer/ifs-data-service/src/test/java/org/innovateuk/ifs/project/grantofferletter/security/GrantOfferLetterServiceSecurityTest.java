package org.innovateuk.ifs.project.grantofferletter.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.project.core.security.ProjectLookupStrategy;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterApprovalResource;
import org.innovateuk.ifs.project.grantofferletter.transactional.GrantOfferLetterService;
import org.innovateuk.ifs.project.grantofferletter.transactional.GrantOfferLetterServiceImpl;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectCompositeId;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.resource.Role;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.EnumSet;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.COMP_ADMIN;
import static org.innovateuk.ifs.user.resource.Role.PROJECT_FINANCE;
import static org.mockito.Mockito.*;

public class GrantOfferLetterServiceSecurityTest extends BaseServiceSecurityTest<GrantOfferLetterService> {

    private static final EnumSet<Role> NON_COMP_ADMIN_ROLES = EnumSet.complementOf(EnumSet.of(COMP_ADMIN, PROJECT_FINANCE));

    private GrantOfferLetterPermissionRules projectGrantOfferPermissionRules;
    private ProjectLookupStrategy projectLookupStrategy;

    @Before
    public void lookupPermissionRules() {
        projectGrantOfferPermissionRules = getMockPermissionRulesBean(GrantOfferLetterPermissionRules.class);
        projectLookupStrategy = getMockPermissionEntityLookupStrategiesBean(ProjectLookupStrategy.class);
    }

    @Test
    public void testSignedGetGrantOfferLetterFileEntryDetails() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getSignedGrantOfferLetterFileEntryDetails(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).supportUsersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).innovationLeadUsersCanViewGrantOfferLetter(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).stakeholdersCanViewGrantOfferLetter(project,
                    getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }

    @Test
    public void testGetGrantOfferLetterFileEntryDetails() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getGrantOfferLetterFileEntryDetails(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).supportUsersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).innovationLeadUsersCanViewGrantOfferLetter(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).stakeholdersCanViewGrantOfferLetter(project,
                    getLoggedInUser());

            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }

    @Test
    public void testGetAdditionalContractFileEntryDetails() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getAdditionalContractFileEntryDetails(projectId), () -> {

            verify(projectGrantOfferPermissionRules).partnersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).supportUsersCanViewGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).innovationLeadUsersCanViewGrantOfferLetter(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).stakeholdersCanViewGrantOfferLetter(project,
                    getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testCreateSignedGrantOfferLetterFileEntry() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.createSignedGrantOfferLetterFileEntry(projectId, null, null), () -> {
            verify(projectGrantOfferPermissionRules).leadPartnerCanUploadGrantOfferLetter(project, getLoggedInUser());
            verify(projectGrantOfferPermissionRules).projectManagerCanUploadGrantOfferLetter(project, getLoggedInUser
                    ());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testGetGrantOfferLetterFileEntryContents() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getGrantOfferLetterFileAndContents(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanDownloadGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());

            verify(projectGrantOfferPermissionRules).supportUsersCanDownloadGrantOfferLetter(project, getLoggedInUser
                    ());

            verify(projectGrantOfferPermissionRules).innovationLeadUsersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).stakeholdersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }

    @Test
    public void testGetSignedGrantOfferLetterFileEntryContents() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getSignedGrantOfferLetterFileAndContents(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanDownloadGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());

            verify(projectGrantOfferPermissionRules).supportUsersCanDownloadGrantOfferLetter(project, getLoggedInUser
                    ());

            verify(projectGrantOfferPermissionRules).innovationLeadUsersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).stakeholdersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });

    }

    @Test
    public void testGetAdditionalContractFileEntryContents() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.getAdditionalContractFileAndContents(projectId), () -> {
            verify(projectGrantOfferPermissionRules).partnersCanDownloadGrantOfferLetter(project, getLoggedInUser());

            verify(projectGrantOfferPermissionRules).internalUsersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());

            verify(projectGrantOfferPermissionRules).supportUsersCanDownloadGrantOfferLetter(project, getLoggedInUser
                    ());

            verify(projectGrantOfferPermissionRules).innovationLeadUsersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).stakeholdersCanDownloadGrantOfferLetter(project,
                    getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testSubmitGrantOfferLetter() {
        final ProjectCompositeId projectId = ProjectCompositeId.id(1L);
        when(projectLookupStrategy.getProjectCompositeId(projectId.id())).thenReturn(projectId);
        assertAccessDenied(() -> classUnderTest.submitGrantOfferLetter(projectId.id()), () -> {
            verify(projectGrantOfferPermissionRules).projectManagerSubmitGrantOfferLetter(projectId, getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testGenerateGrantOfferLetterDeniedIfNotCorrectGlobalRoles() {

        final Long projectId = 1L;

        FileEntryResource fileEntryResource = newFileEntryResource().build();

        EnumSet<Role> nonCompAdminRoles = EnumSet.complementOf(EnumSet.of(COMP_ADMIN, PROJECT_FINANCE));

        nonCompAdminRoles.forEach(role -> {
            setLoggedInUser(
                    newUserResource().withRolesGlobal(singletonList(role)).build());
            try {
                classUnderTest.generateGrantOfferLetter(projectId, fileEntryResource);
                Assert.fail("Should not have been able to generate GOL without the global Comp Admin role");
            } catch (AccessDeniedException e) {
                // expected behaviour
            }
        });
    }

    @Test
    public void testGenerateGrantOfferLetterIfReadyDeniedIfNotCorrectGlobalRoles() {
        final Long projectId = 1L;

        NON_COMP_ADMIN_ROLES.forEach(role -> {

            setLoggedInUser(
                    newUserResource().withRolesGlobal(singletonList(Role.getByName(role.getName()))).build());
            try {
                classUnderTest.generateGrantOfferLetterIfReady(projectId);
                Assert.fail("Should not have been able to generate GOL automatically without the global Comp Admin " +
                        "role");
            } catch (AccessDeniedException e) {
                // expected behaviour
            }
        });
    }

    @Test
    public void testDeleteSignedGrantOfferLetterFileEntry() {

        final Long projectId = 1L;

        ProjectResource project = newProjectResource().build();
        when(projectLookupStrategy.getProjectResource(projectId)).thenReturn(project);

        assertAccessDenied(() -> classUnderTest.removeSignedGrantOfferLetterFileEntry(projectId), () -> {
            verify(projectGrantOfferPermissionRules).leadPartnerCanDeleteSignedGrantOfferLetter(project,
                    getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testSendGrantOfferLetter() {
        ProjectResource project = newProjectResource().build();

        when(projectLookupStrategy.getProjectResource(123L)).thenReturn(project);
        assertAccessDenied(() -> classUnderTest.sendGrantOfferLetter(123L), () -> {
            verify(projectGrantOfferPermissionRules).internalUserCanSendGrantOfferLetter(project, getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testApproveSignedGrantOfferLetter() {
        ProjectResource project = newProjectResource().build();

        when(projectLookupStrategy.getProjectResource(123L)).thenReturn(project);
        assertAccessDenied(() -> classUnderTest.approveOrRejectSignedGrantOfferLetter(123L, new
                GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null)), () -> {
            verify(projectGrantOfferPermissionRules).internalUsersCanApproveSignedGrantOfferLetter(project,
                    getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Test
    public void testGetGrantOfferLetterState() {
        ProjectResource project = newProjectResource().build();

        when(projectLookupStrategy.getProjectResource(123L)).thenReturn(project);
        assertAccessDenied(() -> classUnderTest.getGrantOfferLetterState(123L), () -> {
            verify(projectGrantOfferPermissionRules).internalAdminUserCanViewSendGrantOfferLetterStatus(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).supportUserCanViewSendGrantOfferLetterStatus(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).innovationLeadUserCanViewSendGrantOfferLetterStatus(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).stakeholdersCanViewSendGrantOfferLetterStatus(project,
                    getLoggedInUser());
            verify(projectGrantOfferPermissionRules).externalUserCanViewSendGrantOfferLetterStatus(project,
                    getLoggedInUser());
            verifyNoMoreInteractions(projectGrantOfferPermissionRules);
        });
    }

    @Override
    protected Class<? extends GrantOfferLetterService> getClassUnderTest() {
        return GrantOfferLetterServiceImpl.class;
    }
}
