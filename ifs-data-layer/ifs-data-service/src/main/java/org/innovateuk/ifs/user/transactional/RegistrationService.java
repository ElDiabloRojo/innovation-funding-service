package org.innovateuk.ifs.user.transactional;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.registration.resource.InternalUserRegistrationResource;
import org.innovateuk.ifs.registration.resource.MonitoringOfficerRegistrationResource;
import org.innovateuk.ifs.registration.resource.StakeholderRegistrationResource;
import org.innovateuk.ifs.registration.resource.UserRegistrationResource;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Transactional service around User operations
 */
public interface RegistrationService {

    @PreAuthorize("hasPermission(#user, 'CREATE')")
    ServiceResult<UserResource> createUser(@P("user") UserRegistrationResource userResource);

    @PreAuthorize("hasPermission(#user, 'CREATE')")
    ServiceResult<UserResource> createUser(UserResource user);

    @PreAuthorize("hasPermission(#user, 'CREATE')")
    ServiceResult<UserResource> createUserWithCompetitionContext(long competitionId, long organisationId, @P("user") UserResource userResource);

    @PreAuthorize("hasPermission(#user, 'VERIFY')")
    ServiceResult<Void> resendUserVerificationEmail(@P("user") final UserResource user);

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserResource', 'ACTIVATE')")
    ServiceResult<Void> activateUser(long userId);

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserResource', 'DEACTIVATE')")
    ServiceResult<Void> deactivateUser(long userId);

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserResource', 'ACTIVATE')")
    ServiceResult<Void> activateApplicantAndSendDiversitySurvey(long userId);

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserResource', 'ACTIVATE')")
    ServiceResult<Void> activateAssessorAndSendDiversitySurvey(long userId);

    @PreAuthorize("hasAuthority('system_registrar')")
    @SecuredBySpring(value = "CREATE", securedType = InternalUserRegistrationResource.class, description = "A System Registration User can create new internal Users on behalf of non-logged in users with invite hash")
    ServiceResult<Void> createInternalUser(String inviteHash, InternalUserRegistrationResource userRegistrationResource);

    @PreAuthorize("hasPermission(#user, 'CREATE')")
    ServiceResult<Void> createMonitoringOfficer(String hash, MonitoringOfficerRegistrationResource userRegistrationResource);

    @PreAuthorize("hasPermission(#userToEdit, 'EDIT_INTERNAL_USER')")
    @SecuredBySpring(value = "CREATE", securedType = StakeholderRegistrationResource.class, description = "A System Registration User can create new Stakeholders on behalf of non-logged in users with invite hash")
    ServiceResult<Void> editInternalUser(UserResource userToEdit, Role userRoleType);

    @PreAuthorize("hasAuthority('system_registrar')")
    @SecuredBySpring(value = "CREATE", securedType = StakeholderRegistrationResource.class, description = "A System Registration User can create new Stakeholders on behalf of non-logged in users with invite hash")
    ServiceResult<Void> createStakeholder(String hash, StakeholderRegistrationResource stakeholderRegistrationResource);
}