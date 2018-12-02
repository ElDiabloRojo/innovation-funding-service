package org.innovateuk.ifs.application.forms.controller;

import org.innovateuk.ifs.application.populator.ApplicationModelPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.ASSIGN_QUESTION_PARAM;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.MARK_AS_COMPLETE;
import static org.innovateuk.ifs.application.resource.ApplicationState.SUBMITTED;
import static org.innovateuk.ifs.commons.error.ValidationMessages.collectValidationMessages;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.RESEARCH_CATEGORY;

/**
 * This controller will handle all submit requests that are related to the application overview.
 */

@Controller
@RequestMapping("/application")
public class ApplicationSubmitController {

    private QuestionService questionService;
    private QuestionRestService questionRestService;
    private UserRestService userRestService;
    private ApplicationService applicationService;
    private ApplicationRestService applicationRestService;
    private CompetitionRestService competitionRestService;
    private ApplicationModelPopulator applicationModelPopulator;
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    private static final String FORM_ATTR_NAME = "form";

    public ApplicationSubmitController() {
    }

    @Autowired
    public ApplicationSubmitController(QuestionService questionService,
                                       QuestionRestService questionRestService,
                                       UserRestService userRestService,
                                       ApplicationService applicationService,
                                       ApplicationRestService applicationRestService,
                                       CompetitionRestService competitionRestService,
                                       ApplicationModelPopulator applicationModelPopulator,
                                       CookieFlashMessageFilter cookieFlashMessageFilter) {
        this.questionService = questionService;
        this.questionRestService = questionRestService;
        this.userRestService = userRestService;
        this.applicationService = applicationService;
        this.applicationRestService = applicationRestService;
        this.competitionRestService = competitionRestService;
        this.applicationModelPopulator = applicationModelPopulator;
        this.cookieFlashMessageFilter = cookieFlashMessageFilter;
    }

    private boolean ableToSubmitApplication(UserResource user, ApplicationResource application) {
        return applicationModelPopulator.userIsLeadApplicant(application, user.getId()) && application.isSubmittable();
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAuthority('applicant')")
    @PostMapping("/{applicationId}/summary")
    public String applicationSummarySubmit(@PathVariable("applicationId") long applicationId,
                                           UserResource user,
                                           HttpServletRequest request) {

        Map<String, String[]> params = request.getParameterMap();

        if (params.containsKey(ASSIGN_QUESTION_PARAM)) {
            ProcessRoleResource assignedBy = userRestService.findProcessRole(user.getId(), applicationId).getSuccess();
            questionService.assignQuestion(applicationId, request, assignedBy);
        } else if (params.containsKey(MARK_AS_COMPLETE)) {
            Long markQuestionCompleteId = Long.valueOf(request.getParameter(MARK_AS_COMPLETE));
            if (markQuestionCompleteId != null) {
                ProcessRoleResource processRole = userRestService.findProcessRole(user.getId(), applicationId).getSuccess();
                List<ValidationMessages> markAsCompleteErrors = questionService.markAsComplete(markQuestionCompleteId, applicationId, processRole.getId());

                if (collectValidationMessages(markAsCompleteErrors).hasErrors()) {
                    questionService.markAsIncomplete(markQuestionCompleteId, applicationId, processRole.getId());

                    if (isResearchCategoryQuestion(markQuestionCompleteId)) {
                        return "redirect:/application/" + applicationId + "/form/question/" + markQuestionCompleteId + "/research-category?mark_as_complete=true";
                    }

                    return "redirect:/application/" + applicationId + "/form/question/edit/" + markQuestionCompleteId + "?mark_as_complete=true";
                }
            }
        }

        return "redirect:/application/" + applicationId + "/summary";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAuthority('applicant')")
    @GetMapping("/{applicationId}/confirm-submit")
    public String applicationConfirmSubmit(ApplicationForm form, Model model, @PathVariable("applicationId") long applicationId,
                                           UserResource user) {
        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();
        List<ProcessRoleResource> userApplicationRoles = userRestService.findProcessRole(application.getId()).getSuccess();
        applicationModelPopulator.addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, model, form, userApplicationRoles, Optional.empty());
        return "application-confirm-submit";
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAuthority('applicant')")
    @PostMapping("/{applicationId}/submit")
    public String applicationSubmit(Model model,
                                    @ModelAttribute(FORM_ATTR_NAME) ApplicationForm form,
                                    @SuppressWarnings("UnusedParameters") BindingResult bindingResult,
                                    ValidationHandler validationHandler,
                                    @PathVariable("applicationId") long applicationId,
                                    UserResource user,
                                    HttpServletResponse response) {

        ApplicationResource application = applicationService.getById(applicationId);

        if (!ableToSubmitApplication(user, application)) {
            cookieFlashMessageFilter.setFlashMessage(response, "cannotSubmit");
            return "redirect:/application/" + applicationId + "/confirm-submit";
        }

        RestResult<Void> updateResult = applicationRestService.updateApplicationState(applicationId, SUBMITTED);

        Supplier<String> failureView = () -> applicationConfirmSubmit(form, model, applicationId, user);

        return validationHandler.addAnyErrors(updateResult, fieldErrorsToFieldErrors(), asGlobalErrors()).failNowOrSucceedWith(failureView, () -> {
            CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();
            applicationModelPopulator.addApplicationWithoutDetails(application, competition, model);
            return "application-submitted";
        });
    }

    @SecuredBySpring(value = "TODO", description = "TODO")
    @PreAuthorize("hasAuthority('applicant')")
    @GetMapping("/{applicationId}/track")
    public String applicationTrack(Model model,
                                   @PathVariable("applicationId") long applicationId) {
        ApplicationResource application = applicationService.getById(applicationId);

        if (!application.isSubmitted()) {
            return "redirect:/application/" + applicationId;
        }

        CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();
        applicationModelPopulator.addApplicationWithoutDetails(application, competition, model);
        return "application-track";
    }

    private boolean isResearchCategoryQuestion(Long questionId) {
        QuestionResource question = questionRestService.findById(questionId).getSuccess();
        return question.getQuestionSetupType() == RESEARCH_CATEGORY;
    }
}


