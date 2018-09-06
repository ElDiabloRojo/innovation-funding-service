package org.innovateuk.ifs.management.application.list.viewmodel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.application.resource.PreviousApplicationResource;
import org.innovateuk.ifs.management.navigation.Pagination;

import java.util.List;

/**
 * View model for Competition Management Previous Applications page
 */
public class PreviousApplicationsViewModel {

    private Long competitionId;
    private String competitionName;
    private List<PreviousApplicationResource> previousApplications;
    private long previousApplicationsSize;
    private Pagination previousApplicationsPagination;
    private boolean isIfsAdmin;

    public PreviousApplicationsViewModel(Long competitionId, String competitionName, boolean isIfsAdmin,
                                         List<PreviousApplicationResource> previousApplications,
                                         long previousApplicationsSize,
                                         Pagination previousApplicationsPagination) {
        this.competitionId = competitionId;
        this.competitionName = competitionName;
        this.isIfsAdmin = isIfsAdmin;
        this.previousApplications = previousApplications;
        this.previousApplicationsSize = previousApplicationsSize;
        this.previousApplicationsPagination = previousApplicationsPagination;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public boolean isIfsAdmin() { return isIfsAdmin; }

    public List<PreviousApplicationResource> getPreviousApplications() {
        return previousApplications;
    }

    public long getPreviousApplicationsSize() {
        return previousApplicationsSize;
    }

    public Pagination getPreviousApplicationsPagination() {
        return previousApplicationsPagination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PreviousApplicationsViewModel that = (PreviousApplicationsViewModel) o;

        return new EqualsBuilder()
                .append(previousApplicationsSize, that.previousApplicationsSize)
                .append(competitionId, that.competitionId)
                .append(competitionName, that.competitionName)
                .append(isIfsAdmin, that.isIfsAdmin)
                .append(previousApplications, that.previousApplications)
                .append(previousApplicationsPagination, that.previousApplicationsPagination)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(competitionId)
                .append(competitionName)
                .append(isIfsAdmin)
                .append(previousApplications)
                .append(previousApplicationsSize)
                .append(previousApplicationsPagination)
                .toHashCode();
    }
}
