package org.innovateuk.ifs.project.otherdocuments.controller;

import org.innovateuk.ifs.commons.OtherDocsWindDown;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.UserAuthenticationService;
import org.innovateuk.ifs.file.controller.FileControllerUtils;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.file.service.FilesizeAndTypeFileValidator;
import org.innovateuk.ifs.project.otherdocuments.transactional.OtherDocumentsService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * ProjectOtherDocumentsController exposes Project Other Documents data and operations through a REST API.
 */
@RestController
@RequestMapping("/project")
@OtherDocsWindDown
public class OtherDocumentsController {

    @Value("${ifs.data.service.file.storage.projectsetupotherdocuments.max.filesize.bytes}")
    private Long maxFilesizeBytesForProjectSetupOtherDocuments;

    @Value("${ifs.data.service.file.storage.projectsetupotherdocuments.valid.media.types}")
    private List<String> validMediaTypesForProjectSetupOtherDocuments;

    @Autowired
    private OtherDocumentsService otherDocumentsService;

    @Autowired
    @Qualifier("mediaTypeStringsFileValidator")
    private FilesizeAndTypeFileValidator<List<String>> fileValidator;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    private FileControllerUtils fileControllerUtils = new FileControllerUtils();

    @PostMapping(value = "/{projectId}/collaboration-agreement", produces = "application/json")
    public RestResult<FileEntryResource> addCollaborationAgreementDocument(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return fileControllerUtils.handleFileUpload(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupOtherDocuments, maxFilesizeBytesForProjectSetupOtherDocuments, request, (fileAttributes, inputStreamSupplier) ->
                otherDocumentsService.createCollaborationAgreementFileEntry(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier)
        );
    }

    @GetMapping("/{projectId}/collaboration-agreement")
    public @ResponseBody
    ResponseEntity<Object> getCollaborationAgreementFileContents(
            @PathVariable("projectId") long projectId) throws IOException {

        return fileControllerUtils.handleFileDownload(() -> otherDocumentsService.getCollaborationAgreementFileContents(projectId));
    }

    @GetMapping(value = "/{projectId}/collaboration-agreement/details", produces = "application/json")
    public RestResult<FileEntryResource> getCollaborationAgreementFileEntryDetails(
            @PathVariable("projectId") long projectId) throws IOException {

        return otherDocumentsService.getCollaborationAgreementFileEntryDetails(projectId).toGetResponse();
    }

    @PutMapping(value = "/{projectId}/collaboration-agreement", produces = "application/json")
    public RestResult<Void> updateCollaborationAgreementDocument(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return fileControllerUtils.handleFileUpdate(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupOtherDocuments, maxFilesizeBytesForProjectSetupOtherDocuments, request, (fileAttributes, inputStreamSupplier) ->
                otherDocumentsService.updateCollaborationAgreementFileEntry(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier));
    }

    @DeleteMapping(value = "/{projectId}/collaboration-agreement", produces = "application/json")
    public RestResult<Void> deleteCollaborationAgreementDocument(
            @PathVariable("projectId") long projectId) throws IOException {

        return otherDocumentsService.deleteCollaborationAgreementFile(projectId).toDeleteResponse();
    }

    @PostMapping(value = "/{projectId}/exploitation-plan", produces = "application/json")
    public RestResult<FileEntryResource> addExploitationPlanDocument(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return fileControllerUtils.handleFileUpload(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupOtherDocuments, maxFilesizeBytesForProjectSetupOtherDocuments, request, (fileAttributes, inputStreamSupplier) ->
                otherDocumentsService.createExploitationPlanFileEntry(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier));
    }

    @GetMapping("/{projectId}/exploitation-plan")
    public @ResponseBody
    ResponseEntity<Object> getExploitationPlanFileContents(
            @PathVariable("projectId") long projectId) throws IOException {

        return fileControllerUtils.handleFileDownload(() -> otherDocumentsService.getExploitationPlanFileContents(projectId));
    }

    @GetMapping(value = "/{projectId}/exploitation-plan/details", produces = "application/json")
    public RestResult<FileEntryResource> getExploitationPlanFileEntryDetails(
            @PathVariable("projectId") long projectId) throws IOException {

        return otherDocumentsService.getExploitationPlanFileEntryDetails(projectId).toGetResponse();
    }

    @PutMapping(value = "/{projectId}/exploitation-plan", produces = "application/json")
    public RestResult<Void> updateExploitationPlanDocument(
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestHeader(value = "Content-Length", required = false) String contentLength,
            @PathVariable(value = "projectId") long projectId,
            @RequestParam(value = "filename", required = false) String originalFilename,
            HttpServletRequest request) {

        return fileControllerUtils.handleFileUpdate(contentType, contentLength, originalFilename, fileValidator, validMediaTypesForProjectSetupOtherDocuments, maxFilesizeBytesForProjectSetupOtherDocuments, request, (fileAttributes, inputStreamSupplier) ->
                otherDocumentsService.updateExploitationPlanFileEntry(projectId, fileAttributes.toFileEntryResource(), inputStreamSupplier));
    }

    @DeleteMapping(value = "/{projectId}/exploitation-plan", produces = "application/json")
    public RestResult<Void> deleteExploitationPlanDocument(
            @PathVariable("projectId") long projectId) throws IOException {

        return otherDocumentsService.deleteExploitationPlanFile(projectId).toDeleteResponse();
    }

    @PostMapping("/{projectId}/partner/documents/approved/{approved}")
    public RestResult<Void> acceptOrRejectOtherDocuments(@PathVariable("projectId") long projectId, @PathVariable("approved") Boolean approved) {
        //TODO IFS-471 use workflow for approving other documents
        return otherDocumentsService.acceptOrRejectOtherDocuments(projectId, approved).toPostResponse();
    }

    @GetMapping("/{projectId}/partner/documents/ready")
    public RestResult<Boolean>isOtherDocumentsSubmitAllowed(@PathVariable("projectId") final Long projectId,
                                                            HttpServletRequest request) {

        UserResource authenticatedUser = userAuthenticationService.getAuthenticatedUser(request);
        return otherDocumentsService.isOtherDocumentsSubmitAllowed(projectId, authenticatedUser.getId()).toGetResponse();
    }

    @PostMapping("/{projectId}/partner/documents/submit")
    public RestResult<Void>setPartnerDocumentsSubmitted(@PathVariable("projectId") final Long projectId) {
        return otherDocumentsService.saveDocumentsSubmitDateTime(projectId, ZonedDateTime.now()).toPostResponse();
    }
}

