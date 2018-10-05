package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class GrantClaimMaximumDocs {

    public static final FieldDescriptor[] grantClaimMaximumResourceFields = {
            fieldWithPath("id").description("Id of the Grant Claim Maximum"),
            fieldWithPath("competitions").description("Linked competitions to this Grant Claim Maximum"),
            fieldWithPath("researchCategory").description("The research category the Grant Claim belongs to"),
            fieldWithPath("organisationSize").description("The organisation size for this Grant Claim Maximum"),
            fieldWithPath("maximum").description("The maximum for this Grant Claim")
    };
}
