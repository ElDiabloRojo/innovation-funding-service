package org.innovateuk.ifs.project.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.project.document.resource.ProjectDocumentResource;

import javax.validation.constraints.Digits;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectResource {
    private static final int MAX_DURATION_IN_MONTHS_DIGITS = 2;

    private Long id;
    private long application;
    private long competition;
    private LocalDate targetStartDate;
    private AddressResource address;
    private String name;
    private ZonedDateTime documentsSubmittedDate;
    private ZonedDateTime offerSubmittedDate;
    private List<Long> projectUsers;
    private Long signedGrantOfferLetter;
    private Long grantOfferLetter;
    private Long additionalContractFile;
    private String grantOfferLetterRejectionReason;
    private ApprovalType otherDocumentsApproved;
    private ZonedDateTime spendProfileSubmittedDate;
    private ProjectState projectState;
    private List<ProjectDocumentResource> projectDocuments = new ArrayList<>();

    @Digits(integer = MAX_DURATION_IN_MONTHS_DIGITS, fraction = 0, message="{validation.application.details.duration.in.months.max.digits}")
    private Long durationInMonths;

    @JsonIgnore
    public boolean isOfferSubmitted(){
        return offerSubmittedDate != null;
    }

    @JsonIgnore
    public boolean isWithdrawn() { return projectState.equals(ProjectState.WITHDRAWN); }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTargetStartDate() {
        return targetStartDate;
    }

    public void setTargetStartDate(LocalDate targetStartDate) {
        this.targetStartDate = targetStartDate;
    }

    public AddressResource getAddress() {
        return address;
    }

    public void setAddress(AddressResource address) {
        this.address = address;
    }

    public Long getDurationInMonths() {
        return durationInMonths;
    }

    public void setDurationInMonths(Long durationInMonths) {
        this.durationInMonths = durationInMonths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getProjectUsers() {
        return projectUsers;
    }

    public void setProjectUsers(List<Long> projectUsers) {
        this.projectUsers = projectUsers;
    }

    public long getApplication() {
        return application;
    }

    public void setApplication(long application) {
        this.application = application;
    }

    public long getCompetition() {
        return competition;
    }

    public void setCompetition(long competition) {
        this.competition = competition;
    }

    public ZonedDateTime getDocumentsSubmittedDate() {
        return documentsSubmittedDate;
    }

    public void setDocumentsSubmittedDate(ZonedDateTime documentsSubmittedDate) {
        this.documentsSubmittedDate = documentsSubmittedDate;
    }

    public ZonedDateTime getOfferSubmittedDate() {
        return offerSubmittedDate;
    }

    public void setOfferSubmittedDate(ZonedDateTime offerSubmittedDate) {
        this.offerSubmittedDate = offerSubmittedDate;
    }

    public Long getSignedGrantOfferLetter() {
        return signedGrantOfferLetter;
    }

    public void setSignedGrantOfferLetter(Long signedGrantOfferLetter) {
        this.signedGrantOfferLetter = signedGrantOfferLetter;
    }

    public Long getAdditionalContractFile() {
        return additionalContractFile;
    }

    public void setAdditionalContractFile(Long additionalContractFile) {
        this.additionalContractFile = additionalContractFile;
    }

    public Long getGrantOfferLetter() {
        return grantOfferLetter;
    }

    public void setGrantOfferLetter(Long grantOfferLetter) {
        this.grantOfferLetter = grantOfferLetter;
    }

    public String getGrantOfferLetterRejectionReason() {
        return grantOfferLetterRejectionReason;
    }

    public void setGrantOfferLetterRejectionReason(String grantOfferLetterRejectionReason) {
        this.grantOfferLetterRejectionReason = grantOfferLetterRejectionReason;
    }

    public ApprovalType getOtherDocumentsApproved() {
        return otherDocumentsApproved;
    }

    public void setOtherDocumentsApproved(ApprovalType otherDocumentsApproved) {
        this.otherDocumentsApproved = otherDocumentsApproved;
    }

    public ZonedDateTime getSpendProfileSubmittedDate() {
        return spendProfileSubmittedDate;
    }

    public void setSpendProfileSubmittedDate(ZonedDateTime spendProfileSubmittedDate) {
        this.spendProfileSubmittedDate = spendProfileSubmittedDate;
    }

    public ProjectState getProjectState() {
        return projectState;
    }

    public void setProjectState(ProjectState projectState) {
        this.projectState = projectState;
    }

    public List<ProjectDocumentResource> getProjectDocuments() {
        return projectDocuments;
    }

    public void setProjectDocuments(List<ProjectDocumentResource> projectDocuments) {
        this.projectDocuments = projectDocuments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProjectResource that = (ProjectResource) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(application, that.application)
                .append(targetStartDate, that.targetStartDate)
                .append(address, that.address)
                .append(name, that.name)
                .append(documentsSubmittedDate, that.documentsSubmittedDate)
                .append(offerSubmittedDate, that.offerSubmittedDate)
                .append(projectUsers, that.projectUsers)
                .append(signedGrantOfferLetter, that.signedGrantOfferLetter)
                .append(grantOfferLetter, that.grantOfferLetter)
                .append(additionalContractFile, that.additionalContractFile)
                .append(grantOfferLetterRejectionReason, that.grantOfferLetterRejectionReason)
                .append(spendProfileSubmittedDate, that.spendProfileSubmittedDate)
                .append(durationInMonths, that.durationInMonths)
                .append(projectState, that.projectState)
                .append(otherDocumentsApproved, that.otherDocumentsApproved)
                .append(projectDocuments, that.projectDocuments)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(application)
                .append(targetStartDate)
                .append(address)
                .append(name)
                .append(documentsSubmittedDate)
                .append(offerSubmittedDate)
                .append(projectUsers)
                .append(signedGrantOfferLetter)
                .append(grantOfferLetter)
                .append(additionalContractFile)
                .append(grantOfferLetterRejectionReason)
                .append(spendProfileSubmittedDate)
                .append(durationInMonths)
                .append(projectState)
                .append(otherDocumentsApproved)
                .append(projectDocuments)
                .toHashCode();
    }
}
