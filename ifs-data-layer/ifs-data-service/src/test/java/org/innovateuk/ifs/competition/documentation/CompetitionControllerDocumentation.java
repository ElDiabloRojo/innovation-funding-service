package org.innovateuk.ifs.competition.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.controller.CompetitionController;
import org.innovateuk.ifs.competition.resource.CompetitionCountResource;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResult;
import org.innovateuk.ifs.competition.transactional.CompetitionService;
import org.innovateuk.ifs.documentation.*;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionSearchResultItemBuilder.newCompetitionSearchResultItem;
import static org.innovateuk.ifs.documentation.CompetitionResourceDocs.competitionResourceBuilder;
import static org.innovateuk.ifs.documentation.CompetitionResourceDocs.competitionResourceFields;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompetitionControllerDocumentation extends BaseControllerMockMVCTest<CompetitionController> {
    @Mock
    private CompetitionService competitionService;

    @Override
    protected CompetitionController supplyControllerUnderTest() {
        return new CompetitionController();
    }

    @Test
    public void findOne() throws Exception {
        final Long competitionId = 1L;

        when(competitionService.getCompetitionById(competitionId)).thenReturn(serviceSuccess(competitionResourceBuilder.build()));

        mockMvc.perform(get("/competition/{id}", competitionId))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("id of the competition to be retrieved")
                        ),
                        responseFields(competitionResourceFields)
                                .andWithPrefix("termsAndConditions.", TermsAndConditionsResourceDocs.termsAndConditionsResourceFields)
                        )
                );
    }

    @Test
    public void findAll() throws Exception {

        when(competitionService.findAll()).thenReturn(serviceSuccess(competitionResourceBuilder.build(2)));

        mockMvc.perform(get("/competition/findAll"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        relaxedResponseFields(
                                fieldWithPath("[]").description("list of Competitions the authenticated user has access to")
                        ).andWithPrefix("[].", CompetitionResourceDocs.competitionResourceFields)
                        .andWithPrefix("[].termsAndConditions.", TermsAndConditionsResourceDocs.termsAndConditionsResourceFields)
                ));
    }

    @Test
    public void live() throws Exception {
        when(competitionService.findLiveCompetitions()).thenReturn(serviceSuccess(newCompetitionSearchResultItem().build(2)));

        mockMvc.perform(get("/competition/live"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        responseFields(
                                fieldWithPath("[]").description("list of live competitions the authenticated user has access to")
                        ).andWithPrefix("[].", CompetitionSearchResultItemDocs.competitionSearchResultItemFields)

                ));
    }

    @Test
    public void projectSetup() throws Exception {
        when(competitionService.findProjectSetupCompetitions()).thenReturn(serviceSuccess(newCompetitionSearchResultItem().build(2)));

        mockMvc.perform(get("/competition/project-setup"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        responseFields(
                                fieldWithPath("[]").description("list of competitions in project set up the authenticated user has access to")
                        ).andWithPrefix("[].", CompetitionSearchResultItemDocs.competitionSearchResultItemFields)
                ));
    }

    @Test
    public void upcoming() throws Exception {
        when(competitionService.findUpcomingCompetitions()).thenReturn(serviceSuccess(newCompetitionSearchResultItem().build(2)));

        mockMvc.perform(get("/competition/upcoming"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        responseFields(
                                fieldWithPath("[]").description("list of upcoming competitions the authenticated user has access to")
                        ).andWithPrefix("[].", CompetitionSearchResultItemDocs.competitionSearchResultItemFields)
                ));
    }

    @Test
    public void nonIfs() throws Exception {
        when(competitionService.findNonIfsCompetitions()).thenReturn(serviceSuccess(newCompetitionSearchResultItem().build(2)));

        mockMvc.perform(get("/competition/non-ifs"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        responseFields(
                                fieldWithPath("[]").description("list of non ifs competitions the authenticated user has access to")
                        ).andWithPrefix("[].", CompetitionSearchResultItemDocs.competitionSearchResultItemFields)
                ));
    }

    @Test
    public void count() throws Exception {
        CompetitionCountResource resource = new CompetitionCountResource();
        when(competitionService.countCompetitions()).thenReturn(serviceSuccess(resource));

        mockMvc.perform(get("/competition/count"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        responseFields(CompetitionCountResourceDocs.competitionCountResourceFields)
                ));
    }

    @Test
    public void search() throws Exception {
        CompetitionSearchResult results = new CompetitionSearchResult();
        String searchQuery = "test";
        int page = 1;
        int size = 20;
        when(competitionService.searchCompetitions(searchQuery, page, size)).thenReturn(serviceSuccess(results));

        mockMvc.perform(get("/competition/search/{page}/{size}/?searchQuery=" + searchQuery, page, size))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        requestParameters(parameterWithName("searchQuery").description("The search query to lookup")),
                        pathParameters(
                                parameterWithName("page").description("The page number to be requested"),
                                parameterWithName("size").description("The number of competitions per page")
                        ),
                        responseFields(CompetitionSearchResultDocs.competitionSearchResultFields)
                ));
    }

    @Test
    public void findInnovationLeads() throws Exception {
        final Long competitionId = 1L;

        List<UserResource> innovationLeads = new ArrayList<>();
        when(competitionService.findInnovationLeads(competitionId)).thenReturn(serviceSuccess(innovationLeads));

        mockMvc.perform(get("/competition/{id}/innovation-leads", competitionId))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(innovationLeads)))
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("The competition for which innovation leads need to be found")
                        )
                ));
    }

    @Test
    public void addInnovationLead() throws Exception {
        final Long competitionId = 1L;
        final Long innovationLeadUserId = 2L;

        when(competitionService.addInnovationLead(competitionId, innovationLeadUserId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competition/{id}/add-innovation-lead/{innovationLeadUserId}", competitionId, innovationLeadUserId))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("The competition for which innovation lead needs to be added"),
                                parameterWithName("innovationLeadUserId").description("The id of the innovation lead which is being added")
                        )
                ));

        verify(competitionService, only()).addInnovationLead(competitionId, innovationLeadUserId);

    }

    @Test
    public void removeInnovationLead() throws Exception {
        final Long competitionId = 1L;
        final Long innovationLeadUserId = 2L;

        when(competitionService.removeInnovationLead(competitionId, innovationLeadUserId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competition/{id}/remove-innovation-lead/{innovationLeadUserId}", competitionId, innovationLeadUserId))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("The competition for which innovation lead needs to be deleted"),
                                parameterWithName("innovationLeadUserId").description("The id of the innovation lead which is being deleted")
                        )
                ));

        verify(competitionService, only()).removeInnovationLead(competitionId, innovationLeadUserId);

    }

    @Test
    public void updateTermsAndConditions() throws Exception {
        final Long competitionId = 1L;
        final Long termsAndConditionsId = 2L;

        when(competitionService.updateTermsAndConditionsForCompetition(competitionId, termsAndConditionsId)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/competition/{id}/updateTermsAndConditions/{tcId}", competitionId, termsAndConditionsId))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("The competition for which the terms and conditions need to be updated"),
                                parameterWithName("tcId").description("The terms and conditions id to update it to")
                        )
                ));

        verify(competitionService, only()).updateTermsAndConditionsForCompetition(competitionId, termsAndConditionsId);
    }
}
