package org.innovateuk.ifs.security;

import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.assessment.repository.AssessmentRepository;
import org.innovateuk.ifs.competition.domain.InnovationLead;
import org.innovateuk.ifs.competition.repository.InnovationLeadRepository;
import org.innovateuk.ifs.interview.repository.InterviewRepository;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectProcess;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectProcessRepository;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.review.repository.ReviewRepository;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.resource.Role;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.*;

/**
 * Base class to contain useful shorthand methods for the Permission rule subclasses
 */
public abstract class BasePermissionRules extends RootPermissionRules {

    @Autowired
    protected ProjectUserRepository projectUserRepository;

    @Autowired
    protected ApplicationRepository applicationRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected OrganisationRepository organisationRepository;

    @Autowired
    protected AssessmentRepository assessmentRepository;

    @Autowired
    protected ReviewRepository reviewRepository;

    @Autowired
    protected InterviewRepository interviewRepository;

    @Autowired
    private InnovationLeadRepository innovationLeadRepository;

    @Autowired
    private ProjectProcessRepository projectProcessRepository;

    protected boolean isPartner(long projectId, long userId) {
        List<ProjectUser> partnerProjectUser = projectUserRepository.findByProjectIdAndUserIdAndRole(projectId, userId, PROJECT_PARTNER);
        return !partnerProjectUser.isEmpty();
    }

    protected boolean isMonitoringOfficer(long projectId, long userId) {
        List<ProjectUser> monitoringOfficerForProject = projectUserRepository.findByProjectIdAndUserIdAndRole(projectId, userId, MONITORING_OFFICER);
        return !monitoringOfficerForProject.isEmpty();
    }

    protected boolean isSpecificProjectPartnerByProjectId(long projectId, long organisationId, long userId) {
        ProjectUser partnerProjectUser = projectUserRepository.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(projectId, userId, organisationId, PROJECT_PARTNER);
        ProjectUser managerProjectUser = projectUserRepository.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(projectId, userId, organisationId, PROJECT_MANAGER);
        return partnerProjectUser != null || managerProjectUser != null;
    }

    protected boolean partnerBelongsToOrganisation(long projectId, long userId, long organisationId){
        ProjectUser partnerProjectUser = projectUserRepository.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(projectId, userId, organisationId, PROJECT_PARTNER);
        return partnerProjectUser != null;
    }

    protected boolean isLeadPartner(long projectId, long userId) {

        Project project = projectRepository.findById(projectId).get();
        ProcessRole leadApplicantProcessRole = processRoleRepository.findOneByApplicationIdAndRole(project.getApplication().getId(), Role.LEADAPPLICANT);
        Organisation leadOrganisation = organisationRepository.findById(leadApplicantProcessRole.getOrganisationId()).get();

        ProjectUser partnerProjectUser = projectUserRepository.findOneByProjectIdAndUserIdAndOrganisationIdAndRole(projectId, userId, leadOrganisation.getId(), PROJECT_PARTNER);
        return partnerProjectUser != null;
    }

    protected boolean isProjectManager(long projectId, long userId) {
        List<ProjectUser> projectManagerUsers = projectUserRepository.findByProjectIdAndUserIdAndRole(projectId, userId, PROJECT_MANAGER);
        return projectManagerUsers != null && !projectManagerUsers.isEmpty();
    }

    protected boolean isFinanceContact(long projectId, long userId) {
        return !projectUserRepository.findByProjectIdAndUserIdAndRole(projectId, userId, PROJECT_FINANCE_CONTACT).isEmpty();
    }

    protected boolean userIsInnovationLeadOnCompetition(long competitionId, long loggedInUserId) {
        List<InnovationLead> competitionParticipants = innovationLeadRepository.findInnovationsLeads(competitionId);
        return competitionParticipants.stream().anyMatch(cp -> cp.getUser().getId().equals(loggedInUserId));
    }

    protected boolean isProjectInSetup(long projectId) {
        ProjectProcess projectProcess = projectProcessRepository.findOneByTargetId(projectId);
        return ProjectState.SETUP.equals(projectProcess.getProcessState());
    }
}
