package org.innovateuk.ifs.organisation.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;
import org.innovateuk.ifs.organisation.transactional.OrganisationApplicationAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organisation-application-address")
public class OrganisationAddressController {

    @Autowired
    private OrganisationApplicationAddressService service;

    @GetMapping("/organisation/{organisationId}/application/{applicationId}/address/{addressId}")
    public RestResult<OrganisationAddressResource> findByOrganisationIdAndAddressId(@PathVariable long organisationId,
                                                                                    @PathVariable long applicationId,
                                                                                    @PathVariable long addressId) {
        return service.findByOrganisationIdAndApplicationIdAndAddressId(organisationId, applicationId, addressId).toGetResponse();
    }
}
