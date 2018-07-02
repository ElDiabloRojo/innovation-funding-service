package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;

/**
 * Helper for Spring REST Docs, specifically for alerts.
 */
public class AlertDocs {

    public static final FieldDescriptor[] alertResourceFields = {
            PayloadDocumentation.fieldWithPath("id").description("id of the alert"),
            PayloadDocumentation.fieldWithPath("message").description("message of the alert"),
            PayloadDocumentation.fieldWithPath("type").description("type of the alert"),
            PayloadDocumentation.fieldWithPath("validFromDate").description("date that the alert is visible from"),
            PayloadDocumentation.fieldWithPath("validToDate").description("date that the alert is visible until")
    };

}
