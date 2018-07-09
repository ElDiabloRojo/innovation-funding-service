package org.innovateuk.ifs.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.question.resource.QuestionSetupType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.innovateuk.ifs.application.builder.FormInputResponseResourceBuilder.newFormInputResponseResource;
import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.formInputResponseListType;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.PROJECT_SUMMARY;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class FormInputResponseRestServiceMocksTest extends BaseRestServiceUnitTest<FormInputResponseRestServiceImpl> {

    private static final String formInputResponseRestURL = "/forminputresponse";

    @Override
    protected FormInputResponseRestServiceImpl registerRestServiceUnderTest() {
        FormInputResponseRestServiceImpl formInputResponseService = new FormInputResponseRestServiceImpl();
        return formInputResponseService;
    }

    @Test
    public void getResponsesByApplicationId() {
        List<FormInputResponseResource> returnedResponses = Arrays.asList(1,2,3).stream().map(i -> new FormInputResponseResource()).collect(Collectors.toList());


        setupGetWithRestResultExpectations(formInputResponseRestURL + "/findResponsesByApplication/123", formInputResponseListType(), returnedResponses);

        List<FormInputResponseResource> responses = service.getResponsesByApplicationId(123L).getSuccess();
        assertEquals(returnedResponses, responses);
    }

    @Test
    public void saveQuestionResponse() {

        ObjectNode entityUpdates = new ObjectMapper().createObjectNode().
                put("userId", 123L).put("applicationId", 456L).
                put("formInputId", 789L).
                put("value", "Very good answer!").
                put("ignoreEmpty", false);

        List<Error> returnedResponses = asList(new Error("A returned string", BAD_REQUEST), new Error("A returned string 2", BAD_REQUEST));
        ValidationMessages validationMessages = new ValidationMessages(returnedResponses);

        setupPostWithRestResultExpectations(formInputResponseRestURL + "/saveQuestionResponse/", ValidationMessages.class, entityUpdates, validationMessages, OK);

        ValidationMessages responses = service.saveQuestionResponse(123L, 456L, 789L, "Very good answer!", false).getSuccess();
        Assert.assertEquals(returnedResponses, responses.getErrors());
    }

    @Test
    public void getByFormInputIdAndApplication(){
        List<FormInputResponseResource> returnedResponses = Arrays.asList(1,2,3).stream().map(i -> new FormInputResponseResource()).collect(Collectors.toList());

        setupGetWithRestResultExpectations(formInputResponseRestURL + "/findResponseByFormInputIdAndApplicationId/456/123", formInputResponseListType(), returnedResponses);

        List<FormInputResponseResource> responses = service.getByFormInputIdAndApplication(456L, 123L).getSuccess();
        assertEquals(returnedResponses, responses);
    }

    @Test
    public void getByApplicationIdAndQuestionSetupType() {
        long applicationId = 1L;
        QuestionSetupType questionSetupType = PROJECT_SUMMARY;

        FormInputResponseResource expected = newFormInputResponseResource().build();
        setupGetWithRestResultExpectations(format("%s/%s/%s/%s", formInputResponseRestURL, "findByApplicationIdAndQuestionSetupType", applicationId, questionSetupType), FormInputResponseResource.class, expected);
        FormInputResponseResource actual = service.getByApplicationIdAndQuestionSetupType(applicationId, PROJECT_SUMMARY).getSuccess();
        assertEquals(expected, actual);
    }

    @Test
    public void getByApplicationIdAndQuestionId() {
        long applicationId = 1L;
        long questionId = 2L;

        List<FormInputResponseResource> expected = newFormInputResponseResource().build(2);
        setupGetWithRestResultExpectations(format("%s/%s/%s/%s", formInputResponseRestURL, "findByApplicationIdAndQuestionId",
                applicationId, questionId), formInputResponseListType(), expected);
        List<FormInputResponseResource> actual = service.getByApplicationIdAndQuestionId(applicationId, questionId).getSuccess();
        assertEquals(expected, actual);
    }
}
