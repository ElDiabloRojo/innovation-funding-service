package org.innovateuk.ifs.organisation.controller;

import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.organisation.resource.OrganisationSearchResult;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.organisation.transactional.CompaniesHouseApiService;
import org.innovateuk.ifs.organisation.transactional.OrganisationService;
import org.innovateuk.ifs.organisation.transactional.OrganisationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This rest controller is only use to search organisations, form external systems, like Companies House, and the list of academics.
 * This is used when a user registers a new organisation.
 */
@RestController
@RequestMapping("/organisationsearch")
public class ExternalOrganisationSearchController {

    @Autowired
    private OrganisationService organisationService;
    @Autowired
    private OrganisationTypeService organisationTypeService;
    @Autowired
    private CompaniesHouseApiService companyHouseService;

    private static final int SEARCH_ITEMS_MAX = 10;


    @GetMapping("/searchOrganisations/{organisationType}")
    public RestResult<List<OrganisationSearchResult>> searchOrganisations(@PathVariable("organisationType") final long organisationTypeId,
                                                                          @RequestParam("organisationSearchText") final String organisationSearchText) {
        OrganisationTypeEnum organisationType = OrganisationTypeEnum.getFromId(organisationTypeId);

        switch (organisationType){
            case BUSINESS:
            case RTO:
            case PUBLIC_SECTOR_OR_CHARITY:
                return companyHouseService.searchOrganisations(organisationSearchText).toGetResponse();
            case RESEARCH:
                return organisationService.searchAcademic(organisationSearchText, SEARCH_ITEMS_MAX).toGetResponse();
            default:
                break;
        }

        return RestResult.restFailure(new Error("Search for organisation failed", HttpStatus.NOT_FOUND));
    }

    @GetMapping("/getOrganisation/{organisationType}/{organisationSearchId}")
    public RestResult<OrganisationSearchResult> searchOrganisation(@PathVariable("organisationType") final long organisationTypeId, @PathVariable("organisationSearchId") final String organisationSearchId) {
        OrganisationTypeEnum organisationType = OrganisationTypeEnum.getFromId(organisationTypeId);
        switch (organisationType){
            case BUSINESS:
            case RTO:
            case PUBLIC_SECTOR_OR_CHARITY:
                return companyHouseService.getOrganisationById(organisationSearchId).toGetResponse();
            case RESEARCH:
                return organisationService.getSearchOrganisation(Long.valueOf(organisationSearchId)).toGetResponse();
            default:
                break;
        }

        return RestResult.restFailure(new Error("Search for organisation failed", HttpStatus.NOT_FOUND));
    }
}
