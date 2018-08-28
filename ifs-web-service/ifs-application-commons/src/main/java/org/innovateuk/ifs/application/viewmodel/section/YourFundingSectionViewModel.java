package org.innovateuk.ifs.application.viewmodel.section;

import org.innovateuk.ifs.applicant.resource.ApplicantSectionResource;
import org.innovateuk.ifs.application.viewmodel.NavigationViewModel;
import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;

import java.util.List;
import java.util.Optional;

import static org.innovateuk.ifs.competition.resource.CompetitionStatus.OPEN;

/**
 * View model for your funding section.
 */
public class YourFundingSectionViewModel extends AbstractSectionViewModel {
    private boolean complete;
    private boolean researchCategoryComplete;
    private boolean yourOrganisationComplete;
    private long researchCategoryQuestionId;
    private long yourOrganisationSectionId;

    public YourFundingSectionViewModel(ApplicantSectionResource applicantResource, List<AbstractFormInputViewModel> formInputViewModels, NavigationViewModel navigationViewModel, boolean allReadOnly, Optional<Long> applicantOrganisationId, boolean readOnlyAllApplicantApplicationFinances) {
        super(applicantResource, formInputViewModels, navigationViewModel, allReadOnly, applicantOrganisationId, readOnlyAllApplicantApplicationFinances);
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isFundingSectionLocked() {
        return !getCompetition().getCompetitionStatus().isLaterThan(OPEN) &&
                !(this.researchCategoryComplete && this.yourOrganisationComplete);
    }

    public boolean isResearchCategoryComplete() {
        return researchCategoryComplete;
    }

    public void setResearchCategoryComplete(boolean researchCategoryComplete) {
        this.researchCategoryComplete = researchCategoryComplete;
    }

    public boolean isYourOrganisationComplete() {
        return yourOrganisationComplete;
    }

    public void setYourOrganisationComplete(boolean yourOrganisationComplete) {
        this.yourOrganisationComplete = yourOrganisationComplete;
    }

    public long getResearchCategoryQuestionId() {
        return researchCategoryQuestionId;
    }

    public void setResearchCategoryQuestionId(long researchCategoryQuestionId) {
        this.researchCategoryQuestionId = researchCategoryQuestionId;
    }

    public long getYourOrganisationSectionId() {
        return yourOrganisationSectionId;
    }

    public void setYourOrganisationSectionId(long yourOrganisationSectionId) {
        this.yourOrganisationSectionId = yourOrganisationSectionId;
    }
}

