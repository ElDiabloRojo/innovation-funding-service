package com.worth.ifs.application;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;

import com.worth.ifs.BaseUnitTest;
import com.worth.ifs.application.domain.Question;
import com.worth.ifs.application.resource.ApplicationResource;
import com.worth.ifs.application.resource.SectionResource;
import com.worth.ifs.exception.ErrorController;
import com.worth.ifs.finance.resource.category.CostCategory;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.finance.resource.cost.CostType;
import com.worth.ifs.finance.resource.cost.Materials;
import com.worth.ifs.security.CookieFlashMessageFilter;
import com.worth.ifs.user.domain.ProcessRole;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import static com.worth.ifs.application.builder.QuestionBuilder.newQuestion;
import static com.worth.ifs.application.builder.SectionResourceBuilder.newSectionResource;
import static com.worth.ifs.application.service.Futures.settable;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@RunWith(MockitoJUnitRunner.class)
@TestPropertySource(locations="classpath:application.properties")
public class ApplicationFormControllerTest  extends BaseUnitTest {

    @InjectMocks
    private ApplicationFormController applicationFormController;

    @Mock
    private CookieFlashMessageFilter cookieFlashMessageFilter;

    @Mock
    private Model model;

    private ApplicationResource application;
    private Long sectionId;
    private Long questionId;
    private Long formInputId;
    private Long costId;

    private static ResultMatcher matchUrl(final String expectedString) {
        return result -> assertTrue(result.getResponse().getRedirectedUrl().equals(expectedString));
    }

    @Before
    public void setUp(){
        super.setup();

        // Process mock annotations
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(applicationFormController, new ErrorController())
//                .setHandlerExceptionResolvers(withExceptionControllerAdvice())
                .setViewResolvers(viewResolver())
                .addFilter(new CookieFlashMessageFilter())
                .build();

        this.setupCompetition();
        this.setupApplicationWithRoles();
        this.setupApplicationResponses();
        this.loginDefaultUser();
        this.setupFinances();


        application = applications.get(0);
        sectionId = Long.valueOf(1);
        questionId = Long.valueOf(1);
        formInputId = Long.valueOf(111);
        costId = Long.valueOf(1);

        // save actions should always succeed.
        ArrayList<String> validationErrors = new ArrayList<>();
        validationErrors.add("Please enter some text 123");
        when(formInputResponseService.save(anyLong(), anyLong(), anyLong(), eq(""))).thenReturn(validationErrors);
        when(formInputResponseService.save(anyLong(), anyLong(), anyLong(), anyString())).thenReturn(new ArrayList<>());
    }

    //@Test
    public void testApplicationForm() throws Exception {
        ApplicationResource app = applications.get(0);
        ProcessRole userAppRole = new ProcessRole();

        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(app.getId())).thenReturn(app);
        when(processRoleService.findProcessRole(loggedInUser.getId(), app.getId())).thenReturn(userAppRole);

        mockMvc.perform(get("/application/1/form"))
                .andExpect(view().name("application-form"))
                .andExpect(model().attribute("currentApplication", app))
                .andExpect(model().attribute("currentSectionId", 1L));

    }

    @Test
    public void testApplicationFormWithOpenSection() throws Exception {
        EnumMap<CostType, CostCategory> costCategories = new EnumMap<>(CostType.class);

        Long currentSectionId = 1L;

        SectionResource currentSection = newSectionResource().with(s -> s.setId(currentSectionId)).build();

        //when(applicationService.getApplicationsByUserId(loggedInUser.getId())).thenReturn(applications);
        when(applicationService.getById(application.getId())).thenReturn(application);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        when(sectionService.getById(anyLong())).thenReturn(currentSection);
        when(sectionService.getByName("Your finances")).thenReturn(currentSection);
        mockMvc.perform(get("/application/1/form/section/1"))
                .andExpect(view().name("application-form"))
                .andExpect(model().attribute("currentApplication", application))
                .andExpect(model().attribute("leadOrganisation", organisations.get(0)))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasSize(organisations.size())))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(0))))
                .andExpect(model().attribute("applicationOrganisations", Matchers.hasItem(organisations.get(1))))
                .andExpect(model().attribute("userIsLeadApplicant", true))
                .andExpect(model().attribute("leadApplicant", processRoles.get(0)))
                .andExpect(model().attribute("currentSectionId", currentSectionId));

    }

    @Test
    public void testQuestionSubmit() throws Exception {
        Question question = newQuestion().build();
        ApplicationResource application = applications.get(0);

        when(questionService.getById(anyLong())).thenReturn(question);
        when(applicationService.getById(application.getId(), false)).thenReturn(application);
        when(competitionService.getById(anyLong())).thenReturn(competitionResource);
        mockMvc.perform(post("/application/1/form/question/1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testAddAnother() throws Exception {

        when(sectionService.getById(anyLong())).thenReturn(null);
        when(sectionService.getByName("Your finances")).thenReturn(newSectionResource().with(s -> s.setId(1L)).build());
        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("add_cost", String.valueOf(questionId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/" + application.getId() + "/form/section/" + sectionId));



        // verify that the method is called to send the data to the data services.
        Mockito.inOrder(financeService).verify(financeService, calls(1)).addCost(applicationFinanceResource.getId(), questionId);
    }


    @Test
    public void testAjaxAddCost() throws Exception {
        CostItem costItem = new Materials();
        when(costService.add(anyLong(),anyLong(), any())).thenReturn(costItem);
        MvcResult result = mockMvc.perform(
                get("/application/{applicationId}/form/add_cost/{questionId}", application.getId(), questionId)
        ).andReturn();

        System.out.println("AjaxAddRow");
        System.out.println(result.getResponse().getContentAsString());

        // verify that the method is called to send the data to the data services.
        Mockito.inOrder(costService).verify(costService, calls(1)).add(eq(applicationFinanceResource.getId()), eq(questionId), any());
    }

    @Test
    public void testAjaxRemoveCost() throws Exception {
        CostItem costItem = new Materials();
        when(costService.add(anyLong(),anyLong(), any())).thenReturn(costItem);
        MvcResult result = mockMvc.perform(
                get("/application/{applicationId}/form/remove_cost/{costId}", application.getId(), costId)
        ).andReturn();

        System.out.println("AjaxRemoveRow");
        System.out.println(result.getResponse().getContentAsString());

        // verify that the method is called to send the data to the data services.
        Mockito.inOrder(costService).verify(costService, calls(1)).delete(eq(costId));
    }

    @Test
    public void testApplicationFormSubmit() throws Exception {
        Long userId = loggedInUser.getId();

        when(sectionService.getById(anyLong())).thenReturn(null);
        when(sectionService.getByName("Your finances")).thenReturn(newSectionResource().with(s -> s.setId(1L)).build());
        // without assign or mark as complete, just redirect to application overview.

        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "Question 1 Response")
                        .param("formInput[2]", "Question 2 Response")
                        .param("formInput[3]", "Question 3 Response")
                        .param("formInput[application_details-startdate_year]", "2015")
                        .param("formInput[application_details-startdate_day]", "15")
                        .param("formInput[application_details-startdate_month]", "11")
                        .param("formInput[application_details-title]", "New Application Title")
                        .param("formInput[application_details-duration]", "12")
                        .param("submit-section", "Save")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() +"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME))
//                .andExpect(cookie().value(CookieFlashMessageFilter.COOKIE_NAME, "applicationSaved"))
                .andReturn();
    }

    @Test
    public void testApplicationFormSubmitMarkAsComplete() throws Exception {
        Long userId = loggedInUser.getId();

        when(sectionService.getById(anyLong())).thenReturn(null);
        when(sectionService.getByName("Your finances")).thenReturn(newSectionResource().with(s -> s.setId(1L)).build());

        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("mark_as_complete", "12")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/" + sectionId+"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME))
                .andReturn();
    }

    @Test
    public void testApplicationFormSubmitMarkAsIncomplete() throws Exception {
        Long userId = loggedInUser.getId();

        when(sectionService.getById(anyLong())).thenReturn(null);
        when(sectionService.getByName("Your finances")).thenReturn(newSectionResource().with(s -> s.setId(1L)).build());

        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("mark_as_complete", "12")
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/" + sectionId +"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME))
                .andReturn();
    }

    @Test
    public void testApplicationFormSubmitValidationErrors() throws Exception {

        when(sectionService.getById(anyLong())).thenReturn(null);
        when(sectionService.getByName("Your finances")).thenReturn(newSectionResource().with(s -> s.setId(1L)).build());
        //http://www.disasterarea.co.uk/blog/mockmvc-to-test-spring-mvc-form-validation/
        Long userId = loggedInUser.getId();

        when(formInputResponseService.save(userId, application.getId(), 1L, "")).thenReturn(asList("Please enter some text"));
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "")
                        .param("formInput[2]", "Question 2 Response")
                        .param("submit-section", "Save")
        ).andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeHasFieldErrors("form", "formInput[1]"))
                .andExpect(model().attributeErrorCount("form", 1))
                .andExpect(model().attributeHasFieldErrorCode("form", "formInput[1]", "Please enter some text"))
                .andReturn();
    }

    @Test
    public void testApplicationFormSubmitNotAllowedMarkAsComplete() throws Exception {
        // Question should not be marked as complete, since the input is not valid.

        ArrayList<String> validationErrors = new ArrayList<>();
        validationErrors.add("Please enter some text");
        when(formInputResponseService.save(anyLong(), anyLong(), anyLong(), eq(""))).thenReturn(validationErrors);
        when(questionService.getMarkedAsComplete(anyLong(), anyLong())).thenReturn(settable(new HashSet<>()));
        Long userId = loggedInUser.getId();

        when(sectionService.getById(sectionId)).thenReturn(newSectionResource().with(s -> s.setId(sectionId)).build());
        when(sectionService.getByName("Your finances")).thenReturn(newSectionResource().with(s -> s.setId(1L)).build());

        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "")
                        .param("mark_as_complete", "1")
        ).andExpect(status().isOk())
                .andExpect(view().name("application-form"))
                .andExpect(model().attributeErrorCount("form", 1))
                .andExpect(model().attributeHasFieldErrors("form", "formInput[1]"))
                .andReturn();
    }

    @Test
    public void testApplicationFormSubmitAssignQuestion() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("formInput[1]", "Question 1 Response")
                        .param("formInput[2]", "Question 2 Response")
                        .param("formInput[3]", "Question 3 Response")
                        .param("submit-section", "Save")
                        .param("assign_question", questionId + "_" + loggedInUser.getId())
        ).andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("/application/" + application.getId() + "/form/section/"+sectionId+"**"))
                .andExpect(cookie().exists(CookieFlashMessageFilter.COOKIE_NAME))
//                .andExpect(cookie().value(CookieFlashMessageFilter.COOKIE_NAME, "assignedQuestion"))
                .andReturn();
    }



    @Test
    public void testSaveFormElement() throws Exception {
        String value = "Form Input "+formInputId+" Response";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", formInputId.toString())
                        .param("fieldName", "formInput["+formInputId+"]")
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        Mockito.inOrder(formInputResponseService).verify(formInputResponseService, calls(1)).save(loggedInUser.getId(), application.getId(), formInputId, value);
    }

    @Test
    public void testSaveFormElementApplicationTitle() throws Exception {
        String value = "New application title #216";
        String fieldName = "application.name";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));
    }

    @Test
    public void testSaveFormElementEmptyApplicationTitle() throws Exception {
        String value = "";
        String fieldName = "application.name";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter the full title of the project.\"]}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementSpacesApplicationTitle() throws Exception {
        String value = " ";
        String fieldName = "application.name";

        MvcResult result = mockMvc.perform(
                post("/application/"+application.getId().toString()+"/form/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter the full title of the project.\"]}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
     public void testSaveFormElementApplicationDuration() throws Exception {
        String value = "123";
        String fieldName = "application.durationInMonths";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));
    }

    @Test
    public void testSaveFormElementApplicationInvalidDuration() throws Exception {
        String value = "aaaa";
        String fieldName = "application.durationInMonths";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", "")
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter a valid value.\"]}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementCostSubcontracting() throws Exception {
        String value = "123";
        String questionId = "cost-subcontracting-13-subcontractingCost";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "subcontracting_costs-cost-13")
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementApplicationStartDate() throws Exception {
        String value = "25";
        String questionId= "application_details-startdate_day";
        String fieldName = "application.startDate.dayOfMonth";

        MvcResult result = mockMvc.perform(
                post("/application/1/form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
        ).andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"true\"}";
        Assert.assertEquals(jsonExpectedContent, content);
        Mockito.inOrder(applicationService).verify(applicationService, calls(1)).save(any(ApplicationResource.class));

    }

    //TODO: Change this to AutosaveElementException
    @Test
     public void testSaveFormElementApplicationAttributeInvalidDay() throws Exception {
        String questionId= "application_details-startdate_day";
        String fieldName = "application.startDate.dayOfMonth";
        Long userId = loggedInUser.getId();
        String value = "35";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter a valid date.\"]}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementApplicationAttributeInvalidMonth() throws Exception {
        String questionId= "application_details-startdate_month";
        String fieldName = "application.startDate.monthValue";
        Long userId = loggedInUser.getId();
        String value = "13";

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", fieldName)
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        log.info("Response : "+ content);

        String jsonExpectedContent = "{\"success\":\"false\",\"validation_errors\":[\"Please enter a valid date.\"]}";
        Assert.assertEquals(jsonExpectedContent, content);
    }

    @Test
    public void testSaveFormElementApplicationAttributeValidYear() throws Exception {

        String questionId = "application_details-startdate_year";
        Long userId = loggedInUser.getId();
        String value = "2015";

        when(sectionService.getById(anyLong())).thenReturn(null);
        when(sectionService.getByName("Your finances")).thenReturn(newSectionResource().with(s -> s.setId(1L)).build());

        MvcResult result = mockMvc.perform(
                post("/application/" + application.getId().toString() + "/form/saveFormElement")
                        .param("formInputId", questionId)
                        .param("fieldName", "question[" + questionId + "]")
                        .param("value", value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testDeleteCost() throws Exception {
        String sectionId = "1";
        Long costId = 1L;


        mockMvc.perform(
                post("/application/{applicationId}/form/section/{sectionId}", application.getId(), sectionId)
                        .param("remove_cost", String.valueOf(costId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/application/"+application.getId()+"/form/section/" + sectionId));

        // verify that the method is called to send the data to the data services.
        Mockito.inOrder(costService).verify(costService, calls(1)).delete(costId);
    }

//    @Test
//    public void testAssignQuestion() throws Exception {
//        Model model = new RequestModel;
//        applicationFormController.assignQuestion(model, application.getId(), sectionId);
//    }

}