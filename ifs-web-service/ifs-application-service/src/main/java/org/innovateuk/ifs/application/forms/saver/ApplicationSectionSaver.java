package org.innovateuk.ifs.application.forms.saver;

import org.innovateuk.ifs.application.finance.view.FinanceViewHandlerProvider;
import org.innovateuk.ifs.application.overheads.OverheadFileSaver;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.OrganisationService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.*;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.error.ValidationMessages.collectValidationMessages;
import static org.springframework.util.StringUtils.hasText;

/**
 * This Saver will handle save all sections that are related to the application.
 */
@Service
public class ApplicationSectionSaver extends AbstractApplicationSaver {

    private OrganisationService organisationService;
    private FinanceViewHandlerProvider financeViewHandlerProvider;
    private UserRestService userRestService;
    private SectionService sectionService;
    private QuestionRestService questionRestService;
    private CookieFlashMessageFilter cookieFlashMessageFilter;
    private OverheadFileSaver overheadFileSaver;
    private ApplicationSectionFinanceSaver financeSaver;
    private ApplicationQuestionFileSaver fileSaver;
    private ApplicationQuestionNonFileSaver nonFileSaver;
    private CompetitionRestService competitionRestService;

    public ApplicationSectionSaver(OrganisationService organisationService, FinanceViewHandlerProvider financeViewHandlerProvider, UserRestService userRestService, SectionService sectionService, QuestionRestService questionRestService, CookieFlashMessageFilter cookieFlashMessageFilter, OverheadFileSaver overheadFileSaver, ApplicationSectionFinanceSaver financeSaver, ApplicationQuestionFileSaver fileSaver, ApplicationQuestionNonFileSaver nonFileSaver, CompetitionRestService competitionRestService) {
        this.organisationService = organisationService;
        this.financeViewHandlerProvider = financeViewHandlerProvider;
        this.userRestService = userRestService;
        this.sectionService = sectionService;
        this.questionRestService = questionRestService;
        this.cookieFlashMessageFilter = cookieFlashMessageFilter;
        this.overheadFileSaver = overheadFileSaver;
        this.financeSaver = financeSaver;
        this.fileSaver = fileSaver;
        this.nonFileSaver = nonFileSaver;
        this.competitionRestService = competitionRestService;
    }

    public ValidationMessages saveApplicationForm(ApplicationResource application,
                                                  Long competitionId,
                                                  ApplicationForm form,
                                                  Long sectionId,
                                                  Long userId,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response, Boolean validFinanceTerms) {

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

            Long organisationType = organisationService.getOrganisationType(userId, applicationId);
            ValidationMessages saveErrors = financeViewHandlerProvider.getFinanceFormHandler(competitionRestService.getCompetitionById(competitionId).getSuccess(), organisationType).update(request, userId, applicationId, competitionId);

            if (overheadFileSaver.isOverheadFileRequest(request)) {
                errors.addAll(overheadFileSaver.handleOverheadFileRequest(request));
            } else {
                errors.addAll(saveErrors);
            }
        }

        if (isMarkSectionRequest(params)) {
            errors.addAll(handleMarkSectionRequest(application, selectedSection, params, processRole, errors, validFinanceTerms));
            financeSaver.handleStateAid(params, application, form, selectedSection);
        }

        cookieFlashMessageFilter.setFlashMessage(response, "applicationSaved");

        return sortValidationMessages(errors);
    }

    private ValidationMessages handleMarkSectionRequest(ApplicationResource application, SectionResource selectedSection, Map<String, String[]> params,
                                                        ProcessRoleResource processRole, ValidationMessages errorsSoFar, Boolean validFinanceTerms) {
        ValidationMessages messages = new ValidationMessages();

        if (errorsSoFar.hasErrors()) {
            messages.addError(fieldError("formInput[cost]", "", MARKED_AS_COMPLETE_KEY));
        } else if (isMarkSectionAsIncompleteRequest(params) ||
                (isMarkSectionAsCompleteRequest(params) && validFinanceTerms)) {
            List<ValidationMessages> financeErrorsMark = markAllQuestionsInSection(application, selectedSection, processRole.getId(), params);

            if (collectValidationMessages(financeErrorsMark).hasErrors()) {
                messages.addError(fieldError("formInput[cost]", "", MARKED_AS_COMPLETE_KEY));
                messages.addAll(handleMarkSectionValidationMessages(financeErrorsMark));
            }
        }

        return messages;
    }

    private ValidationMessages handleMarkSectionValidationMessages(List<ValidationMessages> financeErrorsMark) {

        ValidationMessages toFieldErrors = new ValidationMessages();

        financeErrorsMark.forEach(validationMessage ->
                validationMessage.getErrors().stream()
                        .filter(Objects::nonNull)
                        .filter(e -> hasText(e.getErrorKey()))
                        .forEach(mapToApplicationFormErrors(validationMessage, toFieldErrors))
        );

        return toFieldErrors;
    }

    /* We are converting the error messages from the data service to our target ApplicationForm. File uploads cannot be handled as a formInput[id...]. */
    private Consumer<Error> mapToApplicationFormErrors(ValidationMessages dataServiceMessage, ValidationMessages applicationFormErrors) {
        return dataServiceError -> {
            if ("costItem".equals(dataServiceMessage.getObjectName())) {
                if (dataServiceError.isFieldError() && dataServiceError.getFieldName().equals("calculationFile")) {
                    applicationFormErrors.addError(fieldError("overheadfile", dataServiceError));
                } else if (hasText(dataServiceError.getErrorKey())) {
                    applicationFormErrors.addError(fieldError("formInput[cost-" + dataServiceMessage.getObjectId() + "-" + dataServiceError.getFieldName() + "]", dataServiceError));
                } else {
                    applicationFormErrors.addError(fieldError(getFormCostInputKey(dataServiceMessage.getObjectId()), dataServiceError));
                }
            } else {
                if(dataServiceError.isFieldError() && dataServiceError.getFieldName().equals("jesFileUpload")) {
                    applicationFormErrors.addError(dataServiceError);
                } else {
                    applicationFormErrors.addError(fieldError(getFormInputKey(dataServiceMessage.getObjectId()), dataServiceError));
                }
            }
        };
    }

    private List<ValidationMessages> markAllQuestionsInSection(ApplicationResource application,
                                                               SectionResource selectedSection,
                                                               Long processRoleId,
                                                               Map<String, String[]> params) {
        if (isMarkSectionAsCompleteRequest(params)) {
            return sectionService.markAsComplete(selectedSection.getId(), application.getId(), processRoleId);
        } else {
            sectionService.markAsInComplete(selectedSection.getId(), application.getId(), processRoleId);
        }

        return emptyList();
    }
}