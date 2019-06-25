package org.innovateuk.ifs.project.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.spendprofile.resource.SpendProfileResource;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectResourceListType;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectUserResourceList;

@Service
public class ProjectRestServiceImpl extends BaseRestService implements ProjectRestService {

    private String projectRestURL = "/project";

    @Override
    public RestResult<ProjectResource> getProjectById(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId, ProjectResource.class);
    }

    @Override
    public RestResult<SpendProfileResource> getSpendProfile(final Long projectId, final Long organisationId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/partner-organisation/" + organisationId + "/spend-profile/", SpendProfileResource.class);
    }

    @Override
    public RestResult<List<ProjectResource>> findByUserId(long userId) {
        return getWithRestResult(projectRestURL + "/user/" + userId, projectResourceListType());
    }

    @Override
    public RestResult<List<ProjectUserResource>> getProjectUsersForProject(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/project-users", projectUserResourceList());
    }

    @Override
    public RestResult<ProjectResource> getByApplicationId(Long applicationId) {
        return getWithRestResult(projectRestURL + "/application/" + applicationId, ProjectResource.class);
    }

    @Override
    public RestResult<OrganisationResource> getOrganisationByProjectAndUser(Long projectId, Long userId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/get-organisation-by-user/" + userId, OrganisationResource.class);
    }

    @Override
    public RestResult<ProjectUserResource> getProjectManager(Long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/project-manager", ProjectUserResource.class);
    }

    @Override
    public RestResult<PartnerOrganisationResource> getPartnerOrganisation(Long projectId, Long organisationId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/partner/" + organisationId, PartnerOrganisationResource.class);
    }

    @Override
    public RestResult<ProjectResource> createProjectFromApplicationId(Long applicationId) {
        return postWithRestResult(projectRestURL + "/create-project/application/" + applicationId, ProjectResource.class);
    }
}
