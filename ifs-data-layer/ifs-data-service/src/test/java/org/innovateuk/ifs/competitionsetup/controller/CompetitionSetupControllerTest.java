package org.innovateuk.ifs.competitionsetup.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competition.transactional.CompetitionService;
import org.innovateuk.ifs.competitionsetup.transactional.CompetitionSetupService;
import org.innovateuk.ifs.setup.resource.SetupStatusResource;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.setup.builder.SetupStatusResourceBuilder.newSetupStatusResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompetitionSetupControllerTest extends BaseControllerMockMVCTest<CompetitionSetupController> {


    @Mock
    private CompetitionService competitionServiceMock;

    @Mock
    private CompetitionSetupService competitionSetupServiceMock;

    @Override
    protected CompetitionSetupController supplyControllerUnderTest() {
        return new CompetitionSetupController();
    }

    @Test
    public void testUpdateCompetitionInitialDetails() throws Exception {
        final Long competitionId = 1L;
        final Long leadTechnologistUserId = 7L;

        CompetitionResource competitionResource = CompetitionResourceBuilder.newCompetitionResource()
                .withInnovationAreaNames(Collections.emptySet())
                .withLeadTechnologist(leadTechnologistUserId)
                .build();

        when(competitionServiceMock.getCompetitionById(competitionId)).thenReturn(serviceSuccess(competitionResource));
        when(competitionSetupServiceMock.updateCompetitionInitialDetails(any(), any(), any())).thenReturn(serviceSuccess());

        mockMvc.perform(put("/competition/setup/{id}/update-competition-initial-details", competitionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(competitionResource)))
                .andExpect(status().isOk());

        verify(competitionServiceMock, only()).getCompetitionById(competitionId);
        verify(competitionSetupServiceMock, only()).updateCompetitionInitialDetails(competitionId, competitionResource, leadTechnologistUserId);
    }

    @Test
    public void testMarkSectionAsComplete() throws Exception {
        final Long competitionId = 5L;
        final CompetitionSetupSection section = CompetitionSetupSection.APPLICATION_FORM;
        final SetupStatusResource setupStatusResource = newSetupStatusResource().build();

        when(competitionSetupServiceMock.markSectionComplete(competitionId, section))
                .thenReturn(serviceSuccess(setupStatusResource));

        mockMvc.perform(put("/competition/setup/section-status/complete/{competitionId}/{section}",
                competitionId, section))
                .andExpect(status().is2xxSuccessful());

        verify(competitionSetupServiceMock, only()).markSectionComplete(competitionId, section);
    }

    @Test
    public void testMarkSectionAsInComplete() throws Exception {
        final Long competitionId = 5L;
        final CompetitionSetupSection section = CompetitionSetupSection.APPLICATION_FORM;
        final SetupStatusResource setupStatusResource = newSetupStatusResource().build();

        when(competitionSetupServiceMock.markSectionIncomplete(competitionId, section))
                .thenReturn(serviceSuccess(setupStatusResource));

        mockMvc.perform(put("/competition/setup/section-status/incomplete/{competitionId}/{section}",
                competitionId, section))
                .andExpect(status().is2xxSuccessful());

        verify(competitionSetupServiceMock, only()).markSectionIncomplete(competitionId, section);
    }

    @Test
    public void testGetSectionStatuses() throws Exception {
        final Long competitionId = 5L;
        final Map<CompetitionSetupSection, Optional<Boolean>> sectionStatuses = asMap(CompetitionSetupSection.INITIAL_DETAILS, Optional.of(TRUE),
                CompetitionSetupSection.CONTENT, Optional.of(TRUE),
                CompetitionSetupSection.APPLICATION_FORM, Optional.of(FALSE));

        when(competitionSetupServiceMock.getSectionStatuses(competitionId)).thenReturn(serviceSuccess(sectionStatuses));

        mockMvc.perform(get("/competition/setup/section-status/{competitionId}", competitionId))
                .andExpect(status().is2xxSuccessful());

        verify(competitionSetupServiceMock, only()).getSectionStatuses(competitionId);
    }

    @Test
    public void testMarkSubsectionAsComplete() throws Exception {
        final Long competitionId = 5L;
        final CompetitionSetupSection parentSection = CompetitionSetupSection.APPLICATION_FORM;
        final CompetitionSetupSubsection subsection = CompetitionSetupSubsection.APPLICATION_DETAILS;
        final SetupStatusResource setupStatusResource = newSetupStatusResource().build();

        when(competitionSetupServiceMock.markSubsectionComplete(competitionId, parentSection, subsection))
                .thenReturn(serviceSuccess(setupStatusResource));

        mockMvc.perform(put("/competition/setup/subsection-status/complete/{competitionId}/{parentSection}/{subsection}",
                competitionId, parentSection, subsection))
                .andExpect(status().is2xxSuccessful());

        verify(competitionSetupServiceMock, only()).markSubsectionComplete(competitionId, parentSection, subsection);
    }


    @Test
    public void testMarkSubsectionAsInComplete() throws Exception {
        final Long competitionId = 5L;
        final CompetitionSetupSection parentSection = CompetitionSetupSection.APPLICATION_FORM;
        final CompetitionSetupSubsection subsection = CompetitionSetupSubsection.APPLICATION_DETAILS;
        final SetupStatusResource setupStatusResource = newSetupStatusResource().build();

        when(competitionSetupServiceMock.markSubsectionIncomplete(competitionId, parentSection, subsection))
                .thenReturn(serviceSuccess(setupStatusResource));

        mockMvc.perform(put("/competition/setup/subsection-status/incomplete/{competitionId}/{parentSection}/{subsection}",
                competitionId, parentSection, subsection))
                .andExpect(status().is2xxSuccessful());

        verify(competitionSetupServiceMock, only()).markSubsectionIncomplete(competitionId, parentSection, subsection);
    }

    @Test
    public void testGetSubsectionStatuses() throws Exception {
        final Long competitionId = 5L;
        final Map<CompetitionSetupSubsection, Optional<Boolean>> subsectionStatuses = asMap(CompetitionSetupSubsection.APPLICATION_DETAILS, Optional.of(TRUE),
                CompetitionSetupSubsection.FINANCES, Optional.empty());

        when(competitionSetupServiceMock.getSubsectionStatuses(competitionId)).thenReturn(serviceSuccess(subsectionStatuses));

        mockMvc.perform(get("/competition/setup/subsection-status/{competitionId}", competitionId))
                .andExpect(status().is2xxSuccessful());

        verify(competitionSetupServiceMock, only()).getSubsectionStatuses(competitionId);
    }


    @Test
    public void testDelete() throws Exception {
        final long competitionId = 1L;

        when(competitionSetupServiceMock.deleteCompetition(competitionId)).thenReturn(serviceSuccess());

        mockMvc.perform(delete("/competition/setup/{id}", competitionId)).andExpect(status().isNoContent());

        verify(competitionSetupServiceMock, only()).deleteCompetition(competitionId);
    }
}
