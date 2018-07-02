package org.innovateuk.ifs.registration.controller;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.AddressTypeResource;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.organisation.resource.OrganisationAddressResource;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.resource.OrganisationSearchResult;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.registration.form.OrganisationCreationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.innovateuk.ifs.address.resource.OrganisationAddressType.OPERATING;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.REGISTERED;

/**
 * Provides methods for confirming and saving the organisation as an intermediate step in the registration flow.
 */
@Controller
@RequestMapping(AbstractOrganisationCreationController.BASE_URL)
@SecuredBySpring(value = "Controller",
        description = "Any user can confirm and save their organisation as part of registering their account",
        securedType = OrganisationCreationSaveController.class)
@PreAuthorize("permitAll")
public class OrganisationCreationSaveController extends AbstractOrganisationCreationController {

    @Autowired
    private OrganisationService organisationService;

    @GetMapping("/" + CONFIRM_ORGANISATION)
    public String confirmOrganisation(@ModelAttribute(name = ORGANISATION_FORM, binding = false) OrganisationCreationForm organisationForm,
                                 Model model,
                                 HttpServletRequest request) {
        organisationForm = getFormDataFromCookie(organisationForm, model, request);
        addOrganisationType(organisationForm, organisationTypeIdFromCookie(request));
        addSelectedOrganisation(organisationForm, model);
        model.addAttribute(ORGANISATION_FORM, organisationForm);
        model.addAttribute("organisationType", organisationTypeRestService.findOne(organisationForm.getOrganisationTypeId()).getSuccess());

        return TEMPLATE_PATH + "/" + CONFIRM_ORGANISATION;
    }

    @PostMapping("/save-organisation")
    public String saveOrganisation(@ModelAttribute(name = ORGANISATION_FORM, binding = false) OrganisationCreationForm organisationForm,
                                   Model model,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        organisationForm = getFormDataFromCookie(organisationForm, model, request);

        BindingResult bindingResult = new BeanPropertyBindingResult(organisationForm, ORGANISATION_FORM);
        validator.validate(organisationForm, bindingResult);

        //Ignore not null errors on organisationSearchName as its not relevant here. This is due to the same form being used.
        if (bindingResult.hasErrors() && (bindingResult.getAllErrors().size() != 1 || !bindingResult.hasFieldErrors("organisationSearchName"))) {
            return "redirect:/";
        }

        OrganisationSearchResult selectedOrganisation = addSelectedOrganisation(organisationForm, model);
        AddressResource address = organisationForm.getAddressForm().getSelectedPostcode();

        List<OrganisationAddressResource> organisationAddressResources = new ArrayList<>();


        if (address != null && !organisationForm.isUseSearchResultAddress()) {
            organisationAddressResources.add(
                    new OrganisationAddressResource(address,
                                                    new AddressTypeResource(OPERATING.getOrdinal(), OPERATING.name())));
        }
        if (selectedOrganisation != null && selectedOrganisation.getOrganisationAddress() != null) {
            organisationAddressResources.add(
                    new OrganisationAddressResource(selectedOrganisation.getOrganisationAddress(),
                                                    new AddressTypeResource(REGISTERED.getOrdinal(), REGISTERED.name())));
        }

        OrganisationResource organisationResource = new OrganisationResource();
        organisationResource.setName(organisationForm.getOrganisationName());
        organisationResource.setOrganisationType(organisationForm.getOrganisationTypeId());
        organisationResource.setAddresses(organisationAddressResources);

        if (!OrganisationTypeEnum.RESEARCH.getId().equals(organisationForm.getOrganisationTypeId())) {
            organisationResource.setCompanyHouseNumber(organisationForm.getSearchOrganisationId());
        }

        organisationResource = createOrRetrieveOrganisation(organisationResource, request);
        registrationCookieService.saveToOrganisationIdCookie(organisationResource.getId(), response);
        return "redirect:" + RegistrationController.BASE_URL;
    }

    private OrganisationResource createOrRetrieveOrganisation(OrganisationResource organisationResource, HttpServletRequest request) {
        Optional<String> cookieHash = registrationCookieService.getInviteHashCookieValue(request);
        if(cookieHash.isPresent()) {
            return organisationService.createAndLinkByInvite(organisationResource, cookieHash.get());
        }

        return organisationService.createOrMatch(organisationResource);
    }
}
