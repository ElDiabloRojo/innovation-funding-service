package org.innovateuk.ifs.project.projectdetails.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.projectdetails.form.ProjectDurationForm;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectDetailsViewModel;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.service.PartnerOrganisationRestService;
import org.innovateuk.ifs.project.service.ProjectRestService;
import org.innovateuk.ifs.projectdetails.ProjectDetailsService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.util.NavigationUtils;
import org.innovateuk.ifs.util.PrioritySorting;
import org.innovateuk.ifs.util.SecurityRuleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_PROJECT_DURATION_MUST_BE_MINIMUM_ONE_MONTH;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.toField;
import static org.innovateuk.ifs.user.resource.Role.PARTNER;
import static org.innovateuk.ifs.user.resource.Role.PROJECT_MANAGER;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFindFirst;

/**
 * This controller will handle all requests that are related to project details.
 */
@Controller
@RequestMapping("/competition/{competitionId}/project")
public class ProjectDetailsController {

    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private ProjectDetailsService projectDetailsService;

    @Autowired
    private OrganisationRestService organisationRestService;

    @Autowired
    private ProjectRestService projectRestService;

    @Autowired
    private PartnerOrganisationRestService partnerOrganisationService;

    @Autowired
    private ApplicationRestService applicationRestService;

    @Autowired
    private NavigationUtils navigationUtils;

    private static final Log LOG = LogFactory.getLog(ProjectDetailsController.class);

    @PreAuthorize("hasAnyAuthority('project_finance', 'comp_admin', 'support', 'innovation_lead', 'stakeholder')")
    @SecuredBySpring(value = "VIEW_PROJECT_DETAILS", description = "Project finance, comp admin, support, innovation lead and stakeholders can view the project details")
    @GetMapping("/{projectId}/details")
    public String viewProjectDetails(@PathVariable("competitionId") final Long competitionId,
                                     @PathVariable("projectId") final Long projectId, Model model,
                                     UserResource loggedInUser) {

        ProjectResource projectResource = projectService.getById(projectId);
        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectResource.getId());
        OrganisationResource leadOrganisationResource = projectService.getLeadOrganisation(projectId);

        List<OrganisationResource> organisations = sortedOrganisations(getPartnerOrganisations(projectUsers), leadOrganisationResource);

        CompetitionResource competitionResource = competitionRestService.getCompetitionById(competitionId).getSuccess();

        boolean locationPerPartnerRequired = competitionResource.isLocationPerPartner();
        boolean isIfsAdministrator = SecurityRuleUtil.isIFSAdmin(loggedInUser);

        model.addAttribute("model", new ProjectDetailsViewModel(projectResource,
                competitionId,
                competitionResource.getName(),
                isIfsAdministrator,
                leadOrganisationResource.getName(),
                getProjectManager(projectUsers).orElse(null),
                getFinanceContactForPartnerOrganisation(projectUsers, organisations),
                locationPerPartnerRequired,
                locationPerPartnerRequired?
                        partnerOrganisationService.getProjectPartnerOrganisations(projectId).getSuccess()
                        : Collections.emptyList()));

        return "project/detail";
    }

    @PreAuthorize("hasAuthority('ifs_administrator')")
    @SecuredBySpring(value = "WITHDRAW_PROJECT", description = "Only the IFS administrator users are able to withdraw projects")
    @PostMapping("/{projectId}/withdraw")
    public String withdrawProject(@PathVariable("competitionId") final long competitionId,
                                  @PathVariable("projectId") final long projectId,
                                  HttpServletRequest request) {

        projectRestService.withdrawProject(projectId).getSuccess();

        return String.format("redirect:/competition/%d/status/all", competitionId);
    }

    @PreAuthorize("hasAuthority('ifs_administrator')")
    @SecuredBySpring(value = "HANDLE_PROJECT_OFFLINE", description = "Only the IFS administrator users are able to handle projects offline")
    @PostMapping("/{projectId}/handle-offline")
    public String handleProjectOffline(@PathVariable("competitionId") final long competitionId,
                                  @PathVariable("projectId") final long projectId,
                                  HttpServletRequest request) {

        projectRestService.handleProjectOffline(projectId).getSuccess();
        return String.format("redirect:/competition/%d/project/%d/details", competitionId, projectId);
    }

    @PreAuthorize("hasAuthority('ifs_administrator')")
    @SecuredBySpring(value = "COMPLETE_PROJECT_OFFLINE", description = "Only the IFS administrator users are able to complete projects offline")
    @PostMapping("/{projectId}/complete-offline")
    public String completeProjectOffline(@PathVariable("competitionId") final long competitionId,
                                       @PathVariable("projectId") final long projectId,
                                       HttpServletRequest request) {

        projectRestService.completeProjectOffline(projectId).getSuccess();
        return String.format("redirect:/competition/%d/project/%d/details", competitionId, projectId);
    }

    private List<OrganisationResource> getPartnerOrganisations(final List<ProjectUserResource> projectRoles) {
        return  projectRoles.stream()
                .filter(uar -> uar.getRole() == PARTNER.getId())
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisation()).getSuccess())
                .collect(Collectors.toList());
    }

    private List<OrganisationResource> sortedOrganisations(List<OrganisationResource> organisations,
                                                           OrganisationResource lead)
    {
        return new PrioritySorting<>(organisations, lead, OrganisationResource::getName).unwrap();
    }

    private Optional<ProjectUserResource> getProjectManager(List<ProjectUserResource> projectUsers) {
        return simpleFindFirst(projectUsers, pu -> PROJECT_MANAGER.getId() == pu.getRole());
    }

    private Map<OrganisationResource, ProjectUserResource> getFinanceContactForPartnerOrganisation(List<ProjectUserResource> projectUsers, List<OrganisationResource> partnerOrganisations) {
        List<ProjectUserResource> financeRoles = simpleFilter(projectUsers, ProjectUserResource::isFinanceContact);

        Map<OrganisationResource, ProjectUserResource> organisationFinanceContactMap = new LinkedHashMap<>();

        partnerOrganisations.stream().forEach(organisation ->
                organisationFinanceContactMap.put(organisation,
                        simpleFindFirst(financeRoles, financeUserResource -> financeUserResource.getOrganisation().equals(organisation.getId())).orElse(null))
        );

        return organisationFinanceContactMap;
    }

    @PreAuthorize("hasAuthority('project_finance')")
    @SecuredBySpring(value = "VIEW_EDIT_PROJECT_DURATION", description = "Only the project finance can view the page to edit the project duration")
    @GetMapping("/{projectId}/duration")
    public String viewEditProjectDuration(@PathVariable("competitionId") final long competitionId,
                                          @PathVariable("projectId") final long projectId, Model model,
                                          UserResource loggedInUser) {


        ProjectDurationForm form = new ProjectDurationForm();
        return doViewEditProjectDuration(competitionId, projectId, model, form);
    }

    private String doViewEditProjectDuration(long competitionId, long projectId, Model model, ProjectDurationForm form) {

        ProjectResource project = projectService.getById(projectId);
        CompetitionResource competition = competitionRestService.getCompetitionById(competitionId).getSuccess();

        model.addAttribute("model", new ProjectDetailsViewModel(project,
                competitionId,
                competition.getName(),
                false,
                null,
                null,
                null,
                false,
                Collections.emptyList()));
        model.addAttribute(FORM_ATTR_NAME, form);

        return "project/edit-duration";

    }

    @PreAuthorize("hasAuthority('project_finance')")
    @SecuredBySpring(value = "UPDATE_PROJECT_DURATION", description = "Only the project finance can update the project duration")
    @PostMapping("/{projectId}/duration")
    public String updateProjectDuration(@PathVariable("competitionId") final long competitionId,
                                        @PathVariable("projectId") final long projectId,
                                        @Valid @ModelAttribute(FORM_ATTR_NAME) ProjectDurationForm form,
                                        @SuppressWarnings("unused") BindingResult bindingResult,
                                        ValidationHandler validationHandler,
                                        Model model,
                                        UserResource loggedInUser) {

        Supplier<String> failureView = () -> doViewEditProjectDuration(competitionId, projectId, model, form);

        Supplier<String> successView = () -> "redirect:/project/" + projectId + "/finance-check";

        validateDuration(form.getDurationInMonths(), validationHandler);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            ServiceResult<Void> updateResult = projectDetailsService.updateProjectDuration(projectId, Long.parseLong(form.getDurationInMonths()));

            return validationHandler.addAnyErrors(updateResult, toField("durationInMonths")).failNowOrSucceedWith(failureView, successView);
        });
    }

    private void validateDuration(String durationInMonths, ValidationHandler validationHandler) {

        if (StringUtils.isBlank(durationInMonths)) {
            validationHandler.addAnyErrors(serviceFailure(new Error("validation.field.must.not.be.blank", HttpStatus.BAD_REQUEST)), toField("durationInMonths"));
            return;
        }

        if (!StringUtils.isNumeric(durationInMonths)) {
            validationHandler.addAnyErrors(serviceFailure(new Error("validation.standard.integer.non.decimal.format", HttpStatus.BAD_REQUEST)), toField("durationInMonths"));
            return;
        }

        if (Long.parseLong(durationInMonths) < 1) {
            validationHandler.addAnyErrors(serviceFailure(PROJECT_SETUP_PROJECT_DURATION_MUST_BE_MINIMUM_ONE_MONTH), toField("durationInMonths"));
        }
    }
}
