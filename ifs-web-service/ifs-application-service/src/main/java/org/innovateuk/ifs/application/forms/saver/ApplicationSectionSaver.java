package org.innovateuk.ifs.application.forms.saver;

import org.innovateuk.ifs.application.overheads.OverheadFileSaver;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.*;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.error.ValidationMessages.noErrors;

/**
 * This Saver will handle save all sections that are related to the application.
 */
@Service
public class ApplicationSectionSaver extends AbstractApplicationSaver {

    private UserRestService userRestService;
    private SectionService sectionService;
    private QuestionRestService questionRestService;
    private CookieFlashMessageFilter cookieFlashMessageFilter;
    private OverheadFileSaver overheadFileSaver;
    private ApplicationSectionFinanceSaver financeSaver;
    private ApplicationQuestionFileSaver fileSaver;
    private ApplicationQuestionNonFileSaver nonFileSaver;

    public ApplicationSectionSaver(UserRestService userRestService, SectionService sectionService, QuestionRestService questionRestService, CookieFlashMessageFilter cookieFlashMessageFilter, OverheadFileSaver overheadFileSaver, ApplicationSectionFinanceSaver financeSaver, ApplicationQuestionFileSaver fileSaver, ApplicationQuestionNonFileSaver nonFileSaver) {
        this.userRestService = userRestService;
        this.sectionService = sectionService;
        this.questionRestService = questionRestService;
        this.cookieFlashMessageFilter = cookieFlashMessageFilter;
        this.overheadFileSaver = overheadFileSaver;
        this.financeSaver = financeSaver;
        this.fileSaver = fileSaver;
        this.nonFileSaver = nonFileSaver;
    }

    public ValidationMessages saveApplicationForm(ApplicationResource application,
                                                  ApplicationForm form,
                                                  Long sectionId,
                                                  Long userId,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        Long applicationId = application.getId();
        ProcessRoleResource processRole = userRestService.findProcessRole(userId, applicationId).getSuccess();
        SectionResource selectedSection = sectionService.getById(sectionId);
        Map<String, String[]> params = request.getParameterMap();
        boolean ignoreEmpty = !isMarkSectionRequest(params);

        ValidationMessages errors = new ValidationMessages();

        if (!isMarkSectionAsIncompleteRequest(params)) {
            List<QuestionResource> questions = selectedSection.getQuestions()
                    .stream()
                    .map(questionId -> questionRestService.findById(questionId).getSuccess())
                    .collect(Collectors.toList());

            errors.addAll(nonFileSaver.saveNonFileUploadQuestions(questions, request, userId, applicationId, ignoreEmpty));
            errors.addAll(fileSaver.saveFileUploadQuestionsIfAny(questions, request.getParameterMap(), request, applicationId, processRole.getId()));

            if (overheadFileSaver.isOverheadFileRequest(request)) {
                errors.addAll(overheadFileSaver.handleOverheadFileRequest(request));
            }
        }

        if (isMarkSectionRequest(params)) {
            errors.addAll(handleMarkSectionRequest(application, selectedSection, params, processRole, errors));
            financeSaver.handleStateAid(params, application, form, selectedSection);
        }

        cookieFlashMessageFilter.setFlashMessage(response, "applicationSaved");

        return sortValidationMessages(errors);
    }

    private ValidationMessages handleMarkSectionRequest(ApplicationResource application, SectionResource selectedSection, Map<String, String[]> params,
                                                        ProcessRoleResource processRole, ValidationMessages errorsSoFar) {
        ValidationMessages messages = new ValidationMessages();

        if (errorsSoFar.hasErrors()) {
            messages.addError(fieldError("formInput[cost]", "", MARKED_AS_COMPLETE_KEY));
        } else if (isMarkSectionAsIncompleteRequest(params) || isMarkSectionAsCompleteRequest(params)) {
            ValidationMessages financeErrorsMark = markAllQuestionsInSection(application, selectedSection, processRole.getId(), params);

            if (financeErrorsMark.hasErrors()) {
                messages.addError(fieldError("formInput[cost]", "", MARKED_AS_COMPLETE_KEY));
                messages.addAll(financeErrorsMark);
            }
        }

        return messages;
    }

    private ValidationMessages markAllQuestionsInSection(ApplicationResource application,
                                                               SectionResource selectedSection,
                                                               Long processRoleId,
                                                               Map<String, String[]> params) {
        if (isMarkSectionAsCompleteRequest(params)) {
            return sectionService.markAsComplete(selectedSection.getId(), application.getId(), processRoleId);
        } else {
            sectionService.markAsInComplete(selectedSection.getId(), application.getId(), processRoleId);
        }

        return noErrors();
    }
}