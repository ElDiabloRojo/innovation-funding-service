package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class OrganisationAddressDocs {
    public static final FieldDescriptor[] organisationAddressResourceFields = {
            fieldWithPath("id").description("Id of the Organisation Address"),
            fieldWithPath("organisation").description("Id of the Organisation"),
            fieldWithPath("address").description("The associated Address"),
            fieldWithPath("addressType").description("The associated Address Type")
    };
}