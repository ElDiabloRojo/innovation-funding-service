package org.innovateuk.ifs.competitionsetup.completionstage.populator;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.competitionsetup.completionstage.form.CompletionStageForm;
import org.innovateuk.ifs.competitionsetup.core.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.core.populator.CompetitionSetupFormPopulator;
import org.innovateuk.ifs.competitionsetup.core.service.CompetitionSetupMilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO DW - comment
 */
@Service
public class CompletionStageFormPopulator implements CompetitionSetupFormPopulator {

    @Autowired
    private MilestoneRestService milestoneRestService;

    @Autowired
    private CompetitionSetupMilestoneService competitionSetupMilestoneService;

    @Override
    public CompetitionSetupSection sectionToFill() {
        return CompetitionSetupSection.COMPLETION_STAGE;
    }

    @Override
    public CompetitionSetupForm populateForm(CompetitionResource competitionResource) {
        return new CompletionStageForm(competitionResource.getCompletionStage());
    }
}


