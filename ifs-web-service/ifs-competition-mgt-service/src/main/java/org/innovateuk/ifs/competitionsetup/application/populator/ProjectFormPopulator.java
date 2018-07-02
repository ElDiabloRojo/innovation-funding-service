package org.innovateuk.ifs.competitionsetup.application.populator;

import org.innovateuk.ifs.commons.exception.ObjectNotFoundException;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.application.form.ProjectForm;
import org.innovateuk.ifs.competitionsetup.core.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.core.populator.CompetitionSetupSubsectionFormPopulator;
import org.innovateuk.ifs.competitionsetup.core.service.CompetitionSetupQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Form populator for the application form competition setup section.
 */
@Service
public class ProjectFormPopulator implements CompetitionSetupSubsectionFormPopulator {

	@Autowired
	private CompetitionSetupQuestionService competitionSetupQuestionService;

	@Override
	public CompetitionSetupSubsection sectionToFill() {
		return CompetitionSetupSubsection.PROJECT_DETAILS;
	}

	@Override
	public CompetitionSetupForm populateForm(CompetitionResource competitionResource, Optional<Long> objectId) {

		ProjectForm competitionSetupForm = new ProjectForm();

		if(objectId.isPresent()) {
			CompetitionSetupQuestionResource questionResource = competitionSetupQuestionService.getQuestion(objectId.get()).getSuccess();
			competitionSetupForm.setQuestion(questionResource);
			competitionSetupForm.setRemovable(true);


		} else {
            throw new ObjectNotFoundException();
        }

		return competitionSetupForm;
	}
}
