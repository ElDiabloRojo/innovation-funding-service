package org.innovateuk.ifs.application.overview.viewmodel;

import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.viewmodel.ApplicationCompletedViewModel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;


/**
 * View model for the application overview
 */
public class ApplicationOverviewViewModel {

    private Long applicationId;
    private String applicationName;
    private ApplicationState applicationState;
    private boolean applicationSubmitted;
    private boolean projectWithdrawn;
    private CompetitionResource currentCompetition;
    private OrganisationResource userOrganisation;
    private Integer completedQuestionsPercentage;
    private ApplicationOverviewUserViewModel user;
    private ApplicationOverviewAssignableViewModel assignable;
    private ApplicationCompletedViewModel completed;
    private ApplicationOverviewSectionViewModel section;

    public ApplicationOverviewViewModel(Long applicationId,
                                        String applicationName,
                                        ApplicationState applicationState,
                                        boolean applicationSubmitted, boolean projectWithdrawn, CompetitionResource currentCompetition,
                                        OrganisationResource userOrganisation, Integer completedQuestionsPercentage,
                                        ApplicationOverviewUserViewModel user, ApplicationOverviewAssignableViewModel assignable,
                                        ApplicationCompletedViewModel completed, ApplicationOverviewSectionViewModel section) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.applicationState = applicationState;
        this.applicationSubmitted = applicationSubmitted;
        this.projectWithdrawn = projectWithdrawn;
        this.currentCompetition = currentCompetition;
        this.userOrganisation = userOrganisation;
        this.completedQuestionsPercentage = completedQuestionsPercentage;
        this.user = user;
        this.assignable = assignable;
        this.completed = completed;
        this.section = section;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public ApplicationState getApplicationState() {
        return applicationState;
    }

    public boolean isApplicationSubmitted() {
        return applicationSubmitted;
    }

    public boolean isProjectWithdrawn() {
        return projectWithdrawn;
    }

    public CompetitionResource getCurrentCompetition() {
        return currentCompetition;
    }

    public OrganisationResource getUserOrganisation() {
        return userOrganisation;
    }

    public Integer getCompletedQuestionsPercentage() {
        return completedQuestionsPercentage;
    }

    public ApplicationOverviewUserViewModel getUser() {
        return user;
    }

    public ApplicationOverviewAssignableViewModel getAssignable() {
        return assignable;
    }

    public ApplicationCompletedViewModel getCompleted() {
        return completed;
    }

    public ApplicationOverviewSectionViewModel getSection() {
        return section;
    }
}
