package org.innovateuk.ifs.project.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.spendprofile.resource.SpendProfileResource;

import java.util.List;

public interface ProjectRestService {
    RestResult<ProjectResource> getProjectById(Long projectId);

    RestResult<SpendProfileResource> getSpendProfile(Long projectId, Long organisationId);

    RestResult<List<ProjectResource>> findByUserId(long userId);

    RestResult<List<ProjectUserResource>> getProjectUsersForProject(Long projectId);

    RestResult<ProjectResource> getByApplicationId(Long applicationId);

    RestResult<Void> withdrawProject(long projectId);

    RestResult<Void> removeUser(long projectId, long userId);

    RestResult<OrganisationResource> getOrganisationByProjectAndUser(Long projectId, Long userId);

    RestResult<ProjectUserResource> getProjectManager(Long projectId);

    RestResult<PartnerOrganisationResource> getPartnerOrganisation(Long projectId, Long organisationId);

    RestResult<ProjectResource> createProjectFromApplicationId(Long applicationId);

    RestResult<Void> handleProjectOffline(long projectId);

    RestResult<Void> completeProjectOffline(long projectId);
}