package org.innovateuk.ifs.project.invite.transactional;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.InviteOrganisation;
import org.innovateuk.ifs.invite.repository.InviteOrganisationRepository;
import org.innovateuk.ifs.notifications.resource.*;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.innovateuk.ifs.project.invite.domain.ProjectPartnerInvite;
import org.innovateuk.ifs.project.invite.repository.ProjectPartnerInviteRepository;
import org.innovateuk.ifs.project.invite.resource.SendProjectPartnerInviteResource;
import org.innovateuk.ifs.project.invite.resource.SentProjectPartnerInviteResource;
import org.innovateuk.ifs.security.LoggedInUserSupplier;
import org.innovateuk.ifs.user.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.project.core.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class ProjectPartnerInviteServiceImplTest {

    @InjectMocks
    private ProjectPartnerInviteServiceImpl service;

    @Mock
    private ProjectPartnerInviteRepository projectPartnerInviteRepository;

    @Mock
    private InviteOrganisationRepository inviteOrganisationRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SystemNotificationSource systemNotificationSource;

    @Mock
    private ProjectInviteValidator projectInviteValidator;

    @Mock
    private LoggedInUserSupplier loggedInUserSupplier;

    @Test
    public void invite() {
        setField(service, "webBaseUrl", "webBaseUrl");
        long projectId = 1L;
        String organisationName = "Org";
        String userName = "Someone";
        String email = "someone@gmail.com";
        Application application = newApplication().build();
        Organisation leadOrg = newOrganisation()
                .withName("Lead org")
                .build();
        SendProjectPartnerInviteResource invite = new SendProjectPartnerInviteResource(organisationName, userName, email);
        Project project = newProject()
                .withName("Project")
                .withApplication(application)
                .withPartnerOrganisations(newPartnerOrganisation()
                        .withOrganisation(leadOrg)
                        .withLeadOrganisation(true)
                        .build(1))
                .build();

        when(projectRepository.findById(projectId)).thenReturn(of(project));
        when(projectInviteValidator.validate(projectId, invite)).thenReturn(serviceSuccess());
        when(inviteOrganisationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectPartnerInviteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> notificationArguments = new HashMap<>();
        notificationArguments.put("inviteUrl", "webBaseUrl");
        notificationArguments.put("applicationId", application.getId());
        notificationArguments.put("projectName", "Project");
        notificationArguments.put("leadOrganisationName", "Lead org");

        NotificationTarget to = new UserNotificationTarget(userName, email);

        Notification notification = new Notification(systemNotificationSource, singletonList(to), ProjectPartnerInviteServiceImpl.Notifications.INVITE_PROJECT_PARTNER_ORGANISATION, notificationArguments);
        when(notificationService.sendNotificationWithFlush(notification, NotificationMedium.EMAIL)).thenReturn(serviceSuccess());

        User loggedInUser = newUser().build();
        when(loggedInUserSupplier.get()).thenReturn(loggedInUser);

        ServiceResult<Void> result = service.invitePartnerOrganisation(projectId, invite);

        assertTrue(result.isSuccess());
        verify(notificationService).sendNotificationWithFlush(notification, NotificationMedium.EMAIL);
    }

    @Test
    public void getPartnerInvites() {
        long projectId = 1L;
        long inviteId = 2L;
        String email = "Partner@gmail.com";
        String userName = "Partner";
        String organisationName = "Partners Ltd.";
        ZonedDateTime sentOn = now();
        ProjectPartnerInvite invite = new ProjectPartnerInvite();
        InviteOrganisation inviteOrganisation = new InviteOrganisation();
        invite.setEmail(email);
        invite.setId(inviteId);
        invite.setName(userName);
        setField(invite, "status", InviteStatus.SENT);
        setField(invite, "sentOn", sentOn);
        invite.setInviteOrganisation(inviteOrganisation);
        inviteOrganisation.setOrganisationName(organisationName);

        when(projectPartnerInviteRepository.findByProjectId(projectId)).thenReturn(singletonList(invite));

        ServiceResult<List<SentProjectPartnerInviteResource>> result = service.getPartnerInvites(projectId);

        SentProjectPartnerInviteResource resource = result.getSuccess().get(0);
        assertEquals(inviteId, resource.getId());
        assertEquals(email, resource.getEmail());
        assertEquals(userName, resource.getUserName());
        assertEquals(organisationName, resource.getOrganisationName());
        assertEquals(sentOn, resource.getSentOn());
    }

    @Test
    public void resendInvite() {
        setField(service, "webBaseUrl", "webBaseUrl");
        long inviteId = 2L;
        String userName = "Someone";
        String email = "someone@gmail.com";
        Application application = newApplication().build();
        Organisation leadOrg = newOrganisation()
                .withName("Lead org")
                .build();
        Project project = newProject()
                .withName("Project")
                .withApplication(application)
                .withPartnerOrganisations(newPartnerOrganisation()
                        .withOrganisation(leadOrg)
                        .withLeadOrganisation(true)
                        .build(1))
                .build();
        ProjectPartnerInvite invite = spy(new ProjectPartnerInvite());
        invite.setEmail(email);
        invite.setName(userName);
        invite.setTarget(project);

        when(projectPartnerInviteRepository.findById(inviteId)).thenReturn(of(invite));
        Map<String, Object> notificationArguments = new HashMap<>();
        notificationArguments.put("inviteUrl", "webBaseUrl");
        notificationArguments.put("applicationId", application.getId());
        notificationArguments.put("projectName", "Project");
        notificationArguments.put("leadOrganisationName", "Lead org");

        NotificationTarget to = new UserNotificationTarget(userName, email);

        Notification notification = new Notification(systemNotificationSource, singletonList(to), ProjectPartnerInviteServiceImpl.Notifications.INVITE_PROJECT_PARTNER_ORGANISATION, notificationArguments);
        when(notificationService.sendNotificationWithFlush(notification, NotificationMedium.EMAIL)).thenReturn(serviceSuccess());
        User loggedInUser = newUser().build();
        when(loggedInUserSupplier.get()).thenReturn(loggedInUser);

        service.resendInvite(inviteId);

        verify(invite).resend(eq(loggedInUser), any());
        verify(notificationService).sendNotificationWithFlush(notification, NotificationMedium.EMAIL);
    }

    @Test
    public void deleteInvite() {
        long inviteId = 2L;
        ProjectPartnerInvite invite = new ProjectPartnerInvite();
        when(projectPartnerInviteRepository.findById(inviteId)).thenReturn(of(invite));

        service.deleteInvite(inviteId);

        verify(projectPartnerInviteRepository).delete(invite);
    }
}