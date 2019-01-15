package org.innovateuk.ifs.competition.populator.publiccontent.section;

import org.innovateuk.ifs.competition.publiccontent.resource.*;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.competition.viewmodel.publiccontent.section.DatesViewModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.MilestoneResourceBuilder.newMilestoneResource;
import static org.innovateuk.ifs.publiccontent.builder.ContentEventResourceBuilder.newContentEventResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentItemResourceBuilder.newPublicContentItemResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentResourceBuilder.newPublicContentResource;
import static org.innovateuk.ifs.publiccontent.builder.PublicContentSectionResourceBuilder.newPublicContentSectionResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Testing {@link DatesViewModelPopulator}
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class DatesViewModelPopulatorTest {

    @Mock
    private MilestoneRestService milestoneRestService;

    @InjectMocks
    private DatesViewModelPopulator populator;

    private DatesViewModel viewModel;
    private PublicContentItemResource publicContentItemResource;
    private PublicContentSectionResource publicContentSectionResource;

    @Before
    public void setup() {
        viewModel = new DatesViewModel();

        publicContentSectionResource = newPublicContentSectionResource()
                .with(sectionResource -> {
                    sectionResource.setId(98125L);
                })
                .withPublicContent(1L)
                .build();
        publicContentItemResource = newPublicContentItemResource().withPublicContentResource(
                newPublicContentResource()
                        .with(contentResource -> {
                            contentResource.setId(89235L);
                        })
                        .withCompetitionId(5372L)
                        .withSummary("Summary")
                        .withProjectSize("5M")
                        .withContentSections(asList(publicContentSectionResource))
                        .build())
                .build();
    }

    @Test
    public void populateSectionWithMilestonesFound() {
        when(milestoneRestService.getAllPublicMilestonesByCompetitionId(publicContentItemResource.getPublicContentResource().getCompetitionId()))
                .thenReturn(restSuccess(newMilestoneResource()
                        .withDate(ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now())
                        .withType(MilestoneType.OPEN_DATE, MilestoneType.NOTIFICATIONS, MilestoneType.SUBMISSION_DATE)
                        .build(3)));
        publicContentItemResource.getPublicContentResource().setContentEvents(emptyList());

        populator.populateSection(viewModel, publicContentItemResource, publicContentSectionResource, Boolean.FALSE);

        assertEquals(3, viewModel.getPublicContentDates().size());
    }

    @Test
    public void populateSectionWithMilestonesNotFound() {
        when(milestoneRestService.getAllPublicMilestonesByCompetitionId(publicContentItemResource.getPublicContentResource().getCompetitionId()))
                .thenReturn(restSuccess(emptyList()));
        publicContentItemResource.getPublicContentResource().setContentEvents(emptyList());

        populator.populateSection(viewModel, publicContentItemResource, publicContentSectionResource, Boolean.FALSE);

        assertEquals(0, viewModel.getPublicContentDates().size());
    }

    @Test
    public void populateSectionWithNoPublicContentDates() {
        when(milestoneRestService.getAllPublicMilestonesByCompetitionId(publicContentItemResource.getPublicContentResource().getCompetitionId()))
                .thenReturn(restSuccess(emptyList()));
        publicContentItemResource.getPublicContentResource().setContentEvents(emptyList());

        populator.populateSection(viewModel, publicContentItemResource, publicContentSectionResource, Boolean.FALSE);

        assertEquals(0, viewModel.getPublicContentDates().size());
    }

    @Test
    public void populateSectionWithPublicContentDatesAndMilestones() {
        when(milestoneRestService.getAllPublicMilestonesByCompetitionId(publicContentItemResource.getPublicContentResource().getCompetitionId()))
                .thenReturn(restSuccess(newMilestoneResource()
                        .withDate(ZonedDateTime.now(), ZonedDateTime.now().plusDays(3))
                        .withType(MilestoneType.OPEN_DATE, MilestoneType.SUBMISSION_DATE)
                        .build(2)));
        publicContentItemResource.getPublicContentResource().setContentEvents(newContentEventResource().build(2));

        populator.populateSection(viewModel, publicContentItemResource, publicContentSectionResource, Boolean.FALSE);

        assertEquals(2, viewModel.getPublicContentDates().size());
    }

    @Test
    public void populateSectionWithPublicContentDatesAndMilestonesNonIFS() {
        when(milestoneRestService.getAllPublicMilestonesByCompetitionId(publicContentItemResource.getPublicContentResource().getCompetitionId()))
                .thenReturn(restSuccess(newMilestoneResource()
                        .withDate(ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2), ZonedDateTime.now().plusDays(3), ZonedDateTime.now().plusDays(4))
                        .withType(MilestoneType.OPEN_DATE, MilestoneType.REGISTRATION_DATE, MilestoneType.NOTIFICATIONS, MilestoneType.SUBMISSION_DATE)
                        .build(4)));
        publicContentItemResource.getPublicContentResource().setContentEvents(newContentEventResource().build(2));

        populator.populateSection(viewModel, publicContentItemResource, publicContentSectionResource, Boolean.TRUE);

        assertEquals(4, viewModel.getPublicContentDates().size());
    }

    @Test
    public void getType() {
        assertEquals(PublicContentSectionType.DATES, populator.getType());
    }
}
