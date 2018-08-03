package org.innovateuk.ifs.project.otherdocuments.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.builder.ApplicationResourceBuilder;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.otherdocuments.OtherDocumentsService;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.otherdocuments.viewmodel.OtherDocumentsViewModel;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.user.resource.Role.PROJECT_MANAGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class OtherDocumentsControllerMockMvcTest extends BaseControllerMockMVCTest<OtherDocumentsController> {

    long applicationId = 456L;
    long projectId = 123L;

    OrganisationResource leadOrganisation = newOrganisationResource().withId(1L).withName("Test Lead Organisation").build();

    @Mock
    private ProjectService projectService;

    @Mock
    private OtherDocumentsService otherDocumentsService;

    @Mock
    private ApplicationService applicationService;

    private void setupViewOtherDocumentsTestExpectations(ProjectResource project) {

        List<ProjectUserResource> projectUsers = newProjectUserResource().with(id(999L)).withUserName("Dave Smith").withPhoneNumber("01234123123")
                .withEmail("d@d.com")
                .withRole(PROJECT_MANAGER).build(1);

        List<OrganisationResource> partnerOrganisations = newOrganisationResource().withName("Org1", "Org2", "Org3").build(3);

        ApplicationResource applicationResource = ApplicationResourceBuilder.newApplicationResource()
                .withId(applicationId)
                .withCompetition(1L)
                .build();

        when(projectService.getById(projectId)).thenReturn(project);
        when(otherDocumentsService.getCollaborationAgreementFileDetails(projectId)).thenReturn(Optional.empty());
        when(otherDocumentsService.getExploitationPlanFileDetails(projectId)).thenReturn(Optional.empty());
        when(projectService.getPartnerOrganisationsForProject(projectId)).thenReturn(partnerOrganisations);
        when(projectService.getLeadOrganisation(projectId)).thenReturn(leadOrganisation);
        when(applicationService.getById(project.getApplication())).thenReturn(applicationResource);
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);
    }


    private void assertProjectDetailsPrepopulatedOkWithPartners(OtherDocumentsViewModel model, boolean solePartner) {

        assertEquals(Long.valueOf(123), model.getProjectId());
        assertEquals(Long.valueOf(456), model.getApplicationId());
        assertEquals("My Project", model.getProjectName());
        assertEquals("Test Lead Organisation", model.getLeadPartnerOrganisationName());
        assertEquals("Dave Smith", model.getProjectManagerName());
        assertEquals("01234123123", model.getProjectManagerTelephone());
        assertEquals("d@d.com", model.getProjectManagerEmail());
        assertEquals(Long.valueOf(1L), model.getCompetitionId());

        List<String> testOrgList;
        if (solePartner) {
            testOrgList = new ArrayList<String>(Arrays.asList("Org1"));
        } else {
            testOrgList = new ArrayList<String>(Arrays.asList("Org1", "Org2", "Org3"));
        }
        assertEquals(asList(testOrgList), asList(model.getPartnerOrganisationNames()));
    }

    private void assertProjectDetailsPrepopulatedOk(OtherDocumentsViewModel model) {

        assertProjectDetailsPrepopulatedOkWithPartners(model, false);
    }

    @Test
    public void testViewOtherDocumentsPage() throws Exception {

        ProjectResource project = newProjectResource().withId(projectId).withApplication(applicationId).withName("My Project").build();

        setupViewOtherDocumentsTestExpectations (project);

        MvcResult result = mockMvc.perform(get("/project/123/partner/documents")).
                andExpect(view().name("project/other-documents")).
                andReturn();

        OtherDocumentsViewModel model = (OtherDocumentsViewModel) result.getModelAndView().getModel().get("model");

        assertProjectDetailsPrepopulatedOk(model);

    }

    @Test
    public void testViewOtherDocumentsPageWithExistingDocuments() throws Exception {

        ProjectResource project = newProjectResource().withId(projectId).withName("My Project").build();

        setupViewOtherDocumentsTestExpectations (project);

        FileEntryResource existingCollaborationAgreement = newFileEntryResource().build();
        FileEntryResource existingExplotationPlan = newFileEntryResource().build();

        when(otherDocumentsService.getCollaborationAgreementFileDetails(projectId)).thenReturn(Optional.of(existingCollaborationAgreement));
        when(otherDocumentsService.getExploitationPlanFileDetails(projectId)).thenReturn(Optional.of(existingExplotationPlan));

        MvcResult result = mockMvc.perform(get("/project/123/partner/documents")).
                andExpect(view().name("project/other-documents")).
                andReturn();

        OtherDocumentsViewModel model = (OtherDocumentsViewModel) result.getModelAndView().getModel().get("model");

        // assert the project details are correct
        assertProjectDetailsPrepopulatedOk(model);
   }

    @Test
    public void acceptOrRejectOtherDocuments() throws Exception {

        ProjectResource project = newProjectResource()
                .withId(projectId)
                .withName("My Project")
                .withOtherDocumentsApproved(ApprovalType.APPROVED)
                .build();
        boolean approved = true;

        setupViewOtherDocumentsTestExpectations(project);

        when(otherDocumentsService.acceptOrRejectOtherDocuments(projectId, approved)).thenReturn(ServiceResult.serviceSuccess());

        MvcResult result = mockMvc.perform(post("/project/123/partner/documents")
                .param("approved", String.valueOf(approved)))
                .andExpect(view().name("project/other-documents"))
                .andReturn();

        OtherDocumentsViewModel model = (OtherDocumentsViewModel) result.getModelAndView().getModel().get("model");

        assertProjectDetailsPrepopulatedOk(model);
        assertEquals(true, model.isApproved());

    }

    @Test
    public void testDownloadCollaborationAgreementButFileDoesntExist() throws Exception {

        when(otherDocumentsService.getExploitationPlanFile(123L)).
                thenReturn(Optional.empty());

        when(otherDocumentsService.getExploitationPlanFileDetails(123L)).
                thenReturn(Optional.empty());

        mockMvc.perform(get("/project/123/partner/documents/exploitation-plan")).
                andExpect(status().isNotFound()).
                andExpect(view().name("404"));
    }


    @Test
    public void testDownloadExploitationPlanButFileDoesntExist() throws Exception {

        when(otherDocumentsService.getExploitationPlanFile(123L)).
                thenReturn(Optional.empty());

        when(otherDocumentsService.getExploitationPlanFileDetails(123L)).
                thenReturn(Optional.empty());

        mockMvc.perform(get("/project/123/partner/documents/exploitation-plan")).
                andExpect(status().isNotFound()).
                andExpect(view().name("404"));
    }

    @Test
    public void testViewOtherDocumentsPageSolePartner() throws Exception {

        ProjectResource project = newProjectResource().withId(projectId).withApplication(applicationId).withName("My Project").build();

        setupViewOtherDocumentsTestExpectations (project);

        List<OrganisationResource> partnerOrganisations = newOrganisationResource().withName("Org1").build(1);
        when(projectService.getPartnerOrganisationsForProject(projectId)).thenReturn(partnerOrganisations);

        MvcResult result = mockMvc.perform(get("/project/123/partner/documents")).
                andExpect(view().name("project/other-documents")).
                andReturn();

        OtherDocumentsViewModel model = (OtherDocumentsViewModel) result.getModelAndView().getModel().get("model");

        assertProjectDetailsPrepopulatedOkWithPartners(model, true);
        assertNull(model.getCollaborationAgreementFileDetails());

    }

    @Override
    protected OtherDocumentsController supplyControllerUnderTest() {
        return new OtherDocumentsController();
    }
}
