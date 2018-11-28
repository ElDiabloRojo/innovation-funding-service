package org.innovateuk.ifs.application.forms.saver;

import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationRestService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.form.resource.SectionType;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.isMarkSectionAsCompleteRequest;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.isMarkSectionAsIncompleteRequest;

/**
 * This Saver will handle save all finance sections that are related to the application.
 */
@Service
public class ApplicationSectionFinanceSaver extends AbstractApplicationSaver {

    private SectionService sectionService;

    private OrganisationRestService organisationRestService;

    private ApplicationRestService applicationRestService;

    private CompetitionRestService competitionRestService;

    public ApplicationSectionFinanceSaver(SectionService sectionService, OrganisationRestService organisationRestService, ApplicationRestService applicationRestService, CompetitionRestService competitionRestService) {
        this.sectionService = sectionService;
        this.organisationRestService = organisationRestService;
        this.applicationRestService = applicationRestService;
        this.competitionRestService = competitionRestService;
    }

    public void handleMarkAcademicFinancesAsNotRequired(long organisationType, SectionResource selectedSection, long applicationId, long competitionId, long processRoleId) {
        handleMarkAcademicFinancesAsNotRequired(organisationType, selectedSection.getType(), applicationId, competitionId, processRoleId);
    }

    public void handleMarkProjectCostsAsComplete(ProcessRoleResource processRole) {
        ApplicationResource application =  applicationRestService.getApplicationById(processRole.getApplicationId()).getSuccess();
        OrganisationResource organisation = organisationRestService.getOrganisationById(processRole.getOrganisationId()).getSuccess();
        handleMarkAcademicFinancesAsNotRequired(organisation.getOrganisationType(), SectionType.PROJECT_COST_FINANCES, processRole.getApplicationId(), application.getCompetition(), processRole.getId());
    }

    private void handleMarkAcademicFinancesAsNotRequired(long organisationType, SectionType sectionType, long applicationId, long competitionId, long processRoleId) {
        if (SectionType.PROJECT_COST_FINANCES.equals(sectionType)
                && OrganisationTypeEnum.RESEARCH.getId() == organisationType
                && !researchUserSeesOrganisationSection(competitionId)) {
            SectionResource organisationSection = sectionService.getSectionsForCompetitionByType(competitionId, SectionType.ORGANISATION_FINANCES).get(0);
            sectionService.markAsNotRequired(organisationSection.getId(), applicationId, processRoleId);
        }
    }

    private boolean researchUserSeesOrganisationSection(long competitionId) {
        return Boolean.TRUE.equals(competitionRestService.getCompetitionById(competitionId)
                .getSuccess()
                .getIncludeYourOrganisationSection());
    }

    public void handleStateAid(Map<String, String[]> params, ApplicationResource application, ApplicationForm form, SectionResource selectedSection) {
        if (isMarkSectionAsCompleteRequest(params)) {
            application.setStateAidAgreed(form.isStateAidAgreed());
        } else if (isMarkSectionAsIncompleteRequest(params) && selectedSection.getType() == SectionType.FINANCE) {
            application.setStateAidAgreed(Boolean.FALSE);
        }
    }
}