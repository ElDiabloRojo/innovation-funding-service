package org.innovateuk.ifs.project.core.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.project.core.transactional.ProjectService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.GENERAL_NOT_FOUND;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_CANNOT_BE_WITHDRAWN;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProjectControllerTest extends BaseControllerMockMVCTest<ProjectController> {

    @Mock
    private ProjectService projectServiceMock;

    @Override
    protected ProjectController supplyControllerUnderTest() {
        return new ProjectController();
    }

    @Test
    public void projectControllerShouldReturnProjectById() throws Exception {
        Long project1Id = 1L;
        Long project2Id = 2L;

        ProjectResource testProjectResource1 = newProjectResource().withId(project1Id).build();
        ProjectResource testProjectResource2 = newProjectResource().withId(project2Id).build();

        when(projectServiceMock.getProjectById(project1Id)).thenReturn(serviceSuccess(testProjectResource1));
        when(projectServiceMock.getProjectById(project2Id)).thenReturn(serviceSuccess(testProjectResource2));

        mockMvc.perform(get("/project/{id}", project1Id))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(testProjectResource1)));

        mockMvc.perform(get("/project/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(testProjectResource2)));
    }

    @Test
    public void projectControllerShouldReturnAllProjects() throws Exception {
        int projectNumber = 3;
        List<ProjectResource> projects = newProjectResource().build(projectNumber);
        when(projectServiceMock.findAll()).thenReturn(serviceSuccess(projects));

        mockMvc.perform(get("/project/").contentType(APPLICATION_JSON).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(projectNumber)));
    }

    @Test
    public void getProjectUsers() throws Exception {

        List<ProjectUserResource> projectUsers = newProjectUserResource().build(3);

        when(projectServiceMock.getProjectUsers(123L)).thenReturn(serviceSuccess(projectUsers));

        mockMvc.perform(get("/project/{projectId}/project-users", 123L)).
                andExpect(status().isOk()).
                andExpect(content().json(toJson(projectUsers)));
    }

    @Test
    public void testCreateProjectFromApplication() throws Exception {
        Long applicationId = 1L;
        ProjectResource expectedProject = newProjectResource().build();

        when(projectServiceMock.createProjectFromApplication(applicationId)).thenReturn(serviceSuccess(expectedProject));

        mockMvc.perform(post("/project/create-project/application/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedProject)));

        verify(projectServiceMock).createProjectFromApplication(applicationId);
    }

    @Test
    public void testWithdrawProject() throws Exception {
        Long projectId = 456L;
        when(projectServiceMock.withdrawProject(projectId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/project/{projectId}/withdraw", projectId))
                .andExpect(status().isOk());

        verify(projectServiceMock).withdrawProject(projectId);
    }

    @Test
    public void testWithdrawProjectFails() throws Exception {
        Long projectId = 789L;
        when(projectServiceMock.withdrawProject(projectId)).thenReturn(serviceFailure(PROJECT_CANNOT_BE_WITHDRAWN));

        mockMvc.perform(post("/project/{projectId}/withdraw", projectId))
                .andExpect(status().isBadRequest());

        verify(projectServiceMock).withdrawProject(projectId);
    }

    @Test
    public void testWithdrawProjectWhenProjectDoesntExist() throws Exception {
        Long projectId = 432L;
        when(projectServiceMock.withdrawProject(projectId)).thenReturn(serviceFailure(GENERAL_NOT_FOUND));

        mockMvc.perform(post("/project/{projectId}/withdraw", projectId))
                .andExpect(status().isNotFound());

        verify(projectServiceMock).withdrawProject(projectId);

    }
}

