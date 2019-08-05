package org.innovateuk.ifs.application.forms.sections.yourfunding.saver;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.forms.sections.yourfunding.form.OtherFundingRowForm;
import org.innovateuk.ifs.application.forms.sections.yourfunding.form.YourFundingForm;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.category.OtherFundingCostCategory;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.finance.resource.cost.GrantClaimPercentage;
import org.innovateuk.ifs.finance.resource.cost.OtherFunding;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRowRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.application.forms.sections.yourprojectcosts.form.AbstractCostRowForm.UNSAVED_ROW_PREFIX;
import static org.innovateuk.ifs.application.forms.sections.yourprojectcosts.form.AbstractCostRowForm.generateUnsavedRowId;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.finance.builder.GrantClaimCostCategoryBuilder.newGrantClaimCostCategory;
import static org.innovateuk.ifs.finance.builder.OtherFundingCostBuilder.newOtherFunding;
import static org.innovateuk.ifs.finance.builder.OtherFundingCostCategoryBuilder.newOtherFundingCostCategory;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class YourFundingSaverTest extends BaseServiceUnitTest<YourFundingSaver> {
    private static final long APPLICATION_ID = 1L;

    @Mock
    private ApplicationFinanceRestService applicationFinanceRestService;

    @Mock
    private OrganisationRestService organisationRestService;

    @Mock
    private ApplicationFinanceRowRestService financeRowRestService;

    @Override
    protected YourFundingSaver supplyServiceUnderTest() {
        return new YourFundingSaver();
    }

    @Test
    public void save() {
        long otherFundingQuestionId = 2L;
        OrganisationResource organisation = newOrganisationResource().build();
        UserResource user = newUserResource().build();
        OtherFunding otherFunding = newOtherFunding()
                .withFundingSource(OtherFundingCostCategory.OTHER_FUNDING)
                .withOtherPublicFunding("No")
                .build();
        ApplicationFinanceResource finance = newApplicationFinanceResource()
                .withFinanceOrganisationDetails(asMap(
                FinanceRowType.FINANCE,  newGrantClaimCostCategory()
                    .withCosts(asList(new GrantClaimPercentage(1L)))
                    .build(),
                FinanceRowType.OTHER_FUNDING, newOtherFundingCostCategory()
                    .withCosts(asList(otherFunding))
                    .build()
        )).build();

        when(financeRowRestService.update(any())).thenReturn(restSuccess(new ValidationMessages()));
        when(financeRowRestService.create(any())).thenReturn(restSuccess(mock(FinanceRowItem.class)));
        when(organisationRestService.getByUserAndApplicationId(user.getId(), APPLICATION_ID)).thenReturn(restSuccess(organisation));
        when(applicationFinanceRestService.getFinanceDetails(APPLICATION_ID, organisation.getId())).thenReturn(restSuccess(finance));

        YourFundingForm form = new YourFundingForm();
        form.setRequestingFunding(true);
        form.setGrantClaimPercentage(100);

        form.setOtherFundingQuestionId(otherFundingQuestionId);
        form.setOtherFunding(true);

        OtherFundingRowForm emptyRow = new OtherFundingRowForm(new OtherFunding(null, null, "emptySource", "emptyDate", new BigDecimal(123), finance.getId()));

        OtherFundingRowForm existingRow = new OtherFundingRowForm(new OtherFunding(20L, null, "existingSource", "existingDate", new BigDecimal(321), finance.getId()));

        form.setOtherFundingRows(asMap(
                generateUnsavedRowId(), emptyRow,
                "20", existingRow
                ));

        service.save(APPLICATION_ID, form, user);

        GrantClaimPercentage expectedGrantClaim = new GrantClaimPercentage(finance.getGrantClaim().getId(), 100, finance.getId());
        verify(financeRowRestService).update(expectedGrantClaim);

        OtherFunding expectedOtherFundingSet = new OtherFunding(finance.getId());
        expectedOtherFundingSet.setId(otherFunding.getId());
        expectedOtherFundingSet.setOtherPublicFunding("Yes");
        expectedOtherFundingSet.setFundingSource(OtherFundingCostCategory.OTHER_FUNDING);
        verify(financeRowRestService).update(expectedGrantClaim);

        OtherFunding expectedEmptyRow = new OtherFunding(null, null, "emptySource", "emptyDate", new BigDecimal(123), finance.getId());
        verify(financeRowRestService).create(expectedEmptyRow);

        OtherFunding updatedEmptyRow = new OtherFunding(20L, null, "existingSource", "existingDate", new BigDecimal(321), finance.getId());
        verify(financeRowRestService).update(updatedEmptyRow);
    }


    @Test
    public void removeOtherFundingRowForm() {
        String rowId = "12";
        YourFundingForm form = new YourFundingForm();
        form.setOtherFundingRows(asMap(rowId, new OtherFundingRowForm()));

        service.removeOtherFundingRowForm(form, rowId);

        assertTrue(form.getOtherFundingRows().isEmpty());
        verify(financeRowRestService).delete(Long.valueOf(rowId));
    }

    @Test
    public void addOtherFundingRow() {
        YourFundingForm form = new YourFundingForm();
        form.setOtherFundingRows(new LinkedHashMap<>());

        service.addOtherFundingRow(form);

        assertTrue(form.getOtherFundingRows().keySet().stream().anyMatch(key -> key.startsWith(UNSAVED_ROW_PREFIX)));
    }
}
