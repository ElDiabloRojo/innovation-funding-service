package org.innovateuk.ifs.finance.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.transactional.ApplicationService;
import org.innovateuk.ifs.application.transactional.FormInputResponseService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.finance.resource.*;
import org.innovateuk.ifs.form.domain.Question;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.form.transactional.QuestionService;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.util.AuthenticationHelper;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.finance.builder.EmployeesAndTurnoverResourceBuilder.newEmployeesAndTurnoverResource;
import static org.innovateuk.ifs.finance.builder.GrowthTableResourceBuilder.newGrowthTableResource;
import static org.innovateuk.ifs.finance.builder.OrganisationFinancesWithoutGrowthTableResourceBuilder.newOrganisationFinancesWithoutGrowthTableResource;
import static org.innovateuk.ifs.finance.resource.OrganisationSize.MEDIUM;
import static org.innovateuk.ifs.form.builder.QuestionBuilder.newQuestion;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ApplicationOrganisationFinanceServiceImplTest extends BaseServiceUnitTest<ApplicationOrganisationFinanceServiceImpl> {

    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationFinanceService financeService;
    @Mock
    private QuestionService questionService;
    @Mock
    private FormInputResponseService formInputResponseService;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AuthenticationHelper authenticationHelper;

    @Test
    public void getOrganisationWithGrowthTable() {
        long competitionId = 5;
        YearMonth financialYearEnd = YearMonth.of(2019, JANUARY);
        OrganisationSize organisationSize = MEDIUM;

        BigDecimal annualTurnover = BigDecimal.valueOf(123);
        BigDecimal annualProfits = BigDecimal.valueOf(234);
        BigDecimal annualExports = BigDecimal.valueOf(456);
        BigDecimal researchAndDevelopment = BigDecimal.valueOf(789);
        long financialHeadCount = 11;

        Application application = newApplication().build();
        ApplicationResource applicationResource = newApplicationResource()
                .withCompetition(competitionId)
                .build();
        Organisation organisation = newOrganisation().build();
        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withOrganisationSize(organisationSize)
                .withFinancialYearAccounts(newGrowthTableResource()
                        .withAnnualTurnovers(annualTurnover)
                        .withAnnualProfits(annualProfits)
                        .withAnnualExport(annualExports)
                        .withResearchAndDevelopment(researchAndDevelopment)
                        .withEmployees(financialHeadCount)
                        .withFinancialYearEnd(financialYearEnd.atEndOfMonth())
                        .build())
                .build();

        when(applicationService.getApplicationById(application.getId())).thenReturn(serviceSuccess(applicationResource));
        when(financeService.financeDetails(application.getId(), organisation.getId())).thenReturn(serviceSuccess(applicationFinanceResource));

        OrganisationFinancesWithGrowthTableResource expectedOrganisationFinances = new OrganisationFinancesWithGrowthTableResource();
        expectedOrganisationFinances.setOrganisationSize(organisationSize);
        expectedOrganisationFinances.setFinancialYearEnd(financialYearEnd);
        expectedOrganisationFinances.setAnnualTurnoverAtLastFinancialYear(annualTurnover);
        expectedOrganisationFinances.setAnnualProfitsAtLastFinancialYear(annualProfits);
        expectedOrganisationFinances.setAnnualExportAtLastFinancialYear(annualExports);
        expectedOrganisationFinances.setResearchAndDevelopmentSpendAtLastFinancialYear(researchAndDevelopment);
        expectedOrganisationFinances.setHeadCountAtLastFinancialYear(financialHeadCount);

        ServiceResult<OrganisationFinancesWithGrowthTableResource> result = service.getOrganisationWithGrowthTable(application.getId(), organisation.getId());

        assertEquals(expectedOrganisationFinances, result.getSuccess());
    }
    
    @Test
    public void getOrganisationWithGrowthTableWhenFinancialYearEndIsNull() {
        ApplicationResource application = newApplicationResource()
                .withId(1L)
                .withCompetition(2L)
                .build();
        Organisation organisation = newOrganisation()
                .withId(3L)
                .build();
        Question question = newQuestion()
                .withId(4L)
                .build();

        when(formInputResponseService.findResponseByApplicationIdQuestionIdOrganisationIdAndFormInputType(anyLong(), anyLong(), anyLong(), any(FormInputType.class)))
                .thenReturn(serviceSuccess(new FormInputResponseResource()));
        when(applicationService.getApplicationById(anyLong()))
                .thenReturn(serviceSuccess(application));
        when(financeService.financeDetails(anyLong(), anyLong()))
                .thenReturn(serviceSuccess(new ApplicationFinanceResource()));
        when(questionService.getQuestionByCompetitionIdAndFormInputType(anyLong(), any(FormInputType.class)))
                .thenReturn(serviceSuccess(question));
        when(formInputResponseService.findResponseByApplicationIdQuestionIdOrganisationIdFormInputTypeAndDescription(anyLong(), anyLong(), anyLong(), any(FormInputType.class), anyString()))
                .thenReturn(serviceSuccess(newFormInputResponseResource().build()));

        OrganisationFinancesWithGrowthTableResource expected = new OrganisationFinancesWithGrowthTableResource();
        expected.setFinancialYearEnd(null);

        ServiceResult<OrganisationFinancesWithGrowthTableResource> result = service.getOrganisationWithGrowthTable(application.getId(), organisation.getId());

        assertEquals(expected, result.getSuccess());
    }

    @Test
    public void getOrganisationWithoutGrowthTable() {
        long competitionId = 5;
        OrganisationSize organisationSize = OrganisationSize.LARGE;
        BigDecimal turnover = BigDecimal.valueOf(123);
        long headcount = 13;
        Application application = newApplication().build();
        Organisation organisation = newOrganisation().build();
        ApplicationResource applicationResource = newApplicationResource()
                .withCompetition(competitionId)
                .build();
        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withOrganisationSize(organisationSize)
                .withFinancialYearAccounts(newEmployeesAndTurnoverResource()
                        .withTurnover(turnover)
                        .withEmployees(headcount).build())
                .build();

        when(applicationService.getApplicationById(application.getId())).thenReturn(serviceSuccess(applicationResource));
        when(financeService.financeDetails(application.getId(), organisation.getId())).thenReturn(serviceSuccess(applicationFinanceResource));

        OrganisationFinancesWithoutGrowthTableResource expectedOrganisationFinances = new OrganisationFinancesWithoutGrowthTableResource(organisationSize, turnover, headcount);

        ServiceResult<OrganisationFinancesWithoutGrowthTableResource> result = service.getOrganisationWithoutGrowthTable(application.getId(), organisation.getId());

        assertEquals(expectedOrganisationFinances, result.getSuccess());
    }

    @Test
    public void updateOrganisationWithGrowthTable() {

        boolean stateAid = true;
        Competition competition = newCompetition().withStateAid(stateAid).build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        Application application = newApplication().withCompetition(competition).build();
        Organisation organisation = newOrganisation().build();
        OrganisationFinancesWithGrowthTableResource organisationFinancesWithGrowthTableResource = new OrganisationFinancesWithGrowthTableResource();
        User loggedInUser = newUser().build();
        GrowthTableResource growthTable = new GrowthTableResource();
        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withFinancialYearAccounts(growthTable)
                .build();

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(authenticationHelper.getCurrentlyLoggedInUser()).thenReturn(serviceSuccess(loggedInUser));
        when(applicationService.getCompetitionByApplicationId(application.getId())).thenReturn(serviceSuccess(competitionResource));
        when(financeService.financeDetails(application.getId(), organisation.getId()))
                .thenReturn(serviceSuccess(applicationFinanceResource));
        when(financeService.updateApplicationFinance(applicationFinanceResource.getId(), applicationFinanceResource)).thenReturn(serviceSuccess(applicationFinanceResource));

        ServiceResult<Void> result = service.updateOrganisationWithGrowthTable(application.getId(), organisation.getId(), organisationFinancesWithGrowthTableResource);

        assertTrue(result.isSuccess());

        assertEquals(organisationFinancesWithGrowthTableResource.getHeadCountAtLastFinancialYear(), growthTable.getEmployees());
        assertEquals(organisationFinancesWithGrowthTableResource.getAnnualExportAtLastFinancialYear(), growthTable.getAnnualExport());
    }

    @Test
    public void updateOrganisationWithoutGrowthTable() {

        boolean stateAid = true;

        Competition competition = newCompetition().withStateAid(stateAid).build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        Application application = newApplication().withCompetition(competition).build();
        Organisation organisation = newOrganisation().build();
        OrganisationFinancesWithoutGrowthTableResource organisationFinancesWithoutGrowthTableResource = newOrganisationFinancesWithoutGrowthTableResource().build();
        User loggedInUser = newUser().build();
        EmployeesAndTurnoverResource employeesAndTurnover = new EmployeesAndTurnoverResource();
        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withFinancialYearAccounts(employeesAndTurnover)
                .build();

        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(authenticationHelper.getCurrentlyLoggedInUser()).thenReturn(serviceSuccess(loggedInUser));
        when(applicationService.getCompetitionByApplicationId(application.getId())).thenReturn(serviceSuccess(competitionResource));
        when(financeService.financeDetails(application.getId(), organisation.getId()))
                .thenReturn(serviceSuccess(applicationFinanceResource));
        when(financeService.updateApplicationFinance(applicationFinanceResource.getId(), applicationFinanceResource)).thenReturn(serviceSuccess(applicationFinanceResource));

        ServiceResult<Void> result = service.updateOrganisationWithoutGrowthTable(application.getId(), organisation.getId(), organisationFinancesWithoutGrowthTableResource);

        assertTrue(result.isSuccess());

        assertEquals(organisationFinancesWithoutGrowthTableResource.getHeadCount(), employeesAndTurnover.getEmployees());
        assertEquals(organisationFinancesWithoutGrowthTableResource.getTurnover(), employeesAndTurnover.getTurnover());
    }

    @Test
    public void isShowStateAidAgreement() {

        boolean stateAid = true;

        Competition competition = newCompetition().withStateAid(stateAid).build();
        CompetitionResource competitionResource = newCompetitionResource().build();
        Application application = newApplication().build();
        ApplicationResource applicationResource = newApplicationResource().withCompetition(competition.getId()).build();
        Organisation organisation = newOrganisation().build();
        User loggedInUser = newUser().build();
        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource().build();

        when(applicationService.getApplicationById(application.getId())).thenReturn(serviceSuccess(applicationResource));
        when(authenticationHelper.getCurrentlyLoggedInUser()).thenReturn(serviceSuccess(loggedInUser));
        when(applicationService.getCompetitionByApplicationId(application.getId())).thenReturn(serviceSuccess(competitionResource));
        when(financeService.findApplicationFinanceByApplicationIdAndOrganisation(application.getId(), organisation.getId()))
                .thenReturn(serviceSuccess(applicationFinanceResource));

        service.isShowStateAidAgreement(application.getId(), organisation.getId()).getSuccess();
    }

    @Override
    protected ApplicationOrganisationFinanceServiceImpl supplyServiceUnderTest() {
        return new ApplicationOrganisationFinanceServiceImpl();
    }
}