package org.innovateuk.ifs.affiliation.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.affiliation.controller.AffiliationController;
import org.innovateuk.ifs.affiliation.transactional.AffiliationService;
import org.innovateuk.ifs.user.resource.AffiliationListResource;
import org.innovateuk.ifs.user.resource.AffiliationResource;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.AffiliationDocs.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AffiliationControllerDocumentation extends BaseControllerMockMVCTest<AffiliationController> {

    @Mock
    private AffiliationService affiliationServiceMock;

    @Override
    protected AffiliationController supplyControllerUnderTest() {
        return new AffiliationController();
    }

    @Test
    public void getUserAffiliations() throws Exception {
        Long userId = 1L;
        List<AffiliationResource> responses = affiliationResourceBuilder.build(2);
        when(affiliationServiceMock.getUserAffiliations(userId)).thenReturn(serviceSuccess(new AffiliationListResource(responses)));

        mockMvc.perform(get("/affiliation/id/{id}/getUserAffiliations", userId))
                .andExpect(status().isOk())
                .andDo(document("affiliation/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Identifier of the user associated with affiliations being requested")
                        ),
                        responseFields(
                                fieldWithPath("affiliationResourceList[]").description("List of affiliations belonging to the user")
                        ).andWithPrefix("affiliationResourceList[].", affiliationResourceFields)
                ));
    }

    @Test
    public void updateUserAffiliations() throws Exception {
        Long userId = 1L;
        List<AffiliationResource> affiliations = affiliationResourceBuilder
                .build(2);
        AffiliationListResource affiliationListResource = affiliationListResourceBuilder
                .build();

        when(affiliationServiceMock.updateUserAffiliations(userId, affiliationListResource)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/affiliation/id/{id}/updateUserAffiliations", userId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(affiliationListResource)))
                .andExpect(status().isOk())
                .andDo(document("affiliation/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("Identifier of the user associated with affiliations being updated")
                        ),
                        requestFields(fieldWithPath("affiliationResourceList[]").description("List of affiliations belonging to the user"))
                                .andWithPrefix("affiliationResourceList[].", affiliationResourceFields)
                ));
    }
}
