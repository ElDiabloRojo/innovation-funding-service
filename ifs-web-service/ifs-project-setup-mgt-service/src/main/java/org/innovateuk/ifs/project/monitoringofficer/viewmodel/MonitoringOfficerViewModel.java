package org.innovateuk.ifs.project.monitoringofficer.viewmodel;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.application.resource.CompetitionSummaryResource;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * View model to back the Monitoring Officer page
 */
public class MonitoringOfficerViewModel {

    private Long projectId;
    private String projectTitle;
    private Long applicationId;
    private String area;
    private LocalDate targetProjectStartDate;
    private String projectManagerName;
    private List<String> partnerOrganisationNames;
    private String leadOrganisationName;
    private CompetitionSummaryResource competitionSummary;
    private boolean existingMonitoringOfficer;
    private boolean editMode;
    private boolean editable;
    private List<String> primaryAddressLines;

    public MonitoringOfficerViewModel(Long projectId, String projectTitle, Long applicationId, String area, AddressResource primaryAddress,
                                             LocalDate targetProjectStartDate, String projectManagerName,
                                             List<String> partnerOrganisationNames, String leadOrganisationName,
                                             CompetitionSummaryResource competitionSummary, boolean existingMonitoringOfficer,
                                             boolean editMode, boolean editable) {
        this.projectId = projectId;
        this.projectTitle = projectTitle;
        this.applicationId = applicationId;
        this.area = area;
        this.primaryAddressLines = primaryAddress != null ? primaryAddress.getNonEmptyLines() : emptyList();
        this.targetProjectStartDate = targetProjectStartDate;
        this.projectManagerName = projectManagerName;
        this.partnerOrganisationNames = partnerOrganisationNames;
        this.leadOrganisationName = leadOrganisationName;
        this.competitionSummary = competitionSummary;
        this.existingMonitoringOfficer = existingMonitoringOfficer;
        this.editMode = editMode;
        this.editable = editable;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getArea() {
        return area;
    }

    public LocalDate getTargetProjectStartDate() {
        return targetProjectStartDate;
    }

    public String getProjectManagerName() {
        return projectManagerName;
    }

    public List<String> getPartnerOrganisationNames() {
        return partnerOrganisationNames;
    }

    public CompetitionSummaryResource getCompetitionSummary() {
        return competitionSummary;
    }

    public String getLeadOrganisationName() {
        return leadOrganisationName;
    }

    public void setLeadOrganisationName(String leadOrganisationName) {
        this.leadOrganisationName = leadOrganisationName;
    }

    public List<String> getPrimaryAddressLines() {
        return primaryAddressLines;
    }

    public boolean isReadOnly() {
        return !editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public boolean isExistingMonitoringOfficer() {
        return existingMonitoringOfficer;
    }

    public boolean isDisplayMonitoringOfficerAssignedMessage() {
        return existingMonitoringOfficer && isReadOnly();
    }

    public boolean isDisplayChangeMonitoringOfficerLink() {
        return isReadOnly() && isEditable();
    }

    public boolean isDisplayAssignMonitoringOfficerButton() {
        return isEditMode() && isEditable();
    }

    public boolean isEditable() {
        return editable;
    }
}
