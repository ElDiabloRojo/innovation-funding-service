package org.innovateuk.ifs.application.common.viewmodel;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.finance.resource.BaseFinanceResource;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowType;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * View model for finance/finance-summary :: financial_summary_table.
 */
public class ApplicationFundingBreakdownViewModel {

    private final Map<FinanceRowType, BigDecimal> financeTotalPerType;
    private final List<OrganisationResource> applicationOrganisations;
    private final SectionResource financeSection;
    private BigDecimal financeTotal;
    private final OrganisationResource leadOrganisation;
    private final Map<Long, BaseFinanceResource> organisationFinances;
    private final List<String> pendingOrganisationNames;
    private final ApplicationResource currentApplication;
    private final OrganisationResource userOrganisation;
    private final Map<Long, Boolean> showDetailedFinanceLink;
    private final CompetitionResource currentCompetition;

    public ApplicationFundingBreakdownViewModel(
            Map<FinanceRowType, BigDecimal> financeTotalPerType,
            BigDecimal financeTotal,
            List<OrganisationResource> applicationOrganisations,
            SectionResource financeSection,
            OrganisationResource leadOrganisation,
            Map<Long, BaseFinanceResource> organisationFinances,
            List<String> pendingOrganisationNames,
            ApplicationResource currentApplication,
            OrganisationResource userOrganisation,
            Map<Long, Boolean> showDetailedFinanceLink,
            CompetitionResource currentCompetition) {
        this.financeTotalPerType = financeTotalPerType;
        this.financeTotal = financeTotal;
        this.applicationOrganisations = applicationOrganisations;
        this.financeSection = financeSection;
        this.leadOrganisation = leadOrganisation;
        this.organisationFinances = organisationFinances;
        this.pendingOrganisationNames = pendingOrganisationNames;
        this.currentApplication = currentApplication;
        this.userOrganisation = userOrganisation;
        this.showDetailedFinanceLink = showDetailedFinanceLink;
        this.currentCompetition = currentCompetition;
    }

    //For EOI Competitions
    public ApplicationFundingBreakdownViewModel(Map<FinanceRowType, BigDecimal> financeTotalPerType,
                                                List<OrganisationResource> applicationOrganisations,
                                                SectionResource financeSection,
                                                OrganisationResource leadOrganisation,
                                                Map<Long, BaseFinanceResource> organisationFinances,
                                                List<String> pendingOrganisationNames,
                                                ApplicationResource currentApplication,
                                                OrganisationResource userOrganisation,
                                                Map<Long, Boolean> showDetailedFinanceLink,
                                                CompetitionResource currentCompetition) {
        this.financeTotalPerType = financeTotalPerType;
        this.applicationOrganisations = applicationOrganisations;
        this.financeSection = financeSection;
        this.leadOrganisation = leadOrganisation;
        this.organisationFinances = organisationFinances;
        this.pendingOrganisationNames = pendingOrganisationNames;
        this.currentApplication = currentApplication;
        this.userOrganisation = userOrganisation;
        this.showDetailedFinanceLink = showDetailedFinanceLink;
        this.currentCompetition = currentCompetition;
    }

    public Map<FinanceRowType, BigDecimal> getFinanceTotalPerType() {
        return financeTotalPerType;
    }

    public BigDecimal getFinanceTotal() {
        return financeTotal;
    }

    public List<OrganisationResource> getApplicationOrganisations() {
        return applicationOrganisations;
    }

    public SectionResource getFinanceSection() {
        return financeSection;
    }

    public OrganisationResource getLeadOrganisation() {
        return leadOrganisation;
    }

    public List<String> getPendingOrganisationNames() {
        return pendingOrganisationNames;
    }

    public Map<Long, BaseFinanceResource> getOrganisationFinances() {
        return organisationFinances;
    }

    public ApplicationResource getCurrentApplication() {
        return currentApplication;
    }

    public OrganisationResource getUserOrganisation() {
        return userOrganisation;
    }

    public Map<Long, Boolean> getShowDetailedFinanceLink() {
        return showDetailedFinanceLink;
    }

    public CompetitionResource getCurrentCompetition() {
        return currentCompetition;
    }
}