package org.innovateuk.ifs.application.feedback.controller;

import org.innovateuk.ifs.application.feedback.populator.AssessorQuestionFeedbackPopulator;
import org.innovateuk.ifs.application.forms.questions.team.form.ApplicationTeamForm;
import org.innovateuk.ifs.application.forms.questions.team.populator.ApplicationTeamPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.interview.service.InterviewAssignmentRestService;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/application")
public class ApplicationQuestionFeedbackController {

    private ApplicationRestService applicationRestService;
    private InterviewAssignmentRestService interviewAssignmentRestService;
    private AssessorQuestionFeedbackPopulator assessorQuestionFeedbackPopulator;
    private QuestionRestService questionRestService;
    private ApplicationTeamPopulator applicationTeamPopulator;

    public ApplicationQuestionFeedbackController() {
    }

    @Autowired
    public ApplicationQuestionFeedbackController(ApplicationRestService applicationRestService,
                                                 InterviewAssignmentRestService interviewAssignmentRestService,
                                                 AssessorQuestionFeedbackPopulator assessorQuestionFeedbackPopulator,
                                                 QuestionRestService questionRestService,
                                                 ApplicationTeamPopulator applicationTeamPopulator) {
        this.applicationRestService = applicationRestService;
        this.interviewAssignmentRestService = interviewAssignmentRestService;
        this.assessorQuestionFeedbackPopulator = assessorQuestionFeedbackPopulator;
        this.questionRestService = questionRestService;
        this.applicationTeamPopulator = applicationTeamPopulator;
    }

    @GetMapping(value = "/{applicationId}/question/{questionId}/feedback")
    @SecuredBySpring(value = "READ", description = "Applicants, Assessors, Comp execs, Innovation leads, Stakeholders and Monitoring Officers can view question feedback for an application")
    @PreAuthorize("hasAnyAuthority('applicant', 'assessor', 'comp_admin', 'project_finance', 'innovation_lead', 'stakeholder', 'monitoring_officer')")
    public String applicationAssessorQuestionFeedback(Model model, @PathVariable("applicationId") long applicationId,
                                                      @PathVariable("questionId") long questionId,
                                                      UserResource user
                                                      ) {
        ApplicationResource applicationResource = applicationRestService.getApplicationById(applicationId)
                .getSuccess();

        boolean isApplicationAssignedToInterview = interviewAssignmentRestService.isAssignedToInterview(applicationId).getSuccess();

        if (!applicationResource.getCompetitionStatus().isFeedbackReleased() && !isApplicationAssignedToInterview) {
            return "redirect:/application/" + applicationId + "/summary";
        }

        QuestionResource questionResource = questionRestService.findById(questionId).getSuccess();

        if (questionResource.getQuestionSetupType() == QuestionSetupType.APPLICATION_TEAM) {
            model.addAttribute("form", new ApplicationTeamForm());
            model.addAttribute("model", applicationTeamPopulator.populate(applicationId, questionId, user));
            return "application/questions/application-team";
        }

        model.addAttribute("model", assessorQuestionFeedbackPopulator.populate(applicationResource, questionId, user, model));
        return "application-assessor-feedback";

    }

}
