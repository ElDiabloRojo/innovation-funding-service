package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.competition.resource.search.*;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.*;

/**
 * CompetitionsRestServiceImpl is a utility for CRUD operations on {@link CompetitionResource}.
 * This class connects to the { org.innovateuk.ifs.competition.controller.CompetitionController}
 * through a REST call.
 */
@Service
public class CompetitionRestServiceImpl extends BaseRestService implements CompetitionRestService {

    private String competitionsRestURL = "/competition";
    private String competitionsTypesRestURL = "/competition-type";

    @Override
    public RestResult<List<CompetitionResource>> getAll() {
        return getWithRestResult(competitionsRestURL + "/find-all", competitionResourceListType());
    }

    @Override
    public RestResult<List<LiveCompetitionSearchResultItem>> findLiveCompetitions() {
        return getWithRestResult(competitionsRestURL + "/live", liveCompetitionSearchResultItemListType());
    }

    @Override
    public RestResult<List<ProjectSetupCompetitionSearchResultItem>> findProjectSetupCompetitions() {
        return getWithRestResult(competitionsRestURL + "/project-setup", projectSetupCompetitionSearchResultItemListType());
    }

    @Override
    public RestResult<List<UpcomingCompetitionSearchResultItem>> findUpcomingCompetitions() {
        return getWithRestResult(competitionsRestURL + "/upcoming", upcomingCompetitionSearchResultItemListType());
    }

    @Override
    public RestResult<List<NonIfsCompetitionSearchResultItem>> findNonIfsCompetitions() {
        return getWithRestResult(competitionsRestURL + "/non-ifs", nonIfsCompetitionSearchReultItemListType());
    }

    @Override
    public RestResult<List<PreviousCompetitionSearchResultItem>> findFeedbackReleasedCompetitions() {
        return getWithRestResult(competitionsRestURL +  "/feedback-released", previousCompetitionSearchResultItemListType());
    }

    @Override
    public RestResult<CompetitionSearchResult> searchCompetitions(String searchQuery, int page, int size) {
        return getWithRestResult(competitionsRestURL + "/search/" + page + "/" + size + "?searchQuery=" + searchQuery, CompetitionSearchResult.class);
    }

    @Override
    public RestResult<CompetitionCountResource> countCompetitions() {
        return getWithRestResult(competitionsRestURL + "/count", CompetitionCountResource.class);
    }

    @Override
    public RestResult<CompetitionResource> getCompetitionById(long competitionId) {
        return getWithRestResult(competitionsRestURL + "/" + competitionId, CompetitionResource.class);
    }

    @Override
    public RestResult<List<UserResource>> findInnovationLeads(long competitionId) {
        return getWithRestResult(competitionsRestURL + "/" + competitionId + "/innovation-leads", userListType());
    }

    @Override
    public RestResult<Void> addInnovationLead(long competitionId, long innovationLeadUserId) {
        return postWithRestResult(competitionsRestURL + "/" + competitionId + "/add-innovation-lead/" + innovationLeadUserId, Void.class);
    }

    @Override
    public RestResult<Void> removeInnovationLead(long competitionId, long innovationLeadUserId) {
        return postWithRestResult(competitionsRestURL + "/" + competitionId + "/remove-innovation-lead/" + innovationLeadUserId, Void.class);
    }

    @Override
    public RestResult<List<OrganisationTypeResource>> getCompetitionOrganisationType(long competitionId) {
        return getWithRestResultAnonymous(competitionsRestURL + "/" + competitionId + "/get-organisation-types", organisationTypeResourceListType());
    }

    @Override
    public RestResult<CompetitionResource> getPublishedCompetitionById(long competitionId) {
        return getWithRestResultAnonymous(competitionsRestURL + "/" + competitionId, CompetitionResource.class);
    }

    @Override
    public RestResult<Void> updateTermsAndConditionsForCompetition(long competitionId, long termsAndConditionsId) {
        return putWithRestResult(competitionsRestURL + "/" + competitionId + "/update-terms-and-conditions/" + termsAndConditionsId, Void.class);
    }

    @Override
    public RestResult<List<CompetitionTypeResource>> getCompetitionTypes() {
        return getWithRestResult(competitionsTypesRestURL + "/find-all", competitionTypeResourceListType());
    }

}
