package org.innovateuk.ifs.application.overview.controller;

import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.forms.populator.AssessorQuestionFeedbackPopulator;
import org.innovateuk.ifs.application.overview.populator.ApplicationOverviewModelPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.filter.CookieFlashMessageFilter;
import org.innovateuk.ifs.interview.service.InterviewAssignmentRestService;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.innovateuk.ifs.application.resource.ApplicationState.OPEN;
import static org.innovateuk.ifs.user.resource.Role.INTERVIEW_ASSESSOR;
import static org.innovateuk.ifs.user.resource.Role.LEADAPPLICANT;

/**
 * This controller will handle all requests that are related to the application overview.
 * Application overview is the page that contains the most basic information about the current application and
 * the basic information about the competition the application is related to.
 */
@Controller
@RequestMapping("/application")
@PreAuthorize("hasAuthority('applicant')")
@SecuredBySpring(value="Controller",
        description = "Only applicants on an application are allowed to view the corresponding application overview",
        securedType = ApplicationController.class)
public class ApplicationController {
    @Autowired
    private ApplicationOverviewModelPopulator applicationOverviewModelPopulator;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private ApplicationRestService applicationRestService;

    @Autowired
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Autowired
    private AssessorQuestionFeedbackPopulator assessorQuestionFeedbackPopulator;

    @Autowired
    private InterviewAssignmentRestService interviewAssignmentRestService;

    @GetMapping("/{applicationId}")
    public String applicationOverview(ApplicationForm form,
                                     Model model,
                                     @PathVariable("applicationId") long applicationId,
                                     UserResource user) {
        ApplicationResource application = applicationRestService.getApplicationById(applicationId)
                .getSuccess();

        if (application.getCompetitionStatus() != CompetitionStatus.OPEN) {
            return format("redirect:/application/%s/summary", application.getId());
        }

        if (application.isSubmitted()) {
            return format("redirect:/application/%s/track", application.getId());
        }

        if (form == null) {
            form = new ApplicationForm();
        }

        form.setApplication(application);
        changeApplicationStatusToOpen(application, user);

        Long userId = user.getId();
        model.addAttribute("form", form);
        model.addAttribute("model", applicationOverviewModelPopulator.populateModel(application, userId));
        return "application-overview";
    }

    private void changeApplicationStatusToOpen(ApplicationResource applicationResource, UserResource userResource) {
        if (ApplicationState.CREATED.equals(applicationResource.getApplicationState())
                && userIsLeadApplicant(userResource.getId(), applicationResource.getId())) {
            applicationRestService.updateApplicationState(applicationResource.getId(), OPEN).getSuccess();
        }
    }

    private boolean userIsLeadApplicant(long userId, long applicationId) {
        return processRoleService.findProcessRole(userId, applicationId)
                .getRole() == LEADAPPLICANT;
    }

    @PostMapping(value = "/{applicationId}")
    public String applicationOverview(@PathVariable("applicationId") long applicationId,
                                     UserResource user,
                                     HttpServletRequest request) {

        ProcessRoleResource assignedBy = processRoleService.findProcessRole(user.getId(), applicationId);

        questionService.assignQuestion(applicationId, request, assignedBy);
        return "redirect:/application/" + applicationId;
    }

    @GetMapping(value = "/{applicationId}/question/{questionId}/feedback")
    @SecuredBySpring(value = "READ", description = "Applicants and Assessors and Comp exec users can view question feedback for an application")
    @PreAuthorize("hasAnyAuthority('applicant', 'assessor', 'comp_admin', 'project_finance', 'innovation_lead')")
    public String applicationAssessorQuestionFeedback(Model model, @PathVariable("applicationId") long applicationId,
                                                      @PathVariable("questionId") long questionId,
                                                      UserResource user) {
        ApplicationResource applicationResource = applicationRestService.getApplicationById(applicationId)
                .getSuccess();

        List<ProcessRoleResource> processRoleResources = processRoleService.findProcessRolesByApplicationId(applicationId);
        List<Role> userRoles = processRoleResources.stream()
                .filter(pr -> pr.getUser().equals(user.getId()))
                .map(pr -> pr.getRole())
                .collect(Collectors.toList());

        boolean isInterviewAssessor = userRoles.contains(INTERVIEW_ASSESSOR) || userIsInternal(user.getRoles());

        boolean isApplicationAssignedToInterview = interviewAssignmentRestService.isAssignedToInterview(applicationId).getSuccess();

        if (!applicationResource.getCompetitionStatus().isFeedbackReleased() && !isApplicationAssignedToInterview) {
            return "redirect:/application/" + applicationId + "/summary";
        }
        model.addAttribute("model", assessorQuestionFeedbackPopulator.populate(applicationResource, questionId, isInterviewAssessor));
        return "application-assessor-feedback";

    }

    private boolean userIsInternal(List<Role> userroles){
        for (Role ur : userroles) {
            if(Role.internalRoles().contains(ur)){
                return true;
            }
        }
        return false;
    }

    @GetMapping("/terms-and-conditions")
    public String termsAndConditions() {
        return "application-terms-and-conditions";
    }

    /**
     * Assign a question to a user
     *
     * @param applicationId the application for which the user is assigned
     * @param sectionId     section id for showing details
     * @param request       request parameters
     * @return
     */
    @PostMapping("/{applicationId}/section/{sectionId}")
    public String assignQuestion(@PathVariable("applicationId") long applicationId,
                                 @PathVariable("sectionId") long sectionId,
                                 UserResource user,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        doAssignQuestion(applicationId, user, request, response);

        return "redirect:/application/" + applicationId + "/section/" + sectionId;
    }

    private void doAssignQuestion(Long applicationId, UserResource user, HttpServletRequest request, HttpServletResponse response) {
        ProcessRoleResource assignedBy = processRoleService.findProcessRole(user.getId(), applicationId);

        questionService.assignQuestion(applicationId, request, assignedBy);
        cookieFlashMessageFilter.setFlashMessage(response, "assignedQuestion");
    }
}
