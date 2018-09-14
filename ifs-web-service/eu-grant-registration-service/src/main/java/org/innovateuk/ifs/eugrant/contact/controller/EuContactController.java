package org.innovateuk.ifs.eugrant.contact.controller;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.eugrant.EuGrantResource;
import org.innovateuk.ifs.eugrant.contact.form.EuContactForm;
import org.innovateuk.ifs.eugrant.overview.service.EuGrantCookieService;
import org.innovateuk.ifs.eugrant.contact.populator.EuContactFormPopulator;
import org.innovateuk.ifs.eugrant.contact.saver.EuContactSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Supplier;

import static org.innovateuk.ifs.commons.rest.RestFailure.error;
import static org.innovateuk.ifs.util.CollectionFunctions.removeDuplicates;

/**
 * This Controller handles the contact details activity
 */
@Controller
@RequestMapping("/")
public class EuContactController {

    private EuGrantCookieService euGrantCookieService;

    private EuContactFormPopulator euContactFormPopulator;

    private EuContactSaver euContactSaver;

    public EuContactController() {
    }

    @Autowired
    public EuContactController(EuGrantCookieService euGrantCookieService,
                               EuContactFormPopulator euContactFormPopulator,
                               EuContactSaver euContactSaver) {
        this.euGrantCookieService = euGrantCookieService;
        this.euContactFormPopulator = euContactFormPopulator;
        this.euContactSaver = euContactSaver;
    }

    @GetMapping("/contact-details")
    public String contactDetails(@ModelAttribute(value = "form", binding = false) EuContactForm form) {

        EuGrantResource grantResource = euGrantCookieService.get();

        if (grantResource.getContact() == null) {
            return "redirect:/contact-details/edit";
        }

        form = euContactFormPopulator.populate(form);

        return "contact/contact-details";
    }

    @GetMapping("/contact-details/edit")
    public String contactDetailsEdit(@ModelAttribute(value = "form", binding = false) EuContactForm form,
                                     BindingResult bindingResult) {

        form = euContactFormPopulator.populate(form);

        return "contact/contact-details-edit";
    }

    @PostMapping("/contact-details/edit")
    public String submitContactDetails(@ModelAttribute("form") @Valid EuContactForm contactForm,
                                       BindingResult bindingResult,
                                       ValidationHandler validationHandler) {

        Supplier<String> failureView = () -> contactDetailsEdit(contactForm, bindingResult);
        Supplier<String> successView = () -> "redirect:/contact-details";

        if (bindingResult.hasErrors()) {
            return failureView.get();
        }

        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            RestResult<Void> sendResult = euContactSaver.save(contactForm);

            return validationHandler.addAnyErrors(error(removeDuplicates(sendResult.getErrors()))).
                    failNowOrSucceedWith(failureView, successView);
        });

    }

}
