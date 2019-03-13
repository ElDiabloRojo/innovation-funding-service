package org.innovateuk.ifs.eugrant.domain;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Registers EU Grant funding for a UK Organisation.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
public class EuGrant {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @JoinColumn(name = "eu_organisation_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private EuOrganisation organisation;

    @JoinColumn(name = "eu_contact_id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private EuContact contact;

    @JoinColumn(name = "eu_funding_id")
    @OneToOne(cascade = CascadeType.ALL)
    private EuFunding funding;

    private boolean submitted;

    private boolean notified;

    private String shortCode;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdOn;

    @LastModifiedDate
    private ZonedDateTime modifiedOn;

    public EuGrant() {
        this.submitted = false;
    }

    public EuGrant submit(String shortCode) {
        if (submitted) {
            throw new IllegalStateException("cannot resubmit an eugrant");
        }
        if (!isOrganisationComplete() || !isContactComplete() || !isFundingComplete()) {
            throw new IllegalStateException("cannot submit until organisation, contact and funding are complete");
        }
        this.submitted = true;
        this.shortCode = shortCode;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setContact(EuContact contact) {
        this.contact = contact;
    }

    public EuContact getContact() {
        return contact;
    }

    public void setOrganisation(EuOrganisation organisation) {
        this.organisation = organisation;
    }

    public EuOrganisation getOrganisation() {
        return organisation;
    }

    public EuFunding getFunding() {
        return funding;
    }

    public void setFunding(EuFunding funding) {
        this.funding = funding;
    }

    public String getShortCode() {
        return shortCode;
    }

    public boolean isOrganisationComplete() {
        return organisation != null;
    }

    public boolean isContactComplete() {
        return contact != null;
    }

    public boolean isFundingComplete() {
        return funding != null;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public ZonedDateTime getModifiedOn() {
        return modifiedOn;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}