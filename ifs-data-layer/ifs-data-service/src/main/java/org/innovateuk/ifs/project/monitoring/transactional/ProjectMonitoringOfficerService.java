package org.innovateuk.ifs.project.monitoring.transactional;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.project.monitoring.resource.ProjectMonitoringOfficerResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ProjectMonitoringOfficerService {

    @SecuredBySpring(value = "GET_MONITORING_OFFICERS",
            description = "Only comp admin, project finance and ifs administrators can get a project list of monitoring officers")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'ifs_administrator')")
    ServiceResult<List<ProjectMonitoringOfficerResource>> findAll();

    @SecuredBySpring(value = "GET_MONITORING_OFFICER",
            description = "Only comp admin, project finance and ifs administrators can get a project monitoring officer")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'ifs_administrator')")
    ServiceResult<ProjectMonitoringOfficerResource> getProjectMonitoringOfficer(long userId);

    @SecuredBySpring(value = "ASSIGN_MONITORING_OFFICER",
            description = "Only comp admin, project finance and ifs administrators can assign projects to a monitoring officer")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'ifs_administrator')")
    ServiceResult<Void> assignProjectToMonitoringOfficer(long userId, long projectId);

    @SecuredBySpring(value = "UNASSIGN_MONITORING_OFFICER",
            description = "Only comp admin, project finance and ifs administrators can unassign projects from a monitoring officer")
    @PreAuthorize("hasAnyAuthority('comp_admin', 'project_finance', 'ifs_administrator')")
    ServiceResult<Void> unassignProjectFromMonitoringOfficer(long userId, long projectId);
}