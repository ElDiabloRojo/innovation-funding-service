
package org.innovateuk.ifs.competition.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.resource.*;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.*;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.junit.Assert.*;

public class CompetitionRestServiceMocksTest extends BaseRestServiceUnitTest<CompetitionRestServiceImpl> {

    private static final String competitionsRestURL = "/competition";

    @Override
    protected CompetitionRestServiceImpl registerRestServiceUnderTest() {
        return new CompetitionRestServiceImpl();
    }

    @Test
    public void getAll() {

        List<CompetitionResource> returnedResponse = newCompetitionResource().build(3);
        setupGetWithRestResultExpectations(competitionsRestURL + "/find-all", competitionResourceListType(), returnedResponse);
        List<CompetitionResource> responses = service.getAll().getSuccess();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void getCompetitionById() {

        CompetitionResource returnedResponse = new CompetitionResource();

        setupGetWithRestResultExpectations(competitionsRestURL + "/123", CompetitionResource.class, returnedResponse);

        CompetitionResource response = service.getCompetitionById(123L).getSuccess();
        assertNotNull(response);
        Assert.assertEquals(returnedResponse, response);
    }

    @Test
    public void findInnovationLeads() {

        List<UserResource> returnedResponse = asList(new UserResource(), new UserResource());

        setupGetWithRestResultExpectations(competitionsRestURL + "/123" + "/innovation-leads", userListType(), returnedResponse);

        List<UserResource> response = service.findInnovationLeads(123L).getSuccess();
        assertNotNull(response);
        Assert.assertEquals(returnedResponse, response);
    }

    @Test
    public void addInnovationLead() {

        setupPostWithRestResultExpectations(competitionsRestURL + "/123" + "/add-innovation-lead" + "/234", HttpStatus.OK);

        RestResult<Void> response = service.addInnovationLead(123L, 234L);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void removeInnovationLead() {

        setupPostWithRestResultExpectations(competitionsRestURL + "/123" + "/remove-innovation-lead" + "/234", HttpStatus.OK);

        RestResult<Void> response = service.removeInnovationLead(123L, 234L);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getCompetitionTypes() {
        List<CompetitionTypeResource> returnedResponse = asList(new CompetitionTypeResource(), new CompetitionTypeResource());

        setupGetWithRestResultExpectations("/competition-type/find-all", competitionTypeResourceListType(), returnedResponse);

        List<CompetitionTypeResource> response = service.getCompetitionTypes().getSuccess();
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(returnedResponse, response);
    }

    @Test
    public void findLiveCompetitions() {
        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", singleton(""), 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/live", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findLiveCompetitions().getSuccess();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void findProjectSetupCompetitions() {

        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", singleton(""), 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/project-setup", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findProjectSetupCompetitions().getSuccess();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void findUpcomingCompetitions() {

        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", Collections.EMPTY_SET, 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/upcoming", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findUpcomingCompetitions().getSuccess();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void findNonIfsCompetitions() {

        List<CompetitionSearchResultItem> returnedResponse =
                singletonList(new CompetitionSearchResultItem(1L, "Name", Collections.EMPTY_SET, 0, "", CompetitionStatus.OPEN, "Comp Type", 0, null, null, null));

        setupGetWithRestResultExpectations(competitionsRestURL + "/non-ifs", competitionSearchResultItemListType(), returnedResponse);

        List<CompetitionSearchResultItem> responses = service.findNonIfsCompetitions().getSuccess();
        assertNotNull(responses);
        assertEquals(returnedResponse, responses);
    }

    @Test
    public void countCompetitions() {
        CompetitionCountResource returnedResponse = new CompetitionCountResource();

        setupGetWithRestResultExpectations(competitionsRestURL + "/count", CompetitionCountResource.class, returnedResponse);

        CompetitionCountResource responses = service.countCompetitions().getSuccess();
        assertNotNull(responses);
        Assert.assertEquals(returnedResponse, responses);
    }

    @Test
    public void searchCompetitions() {
        CompetitionSearchResult returnedResponse = new CompetitionSearchResult();
        String searchQuery = "SearchQuery";
        int page = 1;
        int size = 20;

        setupGetWithRestResultExpectations(competitionsRestURL + "/search/" + page + "/" + size + "?searchQuery=" + searchQuery, CompetitionSearchResult.class, returnedResponse);

        CompetitionSearchResult responses = service.searchCompetitions(searchQuery, page, size).getSuccess();
        assertNotNull(responses);
        Assert.assertEquals(returnedResponse, responses);
    }

    @Test
    public void updateTermsAndConditionsForCompetition() {
        setupPutWithRestResultExpectations(competitionsRestURL + "/123" + "/update-terms-and-conditions" + "/234", HttpStatus.OK);

        RestResult<Void> response = service.updateTermsAndConditionsForCompetition(123L, 234L);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
