package org.innovateuk.ifs.project.monitoringofficer.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.monitoring.resource.MonitoringOfficerResource;
import org.innovateuk.ifs.project.monitoring.service.MonitoringOfficerRestService;
import org.innovateuk.ifs.project.monitoringofficer.viewmodel.LegacyMonitoringOfficerViewModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;

import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.project.builder.MonitoringOfficerResourceBuilder.newMonitoringOfficerResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class LegacyMonitoringOfficerControllerMockMvcTest extends BaseControllerMockMVCTest<LegacyMonitoringOfficerController> {
    @Mock
    private ProjectService projectService;

    @Mock
    private MonitoringOfficerRestService monitoringOfficerService;

    @Test
    public void testViewMonitoringOfficer() throws Exception {

        ProjectResource project = newProjectResource().withId(123L).withApplication(345L).build();
        MonitoringOfficerResource monitoringOfficer = newMonitoringOfficerResource().build();

        when(projectService.getById(123L)).thenReturn(project);
        when(monitoringOfficerService.findMonitoringOfficerForProject(123L)).thenReturn(restSuccess(monitoringOfficer));

        MvcResult result = mockMvc.perform(get("/project/123/monitoring-officer")).
                andExpect(view().name("project/monitoring-officer")).
                andReturn();

        LegacyMonitoringOfficerViewModel viewModel =
                (LegacyMonitoringOfficerViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(Long.valueOf(123), viewModel.getProjectId());
        assertEquals(Long.valueOf(345), viewModel.getApplicationId());
        assertEquals(project.getName(), viewModel.getProjectName());
        assertTrue(viewModel.isMonitoringOfficerAssigned());
        assertEquals(monitoringOfficer.getFullName(), viewModel.getMonitoringOfficerName());
        assertEquals(monitoringOfficer.getEmail(), viewModel.getMonitoringOfficerEmailAddress());
        assertEquals(monitoringOfficer.getPhoneNumber(), viewModel.getMonitoringOfficerPhoneNumber());
    }

    @Test
    public void testViewMonitoringOfficerWithNoMonitoringOfficerYetAssigned() throws Exception {

        ProjectResource project = newProjectResource().withId(123L).withApplication(345L).build();

        when(projectService.getById(123L)).thenReturn(project);
        when(monitoringOfficerService.findMonitoringOfficerForProject(123L)).thenReturn(restFailure(HttpStatus.NOT_FOUND));

        MvcResult result = mockMvc.perform(get("/project/123/monitoring-officer")).
                andExpect(view().name("project/monitoring-officer")).
                andReturn();

        LegacyMonitoringOfficerViewModel viewModel =
                (LegacyMonitoringOfficerViewModel) result.getModelAndView().getModel().get("model");

        assertEquals(Long.valueOf(123), viewModel.getProjectId());
        assertEquals(Long.valueOf(345), viewModel.getApplicationId());
        assertEquals(project.getName(), viewModel.getProjectName());
        assertFalse(viewModel.isMonitoringOfficerAssigned());
        assertEquals("", viewModel.getMonitoringOfficerName());
        assertEquals("", viewModel.getMonitoringOfficerEmailAddress());
        assertEquals("", viewModel.getMonitoringOfficerPhoneNumber());
    }

    @Override
    protected LegacyMonitoringOfficerController supplyControllerUnderTest() {
        return new LegacyMonitoringOfficerController();
    }
}
