package org.innovateuk.ifs.organisation.viewmodel;

import org.innovateuk.ifs.organisation.resource.OrganisationTypeResource;

import java.util.List;

/**
 * View model for Organisation creation lead applicant - choosing organisation type
 */
public class OrganisationCreationSelectTypeViewModel {
    private List<OrganisationTypeResource> types;

    public OrganisationCreationSelectTypeViewModel(List<OrganisationTypeResource> types) {
        this.types = types;
    }

    public List<OrganisationTypeResource> getTypes() {
        return types;
    }

    public void setTypes(List<OrganisationTypeResource> types) {
        this.types = types;
    }

}
