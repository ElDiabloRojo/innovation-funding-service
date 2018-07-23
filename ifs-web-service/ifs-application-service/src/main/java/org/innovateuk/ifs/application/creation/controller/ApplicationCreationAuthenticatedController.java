package org.innovateuk.ifs.application.creation.controller;

import org.innovateuk.ifs.application.creation.form.ApplicationCreationAuthenticatedForm;
import org.innovateuk.ifs.application.creation.viewmodel.AuthenticatedNotEligibleViewModel;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.commons.exception.ObjectNotFoundException;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.APPLICATION_TEAM;

/**
 * This controller is used when a existing user want to create a new application.
 * Shibboleth makes sure the current visitor of this page is authenticated.
 */
@Controller
@RequestMapping("/application/create-authenticated")
@SecuredBySpring(value = "Controller", description = "TODO", securedType = ApplicationCreationAuthenticatedController.class)
@PreAuthorize("hasAuthority('applicant')")
public class ApplicationCreationAuthenticatedController {
    public static final String COMPETITION_ID = "competitionId";
    public static final String FORM_NAME = "form";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private QuestionRestService questionRestService;

    @Autowired
    protected UserService userService;

    @GetMapping("/{competitionId}")
    public String view(Model model,
                       @PathVariable(COMPETITION_ID) Long competitionId,
                       UserResource user) {

        if (!isAllowedToLeadApplication(user.getId(), competitionId)) {
            return redirectToNotEligible(competitionId);
        }

        Boolean userHasApplication = userService.userHasApplicationForCompetition(user.getId(), competitionId);
        if (Boolean.TRUE.equals(userHasApplication)) {
            model.addAttribute(COMPETITION_ID, competitionId);
            model.addAttribute(FORM_NAME, new ApplicationCreationAuthenticatedForm());
            return "create-application/confirm-new-application";
        } else {
            return createApplicationAndShowInvitees(user, competitionId);
        }
    }

    private String redirectToNotEligible(Long competitionId) {
        return format("redirect:/application/create-authenticated/%s/not-eligible", competitionId);
    }

    @PostMapping("/{competitionId}")
    public String post(@PathVariable(COMPETITION_ID) Long competitionId,
                       @Valid @ModelAttribute(FORM_NAME) ApplicationCreationAuthenticatedForm form,
                       BindingResult bindingResult,
                       ValidationHandler validationHandler,
                       Model model,
                       UserResource user) {

        if (!isAllowedToLeadApplication(user.getId(), competitionId)) {
            return redirectToNotEligible(competitionId);
        }

        Supplier<String> failureView = () -> "create-application/confirm-new-application";
        Supplier<String> successView = () -> {
            if (form.getCreateNewApplication()) {
                return createApplicationAndShowInvitees(user, competitionId);
            }
            // redirect to dashboard
            return "redirect:/";
        };

        return validationHandler.failNowOrSucceedWith(failureView, successView);
    }

    @GetMapping("/{competitionId}/not-eligible")
    public String showNotEligiblePage(Model model,
                                      @PathVariable(COMPETITION_ID) Long competitionId,
                                      UserResource userResource) {
        OrganisationResource organisation = organisationService.getOrganisationForUser(userResource.getId());

        model.addAttribute("model", new AuthenticatedNotEligibleViewModel(organisation.getOrganisationTypeName(), competitionId));
        return "create-application/authenticated-not-eligible";
    }

    private String createApplicationAndShowInvitees(UserResource user, Long competitionId) {
        ApplicationResource application = applicationService.createApplication(competitionId, user.getId(), "");
        if (application != null) {
            return questionRestService
                    .getQuestionByCompetitionIdAndQuestionSetupType(competitionId, APPLICATION_TEAM)
                    .handleSuccessOrFailure(
                            failure -> format("redirect:/application/%s/team", application.getId()),
                            question -> format("redirect:/application/%s/form/question/%s", application.getId(),
                                    question.getId())
                    );
        } else {
            // Application not created, throw exception
            List<Object> args = new ArrayList<>();
            args.add(competitionId);
            args.add(user.getId());
            throw new ObjectNotFoundException("Could not create a new application", args);
        }
    }

    private boolean isAllowedToLeadApplication(Long userId, Long competitionId) {
        OrganisationResource organisation = organisationService.getOrganisationForUser(userId);
        CompetitionResource competition = competitionRestService.getCompetitionById(competitionId).getSuccess();

        return competition.getLeadApplicantTypes().contains(organisation.getOrganisationType());
    }
}
