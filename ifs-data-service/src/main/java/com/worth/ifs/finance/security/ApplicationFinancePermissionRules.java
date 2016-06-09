package com.worth.ifs.finance.security;

import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.repository.ApplicationRepository;
import com.worth.ifs.competition.resource.CompetitionResource;
import com.worth.ifs.finance.resource.ApplicationFinanceResource;
import com.worth.ifs.security.PermissionRule;
import com.worth.ifs.security.PermissionRules;
import com.worth.ifs.security.SecurityRuleUtil;
import com.worth.ifs.user.repository.ProcessRoleRepository;
import com.worth.ifs.user.repository.RoleRepository;
import com.worth.ifs.user.resource.UserResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.worth.ifs.security.SecurityRuleUtil.checkRole;
import static com.worth.ifs.user.resource.UserRoleType.ASSESSOR;
import static com.worth.ifs.user.resource.UserRoleType.COLLABORATOR;
import static com.worth.ifs.user.resource.UserRoleType.LEADAPPLICANT;

/**
 * ApplicationFinancePermissionRules are applying rules for seeing / updating the application
 */

@Component
@PermissionRules
public class ApplicationFinancePermissionRules {

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @PermissionRule(value = "READ", description = "The consortium can see the application finances of their own organisation")
    public boolean consortiumCanSeeTheApplicationFinancesForTheirOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return isAConsortiumMemberOnApplication(applicationFinanceResource, user);
    }

    @PermissionRule(value = "READ", description = "An assessor can see the application finances for organisations in the applications they assess")
    public boolean assessorCanSeeTheApplicationFinanceForOrganisationsInApplicationsTheyAssess(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        final boolean isAssessor = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), ASSESSOR, roleRepository, processRoleRepository);
        return isAssessor;
    }

    @PermissionRule(value = "READ", description = "A comp admin can see application finances for organisations")
    public boolean compAdminCanSeeApplicationFinancesForOrganisations(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return SecurityRuleUtil.isCompAdmin(user);
    }

    @PermissionRule(value = "ADD_COST", description = "The consortium can add a cost to the application finances of their own organisation")
    public boolean consortiumCanAddACostToApplicationFinanceForTheirOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return applicationBelongsToOpenCompetition(applicationFinanceResource.getApplication()) && isAConsortiumMemberOnApplication(applicationFinanceResource, user);
    }

    @PermissionRule(value = "UPDATE_COST", description = "The consortium can update a cost to the application finances of their own organisation")
    public boolean consortiumCanUpdateACostToApplicationFinanceForTheirOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return applicationBelongsToOpenCompetition(applicationFinanceResource.getApplication()) && isAConsortiumMemberOnApplication(applicationFinanceResource, user);
    }

    @PermissionRule(value = "READ_FILE_ENTRY", description = "The consortium can get file entry resource for finance section of a collaborator")
    public boolean consortiumMemberCanGetFileEntryResourceByFinanceIdOfACollaborator(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return isAConsortiumMemberOnApplication(applicationFinanceResource.getApplication(), user);
    }

    @PermissionRule(value = "READ_FILE_ENTRY", description = "A comp admin can get file entry resource for finance section of a collaborator")
    public boolean compAdminCanGetFileEntryResourceForFinanceIdOfACollaborator(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return SecurityRuleUtil.isCompAdmin(user);
    }


    @PermissionRule(value = "CREATE_FILE_ENTRY", description = "A consortium member can create a file entry for the finance section for their organisation")
    public boolean consortiumMemberCanCreateAFileForTheApplicationFinanceForTheirOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return applicationBelongsToOpenCompetition(applicationFinanceResource.getApplication()) && isAConsortiumMemberOnApplicationAndOrganisation(applicationFinanceResource, user);
    }


    @PermissionRule(value = "UPDATE_FILE_ENTRY", description = "A consortium member can update a file entry for the finance section for their organisation")
    public boolean consortiumMemberCanUpdateAFileForTheApplicationFinanceForTheirOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return applicationBelongsToOpenCompetition(applicationFinanceResource.getApplication()) && isAConsortiumMemberOnApplicationAndOrganisation(applicationFinanceResource, user);
    }

    @PermissionRule(value = "DELETE_FILE_ENTRY", description = "A consortium member can delete a file entry for the finance section for their organisation")
    public boolean consortiumMemberCanDeleteAFileForTheApplicationFinanceForTheirOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        return applicationBelongsToOpenCompetition(applicationFinanceResource.getApplication()) && isAConsortiumMemberOnApplicationAndOrganisation(applicationFinanceResource, user);
    }

    private boolean isAConsortiumMemberOnApplicationAndOrganisation(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        final boolean isLeadApplicant = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), LEADAPPLICANT, roleRepository, processRoleRepository);
        final boolean isCollaborator = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), COLLABORATOR, roleRepository, processRoleRepository);
        return isLeadApplicant || isCollaborator;
    }

    private boolean isAConsortiumMemberOnApplication(final ApplicationFinanceResource applicationFinanceResource, final UserResource user) {
        final boolean isLeadApplicant = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), LEADAPPLICANT, roleRepository, processRoleRepository);
        final boolean isCollaborator = checkRole(user, applicationFinanceResource.getApplication(), applicationFinanceResource.getOrganisation(), COLLABORATOR, roleRepository, processRoleRepository);

        return isLeadApplicant || isCollaborator;
    }

    private boolean isAConsortiumMemberOnApplication(final Long applicationId, final UserResource user) {
        final boolean isLeadApplicant = checkRole(user, applicationId, LEADAPPLICANT, processRoleRepository);
        final boolean isCollaborator = checkRole(user, applicationId, COLLABORATOR, processRoleRepository);

        return isLeadApplicant || isCollaborator;
    }

    boolean applicationBelongsToOpenCompetition(final Long applicationId){
        final Application application = applicationRepository.findOne(applicationId);
        return application.getCompetition().getCompetitionStatus().equals(CompetitionResource.Status.OPEN);
    }

}
