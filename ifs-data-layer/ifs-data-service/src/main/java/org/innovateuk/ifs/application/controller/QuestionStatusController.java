package org.innovateuk.ifs.application.controller;

import org.innovateuk.ifs.application.resource.QuestionApplicationCompositeId;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.transactional.QuestionStatusService;
import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * QuestionStatusController exposes question status data and operations through a REST API.
 * It is mainly used at present for getting question statuses for given question in given application.
 */
@ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
@RestController
@RequestMapping({"/questionStatus", "question-status"})
public class QuestionStatusController {

    @Autowired
    private QuestionStatusService questionStatusService;

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/findByQuestionAndApplication/{questionId}/{applicationId}", "/find-by-question-and-application/{questionId}/{applicationId}"})
    public RestResult<List<QuestionStatusResource>> getQuestionStatusByQuestionIdAndApplicationId(@PathVariable("questionId") long questionId, @PathVariable("applicationId") long applicationId) {
        return questionStatusService.getQuestionStatusByQuestionIdAndApplicationId(questionId, applicationId).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/findByQuestionAndApplicationAndOrganisation/{questionId}/{applicationId}/{organisationId}", "/find-by-question-and-application-and-organisation/{questionId}/{applicationId}/{organisationId}"})
    public RestResult<List<QuestionStatusResource>> getQuestionStatusByApplicationIdAndAssigneeIdAndOrganisationId(@PathVariable("questionId") long questionId, @PathVariable("applicationId") long applicationId, @PathVariable("organisationId") Long organisationId) {
        return questionStatusService.getQuestionStatusByApplicationIdAndAssigneeIdAndOrganisationId(questionId, applicationId, organisationId).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/findByQuestionIdsAndApplicationIdAndOrganisationId/{questionIds}/{applicationId}/{organisationId}", "/find-by-question-ids-and-application-id-and-organisation-id/{questionIds}/{applicationId}/{organisationId}"})
    public RestResult<List<QuestionStatusResource>> getQuestionStatusByQuestionIdsAndApplicationIdAndOrganisationId(@PathVariable Long[] questionIds, @PathVariable("applicationId") long applicationId, @PathVariable("organisationId") long organisationId){
        return questionStatusService.getQuestionStatusByQuestionIdsAndApplicationIdAndOrganisationId(questionIds, applicationId, organisationId).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/findByApplicationAndOrganisation/{applicationId}/{organisationId}", "/find-by-application-and-organisation/{applicationId}/{organisationId}"})
    public RestResult<List<QuestionStatusResource>> findByApplicationAndOrganisation(@PathVariable("applicationId") long applicationId, @PathVariable("organisationId") long organisationId){
        return questionStatusService.findByApplicationAndOrganisation(applicationId, organisationId).toGetResponse();
    }

    @GetMapping("/find-marked-complete-by-question-and-application-and-organisation/{questionId}/{applicationId}/{organisationId}")
    public RestResult<Optional<QuestionStatusResource>> getMarkedAsCompleteByQuestionApplicationAndOrganisation(@PathVariable("questionId") long questionId, @PathVariable("applicationId") long applicationId, @PathVariable("organisationId") long organisationId) {
        return questionStatusService.findApplicationAndMarkedAsCompleteByOrganisation(questionId, applicationId, organisationId).toGetResponse();
    }

    @GetMapping("/{id}")
    public RestResult<QuestionStatusResource> getQuestionStatusResourceById(@PathVariable("id") Long id){
        return questionStatusService.getQuestionStatusResourceById(id).toGetResponse();
    }

    @ZeroDowntime(reference = "IFS-430", description = "remove camelCase mapping in h2020 sprint 6")
    @GetMapping({"/getAssignedQuestionsCountByApplicationIdAndAssigneeId/{applicationId}/{assigneeId}", "/get-assigned-questions-count-by-application-id-and-assignee-id/{applicationId}/{assigneeId}"})
    public RestResult<Integer> getAssignedQuestionsCountByApplicationIdAndAssigneeId(@PathVariable("applicationId") final long applicationId,
                                                                                     @PathVariable("assigneeId") final long assigneeId) {
        return questionStatusService.getCountByApplicationIdAndAssigneeId(applicationId, assigneeId).toGetResponse();
    }

    @PutMapping("/mark-as-complete/{questionId}/{applicationId}/{markedAsCompleteById}")
    public RestResult<List<ValidationMessages>> markAsComplete(@PathVariable("questionId") final long questionId,
                                                               @PathVariable("applicationId") final long applicationId,
                                                               @PathVariable("markedAsCompleteById") final long markedAsCompleteById) {
        QuestionApplicationCompositeId ids = new QuestionApplicationCompositeId(questionId, applicationId);
        return questionStatusService.markAsComplete(ids, markedAsCompleteById).toPutWithBodyResponse();
    }

    @PutMapping("/mark-as-in-complete/{questionId}/{applicationId}/{markedAsInCompleteById}")
    public RestResult<Void> markAsInComplete(@PathVariable("questionId") final long questionId,
                                             @PathVariable("applicationId") final long applicationId,
                                             @PathVariable("markedAsInCompleteById") final long markedAsInCompleteById) {
        QuestionApplicationCompositeId ids = new QuestionApplicationCompositeId(questionId, applicationId);
        return questionStatusService.markAsInComplete(ids, markedAsInCompleteById).toPutResponse();
    }

    @PutMapping("/mark-team-as-in-complete/{questionId}/{applicationId}/{markedAsInCompleteById}")
    public RestResult<Void> markTeamAsInComplete(@PathVariable("questionId") final long questionId,
                                             @PathVariable("applicationId") final long applicationId,
                                             @PathVariable("markedAsInCompleteById") final long markedAsInCompleteById) {
        QuestionApplicationCompositeId ids = new QuestionApplicationCompositeId(questionId, applicationId);
        return questionStatusService.markTeamAsInComplete(ids, markedAsInCompleteById).toPutResponse();
    }

    @PutMapping("/assign/{questionId}/{applicationId}/{assigneeId}/{assignedById}")
    public RestResult<Void> assign(@PathVariable("questionId") final long questionId,
                                   @PathVariable("applicationId") final long applicationId,
                                   @PathVariable("assigneeId") final long assigneeId,
                                   @PathVariable("assignedById") final long assignedById) {
        QuestionApplicationCompositeId ids = new QuestionApplicationCompositeId(questionId, applicationId);
        return questionStatusService.assign(ids, assigneeId, assignedById).toPutResponse();
    }

    @GetMapping("/get-marked-as-complete/{applicationId}/{organisationId}")
    public RestResult<Set<Long>> getMarkedAsComplete(@PathVariable("applicationId") final long applicationId,
                                                     @PathVariable("organisationId") final long organisationId) {
        return questionStatusService.getMarkedAsComplete(applicationId, organisationId).toGetResponse();
    }

    @PutMapping("/update-notification/{questionStatusId}/{notify}")
    public RestResult<Void> updateNotification(@PathVariable("questionStatusId") final long questionStatusId,
                                               @PathVariable("notify") final boolean notify) {
        return questionStatusService.updateNotification(questionStatusId, notify).toPutResponse();
    }

}
