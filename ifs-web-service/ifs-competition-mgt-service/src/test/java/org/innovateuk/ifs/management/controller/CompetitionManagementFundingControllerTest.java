package org.innovateuk.ifs.management.controller;

import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationFundingDecisionService;
import org.innovateuk.ifs.application.service.ApplicationSummaryService;
import org.innovateuk.ifs.application.service.AssessorFeedbackService;
import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.ApplicationSummarySortFieldService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.application.builder.CompetitionSummaryResourceBuilder.newCompetitionSummaryResource;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.ASSESSOR_FEEDBACK;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.FUNDERS_PANEL;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class CompetitionManagementFundingControllerTest {

    public static final Long COMPETITION_ID = Long.valueOf(123L);

    @InjectMocks
    private CompetitionManagementFundingController controller;

    @Mock
    private ApplicationSummaryService applicationSummaryService;

    @Mock
    private ApplicationSummarySortFieldService applicationSummarySortFieldService;

    @Mock
    private AssessorFeedbackService assessorFeedbackService;

    @Mock
    private CompetitionService competitionService;

    @Mock
    private ApplicationFundingDecisionService applicationFundingDecisionService;

    private MockMvc mockMvc;

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }


    @Test
    public void getByCompetitionIdForCompetitionFundersPanelNotSubmittedRequested() throws Exception {
        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(COMPETITION_ID).withCompetitionStatus(FUNDERS_PANEL).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(COMPETITION_ID)).thenReturn(competitionSummaryResource);

        when(applicationSummarySortFieldService.sortFieldForNotSubmittedApplications(null)).thenReturn("sortfield");

        ApplicationSummaryPageResource summary = new ApplicationSummaryPageResource();
        when(applicationSummaryService.getNotSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, 20)).thenReturn(summary);

        mockMvc.perform(get("/competition/{competitionId}/funding?tab=notSubmitted", COMPETITION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comp-mgt-funders-panel"))
                .andExpect(model().attribute("competitionSummary", competitionSummaryResource))
                .andExpect(model().attribute("results", summary))
                .andExpect(model().attribute("activeTab", "notSubmitted"))
                .andExpect(model().attribute("activeSortField", "sortfield"));

        verify(applicationSummaryService).getNotSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, 20);
        verify(applicationSummaryService).getCompetitionSummaryByCompetitionId(COMPETITION_ID);
    }

    @Test
    public void getByCompetitionIdForCompetitionFundersPanelSubmittedRequested() throws Exception {
        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(COMPETITION_ID).withCompetitionStatus(FUNDERS_PANEL).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(COMPETITION_ID)).thenReturn(competitionSummaryResource);

        when(applicationSummarySortFieldService.sortFieldForSubmittedApplications(null)).thenReturn("sortfield");

        ApplicationSummaryPageResource summary = new ApplicationSummaryPageResource();
        when(applicationSummaryService.getSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, Integer.MAX_VALUE)).thenReturn(summary);

        mockMvc.perform(get("/competition/{competitionId}/funding?tab=submitted", COMPETITION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comp-mgt-funders-panel"))
                .andExpect(model().attribute("competitionSummary", competitionSummaryResource))
                .andExpect(model().attribute("results", summary))
                .andExpect(model().attribute("activeTab", "submitted"))
                .andExpect(model().attribute("activeSortField", "sortfield"));

        verify(applicationSummaryService).getSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, Integer.MAX_VALUE);
        verify(applicationSummaryService).getCompetitionSummaryByCompetitionId(COMPETITION_ID);
    }

    @Test
    public void getByCompetitionIdForCompetitionAssessorFeedbackNotSubmittedRequested() throws Exception {
        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(COMPETITION_ID).withCompetitionStatus(ASSESSOR_FEEDBACK).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(COMPETITION_ID)).thenReturn(competitionSummaryResource);

        when(applicationSummarySortFieldService.sortFieldForNotSubmittedApplications(null)).thenReturn("sortfield");

        ApplicationSummaryPageResource summary = new ApplicationSummaryPageResource();
        when(applicationSummaryService.getNotSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, 20)).thenReturn(summary);

        mockMvc.perform(get("/competition/{competitionId}/funding?tab=notSubmitted", COMPETITION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comp-mgt-assessor-feedback"))
                .andExpect(model().attribute("competitionSummary", competitionSummaryResource))
                .andExpect(model().attribute("results", summary))
                .andExpect(model().attribute("activeTab", "notSubmitted"))
                .andExpect(model().attribute("activeSortField", "sortfield"));

        verify(applicationSummaryService).getNotSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, 20);
        verify(applicationSummaryService).getCompetitionSummaryByCompetitionId(COMPETITION_ID);
    }

    @Test
    public void getByCompetitionIdForCompetitionAssessorFeedbackSubmittedRequested() throws Exception {
        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(COMPETITION_ID).withCompetitionStatus(ASSESSOR_FEEDBACK).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(COMPETITION_ID)).thenReturn(competitionSummaryResource);

        when(applicationSummarySortFieldService.sortFieldForSubmittedApplications(null)).thenReturn("sortfield");

        ApplicationSummaryPageResource summary = new ApplicationSummaryPageResource();
        when(applicationSummaryService.getSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, 20)).thenReturn(summary);

        when(assessorFeedbackService.feedbackUploaded(COMPETITION_ID)).thenReturn(false);

        mockMvc.perform(get("/competition/{competitionId}/funding?tab=submitted", COMPETITION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comp-mgt-assessor-feedback"))
                .andExpect(model().attribute("competitionSummary", competitionSummaryResource))
                .andExpect(model().attribute("results", summary))
                .andExpect(model().attribute("activeTab", "submitted"))
                .andExpect(model().attribute("activeSortField", "sortfield"))
                .andExpect(model().attribute("canPublishAssessorFeedback", false));

        verify(applicationSummaryService).getSubmittedApplicationSummariesByCompetitionId(COMPETITION_ID, "sortfield", 0, 20);
        verify(applicationSummaryService).getCompetitionSummaryByCompetitionId(COMPETITION_ID);
        verify(assessorFeedbackService).feedbackUploaded(COMPETITION_ID);
    }

    @Test
    public void getByCompetitionIdForCompetitionAssessorFeedbackOverviewRequested() throws Exception {
        CompetitionSummaryResource competitionSummaryResource = newCompetitionSummaryResource().withId(COMPETITION_ID).withCompetitionStatus(ASSESSOR_FEEDBACK).build();
        when(applicationSummaryService.getCompetitionSummaryByCompetitionId(COMPETITION_ID)).thenReturn(competitionSummaryResource);

        when(applicationSummaryService.getApplicationsRequiringFeedbackCountByCompetitionId(COMPETITION_ID)).thenReturn(3L);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime assessmentStartDate = now.minusDays(7L);
        LocalDateTime assessorDeadlineDate = now.plusDays(2L);
        LocalDateTime fundersPanelDate = now.plusDays(3L);

        CompetitionResource competition = newCompetitionResource()
                .withAssessorAcceptsDate(assessmentStartDate)
                .withAssessorDeadlineDate(assessorDeadlineDate)
                .withFundersPanelDate(fundersPanelDate)
                .build();
        when(competitionService.getById(COMPETITION_ID)).thenReturn(competition);

        mockMvc.perform(get("/competition/{competitionId}/funding?tab=overview", COMPETITION_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("comp-mgt-assessor-feedback"))
                .andExpect(model().attribute("competitionSummary", competitionSummaryResource))
                .andExpect(model().attribute("activeTab", "overview"))
                .andExpect(model().attribute("applicationsRequiringFeedback", 3L))
                .andExpect(model().attribute("assessmentEndDate", fundersPanelDate))
                .andExpect(model().attribute("assessmentDaysLeft", 1L))
                .andExpect(model().attribute("assessmentDaysLeftPercentage", 88L));

        verify(applicationSummaryService).getCompetitionSummaryByCompetitionId(COMPETITION_ID);
    }
    @Before
    public void setup() {

        ApplicationSummaryPageResource applicationSummaries = new ApplicationSummaryPageResource();
        ApplicationSummaryResource app8 = app(8L);
        ApplicationSummaryResource app9 = app(9L);
        ApplicationSummaryResource app10 = app(10L);
        applicationSummaries.setContent(asList(app8, app9, app10));
        when(applicationSummaryService.getSubmittedApplicationSummariesByCompetitionId(123L, null, 0, Integer.MAX_VALUE)).thenReturn(applicationSummaries);
    }

    private ApplicationSummaryResource app(Long id) {
        ApplicationSummaryResource app = new ApplicationSummaryResource();
        app.setId(id);
        return app;
    }


    @SuppressWarnings("unchecked")
    @Test
    public void saveFundingDecision() throws Exception {
        String fundingDecisionChoice = "ON_HOLD";

        Map<Long, FundingDecision> expectedDecisionMap = new HashMap<>();
        expectedDecisionMap.put(8L, FundingDecision.ON_HOLD);
        expectedDecisionMap.put(9L, FundingDecision.ON_HOLD);

        when(applicationFundingDecisionService.fundingDecisionForString(fundingDecisionChoice)).thenReturn(FundingDecision.ON_HOLD);

        mockMvc.perform(
                post("/competition/123/fundingdecisionsubmit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("applicationIds", "8")
                        .param("applicationIds", "9")
                        .param("fundingDecisionChoice", fundingDecisionChoice)
        )
                .andExpect(redirectedUrl("/competition/123/funding"));

        verify(applicationFundingDecisionService).fundingDecisionForString(fundingDecisionChoice);
        //verify(applicationFundingDecisionService).saveApplicationFundingDecisionData(123L, expectedDecisionMap);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void saveFundingDecisionUnsubmittedApplications() throws Exception {
        String fundingDecisionChoice = "ON_HOLD";

        List<Long> applicationIds = new ArrayList<>();
        applicationIds.add(8L);
        applicationIds.add(9L);

        when(applicationFundingDecisionService.fundingDecisionForString(fundingDecisionChoice)).thenReturn(FundingDecision.ON_HOLD);

        mockMvc.perform(
                post("/competition/123/fundingdecisionsubmit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("applicationIds", "6")
                        .param("applicationIds", "7")
                        .param("applicationIds", "8")
                        .param("applicationIds", "9")
                        .param("fundingDecisionChoice", fundingDecisionChoice)
        )
                .andExpect(redirectedUrl("/competition/123/applications"));

        verify(applicationFundingDecisionService).fundingDecisionForString(fundingDecisionChoice);
        verify(applicationFundingDecisionService).saveApplicationFundingDecisionData(123L, FundingDecision.ON_HOLD.name(), applicationIds);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void saveFundingDecisionNonExistentFundingChoice() throws Exception {
        String fundingDecisionChoice = "ON_HOLD";

        Map<Long, FundingDecision> expectedDecisionMap = new HashMap<>();
        expectedDecisionMap.put(8L, FundingDecision.ON_HOLD);
        expectedDecisionMap.put(9L, FundingDecision.ON_HOLD);

        when(applicationFundingDecisionService.fundingDecisionForString(fundingDecisionChoice)).thenReturn(FundingDecision.ON_HOLD);

        mockMvc.perform(
                post("/competition/123/fundingdecisionsubmit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("applicationIds", "6")
                        .param("applicationIds", "7")
                        .param("applicationIds", "8")
                        .param("applicationIds", "9")
                        .param("fundingDecisionChoice", fundingDecisionChoice)
        )
                .andExpect(redirectedUrl("/competition/123/applications"));

        verify(applicationFundingDecisionService).fundingDecisionForString(fundingDecisionChoice);
        //verify(applicationFundingDecisionService).saveApplicationFundingDecisionData(123L, expectedDecisionMap);
    }
}
