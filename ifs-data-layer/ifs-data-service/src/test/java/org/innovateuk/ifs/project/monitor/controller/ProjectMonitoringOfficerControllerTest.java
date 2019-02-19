package org.innovateuk.ifs.project.monitor.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.invite.resource.MonitoringOfficerInviteResource;
import org.innovateuk.ifs.project.monitor.transactional.ProjectMonitoringOfficerService;
import org.innovateuk.ifs.registration.resource.MonitoringOfficerRegistrationResource;
import org.innovateuk.ifs.user.transactional.RegistrationService;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.builder.MonitoringOfficerInviteResourceBuilder.newMonitoringOfficerInviteResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProjectMonitoringOfficerControllerTest extends BaseControllerMockMVCTest<ProjectMonitoringOfficerController> {

    @Mock
    private ProjectMonitoringOfficerService projectMonitoringOfficerServiceMock;

    @Mock
    private RegistrationService registrationServiceMock;

    @Override
    protected ProjectMonitoringOfficerController supplyControllerUnderTest() {
        return new ProjectMonitoringOfficerController(projectMonitoringOfficerServiceMock, registrationServiceMock);
    }

    @Test
    public void openInvite() throws Exception {
        String hash = "hash";
        MonitoringOfficerInviteResource invite = newMonitoringOfficerInviteResource()
                .withHash(hash)
                .build();

        when(projectMonitoringOfficerServiceMock.openInvite(hash)).thenReturn(serviceSuccess(invite));

        mockMvc.perform(get("/competition/setup/open-monitoring-officer-invite/{hash}", hash))
                .andExpect(status().isOk());

        verify(projectMonitoringOfficerServiceMock, only()).openInvite(hash);
    }

    @Test
    public void getInvite() throws Exception {
        String hash = "hash";
        MonitoringOfficerInviteResource invite = newMonitoringOfficerInviteResource()
                .withHash(hash)
                .build();

        when(projectMonitoringOfficerServiceMock.getInviteByHash(hash)).thenReturn(serviceSuccess(invite));

        mockMvc.perform(get("/competition/setup/get-monitoring-officer-invite/{hash}", hash))
                .andExpect(status().isOk());

        verify(projectMonitoringOfficerServiceMock, only()).getInviteByHash(hash);
    }
    @Test
    public void createMonitoringOfficer() throws Exception {
        String hash = "hash";
        MonitoringOfficerRegistrationResource registrationResource = new MonitoringOfficerRegistrationResource(
                "tom", "baldwin", "0114 286 6356", "password"
        );

        when(registrationServiceMock.createMonitoringOfficer(hash, registrationResource)).thenReturn(serviceSuccess());

        mockMvc.perform(MockMvcRequestBuilders.post("/competition/setup/monitoring-officer/create/{hash}", hash)
                .contentType(APPLICATION_JSON)
                .content(toJson(registrationResource)))
                .andExpect(status().is2xxSuccessful());

        verify(registrationServiceMock, only()).createMonitoringOfficer(hash, registrationResource);
    }

    @Test
    public void checkExistingUser() throws Exception {
        String hash = "hash";

        when(projectMonitoringOfficerServiceMock.checkUserExistsForInvite(hash)).thenReturn(serviceSuccess(true));

        mockMvc.perform(get("/competition/setup/monitoring-officer/check-existing-user/{hash}", hash))
                .andExpect(status().isOk());

        verify(projectMonitoringOfficerServiceMock, only()).checkUserExistsForInvite(hash);
    }

    @Test
    public void addMonitoringOfficerRole() throws Exception {
        String hash = "hash";

        when(projectMonitoringOfficerServiceMock.addMonitoringOfficerRole(hash)).thenReturn(serviceSuccess());

        mockMvc.perform(MockMvcRequestBuilders.post("/competition/setup/monitoring-officer/add-monitoring-officer-role/{hash}", hash))
                .andExpect(status().is2xxSuccessful());

        verify(projectMonitoringOfficerServiceMock, only()).addMonitoringOfficerRole(hash);
    }
}