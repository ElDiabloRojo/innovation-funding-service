package org.innovateuk.ifs.management.application.populator;

import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;
import org.innovateuk.ifs.application.service.ApplicationSummaryRestService;
import org.innovateuk.ifs.management.application.viewmodel.SubmittedApplicationsRowViewModel;
import org.innovateuk.ifs.management.application.viewmodel.SubmittedApplicationsViewModel;
import org.innovateuk.ifs.management.navigation.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * Builds the Competition Management Submitted Applications view model.
 */
@Component
public class SubmittedApplicationsModelPopulator {

    @Autowired
    private ApplicationSummaryRestService applicationSummaryRestService;

    public SubmittedApplicationsViewModel populateModel(long competitionId, String origin, int page, String sorting, Optional<String> filter) {
        CompetitionSummaryResource competitionSummary = applicationSummaryRestService
                .getCompetitionSummary(competitionId)
                .getSuccess();

        ApplicationSummaryPageResource summaryPageResource = applicationSummaryRestService
                .getSubmittedApplications(competitionId, sorting, page, 20, filter, Optional.empty())
                .getSuccess();

        return new SubmittedApplicationsViewModel(
                competitionSummary.getCompetitionId(),
                competitionSummary.getCompetitionName(),
                competitionSummary.getAssessorDeadline(),
                competitionSummary.getApplicationsSubmitted(),
                sorting,
                filter.orElse(null),
                getApplications(summaryPageResource),
                new Pagination(summaryPageResource, origin)
        );
    }

    private List<SubmittedApplicationsRowViewModel> getApplications(ApplicationSummaryPageResource summaryPageResource) {
        return simpleMap(
                summaryPageResource.getContent(),
                applicationSummaryResource -> new SubmittedApplicationsRowViewModel(
                        applicationSummaryResource.getId(),
                        applicationSummaryResource.getName(),
                        applicationSummaryResource.getLead(),
                        applicationSummaryResource.getInnovationArea(),
                        applicationSummaryResource.getNumberOfPartners(),
                        applicationSummaryResource.getGrantRequested(),
                        applicationSummaryResource.getTotalProjectCost(),
                        applicationSummaryResource.getDuration()
                )
        );
    }
}
