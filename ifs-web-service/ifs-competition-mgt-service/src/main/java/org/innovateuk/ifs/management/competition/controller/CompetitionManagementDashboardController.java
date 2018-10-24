package org.innovateuk.ifs.management.competition.controller;

import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.application.resource.ApplicationPageResource;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSearchResultItem;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.service.CompetitionSetupRestService;
import org.innovateuk.ifs.management.dashboard.service.CompetitionDashboardSearchService;
import org.innovateuk.ifs.management.dashboard.viewmodel.*;
import org.innovateuk.ifs.management.navigation.Pagination;
import org.innovateuk.ifs.project.bankdetails.service.BankDetailsRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.util.SecurityRuleUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

@Controller
public class CompetitionManagementDashboardController {
    private static final String TEMPLATE_PATH = "dashboard/";
    private static final String MODEL_ATTR = "model";

    private static final String DEFAULT_PAGE = "0";

    private static final String DEFAULT_PAGE_SIZE = "40";

    private CompetitionDashboardSearchService competitionDashboardSearchService;

    private CompetitionSetupRestService competitionSetupRestService;

    private BankDetailsRestService bankDetailsRestService;

    public CompetitionManagementDashboardController(CompetitionDashboardSearchService competitionDashboardSearchService,
                                                    CompetitionSetupRestService competitionSetupRestService,
                                                    BankDetailsRestService bankDetailsRestService) {
        this.competitionDashboardSearchService = competitionDashboardSearchService;
        this.competitionSetupRestService = competitionSetupRestService;
        this.bankDetailsRestService = bankDetailsRestService;
    }

    @SecuredBySpring(value = "READ", description = "The competition admin, project finance," +
            " support, innovation lead and stakeholder roles are allowed to view the competition management dashboard")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead', 'stakeholder')")
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/dashboard/live";
    }

    @SecuredBySpring(value = "READ", description = "The competition admin, project finance," +
            " support, innovation lead and stakeholder roles are allowed to view the list of live competitions")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead', 'stakeholder')")
    @GetMapping("/dashboard/live")
    public String live(Model model, UserResource user) {
        Map<CompetitionStatus, List<CompetitionSearchResultItem>> liveCompetitions = competitionDashboardSearchService.getLiveCompetitions();
        model.addAttribute(MODEL_ATTR, new LiveDashboardViewModel(
                liveCompetitions,
                competitionDashboardSearchService.getCompetitionCounts(),
                new DashboardTabsViewModel(user)));
        return TEMPLATE_PATH + "live";
    }

    @SecuredBySpring(value = "READ", description = "The competition admin, project finance," +
            " support, innovation lead and stakeholder roles are allowed to view the list of competitions in project setup")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead', 'stakeholder')")
    @GetMapping("/dashboard/project-setup")
    public String projectSetup(Model model, UserResource user) {
        final Map<CompetitionStatus, List<CompetitionSearchResultItem>> projectSetupCompetitions = competitionDashboardSearchService.getProjectSetupCompetitions();

        Long countBankDetails = 0L;
        boolean projectFinanceUser = isProjectFinanceUser(user);
        if (projectFinanceUser) {
            countBankDetails = bankDetailsRestService.countPendingBankDetailsApprovals().getSuccess();
        }

        model.addAttribute(MODEL_ATTR,
                new ProjectSetupDashboardViewModel(
                        projectSetupCompetitions,
                        competitionDashboardSearchService.getCompetitionCounts(),
                        countBankDetails,
                        new DashboardTabsViewModel(user),
                        projectFinanceUser));

        return TEMPLATE_PATH + "projectSetup";
    }

    private boolean isProjectFinanceUser(UserResource user) {
        return SecurityRuleUtil.isProjectFinanceUser(user);
    }

    @SecuredBySpring(value = "READ", description = "The competition admin, project finance, and support roles are allowed to view the list of upcoming competitions")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support')")
    @GetMapping("/dashboard/upcoming")
    public String upcoming(Model model, UserResource user) {
        final Map<CompetitionStatus, List<CompetitionSearchResultItem>> upcomingCompetitions = competitionDashboardSearchService.getUpcomingCompetitions();

        model.addAttribute(MODEL_ATTR, new UpcomingDashboardViewModel(upcomingCompetitions,
                competitionDashboardSearchService.getCompetitionCounts(),
                formatInnovationAreaNames(upcomingCompetitions), new DashboardTabsViewModel(user)));

        return TEMPLATE_PATH + "upcoming";
    }

    @SecuredBySpring(value = "READ", description = "The competition admin, project finance," +
            " support, innovation lead and stakeholder roles are allowed to view the list of previous competitions")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support', 'innovation_lead', 'stakeholder')")
    @GetMapping("/dashboard/previous")
    public String previous(Model model, UserResource user) {
        model.addAttribute(MODEL_ATTR, new PreviousDashboardViewModel(
                competitionDashboardSearchService.getPreviousCompetitions(),
                competitionDashboardSearchService.getCompetitionCounts(),
                new DashboardTabsViewModel(user)));

        return TEMPLATE_PATH + "previous";
    }

    @SecuredBySpring(value = "READ", description = "The competition admin, project finance, and support roles are allowed to view the list of non-IFS competitions")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'support')")
    @GetMapping("/dashboard/non-ifs")
    public String nonIfs(Model model, UserResource user) {
        model.addAttribute(MODEL_ATTR, new NonIFSDashboardViewModel(competitionDashboardSearchService.getNonIfsCompetitions(), competitionDashboardSearchService.getCompetitionCounts(), new DashboardTabsViewModel(user)));
        return TEMPLATE_PATH + "non-ifs";
    }

    @SecuredBySpring(value = "READ", description = "The competition admin, project finance," +
            "innovation lead and stakeholder roles are allowed to view the search page for competitions")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'innovation_lead', 'stakeholder')")
    @GetMapping("/dashboard/search")
    public String search(@RequestParam(name = "searchQuery", defaultValue = "") String searchQuery,
                         @RequestParam(name = "page", defaultValue = "0") int page,
                         Model model,
                         UserResource user) {
        String trimmedSearchQuery = StringUtils.normalizeSpace(searchQuery);
        return searchCompetition(trimmedSearchQuery, page, model, user);
    }

    @SecuredBySpring(value = "READ", description = "The support users are allowed to view the application and competition search pages")
    @PreAuthorize("hasAuthority('support')")
    @GetMapping("/dashboard/support/search")
    public String supportSearch(@RequestParam(name = "searchQuery", defaultValue = "") String searchQuery,
                                @RequestParam(value = "page", defaultValue = DEFAULT_PAGE) int page,
                                @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                Model model,
                                HttpServletRequest request,
                                UserResource user) {
        String trimmedSearchQuery = StringUtils.normalizeSpace(searchQuery);
        boolean isSearchNumeric = trimmedSearchQuery.chars().allMatch(Character::isDigit);

        if (isSearchNumeric) {
            return searchApplication(trimmedSearchQuery, page, pageSize, model, request);
        } else {
            return searchCompetition(trimmedSearchQuery, page, model, user);
        }
    }

    @SecuredBySpring(value = "READ", description = "The competition admin and project finance roles are allowed to view the page for setting up new competitions")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance')")
    @GetMapping("/competition/create")
    public String create() {
        CompetitionResource competition = competitionSetupRestService.create().getSuccess();
        return String.format("redirect:/competition/setup/%s", competition.getId());
    }

    private List<String> formatInnovationAreaNames(Map<CompetitionStatus, List<CompetitionSearchResultItem>> competitionTypes) {

        List<String> formattedList = new ArrayList<>();

        for (Map.Entry<CompetitionStatus, List<CompetitionSearchResultItem>> entry : competitionTypes.entrySet()) {
            for (CompetitionSearchResultItem competition : entry.getValue()) {
                formattedList.add(competition.getInnovationAreaNames().stream().collect(joining(", ")));
            }
        }
        return formattedList;
    }

    private String searchCompetition(String searchQuery, int page, Model model, UserResource user) {
        model.addAttribute("results", competitionDashboardSearchService.searchCompetitions(searchQuery, page));
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("tabs", new DashboardTabsViewModel(user));

        return TEMPLATE_PATH + "search";
    }

    private String searchApplication(String searchQuery, int page, int pageSize, Model model, HttpServletRequest request) {
        String existingSearchQuery = Objects.toString(request.getQueryString(), "");

        ApplicationPageResource matchedApplications = competitionDashboardSearchService.wildcardSearchByApplicationId(searchQuery, page, pageSize);

        ApplicationSearchDashboardViewModel viewModel =
                new ApplicationSearchDashboardViewModel(matchedApplications.getContent(),
                        matchedApplications.getTotalElements(),
                        new Pagination(matchedApplications, "search?" + existingSearchQuery),
                        searchQuery);
        model.addAttribute("model", viewModel);

        return TEMPLATE_PATH + "application-search";
    }
}
