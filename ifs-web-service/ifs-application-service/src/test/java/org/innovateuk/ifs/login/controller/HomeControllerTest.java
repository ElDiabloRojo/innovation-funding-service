package org.innovateuk.ifs.login.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.commons.security.authentication.user.UserAuthentication;
import org.innovateuk.ifs.login.form.RoleSelectionForm;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.util.CookieUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import static org.innovateuk.ifs.CookieTestUtil.setupCookieUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class HomeControllerTest extends BaseControllerMockMVCTest<HomeController> {

    @Mock
    private CookieUtil cookieUtil;

    @Override
    protected HomeController supplyControllerUnderTest() {
        return new HomeController();
    }

    @Before
    public void setUpCookies() {
        setupCookieUtil(cookieUtil);
    }

    /**
     * Test if you are redirected to the login page, when you visit the root url http://<domain>/
     */
    @Test
    public void home() throws Exception {

        setLoggedInUser(null);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void homeEmptyAuth() throws Exception {
        setLoggedInUserAuthentication(null);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void homeNotAuthenticated() throws Exception {
        UserAuthentication userAuth = new UserAuthentication(null);
        userAuth.setAuthenticated(false);
        setLoggedInUserAuthentication(userAuth);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void homeLoggedInApplicant() throws Exception {
        setLoggedInUser(applicant);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/applicant/dashboard"));
    }

    @Test
    public void homeLoggedInAssessor() throws Exception {
        setLoggedInUser(assessor);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/assessment/assessor/dashboard"));
    }

    @Test
    public void homeLoggedInStakeholder() throws Exception {
        setLoggedInUser(stakeholder);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/management/dashboard"));
    }

    @Test
    public void homeLoggedInWithoutRoles() throws Exception {
        setLoggedInUser(new UserResource());

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/dashboard"));
    }

    @Test
    public void homeLoggedInDualRoleAssessor() throws Exception {
        setLoggedInUser(assessorAndApplicant);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/roleSelection"));
    }

    @Test
    public void homeLoggedInMultipleRoleStakeholder() throws Exception {
        setLoggedInUser(assessorAndApplicantAndStakeholder);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/roleSelection"));
    }

    @Test
    public void roleChoice() throws Exception {
        setLoggedInUser(stakeholderAndAssessor);

        mockMvc.perform(get("/roleSelection"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/multiple-user-choice"));
    }

    @Test
    public void roleChoiceNotAuthenticated() throws Exception {
        UserAuthentication userAuth = new UserAuthentication(null);
        userAuth.setAuthenticated(false);
        setLoggedInUserAuthentication(userAuth);

        mockMvc.perform(get("/roleSelection"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void roleSelectionWithSingleRoleUser() throws Exception {
        setLoggedInUser(applicant);

        mockMvc.perform(get("/roleSelection"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @Test
    public void roleSelectionAssessor() throws Exception {
        setLoggedInUser(assessorAndApplicant);
        Role selectedRole = Role.ASSESSOR;

        mockMvc.perform(post("/roleSelection")
                .param("selectedRole", selectedRole.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/assessment/assessor/dashboard"))
                .andReturn();
    }

    @Test
    public void roleSelectionApplicant() throws Exception {
        setLoggedInUser(assessorAndApplicant);
        Role selectedRole = Role.APPLICANT;

        mockMvc.perform(post("/roleSelection")
                .param("selectedRole", selectedRole.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/applicant/dashboard"))
                .andReturn();
    }

    @Test
    public void roleSelectionStakeholder() throws Exception {
        setLoggedInUser(stakeholderAndApplicant);
        Role selectedRole = Role.STAKEHOLDER;

        mockMvc.perform(post("/roleSelection")
                .param("selectedRole", selectedRole.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/management/dashboard"))
                .andReturn();
    }

    @Test
    public void roleNotSelected() throws Exception {
        setLoggedInUser(assessorAndApplicant);
        RoleSelectionForm expectedForm = new RoleSelectionForm();
        expectedForm.setSelectedRole(null);

        MvcResult result = mockMvc.perform(post("/roleSelection"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("form", expectedForm))
                .andExpect(model().attributeExists("model"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("form", "selectedRole"))
                .andExpect(view().name("login/multiple-user-choice"))
                .andReturn();

        RoleSelectionForm form = (RoleSelectionForm) result.getModelAndView().getModel().get("form");
        assertEquals(null, form.getSelectedRole());

        BindingResult bindingResult = form.getBindingResult();
        assertEquals(0, bindingResult.getGlobalErrorCount());
        assertEquals(1, bindingResult.getFieldErrorCount());
        assertTrue(bindingResult.hasFieldErrors("selectedRole"));
        assertEquals("Please select a role.", bindingResult.getFieldError("selectedRole").getDefaultMessage());
    }
}
