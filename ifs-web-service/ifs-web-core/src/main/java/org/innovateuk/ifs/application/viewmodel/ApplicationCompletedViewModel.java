package org.innovateuk.ifs.application.viewmodel;

import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.resource.SectionResource;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * View model for the application overview - users
 */
public class ApplicationCompletedViewModel {
    private Set<Long> sectionsMarkedAsComplete;
    private Future<Set<Long>> markedAsComplete;
    private Set<Long> completedSections;
    private boolean userFinanceSectionCompleted;

    public ApplicationCompletedViewModel(Set<Long> sectionsMarkedAsComplete,
                                         Future<Set<Long>> markedAsComplete,
                                         boolean userFinanceSectionCompleted) {
        this.sectionsMarkedAsComplete = sectionsMarkedAsComplete;
        this.markedAsComplete = markedAsComplete;
        this.userFinanceSectionCompleted = userFinanceSectionCompleted;
    }

    public Set<Long> getSectionsMarkedAsComplete() {
        return sectionsMarkedAsComplete;
    }

    public Set<Long> getMarkedAsComplete() throws ExecutionException, InterruptedException {
        return markedAsComplete.get();
    }

    public void setCompletedSections(Set<Long> completedSections) {
        this.completedSections = completedSections;
    }

    public Set<Long> getCompletedSections() {
        return completedSections;
    }

    public boolean getUserFinanceSectionCompleted() {
        return userFinanceSectionCompleted;
    }

    public Boolean completedOrMarkedAsComplete(QuestionResource questionResource, SectionResource sectionResource) throws ExecutionException, InterruptedException {
        return (questionResource.isMarkAsCompletedEnabled() && getMarkedAsComplete().contains(questionResource.getId()))
                || completedSections.contains(sectionResource.getId());
    }
}
