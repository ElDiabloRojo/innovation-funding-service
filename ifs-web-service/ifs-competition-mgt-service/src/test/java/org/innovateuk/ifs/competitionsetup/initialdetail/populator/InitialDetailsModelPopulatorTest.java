package org.innovateuk.ifs.competitionsetup.initialdetail.populator;

import org.innovateuk.ifs.category.resource.InnovationAreaResource;
import org.innovateuk.ifs.category.resource.InnovationSectorResource;
import org.innovateuk.ifs.category.service.CategoryRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.CompetitionTypeResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.competitionsetup.core.viewmodel.GeneralSetupViewModel;
import org.innovateuk.ifs.competitionsetup.initialdetail.viewmodel.InitialDetailsViewModel;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.innovateuk.ifs.user.service.UserService;
import org.innovateuk.ifs.util.CollectionFunctions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.innovateuk.ifs.category.builder.InnovationAreaResourceBuilder.newInnovationAreaResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.user.resource.Role.COMP_ADMIN;
import static org.innovateuk.ifs.user.resource.Role.INNOVATION_LEAD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InitialDetailsModelPopulatorTest {

    @InjectMocks
    private InitialDetailsModelPopulator populator;

    @Mock
    private CategoryRestService categoryRestService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private UserRestService userRestService;

    @Test
    public void testSectionToPopulateModel() {
        CompetitionSetupSection result = populator.sectionToPopulateModel();

        assertEquals(CompetitionSetupSection.INITIAL_DETAILS, result);
    }

    @Test
    public void testPopulateModel() {
        CompetitionResource competition = newCompetitionResource()
                .withCompetitionCode("code")
                .withName("name")
                .withId(8L)
                .withResearchCategories(CollectionFunctions.asLinkedSet(2L, 3L))
                .build();

        List<UserResource> compExecs = new ArrayList<>();
        when(userRestService.findByUserRole(COMP_ADMIN)).thenReturn(restSuccess(compExecs));
        List<InnovationSectorResource> innovationSectors = new ArrayList<>();
        when(categoryRestService.getInnovationSectors()).thenReturn(restSuccess(innovationSectors));
        List<InnovationAreaResource> innovationAreas = newInnovationAreaResource().build(2);
        when(categoryRestService.getInnovationAreas()).thenReturn(restSuccess(innovationAreas));
        List<CompetitionTypeResource> competitionTypes = new ArrayList<>();
        when(competitionRestService.getCompetitionTypes()).thenReturn(restSuccess(competitionTypes));
        List<UserResource> leadTechs = new ArrayList<>();
        when(userRestService.findByUserRole(INNOVATION_LEAD)).thenReturn(restSuccess(leadTechs));

        InitialDetailsViewModel viewModel = (InitialDetailsViewModel) populator.populateModel(getBasicGeneralSetupView(competition), competition);

        assertEquals(compExecs, viewModel.getCompetitionExecutiveUsers());
        assertEquals(innovationSectors, viewModel.getInnovationSectors());
        assertTrue(viewModel.getInnovationAreas().containsAll(innovationAreas));
        assertEquals(competitionTypes, viewModel.getCompetitionTypes());
        assertEquals(leadTechs, viewModel.getInnovationLeadTechUsers());
        assertEquals(CompetitionSetupSection.INITIAL_DETAILS, viewModel.getGeneral().getCurrentSection());
    }

    private GeneralSetupViewModel getBasicGeneralSetupView(CompetitionResource competition) {
        return new GeneralSetupViewModel(Boolean.FALSE, competition, CompetitionSetupSection.INITIAL_DETAILS, CompetitionSetupSection.values(), Boolean.TRUE);
    }
}
