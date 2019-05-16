package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.resource.search.*;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeResource;
import org.innovateuk.ifs.user.resource.UserResource;

import java.util.List;


/**
 * Interface for CRUD operations on {@link org.innovateuk.ifs.competition.resource.CompetitionResource} related data.
 */
public interface CompetitionRestService {
    RestResult<List<CompetitionResource>> getAll();

    RestResult<List<LiveCompetitionSearchResultItem>> findLiveCompetitions();

    RestResult<List<ProjectSetupCompetitionSearchResultItem>> findProjectSetupCompetitions();

    RestResult<List<UpcomingCompetitionSearchResultItem>> findUpcomingCompetitions();

    RestResult<List<NonIfsCompetitionSearchResultItem>> findNonIfsCompetitions();

    RestResult<List<PreviousCompetitionSearchResultItem>> findFeedbackReleasedCompetitions();

    RestResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size);

    RestResult<CompetitionCountResource> countCompetitions();

    RestResult<CompetitionResource> getCompetitionById(long competitionId);

    RestResult<List<UserResource>> findInnovationLeads(long competitionId);

    RestResult<Void> addInnovationLead(long competitionId, long innovationLeadUserId);

    RestResult<Void> removeInnovationLead(long competitionId, long innovationLeadUserId);

    RestResult<CompetitionResource> getPublishedCompetitionById(long competitionId);

    RestResult<List<CompetitionTypeResource>> getCompetitionTypes();

    RestResult<List<OrganisationTypeResource>> getCompetitionOrganisationType(long id);

    RestResult<Void> updateTermsAndConditionsForCompetition(long competitionId, long termsAndConditionsId);
}