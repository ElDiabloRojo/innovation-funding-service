package org.innovateuk.ifs.project.projectdetails.workflow.configuration;

import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.projectdetails.domain.ProjectDetailsProcess;
import org.innovateuk.ifs.project.projectdetails.repository.ProjectDetailsProcessRepository;
import org.innovateuk.ifs.project.resource.ProjectDetailsEvent;
import org.innovateuk.ifs.project.resource.ProjectDetailsState;
import org.innovateuk.ifs.workflow.BaseWorkflowEventHandler;
import org.innovateuk.ifs.workflow.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.project.resource.ProjectDetailsEvent.*;

/**
 * {@code ProjectDetailsWorkflowService} is the entry point for triggering the workflow.
 * Based on the Project Detail's current state the next one is tried to transition to by triggering
 * an event.
 */
@Component
public class ProjectDetailsWorkflowHandler extends BaseWorkflowEventHandler<ProjectDetailsProcess, ProjectDetailsState, ProjectDetailsEvent, Project, ProjectUser> {

    @Autowired
    @Qualifier("projectDetailsStateMachineFactory")
    private StateMachineFactory<ProjectDetailsState, ProjectDetailsEvent> stateMachineFactory;

    @Autowired
    private ProjectDetailsProcessRepository projectDetailsProcessRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectUserRepository projectUserRepository;

    public boolean projectCreated(Project project, ProjectUser originalLeadApplicantProjectUser) {
        return fireEvent(projectCreatedEvent(project, originalLeadApplicantProjectUser), ProjectDetailsState.PENDING);
    }

    public boolean projectStartDateAdded(Project project, ProjectUser projectUser) {
        return fireEvent(mandatoryValueAddedEvent(project, projectUser, PROJECT_START_DATE_ADDED), project);
    }

    public boolean projectAddressAdded(Project project, ProjectUser projectUser) {
        return fireEvent(mandatoryValueAddedEvent(project, projectUser, PROJECT_ADDRESS_ADDED), project);
    }

    public boolean projectLocationAdded(Project project, ProjectUser projectUser) {
        return fireEvent(mandatoryValueAddedEvent(project, projectUser, PROJECT_LOCATION_ADDED), project);
    }

    @Override
    protected ProjectDetailsProcess createNewProcess(Project target, ProjectUser participant) {
        return new ProjectDetailsProcess(participant, target, null);
    }

    public boolean isSubmitted(Project project) {
        ProjectDetailsProcess process = getCurrentProcess(project);
        return process != null && ProjectDetailsState.SUBMITTED.equals(process.getProcessState());
    }

    @Override
    protected ProcessRepository<ProjectDetailsProcess> getProcessRepository() {
        return projectDetailsProcessRepository;
    }

    @Override
    protected CrudRepository<Project, Long> getTargetRepository() {
        return projectRepository;
    }

    @Override
    protected CrudRepository<ProjectUser, Long> getParticipantRepository() {
        return projectUserRepository;
    }

    @Override
    protected StateMachineFactory<ProjectDetailsState, ProjectDetailsEvent> getStateMachineFactory() {
        return stateMachineFactory;
    }

    @Override
    protected ProjectDetailsProcess getOrCreateProcess(Message<ProjectDetailsEvent> message) {
        return getOrCreateProcessCommonStrategy(message);
    }

    private MessageBuilder<ProjectDetailsEvent> projectCreatedEvent(Project project, ProjectUser originalLeadApplicantProjectUser) {
        return MessageBuilder
                .withPayload(PROJECT_CREATED)
                .setHeader("target", project)
                .setHeader("participant", originalLeadApplicantProjectUser);
    }

    private MessageBuilder<ProjectDetailsEvent> mandatoryValueAddedEvent(Project project, ProjectUser projectUser,
                                                                         ProjectDetailsEvent event) {
        return MessageBuilder
                .withPayload(event)
                .setHeader("target", project)
                .setHeader("participant", projectUser);
    }
}