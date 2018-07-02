package org.innovateuk.ifs.assessment.assignment.populator;

import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.assessment.assignment.viewmodel.AssessmentAssignmentViewModel;
import org.innovateuk.ifs.assessment.common.service.AssessmentService;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.user.viewmodel.UserApplicationRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionType.PROJECT_SUMMARY;

/**
 * Build the model for the Assessment Assignment view.
 */
@Component
public class AssessmentAssignmentModelPopulator {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private FormInputResponseRestService formInputResponseRestService;

    @Autowired
    private OrganisationRestService organisationRestService;

    public AssessmentAssignmentViewModel populateModel(Long assessmentId) {
        AssessmentResource assessment = assessmentService.getAssignableById(assessmentId);
        String projectSummary = getProjectSummary(assessment);
        List<ProcessRoleResource> processRoles = processRoleService.findProcessRolesByApplicationId(assessment.getApplication());
        SortedSet<OrganisationResource> collaborators = getApplicationOrganisations(processRoles);
        OrganisationResource leadPartner = getApplicationLeadOrganisation(processRoles).orElse(null);
        return new AssessmentAssignmentViewModel(assessmentId, assessment.getCompetition(), assessment.getApplicationName(),
                collaborators, leadPartner, projectSummary);
    }

    private Optional<OrganisationResource> getApplicationLeadOrganisation(List<ProcessRoleResource> userApplicationRoles) {
        return userApplicationRoles.stream()
                .filter(uar -> uar.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName()))
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisationId()).getSuccess())
                .findFirst();
    }

    private SortedSet<OrganisationResource> getApplicationOrganisations(List<ProcessRoleResource> userApplicationRoles) {
        Comparator<OrganisationResource> compareById =
                Comparator.comparingLong(OrganisationResource::getId);
        Supplier<SortedSet<OrganisationResource>> supplier = () -> new TreeSet<>(compareById);

        return userApplicationRoles.stream()
                .filter(uar -> uar.getRoleName().equals(UserApplicationRole.LEAD_APPLICANT.getRoleName())
                        || uar.getRoleName().equals(UserApplicationRole.COLLABORATOR.getRoleName()))
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisationId()).getSuccess())
                .collect(Collectors.toCollection(supplier));
    }

    private String getProjectSummary(AssessmentResource assessmentResource) {
        Optional<FormInputResponseResource> formInputResponseResource = formInputResponseRestService.getByApplicationIdAndQuestionSetupType(
                assessmentResource.getApplication(), PROJECT_SUMMARY).getOptionalSuccessObject();

        if(formInputResponseResource.isPresent()) {
            return formInputResponseResource.get().getValue();
        }
        return null;
    }
}
