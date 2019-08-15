package org.innovateuk.ifs.application.common.populator;

import org.innovateuk.ifs.application.common.viewmodel.ApplicationFinanceSummaryViewModel;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.finance.view.OrganisationApplicationFinanceOverviewImpl;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.form.resource.SectionType;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.user.service.OrganisationService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.innovateuk.ifs.form.resource.SectionType.OVERVIEW_FINANCES;
import static org.innovateuk.ifs.user.resource.Role.*;
import static org.innovateuk.ifs.util.CollectionFunctions.getOnlyElementOrEmpty;

@Component
public class ApplicationFinanceSummaryViewModelPopulator {

    private FinanceService financeService;
    private FileEntryRestService fileEntryRestService;
    private OrganisationRestService organisationRestService;
    private ApplicationService applicationService;
    private SectionService sectionService;
    private UserRestService userRestService;
    private OrganisationService organisationService;
    private CompetitionRestService competitionRestService;

    public ApplicationFinanceSummaryViewModelPopulator(ApplicationService applicationService,
                                                       SectionService sectionService,
                                                       FinanceService financeService,
                                                       FileEntryRestService fileEntryRestService,
                                                       OrganisationRestService organisationRestService,
                                                       UserRestService userRestService,
                                                       OrganisationService organisationService,
                                                       CompetitionRestService competitionRestService) {
        this.applicationService = applicationService;
        this.sectionService = sectionService;
        this.financeService = financeService;
        this.fileEntryRestService = fileEntryRestService;
        this.organisationRestService = organisationRestService;
        this.userRestService = userRestService;
        this.organisationService = organisationService;
        this.competitionRestService = competitionRestService;
    }

    public ApplicationFinanceSummaryViewModel populate(long applicationId, UserResource user) {

        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionRestService.getCompetitionById(application.getCompetition()).getSuccess();

        OrganisationApplicationFinanceOverviewImpl organisationFinanceOverview = new OrganisationApplicationFinanceOverviewImpl(
                financeService,
                fileEntryRestService,
                applicationId
        );

        SectionResource financeSection = sectionService.getFinanceSection(application.getCompetition());
        final boolean hasFinanceSection = financeSection != null;
        final Long financeSectionId = hasFinanceSection ? financeSection.getId() : null;

        Map<Long, Set<Long>> completedSectionsByOrganisation = sectionService.getCompletedSectionsByOrganisation(application.getId());

        final List<OrganisationResource> applicationOrganisations = getApplicationOrganisations(applicationId);
        OrganisationResource leadOrganisation = organisationService.getLeadOrganisation(applicationId, applicationOrganisations);


        Set<Long> sectionsMarkedAsComplete = getCompletedSectionsForUserOrganisation(completedSectionsByOrganisation, leadOrganisation);

        List<SectionResource> eachOrganisationFinanceSections = sectionService.getSectionsForCompetitionByType(application.getCompetition(), SectionType.FINANCE);
        Long eachCollaboratorFinanceSectionId = getEachCollaboratorFinanceSectionId(eachOrganisationFinanceSections);


        boolean yourFinancesCompleteForAllOrganisations = getFinancesOverviewCompleteForAllOrganisations(
                completedSectionsByOrganisation, application.getCompetition());

        return new ApplicationFinanceSummaryViewModel(
                application,
                hasFinanceSection,
                organisationFinanceOverview.getTotalPerType(competition),
                applicationOrganisations,
                sectionsMarkedAsComplete,
                financeSectionId,
                leadOrganisation,
                competition,
                getUserOrganisation(user, applicationId),
                organisationFinanceOverview.getFinancesByOrganisation(),
                organisationFinanceOverview.getTotalFundingSought(),
                organisationFinanceOverview.getTotalOtherFunding(),
                organisationFinanceOverview.getTotalContribution(),
                organisationFinanceOverview.getTotal(),
                completedSectionsByOrganisation,
                eachCollaboratorFinanceSectionId,
                yourFinancesCompleteForAllOrganisations
        );
    }

    private List<OrganisationResource> getApplicationOrganisations(final Long applicationId) {
        return organisationRestService.getOrganisationsByApplicationId(applicationId).getSuccess();
    }

    private Set<Long> getCompletedSectionsForUserOrganisation(Map<Long, Set<Long>> completedSectionsByOrganisation,
                                                              OrganisationResource userOrganisation) {
        return completedSectionsByOrganisation.getOrDefault(
                userOrganisation.getId(),
                new HashSet<>()
        );
    }

    private boolean getFinancesOverviewCompleteForAllOrganisations(Map<Long, Set<Long>> completedSectionsByOrganisation,
                                                                   Long competitionId) {
        Optional<Long> optionalFinanceOverviewSectionId =
                getOnlyElementOrEmpty(sectionService.getSectionsForCompetitionByType(competitionId,
                        OVERVIEW_FINANCES)).map(SectionResource::getId);

        return optionalFinanceOverviewSectionId
                .map(financeOverviewSectionId -> completedSectionsByOrganisation.values()
                        .stream()
                        .allMatch(completedSections -> completedSections.contains(financeOverviewSectionId)))
                .orElse(false);
    }

    private Long getEachCollaboratorFinanceSectionId(List<SectionResource> eachOrganisationFinanceSections) {
        if (!eachOrganisationFinanceSections.isEmpty()) {
            return eachOrganisationFinanceSections.get(0).getId();
        }

        return null;
    }

    private OrganisationResource getUserOrganisation(UserResource user, Long applicationId) {
        OrganisationResource userOrganisation = null;

        if (!user.isInternalUser() && !user.hasAnyRoles(ASSESSOR, INTERVIEW_ASSESSOR, STAKEHOLDER, MONITORING_OFFICER)) {
            Optional<ProcessRoleResource> processRoleResource = userRestService.findProcessRole(user.getId(), applicationId).toOptionalIfNotFound().getSuccess();
            if (processRoleResource.isPresent()) {
                userOrganisation = organisationRestService.getOrganisationById(processRoleResource.get().getOrganisationId()).getSuccess();
            }
        }

        return userOrganisation;
    }
}