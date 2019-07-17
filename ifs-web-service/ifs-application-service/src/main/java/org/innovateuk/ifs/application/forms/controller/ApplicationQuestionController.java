package org.innovateuk.ifs.application.forms.controller;

import org.innovateuk.ifs.applicant.resource.ApplicantQuestionResource;
import org.innovateuk.ifs.applicant.service.ApplicantRestService;
import org.innovateuk.ifs.application.forms.populator.QuestionModelPopulator;
import org.innovateuk.ifs.application.forms.questions.researchcategory.form.ResearchCategoryForm;
import org.innovateuk.ifs.application.forms.questions.researchcategory.populator.ApplicationResearchCategoryFormPopulator;
import org.innovateuk.ifs.application.forms.questions.researchcategory.populator.ApplicationResearchCategoryModelPopulator;
import org.innovateuk.ifs.application.forms.saver.ApplicationQuestionSaver;
import org.innovateuk.ifs.application.forms.service.ApplicationRedirectionService;
import org.innovateuk.ifs.application.forms.viewmodel.QuestionViewModel;
import org.innovateuk.ifs.application.populator.ApplicationNavigationPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.StringMultipartFileEditor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.*;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.RESEARCH_CATEGORY;

/**
 * This controller will handle all question requests that are related to the application form.
 */
@Controller
@RequestMapping(APPLICATION_BASE_URL + "{applicationId}/form")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = ApplicationQuestionController.class)
@PreAuthorize("hasAnyAuthority('applicant', 'project_finance', 'ifs_administrator', 'comp_admin', 'support', 'innovation_lead', 'assessor', 'monitoring_officer')")
public class ApplicationQuestionController {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationQuestionController.class);

    @Autowired
    private QuestionModelPopulator questionModelPopulator;

    @Autowired
    private ApplicationResearchCategoryModelPopulator researchCategoryPopulator;

    @Autowired
    private ApplicationResearchCategoryFormPopulator researchCategoryFormPopulator;

    @Autowired
    private ApplicationNavigationPopulator applicationNavigationPopulator;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserRestService userRestService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Autowired
    private ApplicantRestService applicantRestService;

    @Autowired
    private ApplicationRedirectionService applicationRedirectionService;

    @Autowired
    private ApplicationQuestionSaver applicationSaver;

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder, WebRequest webRequest) {
        dataBinder.registerCustomEditor(String.class, new StringMultipartFileEditor());
    }

    @GetMapping(value = {QUESTION_URL + "{" + QUESTION_ID + "}", QUESTION_URL + "edit/{" + QUESTION_ID + "}"})
    public String showQuestion(
            @ModelAttribute(name = MODEL_ATTRIBUTE_FORM, binding = false) ApplicationForm form,
            @SuppressWarnings("unused") BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            @PathVariable(APPLICATION_ID) final Long applicationId,
            @PathVariable(QUESTION_ID) final Long questionId,
            @RequestParam("mark_as_complete") final Optional<Boolean> markAsComplete,
            UserResource user,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam MultiValueMap<String, String> queryParams
    ) {
        markAsComplete.ifPresent(markAsCompleteSet -> {
            if (markAsCompleteSet) {
                ValidationMessages errors = applicationSaver.saveApplicationForm(
                        applicationId,
                        form,
                        questionId,
                        user.getId(),
                        request,
                        response,
                        Optional.of(Boolean.TRUE)
                );
                validationHandler.addAnyErrors(errors);
            }
        });

        return viewQuestion(user, applicationId, questionId, model, form, markAsComplete, queryParams);
    }

    @PostMapping(value = {
            QUESTION_URL + "{" + QUESTION_ID + "}",
            QUESTION_URL + "edit/{" + QUESTION_ID + "}"
    })
    public String questionFormSubmit(
            @ModelAttribute(MODEL_ATTRIBUTE_FORM) ApplicationForm form,
            BindingResult bindingResult,
            ValidationHandler validationHandler,
            Model model,
            @PathVariable(APPLICATION_ID) final Long applicationId,
            @PathVariable(QUESTION_ID) final Long questionId,
            UserResource user,
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam MultiValueMap<String, String> queryParams
    ) {
        Map<String, String[]> params = request.getParameterMap();

        // Check if the request is to just open edit view or to save
        if (params.containsKey(EDIT_QUESTION)) {
            return handleEditQuestion(form, model, applicationId, questionId, user, queryParams);
        } else {
            handleAssignedQuestions(applicationId, user, request, response);

            // First check if any errors already exist in bindingResult
            ValidationMessages errors = checkErrorsInFormAndSave(form, applicationId, questionId, user.getId(), request, response);

            model.addAttribute("form", form);

            /* End save action */
            if (hasErrors(request, errors, bindingResult)) {
                // Add any validated fields back in invalid entries are displayed on re-render
                validationHandler.addAnyErrors(errors);
                return viewQuestion(user, applicationId, questionId, model, form, Optional.empty(), queryParams);
            } else {
                return applicationRedirectionService.getRedirectUrl(request, applicationId, Optional.empty());
            }
        }
    }

    private void handleAssignedQuestions(Long applicationId,
                                         UserResource user,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey(ASSIGN_QUESTION_PARAM)) {
            questionService.assignQuestion(applicationId, user, request);
            cookieFlashMessageFilter.setFlashMessage(response, "assignedQuestion");
        }
    }

    private ValidationMessages checkErrorsInFormAndSave(ApplicationForm form,
                                                        Long applicationId,
                                                        Long questionId,
                                                        Long userId,
                                                        HttpServletRequest request,
                                                        HttpServletResponse response) {
        ValidationMessages errors = new ValidationMessages();
        Map<String, String[]> params = request.getParameterMap();
        if (isAllowedToUpdateQuestion(questionId, applicationId, userId) || isMarkQuestionRequest(params)) {
                /* Start save action */
            errors = applicationSaver.saveApplicationForm(
                    applicationId,
                    form,
                    questionId,
                    userId,
                    request,
                    response,
                    Optional.empty()
            );
        }
        return errors;
    }

    private boolean hasErrors(HttpServletRequest request, ValidationMessages errors, BindingResult bindingResult) {
        return isUploadWithValidationErrors(request, errors)
                || isMarkAsCompleteRequestWithValidationErrors(request.getParameterMap(), errors, bindingResult);
    }

    private String viewQuestion(
            UserResource user,
            Long applicationId,
            Long questionId,
            Model model,
            ApplicationForm form,
            Optional<Boolean> markAsComplete,
            MultiValueMap<String, String> queryParams) {

        ApplicantQuestionResource question = applicantRestService.getQuestion(user.getId(), applicationId, questionId);
        QuestionSetupType questionType = question.getQuestion().getQuestionSetupType();

        if (questionType != null) {
            switch (questionType) {
                case GRANT_AGREEMENT:
                    return String.format("redirect:/application/%d/form/question/%d/grant-agreement", applicationId, questionId);
                case GRANT_TRANSFER_DETAILS:
                    return String.format("redirect:/application/%d/form/question/%d/grant-transfer-details", applicationId, questionId);
                case APPLICATION_TEAM:
                    return String.format("redirect:/application/%d/form/question/%d/team", applicationId, questionId) +
                            (markAsComplete.isPresent() ? "?mark_as_complete=true" : "");
                case TERMS_AND_CONDITIONS:
                    return format("redirect:/application/%d/form/question/%d/terms-and-conditions", applicationId, questionId);
            }
        }

        QuestionViewModel questionViewModel = questionModelPopulator.populateModel(question, form);

        if (question.getQuestion().getQuestionSetupType() == RESEARCH_CATEGORY) {
            ApplicationResource applicationResource = applicationService.getById(applicationId);
            model.addAttribute("researchCategoryModel", researchCategoryPopulator.populate(
                    applicationResource, user.getId(), questionId));
            model.addAttribute("form", researchCategoryFormPopulator.populate(applicationResource,
                    new ResearchCategoryForm()));
        }
        model.addAttribute(MODEL_ATTRIBUTE_MODEL, questionViewModel);

        if (questionType == null) {
            return APPLICATION_FORM;
        }
        switch (questionType) {
            case APPLICATION_DETAILS:
            case APPLICATION_TEAM:
            case RESEARCH_CATEGORY:
                return APPLICATION_FORM_LEAD;
            default:
                return APPLICATION_FORM;
        }
    }

    private String handleEditQuestion(
            ApplicationForm form,
            Model model,
            Long applicationId,
            Long questionId,
            UserResource user,
            MultiValueMap<String, String> queryParams
    ) {
        ProcessRoleResource processRole = userRestService.findProcessRole(user.getId(), applicationId).getSuccess();
        if (processRole != null) {
            questionService.markAsIncomplete(questionId, applicationId, processRole.getId());
        } else {
            LOG.error("Not able to find process role for user {} for application id ", user.getName(), applicationId);
        }

        return viewQuestion(user, applicationId, questionId, model, form, Optional.empty(), queryParams);
    }

    private Boolean isMarkAsCompleteRequestWithValidationErrors(Map<String, String[]> params,
            ValidationMessages errors,
            BindingResult bindingResult) {
        return ((errors.hasErrors() || bindingResult.hasErrors()) && isMarkQuestionRequest(params));
    }

    private Boolean isUploadWithValidationErrors(HttpServletRequest request, ValidationMessages errors) {
        return (request.getParameter(UPLOAD_FILE) != null && errors.hasErrors());
    }

    private Boolean isAllowedToUpdateQuestion(Long questionId, Long applicationId, Long userId) {
        List<QuestionStatusResource> questionStatuses = questionService.findQuestionStatusesByQuestionAndApplicationId(
                questionId,
                applicationId);
        return questionStatuses.isEmpty() || questionStatuses.stream()
                .anyMatch(questionStatusResource ->
                        (questionStatusResource.getAssignee() == null
                                || questionStatusResource.getAssigneeUserId().equals(userId))
                        && (questionStatusResource.getMarkedAsComplete() == null
                                || !questionStatusResource.getMarkedAsComplete())
                );
    }
}
