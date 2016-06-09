package com.worth.ifs.project.controller;

import com.worth.ifs.commons.rest.RestResult;
import com.worth.ifs.project.resource.ProjectResource;
import com.worth.ifs.project.resource.ProjectUserResource;
import com.worth.ifs.project.transactional.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * ProjectController exposes Project data and operations through a REST API.
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @RequestMapping("/{id}")
    public RestResult<ProjectResource> getProjectById(@PathVariable("id") final Long id) {
        return projectService.getProjectById(id).toGetResponse();
    }

    @RequestMapping("/")
    public RestResult<List<ProjectResource>> findAll() {
        return projectService.findAll().toGetResponse();
    }

    @RequestMapping(value = "/{projectId}/startdate", method = POST)
    public RestResult<Void> updateProjectStartDate(@PathVariable("projectId") final Long projectId,
                                                   @RequestParam("projectStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate projectStartDate) {
        return projectService.updateProjectStartDate(projectId, projectStartDate).toPostResponse();
    }
    
    @RequestMapping(value = "/{projectId}/finance-contact/{organisation}", method = POST)
    public RestResult<Void> updateFinanceContact(@PathVariable("projectId") final Long projectId,
    		@PathVariable("organisation") final Long organisationId,
                                                   @RequestParam("financeContact") Long financeContactUserId) {
        return projectService.updateFinanceContact(projectId, organisationId, financeContactUserId).toPostResponse();
    }

    @RequestMapping(value = "/{projectId}/project-users", method = GET)
    public RestResult<List<ProjectUserResource>> getProjectUsers(@PathVariable("projectId") final Long projectId) {
        return projectService.getProjectUsers(projectId).toGetResponse();
    }
}
