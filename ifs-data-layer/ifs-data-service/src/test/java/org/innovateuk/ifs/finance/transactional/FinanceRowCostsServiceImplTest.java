package org.innovateuk.ifs.finance.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.domain.ApplicationFinanceRow;
import org.innovateuk.ifs.finance.domain.FinanceRowMetaField;
import org.innovateuk.ifs.finance.domain.FinanceRowMetaValue;
import org.innovateuk.ifs.finance.handler.OrganisationFinanceDefaultHandler;
import org.innovateuk.ifs.finance.handler.OrganisationFinanceDelegate;
import org.innovateuk.ifs.finance.mapper.ApplicationFinanceMapper;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRepository;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRowRepository;
import org.innovateuk.ifs.finance.repository.FinanceRowMetaFieldRepository;
import org.innovateuk.ifs.finance.repository.FinanceRowMetaValueRepository;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResourceId;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.resource.cost.SubContractingCost;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.domain.OrganisationType;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.LambdaMatcher.lambdaMatches;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceBuilder.newApplicationFinance;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceRowBuilder.newApplicationFinanceRow;
import static org.innovateuk.ifs.finance.builder.FinanceRowMetaFieldBuilder.newFinanceRowMetaField;
import static org.innovateuk.ifs.finance.builder.FinanceRowMetaValueBuilder.newFinanceRowMetaValue;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.organisation.builder.OrganisationTypeBuilder.newOrganisationType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 *
 */
public class FinanceRowCostsServiceImplTest extends BaseServiceUnitTest<FinanceRowCostsServiceImpl> {

    @Mock
    private OrganisationFinanceDefaultHandler organisationFinanceDefaultHandlerMock;

    @Mock
    private ApplicationRepository applicationRepositoryMock;

    @Mock
    private OrganisationFinanceDelegate organisationFinanceDelegateMock;

    @Mock
    private OrganisationRepository organisationRepositoryMock;

    @Mock
    private ApplicationFinanceRepository applicationFinanceRepositoryMock;

    @Mock
    private ApplicationFinanceRowRepository applicationFinanceRowRepositoryMock;

    @Mock
    private ApplicationFinanceMapper applicationFinanceMapperMock;

    @Mock
    private FinanceRowMetaValueRepository financeRowMetaValueRepositoryMock;

    @Mock
    private FinanceRowMetaFieldRepository financeRowMetaFieldRepositoryMock;

    @Override
    protected FinanceRowCostsServiceImpl supplyServiceUnderTest() {
        return new FinanceRowCostsServiceImpl();
    }

    private FinanceRowItem newFinanceRowItem;
    private ApplicationFinance applicationFinance;
    private long costId;
    private FinanceRowMetaField financeRowMetaField;

    @Before
    public void setUp() {
        costId = 1;
        String metaFieldTitle = "country";
        String metaFieldType = "String";

        Application application = newApplication()
                .withCompetition(newCompetition().withCompetitionStatus(CompetitionStatus.OPEN).build()
                ).build();
        OrganisationType organisationType = newOrganisationType().withOrganisationType(OrganisationTypeEnum.RESEARCH).build();
        newFinanceRowItem = new SubContractingCost(costId, new BigDecimal(10), "Scotland", "nibbles", "purring");
        applicationFinance = newApplicationFinance()
                .withApplication(application)
                .withOrganisation(newOrganisation().withOrganisationType(organisationType).build())
                .build();
        financeRowMetaField = newFinanceRowMetaField()
                .withTitle(metaFieldTitle)
                .withType(metaFieldType).build();

        when(applicationRepositoryMock.findOne(application.getId())).thenReturn(application);
        when(organisationFinanceDelegateMock.getOrganisationFinanceHandler(application.getCompetition().getId(), organisationType.getId())).thenReturn(organisationFinanceDefaultHandlerMock);
    }


    @Test
    public void addCost() {
        Organisation organisation = newOrganisation().withOrganisationType(newOrganisationType().withOrganisationType(OrganisationTypeEnum.BUSINESS).build()).build();
        final Competition openCompetition = newCompetition().withCompetitionStatus(CompetitionStatus.OPEN).build();
        Application application = newApplication().withCompetition(openCompetition).build();

        when(applicationRepositoryMock.findOne(123L)).thenReturn(application);
        when(organisationRepositoryMock.findOne(456L)).thenReturn(organisation);
        when(organisationFinanceDelegateMock.getOrganisationFinanceHandler(application.getCompetition().getId(), OrganisationTypeEnum.BUSINESS.getId())).thenReturn(organisationFinanceDefaultHandlerMock);

        ApplicationFinance newFinance = new ApplicationFinance(application, organisation);

        ApplicationFinance newFinanceExpectations = argThat(lambdaMatches(finance -> {
            assertEquals(application, finance.getApplication());
            assertEquals(organisation, finance.getOrganisation());
            return true;
        }));


        ApplicationFinanceResource expectedFinance = newApplicationFinanceResource().
                with(id(newFinance.getId())).
                withOrganisation(organisation.getId()).
                withApplication(application.getId()).
                build();

        when(applicationFinanceRepositoryMock.save(newFinanceExpectations)).thenReturn(newFinance);
        when(applicationFinanceMapperMock.mapToResource(newFinance)).thenReturn(expectedFinance);

        ServiceResult<ApplicationFinanceResource> result = service.addCost(new ApplicationFinanceResourceId(123L, 456L));
        assertTrue(result.isSuccess());
        assertEquals(expectedFinance, result.getSuccess());
    }

    @Test
    public void addWhenApplicationNotOpen() {
        final Competition openCompetition = newCompetition().withCompetitionStatus(CompetitionStatus.IN_ASSESSMENT).build();
        Application application = newApplication().withCompetition(openCompetition).build();
        when(applicationRepositoryMock.findOne(123L)).thenReturn(application);
        ServiceResult<ApplicationFinanceResource> result = service.addCost(new ApplicationFinanceResourceId(123L, 456L));
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.COMPETITION_NOT_OPEN));
    }


    @Test
    public void alreadyExistingMetaValueShouldBeUpdated() {
        List<FinanceRowMetaValue> currentFinanceRowMetaValue = singletonList(newFinanceRowMetaValue()
                .withFinanceRowMetaField(financeRowMetaField)
                .withValue("England")
                .build());

        List<FinanceRowMetaValue> newFinanceRowMetaValue = singletonList(newFinanceRowMetaValue()
                .withFinanceRowMetaField(financeRowMetaField)
                .withValue("purring")
                .build());

        ApplicationFinanceRow convertedApplicationFinanceRow = newApplicationFinanceRow()
                .withFinanceRowMetadata(newFinanceRowMetaValue)
                .withTarget(applicationFinance).build();

        ApplicationFinanceRow currentApplicationFinanceRow = newApplicationFinanceRow()
                .withFinanceRowMetadata(currentFinanceRowMetaValue)
                .withTarget(applicationFinance).build();

        when(applicationFinanceRowRepositoryMock.findOne(costId)).thenReturn(currentApplicationFinanceRow);
        when(organisationFinanceDefaultHandlerMock.costItemToCost(any())).thenReturn(convertedApplicationFinanceRow);
        when(organisationFinanceDefaultHandlerMock.updateCost(any())).thenReturn(convertedApplicationFinanceRow);
        when(financeRowMetaValueRepositoryMock.financeRowIdAndFinanceRowMetaFieldId(any(), any())).thenReturn(currentFinanceRowMetaValue.get(0));

        ServiceResult<FinanceRowItem> result = service.updateCost(costId, newFinanceRowItem);

        assertTrue(result.isSuccess());

        FinanceRowMetaValue combinedFinanceRowMetaValue = currentFinanceRowMetaValue.get(0);
        combinedFinanceRowMetaValue.setValue(newFinanceRowMetaValue.get(0).getValue());

        verify(financeRowMetaValueRepositoryMock, times(1)).save(combinedFinanceRowMetaValue);
    }

    @Test
    public void nonExistingMetaValueShouldBeCreated() {
        List<FinanceRowMetaValue> financeRowMetaValue = singletonList(
                newFinanceRowMetaValue()
                        .withFinanceRowMetaField(financeRowMetaField)
                        .withValue("England")
                        .build()
        );

        ApplicationFinanceRow convertedApplicationFinanceRow = newApplicationFinanceRow()
                .withFinanceRowMetadata(financeRowMetaValue)
                .withTarget(applicationFinance).build();

        ApplicationFinanceRow currentApplicationFinanceRow = newApplicationFinanceRow()
                .withTarget(applicationFinance).build();

        when(applicationFinanceRowRepositoryMock.findOne(costId)).thenReturn(currentApplicationFinanceRow);
        when(organisationFinanceDefaultHandlerMock.costItemToCost(any())).thenReturn(convertedApplicationFinanceRow);
        when(organisationFinanceDefaultHandlerMock.updateCost(any())).thenReturn(currentApplicationFinanceRow);
        when(financeRowMetaValueRepositoryMock.financeRowIdAndFinanceRowMetaFieldId(any(), any())).thenReturn(null);
        when(financeRowMetaFieldRepositoryMock.findOne(financeRowMetaField.getId())).thenReturn(financeRowMetaField);

        ServiceResult<FinanceRowItem> result = service.updateCost(costId, newFinanceRowItem);

        assertTrue(result.isSuccess());
        verify(financeRowMetaValueRepositoryMock, times(1)).save(financeRowMetaValue.get(0));
    }

    @Test
    public void noAttachedMetaValueDoesNotCreateOrUpdateMetaValue() {
        ApplicationFinanceRow convertedApplicationFinanceRow = newApplicationFinanceRow()
                .withTarget(applicationFinance).build();

        ApplicationFinanceRow currentApplicationFinanceRow = newApplicationFinanceRow()
                .withTarget(applicationFinance).build();

        when(applicationFinanceRowRepositoryMock.findOne(costId)).thenReturn(currentApplicationFinanceRow);
        when(organisationFinanceDefaultHandlerMock.costItemToCost(any())).thenReturn(convertedApplicationFinanceRow);
        when(organisationFinanceDefaultHandlerMock.updateCost(any())).thenReturn(currentApplicationFinanceRow);
        when(financeRowMetaValueRepositoryMock.financeRowIdAndFinanceRowMetaFieldId(any(), any())).thenReturn(null);
        when(financeRowMetaFieldRepositoryMock.findOne(financeRowMetaField.getId())).thenReturn(financeRowMetaField);

        ServiceResult<FinanceRowItem> result = service.updateCost(costId, newFinanceRowItem);

        assertTrue(result.isSuccess());
        verify(financeRowMetaValueRepositoryMock, times(0)).save(any(FinanceRowMetaValue.class));
    }

}
