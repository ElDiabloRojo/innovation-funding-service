package com.worth.ifs.assessment.documentation;

import com.worth.ifs.BaseControllerMockMVCTest;
import com.worth.ifs.assessment.controller.AssessorFormInputResponseController;
import com.worth.ifs.assessment.resource.AssessorFormInputResponseResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

import java.util.List;

import static com.worth.ifs.commons.service.ServiceResult.serviceSuccess;
import static com.worth.ifs.documentation.AssessorFormInputResponseDocs.assessorFormInputResponseResourceBuilder;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;


public class AssessorFormInputResponseControllerDocumentation extends BaseControllerMockMVCTest<AssessorFormInputResponseController> {

    private RestDocumentationResultHandler document;

    @Override
    protected AssessorFormInputResponseController supplyControllerUnderTest() {
        return new AssessorFormInputResponseController();
    }

    @Before
    public void setup(){
        this.document = document("assessorFormInputResponse/{method-name}",
                preprocessResponse(prettyPrint()));
    }

    @Test
    public void getAllAssessorFormInputResponses() throws Exception {
        Long assessmentId = 1L;
        List<AssessorFormInputResponseResource> responses = assessorFormInputResponseResourceBuilder.build(2);
        when(assessorFormInputResponseServiceMock.getAllAssessorFormInputResponses(assessmentId)).thenReturn(serviceSuccess(responses));

        mockMvc.perform(get("/assessorFormInputResponse/assessment/{assessmentId}", assessmentId))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("assessmentId").description("Id of the assessment associated with responses being requested")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of responses the user is allowed to see")
                        )
                ));
    }

    @Test
    public void getAllAssessorFormInputResponsesByAssessmentAndQuestion() throws Exception {
        Long assessmentId = 1L;
        Long questionId = 2L;
        List<AssessorFormInputResponseResource> responses = assessorFormInputResponseResourceBuilder.build(2);
        when(assessorFormInputResponseServiceMock.getAllAssessorFormInputResponsesByAssessmentAndQuestion(assessmentId, questionId)).thenReturn(serviceSuccess(responses));

        mockMvc.perform(get("/assessorFormInputResponse/assessment/{assessmentId}/question/{questionId}", assessmentId, questionId))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("assessmentId").description("Id of the assessment associated with responses being requested"),
                                parameterWithName("questionId").description("Id of the question associated with responses being requested")
                        ),
                        responseFields(
                                fieldWithPath("[]").description("List of responses the user is allowed to see")
                        )
                ));
    }

    @Test
    public void updateAssessorFormInputResponse() throws Exception {
        final Long assessmentId = 1L;
        final Long formInputId = 2L;
        final String value = "Feedback";

        when(assessorFormInputResponseServiceMock.updateFormInputResponse(assessmentId, formInputId, value)).thenReturn(serviceSuccess());

        mockMvc.perform(put("/assessorFormInputResponse/formInput/{formInputId}/assessment/{assessmentId}", formInputId, assessmentId)
                .content(value))
                .andDo(this.document.snippets(
                        pathParameters(
                                parameterWithName("formInputId").description("Id of the form associated with the response being updated"),
                                parameterWithName("assessmentId").description("Id of the assessment associated with the response being updated")
                        )
                ));
    }
}
