package org.innovateuk.ifs.project.core.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.notifications.resource.*;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.project.core.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectUserRepository;
import org.innovateuk.ifs.project.monitoring.domain.MonitoringOfficer;
import org.innovateuk.ifs.project.monitoring.repository.MonitoringOfficerRepository;
import org.innovateuk.ifs.user.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static freemarker.template.utility.Collections12.singletonList;
import static java.lang.String.format;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.notifications.builders.NotificationBuilder.newNotification;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.project.core.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.core.domain.ProjectParticipantRole.*;
import static org.innovateuk.ifs.project.core.transactional.RemovePartnerNotificationServiceImpl.Notifications.REMOVE_PROJECT_ORGANISATION;
import static org.innovateuk.ifs.project.monitoring.builder.MonitoringOfficerBuilder.newMonitoringOfficer;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RemovePartnerNotificationServiceImplTest extends BaseServiceUnitTest<RemovePartnerNotificationService> {

    @Mock
    private ProjectUserRepository projectUserRepositoryMock;

    @Mock
    private MonitoringOfficerRepository monitoringOfficerRepositoryMock;

    @Mock
    private SystemNotificationSource systemNotificationSourceMock;

    @Mock
    private NotificationService notificationServiceMock;

    private Organisation organisation;
    private Application application;
    private Project project;
    private User orville;
    private List<Notification> notifications;
    private MonitoringOfficer monitoringOfficer;
    private List<ProjectUser> projectUsers;
    private PartnerOrganisation leadPartnerOrganisation;
    private Map<String, Object> notificationArguments = new HashMap<>();

    @Before
    public void setup() {
        organisation = newOrganisation()
                .withId(43L)
                .withOrganisationType(BUSINESS)
                .withName("Ludlow")
                .build();
        leadPartnerOrganisation = newPartnerOrganisation().withLeadOrganisation(true).withOrganisation(organisation).build();
        application = newApplication().withId(77L).build();
        project = newProject()
                .withId(99L)
                .withName("Smart ideas for plastic recycling")
                .withApplication(application)
                .withPartnerOrganisations(singletonList(leadPartnerOrganisation))
                .build();

        orville = newUser().withId(3L).withFirstName("Orville").withLastName("Gibbs").build();
        monitoringOfficer = newMonitoringOfficer()
                .withId(81L)
                .withUser(orville)
                .build();

        notificationArguments.put("applicationId", application.getId());
        notificationArguments.put("projectName", project.getName());
        notificationArguments.put("organisationName", organisation.getName());
        notificationArguments.put("projectTeamLink", getProjectTeamLink(project.getId()));
    }

    @Test
    public void sendNotificationsWhenProjectManagerAndMonitoringOfficerArePresent() {
        List<User> users = newUser().withId(50L, 23L).withFirstName("Lyn", "Rick").withLastName("Brown", "McDonald").build(2);
        projectUsers = newProjectUser()
                .withId(88L, 45L)
                .withProject(project)
                .withRole(PROJECT_MANAGER, PROJECT_PARTNER)
                .withOrganisation(organisation)
                .withUser(users.get(0), users.get(1))
                .build(2);

        project.addProjectUser(projectUsers.get(0));
        project.addProjectUser(projectUsers.get(1));
        project.setProjectMonitoringOfficer(monitoringOfficer);

        NotificationSource from = systemNotificationSourceMock;
        NotificationTarget recipientPM = createProjectNotificationTarget(users.get(0));
        NotificationTarget recipientMO = createProjectNotificationTarget(orville);

        notifications = newNotification()
                .withMessageKey(REMOVE_PROJECT_ORGANISATION)
                .withSource(from)
                .withTargets(singletonList(recipientPM), singletonList(recipientMO))
                .withGlobalArguments(notificationArguments)
                .build(2);

        when(projectUserRepositoryMock.findByProjectIdAndRole(project.getId(), PROJECT_MANAGER)).thenReturn(Optional.of(projectUsers.get(0)));
        when(monitoringOfficerRepositoryMock.existsByProjectIdAndUserId(project.getId(), orville.getId())).thenReturn(true);

        ServiceResult<Void> result = service.sendNotifications(project, organisation);

        assertTrue(result.isSuccess());
        verify(notificationServiceMock, times(1)).sendNotificationWithFlush(notifications.get(0), EMAIL);
        verify(notificationServiceMock, times(1)).sendNotificationWithFlush(notifications.get(1), EMAIL);
    }

    @Test
    public void sendNotificationWhenNoMonitoringOfficerIsAssigned() {
        List<User> users = newUser().withId(50L, 23L).withFirstName("Lyn", "Rick").withLastName("Brown", "McDonald").build(2);
        projectUsers = newProjectUser()
                .withId(88L, 45L)
                .withProject(project)
                .withRole(PROJECT_MANAGER, PROJECT_PARTNER)
                .withOrganisation(organisation)
                .withUser(users.get(0), users.get(1))
                .build(2);

        project.addProjectUser(projectUsers.get(0));
        project.addProjectUser(projectUsers.get(1));

        NotificationSource from = systemNotificationSourceMock;
        NotificationTarget recipientPM = createProjectNotificationTarget(users.get(0));

        notifications = newNotification()
                .withMessageKey(REMOVE_PROJECT_ORGANISATION)
                .withSource(from)
                .withTargets(singletonList(recipientPM))
                .withGlobalArguments(notificationArguments)
                .build(1);

        when(projectUserRepositoryMock.findByProjectIdAndRole(project.getId(), PROJECT_MANAGER)).thenReturn(Optional.of(projectUsers.get(0)));
        when(monitoringOfficerRepositoryMock.existsByProjectIdAndUserId(project.getId(), orville.getId())).thenReturn(false);

        ServiceResult<Void> result = service.sendNotifications(project, organisation);

        assertTrue(result.isSuccess());
        verify(notificationServiceMock, times(1)).sendNotificationWithFlush(notifications.get(0), EMAIL);
    }

    @Test
    public void sendNotificationWhenNoProjectManagerIsAssigned() {
        List<User> users = newUser().withFirstName("Lyn", "Rick").withLastName("Brown", "McDonald").build(2);
        projectUsers = newProjectUser()
                .withProject(project)
                .withRole(PROJECT_PARTNER, PROJECT_PARTNER)
                .withOrganisation(organisation)
                .withUser(users.get(0), users.get(1))
                .build(2);

        project.addProjectUser(projectUsers.get(0));
        project.addProjectUser(projectUsers.get(1));
        project.setProjectMonitoringOfficer(monitoringOfficer);

        NotificationSource from = systemNotificationSourceMock;
        NotificationTarget recipient1 = createProjectNotificationTarget(users.get(0));
        NotificationTarget recipient2 = createProjectNotificationTarget(users.get(1));
        NotificationTarget recipientMO = createProjectNotificationTarget(orville);

        notifications = newNotification()
                .withMessageKey(REMOVE_PROJECT_ORGANISATION)
                .withSource(from)
                .withTargets(singletonList(recipient1), singletonList(recipient2), singletonList(recipientMO))
                .withGlobalArguments(notificationArguments)
                .build(3);

        when(projectUserRepositoryMock.findByProjectIdAndRole(project.getId(), PROJECT_MANAGER)).thenReturn(Optional.empty());
        when(projectUserRepositoryMock.findByProjectIdAndOrganisationId(project.getId(), organisation.getId())).thenReturn(projectUsers);
        when(monitoringOfficerRepositoryMock.existsByProjectIdAndUserId(project.getId(), orville.getId())).thenReturn(true);

        ServiceResult<Void> result = service.sendNotifications(project, organisation);

        assertTrue(result.isSuccess());
        verify(notificationServiceMock, times(1)).sendNotificationWithFlush(notifications.get(0), EMAIL);
        verify(notificationServiceMock, times(1)).sendNotificationWithFlush(notifications.get(1), EMAIL);
        verify(notificationServiceMock, times(1)).sendNotificationWithFlush(notifications.get(2), EMAIL);
    }

    private NotificationTarget createProjectNotificationTarget(User user) {
        String fullName = getProjectManagerFullName(user);
        return new UserNotificationTarget(fullName, user.getEmail());
    }

    private String getProjectManagerFullName(User projectManager) {
        return projectManager.getFirstName() + " " + projectManager.getLastName();
    }

    private String getProjectTeamLink(long projectId) {
        return format("/project-setup/project/%d/team", projectId);
    }

    @Override
    protected RemovePartnerNotificationService supplyServiceUnderTest() {
        return new RemovePartnerNotificationServiceImpl();
    }
}