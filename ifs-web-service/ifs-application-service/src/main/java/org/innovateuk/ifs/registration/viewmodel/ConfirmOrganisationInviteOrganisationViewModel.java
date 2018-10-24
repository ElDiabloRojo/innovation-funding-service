package org.innovateuk.ifs.registration.viewmodel;

import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;

/**
 * View model for invited organisation confirmation
 */
public class ConfirmOrganisationInviteOrganisationViewModel {
    private String partOfOrganisation;
    private String organisationType;
    private String registrationName;
    private String registrationNumber;
    private long organisationTypeId;
    private String emailLeadApplicant;
    private String registerUrl;

    public ConfirmOrganisationInviteOrganisationViewModel(ApplicationInviteResource inviteResource, OrganisationResource organisation, String registerUrl) {
        this.partOfOrganisation = inviteResource.getInviteOrganisationNameConfirmedSafe();
        this.organisationType = organisation.getOrganisationTypeName();
        this.organisationTypeId = organisation.getOrganisationType();
        this.registrationName = organisation.getName();
        this.registrationNumber = organisation.getCompaniesHouseNumber();
        this.emailLeadApplicant = inviteResource.getLeadApplicantEmail();
        this.registerUrl = registerUrl;
    }

    public String getPartOfOrganisation() {
        return partOfOrganisation;
    }

    public String getOrganisationType() {
        return organisationType;
    }

    public String getRegistrationName() {
        return registrationName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Boolean getRegistrationNumberNotEmptyAndNotResearch() {
        return registrationNumber != null && !registrationNumber.isEmpty() && !OrganisationTypeEnum.isResearch(organisationTypeId);
    }

    public String getEmailLeadApplicant() {
        return emailLeadApplicant;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }
}