package org.innovateuk.ifs.form.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.form.controller.FormInputController;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.transactional.FormInputService;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.form.documentation.FormInputResourceDocs.formInputResourceBuilder;
import static org.innovateuk.ifs.form.documentation.FormInputResourceDocs.formInputResourceFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

public class FormInputControllerDocumentation extends BaseControllerMockMVCTest<FormInputController> {
    private static final String baseURI = "/forminput";

    @Mock
    private FormInputService formInputServiceMock;

    @Override
    protected FormInputController supplyControllerUnderTest() {
        return new FormInputController();
    }

    @Test
    public void documentFindById() throws Exception {
        FormInputResource testResource = formInputResourceBuilder.build();
        when(formInputServiceMock.findFormInput(1L)).thenReturn(serviceSuccess(testResource));

        mockMvc.perform(get(baseURI + "/{id}", 1L)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("forminput/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("id of the forminput to be fetched")
                        ),
                        responseFields(
                                formInputResourceFields
                        )
                ));
    }

    @Test
    public void documentFindByQuestionId() throws Exception {
        List<FormInputResource> testResource = formInputResourceBuilder.build(1);
        when(formInputServiceMock.findByQuestionId(1L)).thenReturn(serviceSuccess(testResource));

        mockMvc.perform(get(baseURI + "/findByQuestionId/{id}", 1L)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("forminput/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("id of the question")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of formInputs the user is allowed to see")
                        ).andWithPrefix(
                                "[].", formInputResourceFields)
                        )
                );
    }

    @Test
    public void documentFindByCompetitionId() throws Exception {
        List<FormInputResource> testResource = formInputResourceBuilder.build(1);
        when(formInputServiceMock.findByCompetitionId(1L)).thenReturn(serviceSuccess(testResource));

        mockMvc.perform(get(baseURI + "/findByCompetitionId/{id}", 1L)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("forminput/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("id of the competition")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of formInputs the user is allowed to see")
                        ).andWithPrefix(
                                "[].", formInputResourceFields)
                        )
                );
    }

    @Test
    public void documentSave() throws Exception {
        FormInputResource testResource = formInputResourceBuilder.build();
        when(formInputServiceMock.save(any())).thenReturn(serviceSuccess(testResource));

        mockMvc.perform(put(baseURI + "/")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testResource))
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("forminput/{method-name}",
                        responseFields(formInputResourceFields)
                ));
    }

    @Test
    public void documentDelete() throws Exception {
        when(formInputServiceMock.delete(1L)).thenReturn(serviceSuccess());

        mockMvc.perform(delete(baseURI + "/{id}", 1L)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andDo(document("forminput/{method-name}",
                        pathParameters(
                                parameterWithName("id").description("id of the forminput")
                        )
                ));
    }
}
