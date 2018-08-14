package org.innovateuk.ifs.application.team.controller;

import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.application.team.populator.ApplicationTeamModelPopulator;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.MODEL_ATTRIBUTE_FORM;

/**
 * This controller will handle all requests that are related to the read only view of the application team.
 */
@Controller
@RequestMapping("/application/{applicationId}")
@SecuredBySpring(value="Controller", description = "TODO", securedType = ApplicationTeamController.class)
@PreAuthorize("hasAuthority('applicant')")
public class ApplicationTeamController {

    @Autowired
    private ApplicationTeamModelPopulator applicationTeamModelPopulator;

    @GetMapping("/team")
    @PreAuthorize("hasPermission(#applicationId,'org.innovateuk.ifs.application.resource.ApplicationCompositeId' ,'VIEW_APPLICATION_TEAM_PAGE')")
    public String getApplicationTeam(Model model,
                                     @ModelAttribute(name = MODEL_ATTRIBUTE_FORM, binding = false) ApplicationForm form,
                                     @P("applicationId")@PathVariable("applicationId") long applicationId,
                                     UserResource loggedInUser) {
        model.addAttribute("model", applicationTeamModelPopulator.populateModel(applicationId, loggedInUser.getId(),
                null));
        return "application-team/team";
    }
}