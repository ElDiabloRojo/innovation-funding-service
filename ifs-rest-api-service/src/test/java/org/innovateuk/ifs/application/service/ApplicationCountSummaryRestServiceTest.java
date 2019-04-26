package org.innovateuk.ifs.application.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryPageResource;
import org.junit.Assert;
import org.junit.Test;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.OK;

public class ApplicationCountSummaryRestServiceTest extends BaseRestServiceUnitTest<ApplicationCountSummaryRestServiceImpl> {

    @Override
    protected ApplicationCountSummaryRestServiceImpl registerRestServiceUnderTest() {
        return new ApplicationCountSummaryRestServiceImpl();
    }

    @Test
    public void getApplicationCountSummariesByCompetitionId() {
        String expectedUrl = "/application-count-summary/find-by-competition-id/1?filter=filter&page=2&size=3";
        ApplicationCountSummaryPageResource pageResource = new ApplicationCountSummaryPageResource();

        setupGetWithRestResultExpectations(expectedUrl, ApplicationCountSummaryPageResource.class, pageResource, OK);

        ApplicationCountSummaryPageResource result = service.getApplicationCountSummariesByCompetitionId(1L, 2, 3, "filter").getSuccess();
        Assert.assertEquals(pageResource, result);
    }

    @Test
    public void getApplicationCountSummariesByCompetitionIdAndInnovationArea() {
        String expectedUrl = "/application-count-summary/find-by-competition-id-and-innovation-area/1?assessorId=10&page=2&size=3&filter=filter&sortField=&innovationArea=4";
        ApplicationCountSummaryPageResource pageResource = new ApplicationCountSummaryPageResource();

        setupGetWithRestResultExpectations(expectedUrl, ApplicationCountSummaryPageResource.class, pageResource, OK);

        ApplicationCountSummaryPageResource result = service.getApplicationCountSummariesByCompetitionIdAndInnovationArea(1L, 10L,2, 3, ofNullable(4L), "filter", "").getSuccess();
        Assert.assertEquals(pageResource, result);
    }
}
