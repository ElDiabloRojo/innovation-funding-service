package org.innovateuk.ifs.management.viewmodel;


import java.util.List;

/**
 * Holder of model attributes for the Manage applications page
 */
public class ManagePanelApplicationsViewModel {
    private long competitionId;
    private String competitionName;
    private String competitionStatus;
    private List<ManagePanelApplicationsRowViewModel> applications;
    private String filter;
    private String sorting;
    private PaginationViewModel pagination;

    public ManagePanelApplicationsViewModel(Long competitionId,
                                       String competitionName,
                                       String competitionStatus,
                                       List<ManagePanelApplicationsRowViewModel> applications,
                                       String filter,
                                       String sorting,
                                       PaginationViewModel pagination) {
        this.competitionId = competitionId;
        this.competitionName = competitionName;
        this.competitionStatus = competitionStatus;
        this.applications = applications;
        this.filter = filter;
        this.sorting = sorting;
        this.pagination = pagination;
    }

    public long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public String getCompetitionStatus() { return competitionStatus; }

    public List<ManagePanelApplicationsRowViewModel> getApplications() {
        return applications;
    }

    public String getFilter() {
        return filter;
    }

    public String getSorting() { return sorting; }

    public PaginationViewModel getPagination() {
        return pagination;
    }
}

