package org.innovateuk.ifs.competitionsetup.eligibility.sectionupdater;

import com.google.common.collect.Sets;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.form.enumerable.ResearchParticipationAmount;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.service.CompetitionSetupRestService;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.competitionsetup.core.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.eligibility.form.EligibilityForm;
import org.innovateuk.ifs.finance.resource.GrantClaimMaximumResource;
import org.innovateuk.ifs.finance.service.GrantClaimMaximumRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.primitives.Longs.asList;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.ApplicationFinanceType.STANDARD;
import static org.innovateuk.ifs.competition.resource.CompetitionResource.NON_FINANCE_TYPES;
import static org.innovateuk.ifs.finance.builder.GrantClaimMaximumResourceBuilder.newGrantClaimMaximumResource;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.util.CollectionFunctions.asLinkedSet;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EligibilitySectionSaverTest {

    @InjectMocks
    private EligibilitySectionUpdater service;

    @Mock
    private MilestoneRestService milestoneRestService;

    @Mock
    private CompetitionSetupRestService competitionSetupRestService;

    @Mock
    private GrantClaimMaximumRestService grantClaimMaximumRestService;

    @Test
    public void saveSection() {
        EligibilityForm competitionSetupForm = new EligibilityForm();
        competitionSetupForm.setLeadApplicantTypes(asList(1L, 2L));
        competitionSetupForm.setMultipleStream("yes");
        competitionSetupForm.setStreamName("streamname");
        competitionSetupForm.setResubmission("yes");
        competitionSetupForm.setResearchCategoryId(asLinkedSet(1L, 2L, 3L));
        competitionSetupForm.setResearchParticipationAmountId(ResearchParticipationAmount.THIRTY.getId());
        competitionSetupForm.setSingleOrCollaborative("collaborative");
        competitionSetupForm.setOverrideFundingRules(true);
        competitionSetupForm.setFundingLevelPercentage(10);

        List<GrantClaimMaximumResource> gcms = newGrantClaimMaximumResource().build(2);

        CompetitionResource competition = newCompetitionResource()
                .withGrantClaimMaximums(asLinkedSet(gcms.get(0).getId(), gcms.get(1).getId()))
                .withApplicationFinanceType(STANDARD)
                .build();

        when(competitionSetupRestService.update(competition)).thenReturn(restSuccess());
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(0).getId())).thenReturn(restSuccess(gcms.get(0)));
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(1).getId())).thenReturn(restSuccess(gcms.get(1)));
        when(grantClaimMaximumRestService.save(any())).thenReturn(restSuccess(gcms.get(0)));

        service.saveSection(competition, competitionSetupForm).getSuccess();

        assertEquals(asList(BUSINESS.getId(), OrganisationTypeEnum.RESEARCH.getId()), competition.getLeadApplicantTypes());
        assertTrue(competition.isMultiStream());
        assertEquals("streamname", competition.getStreamName());
        assertEquals(asLinkedSet(1L, 2L, 3L), competition.getResearchCategories());
        assertEquals(ResearchParticipationAmount.THIRTY.getAmount(), competition.getMaxResearchRatio());
        assertEquals(CollaborationLevel.COLLABORATIVE, competition.getCollaborationLevel());

        verify(competitionSetupRestService).update(competition);
    }

    @Test
    public void saveSection_withoutResearchParticipationAmountIdDefaultsToNone() {
        EligibilityForm competitionSetupForm = new EligibilityForm();
        competitionSetupForm.setOverrideFundingRules(true);
        competitionSetupForm.setFundingLevelPercentage(50);
        List<GrantClaimMaximumResource> gcms = newGrantClaimMaximumResource()
                .build(2);

        CompetitionResource competition = newCompetitionResource()
                .withApplicationFinanceType(STANDARD)
                .withGrantClaimMaximums(asLinkedSet(gcms.get(0).getId(), gcms.get(1).getId()))
                .build();

        when(competitionSetupRestService.update(competition)).thenReturn(restSuccess());
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(0).getId())).thenReturn(restSuccess(gcms.get(0)));
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(1).getId())).thenReturn(restSuccess(gcms.get(1)));
        when(grantClaimMaximumRestService.save(any())).thenReturn(restSuccess(gcms.get(0)));

        service.saveSection(competition, competitionSetupForm).getSuccess();

        assertEquals(ResearchParticipationAmount.NONE.getAmount(), competition.getMaxResearchRatio());

        verify(competitionSetupRestService).update(competition);
    }

    @Test
    public void saveSection_defaultsMaxResearchRatioToNoneForCompetitionsWithNoFinances() {
        EligibilityForm competitionSetupForm = new EligibilityForm();
        competitionSetupForm.setResearchParticipationAmountId(ResearchParticipationAmount.HUNDRED.getId());
        competitionSetupForm.setOverrideFundingRules(true);
        competitionSetupForm.setFundingLevelPercentage(50);

        List<GrantClaimMaximumResource> gcms = newGrantClaimMaximumResource().build(2);

        CompetitionResource competition = newCompetitionResource()
                .withCompetitionTypeName(NON_FINANCE_TYPES.iterator().next())
                .withGrantClaimMaximums(asLinkedSet(gcms.get(0).getId(), gcms.get(1).getId()))
                .build();

        when(competitionSetupRestService.update(competition)).thenReturn(restSuccess());
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(0).getId())).thenReturn(restSuccess(gcms.get(0)));
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(1).getId())).thenReturn(restSuccess(gcms.get(1)));
        when(grantClaimMaximumRestService.save(any())).thenReturn(restSuccess(gcms.get(0)));

        service.saveSection(competition, competitionSetupForm).getSuccess();

        assertEquals(0, competition.getMaxResearchRatio().intValue());

        verify(competitionSetupRestService).update(competition);
    }

    @Test
    public void saveSectionWithoutOverriddenFundingRules() {
        EligibilityForm competitionSetupForm = new EligibilityForm();
        competitionSetupForm.setResearchParticipationAmountId(ResearchParticipationAmount.HUNDRED.getId());
        competitionSetupForm.setOverrideFundingRules(false);

        List<GrantClaimMaximumResource> gcms = newGrantClaimMaximumResource().build(4);

        CompetitionResource competition = newCompetitionResource()
                .withCompetitionType(BUSINESS.getId())
                .withGrantClaimMaximums(asLinkedSet(gcms.get(0).getId(), gcms.get(1).getId()))
                .build();

        CompetitionResource template = newCompetitionResource()
                .withCompetitionType(BUSINESS.getId())
                .withGrantClaimMaximums(asLinkedSet(gcms.get(2).getId(), gcms.get(3).getId()))
                .build();

        when(grantClaimMaximumRestService.getGrantClaimMaximumsForCompetitionType(competition.getCompetitionType())).thenReturn(restSuccess(template.getGrantClaimMaximums()));
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(0).getId())).thenReturn(restSuccess(gcms.get(0)));
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(1).getId())).thenReturn(restSuccess(gcms.get(1)));
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(2).getId())).thenReturn(restSuccess(gcms.get(2)));
        when(grantClaimMaximumRestService.getGrantClaimMaximumById(gcms.get(3).getId())).thenReturn(restSuccess(gcms.get(3)));
        when(competitionSetupRestService.update(competition)).thenReturn(restSuccess());

        service.saveSection(competition, competitionSetupForm).getSuccess();

        verify(grantClaimMaximumRestService).getGrantClaimMaximumById(gcms.get(0).getId());
        verify(grantClaimMaximumRestService).getGrantClaimMaximumById(gcms.get(1).getId());
        verify(grantClaimMaximumRestService).getGrantClaimMaximumsForCompetitionType(competition.getCompetitionType());
        verify(competitionSetupRestService).update(competition);

        assertEquals(asLinkedSet(gcms.get(2).getId(), gcms.get(3).getId()), competition.getGrantClaimMaximums());
    }

    @Test
    public void autoSaveResearchCategoryCheck() {
        when(milestoneRestService.getAllMilestonesByCompetitionId(1L)).thenReturn(restSuccess(singletonList(getMilestone())));
        EligibilityForm form = new EligibilityForm();
        Set<Long> researchCategories = Sets.newHashSet(33L, 34L);

        CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
        competition.setMilestones(singletonList(10L));
        when(competitionSetupRestService.update(competition)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.autoSaveSectionField(competition, form, "researchCategoryId", "35", null);

        assertTrue(result.isSuccess());
        verify(competitionSetupRestService).update(competition);

        assertTrue(competition.getResearchCategories().contains(35L));
    }

    @Test
    public void autoSaveResearchCategoryUncheck() {
        when(milestoneRestService.getAllMilestonesByCompetitionId(1L)).thenReturn(restSuccess(singletonList(getMilestone())));
        EligibilityForm form = new EligibilityForm();
        Set<Long> researchCategories = newHashSet(33L, 34L, 35L);

        CompetitionResource competition = newCompetitionResource().withResearchCategories(researchCategories).build();
        competition.setMilestones(singletonList(10L));
        when(competitionSetupRestService.update(competition)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.autoSaveSectionField(competition, form, "researchCategoryId", "35", null);

        assertTrue(result.isSuccess());
        verify(competitionSetupRestService).update(competition);

        assertTrue(!competition.getResearchCategories().contains(35L));
    }

    private MilestoneResource getMilestone() {
        MilestoneResource milestone = new MilestoneResource();
        milestone.setId(10L);
        milestone.setType(MilestoneType.OPEN_DATE);
        milestone.setDate(ZonedDateTime.of(2020, 12, 1, 0, 0, 0, 0, ZoneId.systemDefault()));
        milestone.setCompetitionId(1L);
        return milestone;
    }

    @Test
    public void supportsForm() {
        assertTrue(service.supportsForm(EligibilityForm.class));
        assertFalse(service.supportsForm(CompetitionSetupForm.class));
    }
}
