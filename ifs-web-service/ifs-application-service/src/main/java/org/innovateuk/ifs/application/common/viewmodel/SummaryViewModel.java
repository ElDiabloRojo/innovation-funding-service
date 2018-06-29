package org.innovateuk.ifs.application.common.viewmodel;

import org.innovateuk.ifs.application.overview.viewmodel.ApplicationOverviewCompletedViewModel;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.team.viewmodel.ApplicationTeamViewModel;
import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;
import org.innovateuk.ifs.assessment.resource.ApplicationAssessmentAggregateResource;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.resource.SectionResource;

import java.util.List;
import java.util.Map;

/**
 * View model for fragments/application :: application-summary
 */
public class SummaryViewModel {

    private final ApplicationResource currentApplication;
    private final Map<Long, SectionResource> sections;
    private final Map<Long, List<QuestionResource>> sectionQuestions;
    private final ApplicationAssessmentAggregateResource scores;
    private final Map<Long, List<FormInputResource>> questionFormInputs;
    private final Map<Long, FormInputResponseResource> responses;
    private final Map<Long, QuestionStatusResource> questionAssignees;
    private final List<AssessmentResource> feedbackSummary;
    private final boolean hasFinanceSection;
    private final Long financeSectionId;
    private final ApplicationFinanceSummaryViewModel applicationFinanceSummaryViewModel;
    private final ApplicationFundingBreakdownViewModel applicationFundingBreakdownViewModel;
    private final ApplicationResearchParticipationViewModel applicationResearchParticipationViewModel;
    private final ApplicationOverviewCompletedViewModel completedViewModel;
    private final Map<Long, AbstractFormInputViewModel> formInputViewModels;
    private final boolean fromApplicationService;
    private final ApplicationTeamViewModel applicationTeamModel;

    public SummaryViewModel(ApplicationResource currentApplication,
                            Map<Long, SectionResource> sections,
                            Map<Long, List<QuestionResource>> sectionQuestions,
                            ApplicationAssessmentAggregateResource scores,
                            Map<Long, List<FormInputResource>> questionFormInputs,
                            Map<Long, FormInputResponseResource> responses,
                            Map<Long, QuestionStatusResource> questionAssignees,
                            List<AssessmentResource> feedbackSummary,
                            boolean hasFinanceSection,
                            Long financeSectionId,
                            ApplicationFinanceSummaryViewModel applicationFinanceSummaryViewModel,
                            ApplicationFundingBreakdownViewModel applicationFundingBreakdownViewModel,
                            ApplicationResearchParticipationViewModel applicationResearchParticipationViewModel,
                            ApplicationOverviewCompletedViewModel completedViewModel,
                            Map<Long, AbstractFormInputViewModel> formInputViewModels,
                            boolean fromApplicationService,
                            ApplicationTeamViewModel applicationTeamModel) {
        this.currentApplication = currentApplication;
        this.sections = sections;
        this.sectionQuestions = sectionQuestions;
        this.scores = scores;
        this.questionFormInputs = questionFormInputs;
        this.responses = responses;
        this.questionAssignees = questionAssignees;
        this.feedbackSummary = feedbackSummary;
        this.hasFinanceSection = hasFinanceSection;
        this.financeSectionId = financeSectionId;
        this.applicationFinanceSummaryViewModel = applicationFinanceSummaryViewModel;
        this.applicationFundingBreakdownViewModel = applicationFundingBreakdownViewModel;
        this.applicationResearchParticipationViewModel = applicationResearchParticipationViewModel;
        this.completedViewModel = completedViewModel;
        this.formInputViewModels = formInputViewModels;
        this.fromApplicationService = fromApplicationService;
        this.applicationTeamModel = applicationTeamModel;
    }

    public ApplicationResource getCurrentApplication() {
        return currentApplication;
    }

    public Map<Long, SectionResource> getSections() {
        return sections;
    }

    public Map<Long, List<QuestionResource>> getSectionQuestions() {
        return sectionQuestions;
    }

    public ApplicationAssessmentAggregateResource getScores() {
        return scores;
    }

    public Map<Long, List<FormInputResource>> getQuestionFormInputs() {
        return questionFormInputs;
    }

    public Map<Long, FormInputResponseResource> getResponses() {
        return responses;
    }

    public Map<Long, QuestionStatusResource> getQuestionAssignees() {
        return questionAssignees;
    }

    public List<AssessmentResource> getFeedbackSummary() {
        return feedbackSummary;
    }

    public ApplicationFinanceSummaryViewModel getApplicationFinanceSummaryViewModel() {
        return applicationFinanceSummaryViewModel;
    }

    public ApplicationFundingBreakdownViewModel getApplicationFundingBreakdownViewModel() {
        return applicationFundingBreakdownViewModel;
    }

    public boolean isHasFinanceSection() {
        return hasFinanceSection;
    }

    public Long getFinanceSectionId() {
        return financeSectionId;
    }

    public ApplicationResearchParticipationViewModel getApplicationResearchParticipationViewModel() {
        return applicationResearchParticipationViewModel;
    }

    public ApplicationOverviewCompletedViewModel getCompletedViewModel() {
        return completedViewModel;
    }

    public Map<Long, AbstractFormInputViewModel> getFormInputViewModels() {
        return formInputViewModels;
    }

    public boolean isFromApplicationService() {
        return fromApplicationService;
    }

    public ApplicationTeamViewModel getApplicationTeamModel() {
        return applicationTeamModel;
    }
}
