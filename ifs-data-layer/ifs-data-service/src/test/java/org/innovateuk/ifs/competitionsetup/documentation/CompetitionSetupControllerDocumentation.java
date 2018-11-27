package org.innovateuk.ifs.competitionsetup.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competition.transactional.CompetitionService;
import org.innovateuk.ifs.competitionsetup.controller.CompetitionSetupController;
import org.innovateuk.ifs.competitionsetup.transactional.CompetitionSetupService;
import org.innovateuk.ifs.documentation.CompetitionResourceDocs;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.CompetitionResourceDocs.competitionResourceBuilder;
import static org.innovateuk.ifs.documentation.SetupStatusResourceDocs.setupStatusResourceBuilder;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompetitionSetupControllerDocumentation extends BaseControllerMockMVCTest<CompetitionSetupController> {
    @Mock
    private CompetitionService competitionService;

    @Mock
    private CompetitionSetupService competitionSetupService;

    @Override
    protected CompetitionSetupController supplyControllerUnderTest() {
        return new CompetitionSetupController();
    }

    @Test
    public void markAsComplete() throws Exception {
        Long competitionId = 2L;
        CompetitionSetupSection section = CompetitionSetupSection.INITIAL_DETAILS;
        when(competitionSetupService.markSectionComplete(competitionId, section)).thenReturn(serviceSuccess(setupStatusResourceBuilder.build()));

        mockMvc.perform(put("/competition/setup/section-status/complete/{competitionId}/{section}", competitionId, section)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition on which the section should be marked as complete"),
                                parameterWithName("section").description("the section to mark as complete")
                        )
                ));
    }

    @Test
    public void markAsIncomplete() throws Exception {
        Long competitionId = 2L;
        CompetitionSetupSection section = CompetitionSetupSection.INITIAL_DETAILS;
        when(competitionSetupService.markSectionIncomplete(competitionId, section)).thenReturn(serviceSuccess(setupStatusResourceBuilder.build(1)));

        mockMvc.perform(put("/competition/setup/section-status/incomplete/{competitionId}/{section}", competitionId, section)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition on which the section should be marked as incomplete"),
                                parameterWithName("section").description("the section to mark as incomplete")
                        )
                ));
    }

    @Test
    public void markSubsectionAsComplete() throws Exception {
        Long competitionId = 2L;
        CompetitionSetupSubsection subsection = CompetitionSetupSubsection.APPLICATION_DETAILS;
        CompetitionSetupSection parentSection = CompetitionSetupSection.APPLICATION_FORM;
        when(competitionSetupService.markSubsectionComplete(competitionId, parentSection, subsection)).thenReturn(serviceSuccess(setupStatusResourceBuilder.build()));

        mockMvc.perform(put("/competition/setup/subsection-status/complete/{competitionId}/{parentSection}/{subsection}", competitionId, parentSection, subsection)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition on which the section should be marked as complete"),
                                parameterWithName("parentSection").description("the parent section of the section that needs to be marked as complete"),
                                parameterWithName("subsection").description("the subsection to mark as complete")
                        )
                ));
    }

    @Test
    public void markSubsectionAsIncomplete() throws Exception {
        Long competitionId = 2L;
        CompetitionSetupSubsection subsection = CompetitionSetupSubsection.APPLICATION_DETAILS;
        CompetitionSetupSection parentSection = CompetitionSetupSection.APPLICATION_FORM;
        when(competitionSetupService.markSubsectionIncomplete(competitionId, parentSection, subsection)).thenReturn(serviceSuccess(setupStatusResourceBuilder.build()));

        mockMvc.perform(put("/competition/setup/subsection-status/incomplete/{competitionId}/{parentSection}/{subsection}", competitionId, parentSection, subsection)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition on which the section should be marked as incomplete"),
                                parameterWithName("parentSection").description("the parent section of the section that needs to be marked as incomplete"),
                                parameterWithName("subsection").description("the subsection to mark as incomplete")
                        )
                ));
    }

    @Test
    public void initialiseFormForCompetitionType() throws Exception {
        Long competitionId = 2L;
        Long competitionTypeId = 3L;
        when(competitionSetupService.copyFromCompetitionTypeTemplate(competitionId, competitionTypeId)).thenReturn(serviceSuccess());

        mockMvc.perform(post("/competition/setup/{competitionId}/initialise-form/{competitionTypeId}", competitionId, competitionTypeId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition in competition setup on which the application form should be initialised"),
                                parameterWithName("competitionTypeId").description("id of the competitionType that is being chosen on setup")
                        )
                ));
    }

    @Test
    public void updateCompetitionInitialDetails() throws Exception {
        final Long competitionId = 1L;

        CompetitionResource competitionResource = competitionResourceBuilder.build();

        when(competitionService.getCompetitionById(competitionId)).thenReturn(serviceSuccess(competitionResource));
        when(competitionSetupService.updateCompetitionInitialDetails(any(), any(), any())).thenReturn(serviceSuccess());

        mockMvc.perform(put("/competition/setup/{id}/update-competition-initial-details", competitionId)
                .header("IFS_AUTH_TOKEN", "123abc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(competitionResource)))
                .andExpect(status().isOk())
                .andDo(document(
                        "competition/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Id of the competition whose initial details are being updated")
                        ),
                        requestFields(CompetitionResourceDocs.competitionResourceFields)
                        )
                );
    }

    @Test
    public void deleteCompetition() throws Exception {
        long competitionId = 1L;

        when(competitionSetupService.deleteCompetition(competitionId)).thenReturn(serviceSuccess());

        mockMvc.perform(delete("/competition/setup/{id}", competitionId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isNoContent())
                .andDo(document("competition/{method-name}",
                        pathParameters(parameterWithName("id").description("Id of the competition to delete")))
                );
    }
}
