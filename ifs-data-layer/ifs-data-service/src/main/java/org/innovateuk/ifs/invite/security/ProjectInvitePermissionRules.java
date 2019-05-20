package org.innovateuk.ifs.invite.security;

import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.invite.resource.ProjectUserInviteResource;
import org.innovateuk.ifs.security.BasePermissionRules;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

/**
 * Permission rules for ProjectInvite Service
 */
@Component
@PermissionRules
public class ProjectInvitePermissionRules extends BasePermissionRules {

    @PermissionRule(value = "READ_PROJECT_INVITE", description = "A user can view a project invite that they are partners on")
    public boolean partnersOnProjectCanViewInvite(final ProjectUserInviteResource invite, UserResource user) {
        return isUserMemberOfProjectTeam(invite, user);
    }

    @PermissionRule(value = "SEND_PROJECT_INVITE", description = "A user can send a project invite that they are partners on and belong to same organisation")
    public boolean partnersOnProjectCanSendInvite(final ProjectUserInviteResource invite, UserResource user) {
        return isUserPartnerOnProjectWithinSameOrganisation(invite, user) && isProjectInSetup(invite.getProject());
    }

    @PermissionRule(value = "SAVE_PROJECT_INVITE", description = "A user can save a project invite that they are partners on and belong to same organisation")
    public boolean partnersOnProjectCanSaveInvite(final ProjectUserInviteResource invite, UserResource user) {
        return isUserPartnerOnProjectWithinSameOrganisation(invite, user);
    }

    @PermissionRule(value = "DELETE_PROJECT_INVITE", description = "A user can delete a project invite that they are partners on and belong to same organisation")
    public boolean partnersOnProjectCanDeleteInvite(final ProjectUserInviteResource invite, UserResource user) {
        return isUserPartnerOnProjectWithinSameOrganisation(invite, user);
    }

    private boolean isUserPartnerOnProjectWithinSameOrganisation(final ProjectUserInviteResource invite, UserResource user) {
        if (invite.getProject() != null && invite.getOrganisation() != null) {
            return partnerBelongsToOrganisation(invite.getProject(), user.getId(), invite.getOrganisation());
        }
        return false;
    }

    private boolean isUserMemberOfProjectTeam(final ProjectUserInviteResource invite, UserResource user) {
        return isPartner(invite.getProject(), user.getId());
    }
}