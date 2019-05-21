package org.innovateuk.ifs.finance.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.LabourCost;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.costItemListType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectFinanceRowRestServiceMocksTest extends BaseRestServiceUnitTest<ProjectFinanceRowRestServiceImpl> {
    private static final String costRestURL = "/cost/project";

    @Test
    public void test_getCosts_forProjectFinanceId() {

        List<FinanceRowItem> returnedResponse = new ArrayList<>();

        setupGetWithRestResultExpectations(costRestURL + "/get/123", costItemListType(), returnedResponse);
        List<FinanceRowItem> costs = service.getCosts(123L).getSuccess();
        assertNotNull(costs);
        assertEquals(returnedResponse, costs);
    }

    @Test
    public void test_findById() {
        String expectedUrl = BaseRestServiceUnitTest.dataServicesUrl + costRestURL + "/123";
        FinanceRowItem returnedResponse = new LabourCost();

        setupGetWithRestResultExpectations(costRestURL + "/123", FinanceRowItem.class, returnedResponse);

        FinanceRowItem cost = service.findById(123L).getSuccess();
        assertNotNull(cost);
        Assert.assertEquals(returnedResponse, cost);
    }

    @Test
    public void test_add_byProjectFinanceIdAndQuestionId() {
        LabourCost costToUpdate = new LabourCost();
        String expectedUrl = costRestURL + "/add/123/456";
        setupPostWithRestResultExpectations(expectedUrl, ValidationMessages.class, costToUpdate, new ValidationMessages(), HttpStatus.OK);
        service.add(123L, 456L, costToUpdate).getSuccess();
    }

    @Test
    public void addWithResponse() {
        LabourCost costToUpdate = new LabourCost();
        String expectedUrl = costRestURL + "/add-with-response/123";
        setupPostWithRestResultExpectations(expectedUrl, FinanceRowItem.class, costToUpdate, new LabourCost(), HttpStatus.OK);
        service.addWithResponse(123L, costToUpdate).getSuccess();
    }

    @Test
    public void test_delete_byCostId() {
        setupDeleteWithRestResultExpectations(costRestURL + "/delete/456");
        service.delete(456L);
    }

    @Test
    public void test_update_byCost() {
        LabourCost costToUpdate = new LabourCost();
        String expectedUrl = costRestURL + "/update/" + costToUpdate.getId();
        setupPutWithRestResultExpectations(expectedUrl, ValidationMessages.class, costToUpdate, new ValidationMessages());
        service.update(costToUpdate).getSuccess();
    }

    @Override
    protected ProjectFinanceRowRestServiceImpl registerRestServiceUnderTest() {
        return new ProjectFinanceRowRestServiceImpl();
    }
}
