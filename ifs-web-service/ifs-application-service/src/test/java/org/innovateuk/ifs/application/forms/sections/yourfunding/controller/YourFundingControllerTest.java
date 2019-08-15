package org.innovateuk.ifs.application.forms.sections.yourfunding.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.forms.sections.yourfunding.form.OtherFundingRowForm;
import org.innovateuk.ifs.application.forms.sections.yourfunding.form.YourFundingPercentageForm;
import org.innovateuk.ifs.application.forms.sections.yourfunding.populator.YourFundingFormPopulator;
import org.innovateuk.ifs.application.forms.sections.yourfunding.populator.YourFundingViewModelPopulator;
import org.innovateuk.ifs.application.forms.sections.yourfunding.saver.YourFundingSaver;
import org.innovateuk.ifs.application.forms.sections.yourfunding.validator.YourFundingFormValidator;
import org.innovateuk.ifs.application.forms.sections.yourfunding.viewmodel.ManagementYourFundingViewModel;
import org.innovateuk.ifs.application.forms.sections.yourfunding.viewmodel.YourFundingViewModel;
import org.innovateuk.ifs.application.service.SectionStatusRestService;
import org.innovateuk.ifs.form.resource.SectionType;
import org.innovateuk.ifs.user.service.UserRestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.innovateuk.ifs.application.forms.ApplicationFormUtil.APPLICATION_BASE_URL;
import static org.innovateuk.ifs.commons.error.ValidationMessages.noErrors;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class YourFundingControllerTest extends BaseControllerMockMVCTest<YourFundingController> {

    @Override
    protected YourFundingController supplyControllerUnderTest() {
        return new YourFundingController();
    }

    private static final long APPLICATION_ID = 1L;
    private static final long SECTION_ID = 2L;
    private static final long PROCESS_ROLE_ID = 3L;
    private static final String VIEW = "application/your-funding";

    @Mock
    private YourFundingFormPopulator formPopulator;

    @Mock
    private YourFundingViewModelPopulator viewModelPopulator;

    @Mock
    private YourFundingSaver saver;

    @Mock
    private SectionStatusRestService sectionStatusRestService;

    @Mock
    private UserRestService userRestService;

    @Mock
    private YourFundingFormValidator yourFundingFormValidator;

    @Test
    public void viewYourFunding() throws Exception {
        YourFundingViewModel viewModel = mockUnlockedViewModel();

        mockMvc.perform(get(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID))
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name(VIEW))
                .andExpect(status().isOk());

        verify(formPopulator).populateForm(APPLICATION_ID, getLoggedInUser(), empty());
    }

    @Test
    public void managementViewYourFunding() throws Exception {
        long organisationId = 5L;
        ManagementYourFundingViewModel viewModel = mock(ManagementYourFundingViewModel.class);

        when(viewModelPopulator.populateManagement(APPLICATION_ID, SECTION_ID, organisationId))
                .thenReturn(viewModel);

        mockMvc.perform(get(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}/{organisationId}",
                APPLICATION_ID, SECTION_ID, organisationId))
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name(VIEW))
                .andExpect(status().isOk());

        verify(formPopulator).populateForm(APPLICATION_ID, getLoggedInUser(), of(organisationId));
    }

    @Test
    public void viewYourFunding_sectionIsLocked() throws Exception {
        YourFundingViewModel viewModel = mock(YourFundingViewModel.class);
        when(viewModelPopulator.populate(APPLICATION_ID, SECTION_ID, getLoggedInUser())).thenReturn(viewModel);
        when(viewModel.isFundingSectionLocked()).thenReturn(true);

        mockMvc.perform(get(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID))
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name(VIEW))
                .andExpect(status().isOk());

        verifyZeroInteractions(formPopulator);
    }

    @Test
    public void saveYourFunding() throws Exception {
        when(saver.save(eq(APPLICATION_ID), any(YourFundingPercentageForm.class), eq(getLoggedInUser()))).thenReturn(serviceSuccess());

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID)
                .param("requestingFunding", "true")
                .param("grantClaimPercentage", "100")
                .param("otherFunding", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(String.format("/application/%s/form/%s", APPLICATION_ID, SectionType.FINANCE)));
    }

    @Test
    public void edit() throws Exception {
        YourFundingViewModel viewModel = mockUnlockedViewModel();
        when(userRestService.findProcessRole(APPLICATION_ID, getLoggedInUser().getId()))
                .thenReturn(restSuccess(newProcessRoleResource().withId(PROCESS_ROLE_ID).build()));
        when(sectionStatusRestService.markAsInComplete(SECTION_ID, APPLICATION_ID, PROCESS_ROLE_ID)).thenReturn(restSuccess());

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID)
                .param("edit", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(String.format("%s%s/form/your-funding/%s", APPLICATION_BASE_URL, APPLICATION_ID, SECTION_ID)));

        verifyZeroInteractions(saver);
        verify(sectionStatusRestService).markAsInComplete(SECTION_ID, APPLICATION_ID, PROCESS_ROLE_ID);
    }

    @Test
    public void complete() throws Exception {
        when(saver.save(eq(APPLICATION_ID), any(YourFundingPercentageForm.class), eq(getLoggedInUser()))).thenReturn(serviceSuccess());
        when(userRestService.findProcessRole(APPLICATION_ID, getLoggedInUser().getId()))
                .thenReturn(restSuccess(newProcessRoleResource().withId(PROCESS_ROLE_ID).build()));
        when(sectionStatusRestService.markAsComplete(SECTION_ID, APPLICATION_ID, PROCESS_ROLE_ID)).thenReturn(restSuccess(noErrors()));

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID)
                .param("complete", "true")
                .param("grantClaimPercentage", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(String.format("/application/%s/form/%s", APPLICATION_ID, SectionType.FINANCE)));

        verify(saver).save(eq(APPLICATION_ID), any(YourFundingPercentageForm.class), eq(getLoggedInUser()));
        verify(sectionStatusRestService).markAsComplete(SECTION_ID, APPLICATION_ID, PROCESS_ROLE_ID);
    }


    @Test
    public void complete_error() throws Exception {
        YourFundingViewModel viewModel = mockUnlockedViewModel();
        doAnswer((invocationOnMock) -> {
            ((BindingResult) invocationOnMock.getArguments()[1]).rejectValue("requestingFunding", "something");
            return Void.class;
        }).when(yourFundingFormValidator).validate(any(), any(), eq(getLoggedInUser()), eq(APPLICATION_ID));

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID)
                .param("complete", "true")
                .param("grantClaimPercentage", "100"))
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name(VIEW))
                .andExpect(status().isOk());

        verifyZeroInteractions(saver);
    }


    @Test
    public void addFundingRowFormPost() throws Exception {
        YourFundingViewModel viewModel = mockUnlockedViewModel();

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID)
                .param("add_cost", "true"))
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name(VIEW))
                .andExpect(status().isOk());

        verify(saver).addOtherFundingRow(any());
    }

    @Test
    public void removeFundingRowFormPost() throws Exception {
        YourFundingViewModel viewModel = mockUnlockedViewModel();
        String rowToRemove = "5";

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}",
                APPLICATION_ID, SECTION_ID)
                .param("remove_cost", rowToRemove))
                .andExpect(model().attribute("model", viewModel))
                .andExpect(view().name(VIEW))
                .andExpect(status().isOk());

        verify(saver).removeOtherFundingRowForm(any(), eq(rowToRemove));
    }

    @Test
    public void autoSave() throws Exception {
        String field = "field";
        String value = "value";
        String fieldId = "123";

        when(saver.autoSave(field, value, APPLICATION_ID, getLoggedInUser())).thenReturn(Optional.of(Long.valueOf(fieldId)));
        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}/auto-save",
                APPLICATION_ID, SECTION_ID)
                .param("field", field)
                .param("value", value))
                .andExpect(jsonPath("$.fieldId", equalTo(Integer.valueOf(fieldId))))
                .andExpect(status().isOk());

    }

    @Test
    public void ajaxRemoveRow() throws Exception {
        String costId = "123";

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}/remove-row/{rowId}",
                APPLICATION_ID, SECTION_ID, costId))
                .andExpect(status().isOk());

        verify(saver).removeOtherFundingRow(costId);
    }

    @Test
    public void ajaxAddRow() throws Exception {
        String rowId = "123";
        OtherFundingRowForm row = new OtherFundingRowForm();
        row.setCostId(Long.valueOf(rowId));

        doAnswer((invocation) -> {
            YourFundingPercentageForm form = (YourFundingPercentageForm) invocation.getArguments()[0];
            form.getOtherFundingRows().put(rowId, row);
            return null;
        }).when(saver).addOtherFundingRow(any());

        mockMvc.perform(post(APPLICATION_BASE_URL + "{applicationId}/form/your-funding/{sectionId}/add-row",
                APPLICATION_ID, SECTION_ID))
                .andExpect(view().name("application/your-funding-fragments :: ajax_other_funding_row"))
                .andExpect(model().attribute("row", row))
                .andExpect(model().attribute("id", rowId))
                .andExpect(status().isOk());

        verify(saver).addOtherFundingRow(any());
    }


    private YourFundingViewModel mockUnlockedViewModel() {
        YourFundingViewModel viewModel = mock(YourFundingViewModel.class);
        when(viewModelPopulator.populate(APPLICATION_ID, SECTION_ID, getLoggedInUser())).thenReturn(viewModel);
        when(viewModel.isFundingSectionLocked()).thenReturn(false);
        return viewModel;
    }
}