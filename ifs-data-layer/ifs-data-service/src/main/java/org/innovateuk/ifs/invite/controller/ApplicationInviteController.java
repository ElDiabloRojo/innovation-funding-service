package org.innovateuk.ifs.invite.controller;

import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.crm.transactional.CrmService;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.innovateuk.ifs.invite.transactional.AcceptApplicationInviteService;
import org.innovateuk.ifs.invite.transactional.ApplicationInviteService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * InviteController is to handle the REST calls from the web-service and contains the handling of all call involving the Invite and InviteOrganisations.
 */

@RestController
@RequestMapping("/invite")
public class ApplicationInviteController {

    @Autowired
    private ApplicationInviteService applicationInviteService;

    @Autowired
    private AcceptApplicationInviteService acceptApplicationInviteService;

    @Autowired
    private CrmService crmService;

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @PostMapping({"/createApplicationInvites", "/create-application-invites"})
    public RestResult<Void> createApplicationInvites(@RequestBody InviteOrganisationResource inviteOrganisationResource) {
        return applicationInviteService.createApplicationInvites(inviteOrganisationResource, Optional.empty()).toPostCreateResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @PostMapping({"/createApplicationInvites/{applicationId}", "/create-application-invites/{applicationId}"})
    public RestResult<Void> createApplicationInvitesForApplication(@RequestBody InviteOrganisationResource inviteOrganisationResource, @PathVariable("applicationId") long applicationId) {
        return applicationInviteService.createApplicationInvites(inviteOrganisationResource, Optional.of(applicationId)).toPostCreateResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/getInviteByHash/{hash}", "/get-invite-by-hash/{hash}"})
    public RestResult<ApplicationInviteResource> getInviteByHash(@PathVariable("hash") String hash) {
        return applicationInviteService.getInviteByHash(hash).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/getInviteOrganisationByHash/{hash}", "/get-invite-organisation-by-hash/{hash}"})
    public RestResult<InviteOrganisationResource> getInviteOrganisationByHash(@PathVariable("hash") String hash) {
        return applicationInviteService.getInviteOrganisationByHash(hash).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/getInvitesByApplicationId/{applicationId}", "/get-invites-by-application-id/{applicationId}"})
    public RestResult<List<InviteOrganisationResource>> getInvitesByApplication(@PathVariable("applicationId") Long applicationId) {
        return applicationInviteService.getInvitesByApplication(applicationId).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @PostMapping({"/saveInvites", "/save-invites"})
    public RestResult<Void> saveInvites(@RequestBody List<ApplicationInviteResource> inviteResources) {
        return applicationInviteService.saveInvites(inviteResources).toPostCreateResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @PutMapping({"/acceptInvite/{hash}/{userId}", "/accept-invite/{hash}/{userId}"})
    public RestResult<Void> acceptInvite( @PathVariable("hash") String hash, @PathVariable("userId") Long userId) {
        return acceptApplicationInviteService.acceptInvite(hash, userId, Optional.empty())
                .andOnSuccessReturn(result -> {
                    crmService.syncCrmContact(userId);
                    return result;
                })
                .toPutResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @PutMapping({"/acceptInvite/{hash}/{userId}/{organisationId}", "/accept-invite/{hash}/{userId}/{organisationId}"})
    public RestResult<Void> acceptInvite( @PathVariable("hash") String hash, @PathVariable("userId") long userId, @PathVariable("organisationId") long organisationId) {
        return acceptApplicationInviteService.acceptInvite(hash, userId, Optional.of(organisationId))
                .andOnSuccessReturn(result -> {
                    crmService.syncCrmContact(userId);
                    return result;
                })
                .toPutResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @DeleteMapping({"/removeInvite/{inviteId}", "/remove-invite/{inviteId}"})
    public RestResult<Void> removeApplicationInvite(@PathVariable("inviteId") long applicationInviteResourceId) {
        return applicationInviteService.removeApplicationInvite(applicationInviteResourceId).toDeleteResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/checkExistingUser/{inviteHash}", "/check-existing-user/{inviteHash}"})
    public RestResult<Boolean> checkExistingUser(@PathVariable("inviteHash") String inviteHash) {
        return applicationInviteService.checkUserExistsForInvite(inviteHash).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/getUser/{inviteHash}", "/get-user/{inviteHash}"})
    public RestResult<UserResource> getUser(@PathVariable("inviteHash") String inviteHash) {
        return applicationInviteService.getUserByInviteHash(inviteHash).toGetResponse();
    }
}
