package org.innovateuk.ifs.interview.controller;

import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.interview.resource.*;
import org.innovateuk.ifs.interview.transactional.InterviewAllocationService;
import org.innovateuk.ifs.invite.resource.AssessorInvitesToSendResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for allocating applications to assessors in Interview Panels.
 */
@RestController
@RequestMapping("/interview-panel")
public class InterviewAllocationController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    @Autowired
    private InterviewAllocationService interviewAllocationService;

    @GetMapping("/allocate-assessors/{competitionId}")
    public RestResult<InterviewAcceptedAssessorsPageResource> getInterviewAcceptedAssessors(
            @PathVariable long competitionId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "invite.name", direction = Sort.Direction.ASC) Pageable pageable) {
        return interviewAllocationService.getInterviewAcceptedAssessors(competitionId, pageable).toGetResponse();
    }

    @GetMapping("/{competitionId}/allocated-applications/{assessorId}")
    public RestResult<InterviewApplicationPageResource> getAllocatedApplications(
            @PathVariable long competitionId,
            @PathVariable long assessorId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "target.id", direction = Sort.Direction.ASC) Pageable pageable) {
        return interviewAllocationService.getAllocatedApplications(competitionId, assessorId, pageable).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/{competitionId}/allocated-applications-assessorId/{assessorId}", "/{competitionId}/allocated-applications-assessor-id/{assessorId}"})
    public RestResult<List<InterviewResource>> getAllocatedApplicationsByAssessorId(
            @PathVariable long competitionId,
            @PathVariable long assessorId
    ) {
        return interviewAllocationService.getAllocatedApplicationsByAssessorId(competitionId, assessorId).toGetResponse();
    }

    @GetMapping("/{competitionId}/unallocated-applications/all/{applicationIds}")
    public RestResult<List<InterviewApplicationResource>> getAllocatedApplications(@PathVariable List<Long> applicationIds) {
        return interviewAllocationService.getUnallocatedApplicationsById(applicationIds).toGetResponse();
    }

    @GetMapping("/{competitionId}/allocated-applications/{assessorId}/invite-to-send")
    public RestResult<AssessorInvitesToSendResource> getInviteToSend(@PathVariable long competitionId, @PathVariable long assessorId) {
        return interviewAllocationService.getInviteToSend(competitionId, assessorId).toGetResponse();
    }

    @PostMapping("/{competitionId}/allocated-applications/{assessorId}/send-invite")
    public RestResult<Void> sendInvite(@RequestBody InterviewNotifyAllocationResource interviewNotifyAllocationResource) {
        return interviewAllocationService.notifyAllocation(interviewNotifyAllocationResource).toPostResponse();
    }

    @PostMapping("/allocated-applications/{assessorId}/unallocate/{applicationId}")
    public RestResult<Void> unallocateApplication(@PathVariable long assessorId, @PathVariable long applicationId) {
        return interviewAllocationService.unallocateApplication(assessorId, applicationId).toPostResponse();
    }

    @GetMapping("/{competitionId}/unallocated-applications/{assessorId}")
    public RestResult<InterviewApplicationPageResource> getUnallocatedApplications(
            @PathVariable long competitionId,
            @PathVariable long assessorId,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "target.id", direction = Sort.Direction.ASC) Pageable pageable) {
        return interviewAllocationService.getUnallocatedApplications(competitionId, assessorId, pageable).toGetResponse();
    }

    @GetMapping("/{competitionId}/unallocated-application-ids/{assessorId}")
    public RestResult<List<Long>> getUnallocatedApplicationIds(
            @PathVariable long competitionId,
            @PathVariable long assessorId) {
        return interviewAllocationService.getUnallocatedApplicationIds(competitionId, assessorId).toGetResponse();
    }
}