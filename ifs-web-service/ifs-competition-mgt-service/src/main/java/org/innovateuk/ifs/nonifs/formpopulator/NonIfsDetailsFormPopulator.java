package org.innovateuk.ifs.nonifs.formpopulator;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competitionsetup.milestone.form.MilestoneOrEmptyRowForm;
import org.innovateuk.ifs.competitionsetup.milestone.form.MilestoneRowForm;
import org.innovateuk.ifs.nonifs.form.NonIfsDetailsForm;
import org.springframework.stereotype.Service;

/**
 * Populates a {@link org.innovateuk.ifs.nonifs.form.NonIfsDetailsForm}
 */
@Service
public class NonIfsDetailsFormPopulator {

    public NonIfsDetailsForm populate(CompetitionResource competitionResource) {
        NonIfsDetailsForm form = new NonIfsDetailsForm();
        form.setTitle(competitionResource.getName());
        form.setInnovationSectorCategoryId(competitionResource.getInnovationSector());
        form.setInnovationAreaCategoryId(competitionResource.getInnovationAreas().stream().findAny().orElse(null));
        form.setUrl(competitionResource.getNonIfsUrl());
        form.setApplicantNotifiedDate(new MilestoneOrEmptyRowForm(MilestoneType.NOTIFICATIONS, competitionResource.getFundersPanelEndDate()));
        form.setRegistrationCloseDate(new MilestoneRowForm(MilestoneType.REGISTRATION_DATE, competitionResource.getRegistrationDate()));
        form.setOpenDate(new MilestoneRowForm(MilestoneType.OPEN_DATE, competitionResource.getStartDate()));
        form.setCloseDate(new MilestoneRowForm(MilestoneType.SUBMISSION_DATE, competitionResource.getEndDate()));
        return form;
    }
}
