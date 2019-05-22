package org.innovateuk.ifs.documentation;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class CompetitionSearchResultItemDocs {
    public static final FieldDescriptor[] competitionSearchResultItemFields = {
            fieldWithPath("id").description("The id of the search result item"),
            fieldWithPath("name").description("The name of the search result item"),
            fieldWithPath("innovationAreaNames").description("The innovation area names of the search result item"),
            fieldWithPath("competitionStatus").description("The competition station of the search result item"),
            fieldWithPath("competitionTypeName").description("The competition type name of the search result item"),
            fieldWithPath("clazz").description("The subclass and type of the search result item"),
            fieldWithPath("numberOfApplications").description("The number of applications of the search result item").optional().type(JsonFieldType.NUMBER),
            fieldWithPath("startDateDisplay").description("The start date display of the search result item").optional().type(JsonFieldType.STRING),
            fieldWithPath("projectsCount").description("The projects count of the search result item").optional().type(JsonFieldType.NUMBER),
            fieldWithPath("publishDate").description("The publish date of the search result item").optional().type(JsonFieldType.STRING),
            fieldWithPath("openDate").description("The open date of the search result item").optional().type(JsonFieldType.STRING),
    };
}
