package org.innovateuk.ifs.project.spendprofile.controller;

import org.apache.commons.lang3.StringEscapeUtils;
import org.innovateuk.ifs.project.spendprofile.resource.SpendProfileCSVResource;
import org.innovateuk.ifs.project.spendprofile.service.SpendProfileService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This controller will handle all requests that are related to spend profile export/downloads.
 */
@Controller
@RequestMapping("/" + ProjectSpendProfileExportController.BASE_DIR + "/{projectId}/partner-organisation/{organisationId}/spend-profile-export")
public class ProjectSpendProfileExportController {

    static final String BASE_DIR = "project";
    private static final String CONTENT_TYPE = "text/csv";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-disposition";
    private static final String ATTACHMENT_HEADER = "attachment;filename=";

    @Autowired
    private SpendProfileService spendProfileService;

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_SPEND_PROFILE_SECTION')")
    @GetMapping("/csv")
    public void exportProjectPartnerSpendProfileAsCSV(@P("projectId")@PathVariable("projectId") final Long projectId,
                                                      @PathVariable("organisationId") final Long organisationId,
                                                      UserResource loggedInUser,
                                                      HttpServletResponse response) throws IOException {
        SpendProfileCSVResource spendProfileCSVResource = spendProfileService.getSpendProfileCSV(projectId, organisationId);
        response.setContentType(CONTENT_TYPE);
        //response.setHeader(HEADER_CONTENT_DISPOSITION, getCSVAttachmentHeader(spendProfileCSVResource.getFileName()));
        response.setHeader(HEADER_CONTENT_DISPOSITION, getCSVAttachmentHeader(spendProfileCSVResource.getFileName().replace(',', ' ')));
        response.getOutputStream().print(spendProfileCSVResource.getCsvData());
        response.getOutputStream().flush();
    }

    private String getCSVAttachmentHeader(String fileName) {
        return ATTACHMENT_HEADER + fileName;
        //return StringEscapeUtils.escapeCsv(ATTACHMENT_HEADER + fileName);
    }
}
