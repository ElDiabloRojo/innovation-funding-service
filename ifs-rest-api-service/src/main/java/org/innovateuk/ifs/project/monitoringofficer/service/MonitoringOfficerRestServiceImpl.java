package org.innovateuk.ifs.project.monitoringofficer.service;

import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.invite.resource.CreateMonitoringOfficerResource;
import org.innovateuk.ifs.project.monitoringofficer.resource.MonitoringOfficerResource;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
public class MonitoringOfficerRestServiceImpl extends BaseRestService implements MonitoringOfficerRestService {

    private String projectRestURL = "/project";

    @Override
    public RestResult<Void> updateMonitoringOfficer(long projectId, String firstName, String lastName, String emailAddress, String phoneNumber) {
        MonitoringOfficerResource monitoringOfficerData = new MonitoringOfficerResource(firstName, lastName, emailAddress, phoneNumber, projectId);
        return putWithRestResult(projectRestURL + "/" + projectId + "/monitoring-officer", monitoringOfficerData, Void.class);
    }


    @Override
    public RestResult<MonitoringOfficerResource> getMonitoringOfficerForProject(long projectId) {
        return getWithRestResult(projectRestURL + "/" + projectId + "/monitoring-officer", MonitoringOfficerResource.class);
    }
}