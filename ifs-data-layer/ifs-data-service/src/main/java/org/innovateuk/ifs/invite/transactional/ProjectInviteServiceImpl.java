package org.innovateuk.ifs.invite.transactional;


import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.HibernateValidator;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.domain.ProjectInvite;
import org.innovateuk.ifs.invite.mapper.ProjectInviteMapper;
import org.innovateuk.ifs.invite.repository.InviteRepository;
import org.innovateuk.ifs.invite.repository.ProjectInviteRepository;
import org.innovateuk.ifs.invite.resource.ProjectInviteResource;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.core.transactional.ProjectService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.commons.error.CommonErrors.badRequestError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.domain.Invite.generateInviteHash;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProjectInviteServiceImpl extends InviteService<ProjectInvite> implements ProjectInviteService {

    private static final Log LOG = LogFactory.getLog(ProjectInviteServiceImpl.class);

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    @Autowired
    private ProjectInviteMapper inviteMapper;

    @Autowired
    private ProjectInviteRepository projectInviteRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    private LocalValidatorFactoryBean validator;

    public ProjectInviteServiceImpl() {
        validator = new LocalValidatorFactoryBean();
        validator.setProviderClass(HibernateValidator.class);
        validator.afterPropertiesSet();
    }

    @Override
    protected Class<ProjectInvite> getInviteClass() {
        return ProjectInvite.class;
    }

    @Override
    protected InviteRepository<ProjectInvite> getInviteRepository() {
        return projectInviteRepository;
    }

    @Override
    @Transactional
    public ServiceResult<Void> saveProjectInvite(ProjectInviteResource projectInviteResource) {

        return validateProjectInviteResource(projectInviteResource).andOnSuccess(() ->
               validateUserNotAlreadyInvited(projectInviteResource).andOnSuccess(() ->
               validateTargetUserIsValid(projectInviteResource).andOnSuccess(() -> {

            ProjectInvite projectInvite = inviteMapper.mapToDomain(projectInviteResource);
            Errors errors = new BeanPropertyBindingResult(projectInvite, projectInvite.getClass().getName());
            validator.validate(projectInvite, errors);
            if (errors.hasErrors()) {
                errors.getFieldErrors().stream().peek(e -> LOG.debug(format("Field error: %s ", e.getField())));
                return serviceFailure(badRequestError(errors.toString()));
            } else {
                projectInvite.setHash(generateInviteHash());
                projectInviteRepository.save(projectInvite);
                return serviceSuccess();
            }
        })));
    }

    private ProjectInviteResource mapInviteToInviteResource(ProjectInvite invite) {
        ProjectInviteResource inviteResource = inviteMapper.mapToResource(invite);
        Organisation organisation = organisationRepository.findOne(inviteResource.getLeadOrganisationId());
        inviteResource.setLeadOrganisation(organisation.getName());
        ProjectResource project = projectService.getProjectById(inviteResource.getProject()).getSuccess();
        inviteResource.setApplicationId(project.getApplication());
        return inviteResource;
    }

    @Override
    public ServiceResult<ProjectInviteResource> getInviteByHash(String hash) {
        return getByHash(hash).andOnSuccessReturn(this::mapInviteToInviteResource);
    }

    @Override
    public ServiceResult<List<ProjectInviteResource>> getInvitesByProject(Long projectId) {
        if(projectId == null) {
            return serviceFailure(new Error(PROJECT_INVITE_INVALID_PROJECT_ID, NOT_FOUND));
        }
        List<ProjectInvite> invites = projectInviteRepository.findByProjectId(projectId);
        List<ProjectInviteResource> inviteResources = invites.stream().map(this::mapInviteToInviteResource).collect(Collectors.toList());
        return serviceSuccess(Lists.newArrayList(inviteResources));
    }

    @Override
    @Transactional
    public ServiceResult<Void> acceptProjectInvite(String inviteHash, Long userId) {
        return find(invite(inviteHash), user(userId)).andOnSuccess((invite, user) -> {
            if(invite.getEmail().equalsIgnoreCase(user.getEmail())){
                ProjectInvite projectInvite = projectInviteRepository.save(invite.open());
                return projectService.addPartner(projectInvite.getTarget().getId(), user.getId(), projectInvite.getOrganisation().getId()).andOnSuccess(pu -> {
                    pu.setInvite(projectInvite);
                    projectUserRepository.save(pu.accept());
                    return serviceSuccess();
                });
            }
            LOG.error(format("Invited email address not the same as the users email address %s => %s ", user.getEmail(), invite.getEmail()));
            Error e = new Error("Invited email address not the same as the users email address", HttpStatus.NOT_ACCEPTABLE);
            return serviceFailure(e);
        });
    }

    @Override
    public ServiceResult<Boolean> checkUserExistsForInvite(String inviteHash) {
        return super.checkUserExistsForInvite(inviteHash);
    }

    @Override
    public ServiceResult<UserResource> getUserByInviteHash(String hash) {
        return getByHash(hash)
                .andOnSuccessReturn(i -> userRepository.findByEmail(i.getEmail()).map(userMapper::mapToResource))
                .andOnSuccess(u -> u.map(ServiceResult::serviceSuccess).orElseGet(() -> serviceFailure(notFoundError(UserResource.class))));
    }

    private ServiceResult<Void> validateProjectInviteResource(ProjectInviteResource projectInviteResource) {

        if (StringUtils.isEmpty(projectInviteResource.getEmail()) || StringUtils.isEmpty(projectInviteResource.getName())
                || projectInviteResource.getProject() == null ||projectInviteResource.getOrganisation() == null ){
            return serviceFailure(PROJECT_INVITE_INVALID);
        }
        return serviceSuccess();
    }

    private ServiceResult<Void> validateUserNotAlreadyInvited(ProjectInviteResource invite) {

        List<ProjectInvite> existingInvites = projectInviteRepository.findByProjectIdAndEmail(invite.getProject(), invite.getEmail());
        return existingInvites.isEmpty() ? serviceSuccess() : serviceFailure(PROJECT_SETUP_INVITE_TARGET_USER_ALREADY_INVITED_ON_PROJECT);
    }

    private ServiceResult<Void> validateTargetUserIsValid(ProjectInviteResource invite) {

        String targetEmail = invite.getEmail();

        Optional<User> existingUser = userRepository.findByEmail(targetEmail);

        List<Long> usersOrganisations = existingUser.map(organisationRepository::findDistinctByUsers)
                .map(organisations -> simpleMap(organisations, Organisation::getId))
                .orElse(emptyList());

        return existingUser.map(user ->
               validateUserIsInSameOrganisation(invite, usersOrganisations).andOnSuccess(() ->
               validateUserIsNotAlreadyPartnerInOrganisation(invite, user))).
               orElse(serviceSuccess());
    }

    private ServiceResult<Void> validateUserIsInSameOrganisation(ProjectInviteResource invite, List usersOrganisations) {

        if (usersOrganisations.isEmpty()) {
            serviceSuccess();
        }

        if (!usersOrganisations.contains(invite.getOrganisation())) {
            return serviceFailure(PROJECT_SETUP_INVITE_TARGET_USER_NOT_IN_CORRECT_ORGANISATION);
        }

        return serviceSuccess();
    }

    private ServiceResult<Void> validateUserIsNotAlreadyPartnerInOrganisation(ProjectInviteResource invite, User user) {

        ProjectUser existingUserEntryForOrganisation = projectUserRepository.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(invite.getProject(), invite.getOrganisation(), user.getId(), PROJECT_PARTNER);

        return existingUserEntryForOrganisation == null ? serviceSuccess() :
                serviceFailure(PROJECT_SETUP_INVITE_TARGET_USER_ALREADY_EXISTS_ON_PROJECT);
    }
}
