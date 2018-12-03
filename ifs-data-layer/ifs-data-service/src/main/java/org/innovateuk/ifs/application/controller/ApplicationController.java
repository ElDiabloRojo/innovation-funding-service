package org.innovateuk.ifs.application.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.innovateuk.ifs.application.mapper.IneligibleOutcomeMapper;
import org.innovateuk.ifs.application.resource.*;
import org.innovateuk.ifs.application.transactional.ApplicationNotificationService;
import org.innovateuk.ifs.application.transactional.ApplicationProgressService;
import org.innovateuk.ifs.application.transactional.ApplicationService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.crm.transactional.CrmService;
import org.innovateuk.ifs.user.resource.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.innovateuk.ifs.commons.ZeroDowntime;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * ApplicationController exposes Application data and operations through a REST API.
 */
@RestController
@RequestMapping("/application")
public class ApplicationController {

    private static final String DEFAULT_PAGE_NUMBER = "0";

    private static final String DEFAULT_PAGE_SIZE = "40";

    private static final String DEFAULT_SORT_BY = "id";

    private static final String PREVIOUS_APP_DEFAULT_FILTER = "ALL";

    private IneligibleOutcomeMapper ineligibleOutcomeMapper;

    private ApplicationService applicationService;

    private ApplicationNotificationService applicationNotificationService;

    private ApplicationProgressService applicationProgressService;

    private CrmService crmService;

    public ApplicationController() {}

    @Autowired
    public ApplicationController(IneligibleOutcomeMapper ineligibleOutcomeMapper, ApplicationService applicationService, ApplicationNotificationService applicationNotificationService, ApplicationProgressService applicationProgressService, CrmService crmService) {
        this.ineligibleOutcomeMapper = ineligibleOutcomeMapper;
        this.applicationService = applicationService;
        this.applicationNotificationService = applicationNotificationService;
        this.applicationProgressService = applicationProgressService;
        this.crmService = crmService;
    }

    @GetMapping("/{id}")
    public RestResult<ApplicationResource> getApplicationById(@PathVariable("id") final Long id) {
        return applicationService.getApplicationById(id).toGetResponse();
    }

    @GetMapping("/")
    public RestResult<List<ApplicationResource>> findAll() {
        return applicationService.findAll().toGetResponse();
    }

    @GetMapping("/findByUser/{userId}")
    public RestResult<List<ApplicationResource>> findByUserId(@PathVariable("userId") final Long userId) {
        return applicationService.findByUserId(userId).toGetResponse();
    }

    @GetMapping("/wildcardSearchById")
    public RestResult<ApplicationPageResource> wildcardSearchById(@RequestParam(value = "searchString", defaultValue = "") String searchString,
                                                                  @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NUMBER) int pageIndex,
                                                                  @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize) {
        return applicationService.wildcardSearchById(searchString, new PageRequest(pageIndex, pageSize)).toGetResponse();
    }

    @PostMapping("/saveApplicationDetails/{id}")
    public RestResult<Void> saveApplicationDetails(@PathVariable("id") final Long id,
                                                   @RequestBody ApplicationResource application) {

        return applicationService.saveApplicationDetails(id, application).toPostResponse();
    }

    @GetMapping("/getProgressPercentageByApplicationId/{applicationId}")
    public RestResult<CompletedPercentageResource> getProgressPercentageByApplicationId(@PathVariable("applicationId") final Long applicationId) {
        return applicationService.getProgressPercentageByApplicationId(applicationId).toGetResponse();
    }

    @PutMapping("/updateApplicationState")
    public RestResult<Void> updateApplicationState(@RequestParam("applicationId") final Long id,
                                                   @RequestParam("state") final ApplicationState state) {

        ServiceResult<ApplicationResource> updateStatusResult = applicationService.updateApplicationState(id, state);

        if (updateStatusResult.isSuccess() && ApplicationState.SUBMITTED == state) {
            applicationService.saveApplicationSubmitDateTime(id, ZonedDateTime.now());
            applicationNotificationService.sendNotificationApplicationSubmitted(id);
        }

        return updateStatusResult.toPutResponse();
    }

    @GetMapping("/applicationReadyForSubmit/{applicationId}")
    public RestResult<Boolean> applicationReadyForSubmit(@PathVariable("applicationId") final Long applicationId) {
        return RestResult.toGetResponse(applicationProgressService.applicationReadyForSubmit(applicationId));
    }

    @GetMapping("/getApplicationsByCompetitionIdAndUserId/{competitionId}/{userId}/{role}")
    public RestResult<List<ApplicationResource>> getApplicationsByCompetitionIdAndUserId(@PathVariable("competitionId") final Long competitionId,
                                                                                         @PathVariable("userId") final Long userId,
                                                                                         @PathVariable("role") final Role role) {

        return applicationService.getApplicationsByCompetitionIdAndUserId(competitionId, userId, role).toGetResponse();
    }

    @PostMapping("/createApplicationByName/{competitionId}/{userId}/{organisationId}")
    public RestResult<ApplicationResource> createApplicationByApplicationNameForUserIdAndCompetitionId(
            @PathVariable("competitionId") final long competitionId,
            @PathVariable("userId") final long userId,
            @PathVariable("organisationId") final long organisationId,
            @RequestBody JsonNode jsonObj) {

        String name = jsonObj.get("name").textValue();
        return applicationService.createApplicationByApplicationNameForUserIdAndCompetitionId(name, competitionId, userId, organisationId)
                .andOnSuccessReturn(result -> {
                    crmService.syncCrmContact(userId);
                    return result;
                })
                .toPostCreateResponse();
    }

    @PostMapping("/{applicationId}/ineligible")
    public RestResult<Void> markAsIneligible(@PathVariable("applicationId") long applicationId,
                                             @RequestBody IneligibleOutcomeResource reason) {
        return applicationService
                .markAsIneligible(applicationId, ineligibleOutcomeMapper.mapToDomain(reason))
                .toPostWithBodyResponse();
    }

    @PostMapping("/informIneligible/{applicationId}")
    public RestResult<Void> informIneligible(@PathVariable("applicationId") final long applicationId,
                                             @RequestBody ApplicationIneligibleSendResource applicationIneligibleSendResource) {
        return applicationNotificationService.informIneligible(applicationId, applicationIneligibleSendResource).toPostResponse();
    }

    @PostMapping("/{applicationId}/withdraw")
    public RestResult<Void> withdrawApplication(@PathVariable("applicationId") final long applicationId) {
        return applicationService.withdrawApplication(applicationId).toPostResponse();
    }

    // IFS-43 added to ease future expansion as application team members are expected to have access to the application team page, but the location of links to that page (enabled by tis method) is as yet unknown
    @GetMapping("/showApplicationTeam/{applicationId}/{userId}")
    public RestResult<Boolean> showApplicationTeam(@PathVariable("applicationId") final Long applicationId,
                                                   @PathVariable("userId") final Long userId) {
        return applicationService.showApplicationTeam(applicationId, userId).toGetResponse();
    }

    @ZeroDowntime(description = "delete this controller", reference = "IFS-2471")
    @GetMapping("/{competitionId}/unsuccessful-applications")
    public RestResult<PreviousApplicationPageResource> findUnsuccessfulApplications(@PathVariable("competitionId") final Long competitionId,
                                                                                    @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NUMBER) int pageIndex,
                                                                                    @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                                                                    @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_BY) String sortField,
                                                                                    @RequestParam(value = "filter", defaultValue = PREVIOUS_APP_DEFAULT_FILTER) String filter) {
        return applicationService.findPreviousApplications(competitionId, pageIndex, pageSize, sortField, filter).toGetResponse();
    }

    @GetMapping("/{competitionId}/previous-applications")
    public RestResult<PreviousApplicationPageResource> findPreviousApplications(@PathVariable("competitionId") final Long competitionId,
                                                                                @RequestParam(value = "page", defaultValue = DEFAULT_PAGE_NUMBER) int pageIndex,
                                                                                @RequestParam(value = "size", defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
                                                                                @RequestParam(value = "sort", defaultValue = DEFAULT_SORT_BY) String sortField,
                                                                                @RequestParam(value = "filter", defaultValue = PREVIOUS_APP_DEFAULT_FILTER) String filter) {
        return applicationService.findPreviousApplications(competitionId, pageIndex, pageSize, sortField, filter).toGetResponse();
    }

    @GetMapping("/getLatestEmailFundingDate/{competitionId}")
    public RestResult<ZonedDateTime> getLatestEmailFundingDate(@PathVariable("competitionId") final Long competitionId) {
        return applicationService.findLatestEmailFundingDateByCompetitionId(competitionId).toGetResponse();
    }

    @GetMapping("/{applicationId}/competition")
    public RestResult<CompetitionResource> getCompetitionByApplicationId(@PathVariable("applicationId") long applicationId) {
        return applicationService.getCompetitionByApplicationId(applicationId).toGetResponse();
    }
}
