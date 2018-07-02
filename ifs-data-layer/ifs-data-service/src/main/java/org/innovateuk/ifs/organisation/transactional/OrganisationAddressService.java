package org.innovateuk.ifs.organisation.transactional;

import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;

public interface OrganisationAddressService {

    @NotSecured(value = "Anyone can see an OrganisationAddress", mustBeSecuredByOtherServices = false)
    ServiceResult<OrganisationAddressResource> findOne(Long id);

    @NotSecured(value = "Anyone can see an OrganisationAddress", mustBeSecuredByOtherServices = false)
    ServiceResult<OrganisationAddressResource> findByOrganisationIdAndAddressId(long organisationId, long addressId);
}
