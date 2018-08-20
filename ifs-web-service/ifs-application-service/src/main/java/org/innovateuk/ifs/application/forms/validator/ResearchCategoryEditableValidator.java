package org.innovateuk.ifs.application.forms.validator;

import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.service.QuestionService;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.question.resource.QuestionSetupType.RESEARCH_CATEGORY;

/**
 * Component that can be used to check if research category can be edited or not.
 */
@Component
public class ResearchCategoryEditableValidator extends QuestionEditableValidator {

    public ResearchCategoryEditableValidator(final QuestionService questionService,
                                             final QuestionRestService questionRestService) {
        super(questionService, questionRestService, RESEARCH_CATEGORY);
    }
}