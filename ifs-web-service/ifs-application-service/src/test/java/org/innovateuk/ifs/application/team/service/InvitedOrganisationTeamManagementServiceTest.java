package org.innovateuk.ifs.application.team.service;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.team.form.ApplicantInviteForm;
import org.innovateuk.ifs.application.team.form.ApplicationTeamUpdateForm;
import org.innovateuk.ifs.application.team.populator.ApplicationTeamManagementModelPopulator;
import org.innovateuk.ifs.application.team.viewmodel.ApplicationTeamManagementViewModel;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.innovateuk.ifs.invite.service.InviteOrganisationRestService;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.invite.builder.ApplicationInviteResourceBuilder.newApplicationInviteResource;
import static org.innovateuk.ifs.invite.builder.InviteOrganisationResourceBuilder.newInviteOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class InvitedOrganisationTeamManagementServiceTest extends BaseServiceUnitTest<InvitedOrganisationTeamManagementService> {

    @Mock
    private ApplicationTeamManagementModelPopulator applicationTeamManagementModelPopulator;

    @Mock
    private InviteRestService inviteRestServiceMock;

    @Mock
    private InviteOrganisationRestService inviteOrganisationRestServiceMock;

    private long applicationId = 123L;
    private long inviteOrganisationId = 456L;

    protected InvitedOrganisationTeamManagementService supplyServiceUnderTest() {
        return new InvitedOrganisationTeamManagementService();
    }

    @Test
    public void createViewModel_populatorShouldBeCalledWithAppropriateParameters() throws Exception {
        UserResource userResource = newUserResource().build();

        ApplicationTeamManagementViewModel expectedModel = new ApplicationTeamManagementViewModel(applicationId,
                2L,
                null,
                3L,
                4L,
                null,
                false,
                false,
                null ,
                false);

        when(applicationTeamManagementModelPopulator.populateModelByInviteOrganisationId(applicationId, inviteOrganisationId, userResource.getId())).thenReturn(expectedModel);

        ApplicationTeamManagementViewModel result = service.createViewModel(applicationId, inviteOrganisationId, userResource);

        assertEquals(expectedModel, result);
    }

    @Test
    public void executeStagedInvite_callSaveInviteShouldBeCalledWithCorrectlyMappedInvite() throws Exception {
        String email = "email@test.test";
        String name = "firstname";

        ApplicantInviteForm inviteForm = new ApplicantInviteForm();
        inviteForm.setEmail(email);
        inviteForm.setName(name);

        ApplicationTeamUpdateForm form = new ApplicationTeamUpdateForm();
        form.setStagedInvite(inviteForm);

        ApplicationInviteResource expectedInviteResource = newApplicationInviteResource()
                .withId((Long) null)
                .withEmail(email)
                .withName(name)
                .withApplication(applicationId)
                .withInviteOrganisation(inviteOrganisationId)
                .withHash().build();

        when(inviteRestServiceMock.saveInvites(any())).thenReturn(RestResult.restSuccess());

        ServiceResult<Void> result = service.executeStagedInvite(applicationId, inviteOrganisationId, form);

        assertTrue(result.isSuccess());
        verify(inviteRestServiceMock, times(1)).saveInvites(singletonList(expectedInviteResource));
    }

    @Test
    public void validateOrganisationAndApplicationIds_trueShouldBeReturnedWhenInviteOrganisationContainsInvitesForApplication() throws Exception {

        List<ApplicationInviteResource> inviteResources = newApplicationInviteResource().withApplication(applicationId).build(5);

        InviteOrganisationResource inviteOrganisationResource = newInviteOrganisationResource().withInviteResources(inviteResources).build();

        when(inviteOrganisationRestServiceMock.getById(inviteOrganisationId)).thenReturn(RestResult.restSuccess(inviteOrganisationResource));

        boolean result = service.applicationAndOrganisationIdCombinationIsValid(applicationId, inviteOrganisationId);

        assertTrue(result);
    }

    @Test
    public void validateOrganisationAndApplicationIds_falseShouldBeReturnedWhenInviteOrganisationLacksInvitesForApplication() throws Exception {
        List<ApplicationInviteResource> inviteResources = newApplicationInviteResource().withApplication(2L).build(5);

        InviteOrganisationResource inviteOrganisationResource = newInviteOrganisationResource().withInviteResources(inviteResources).build();

        when(inviteOrganisationRestServiceMock.getById(inviteOrganisationId)).thenReturn(RestResult.restSuccess(inviteOrganisationResource));

        boolean result = service.applicationAndOrganisationIdCombinationIsValid(applicationId, inviteOrganisationId);

        assertFalse(result);
    }

    @Test(expected=RuntimeException.class)
    public void validateOrganisationAndApplicationIds_exceptionShouldBeThrownWhenTheOrganisationIsNotFound() throws Exception {
        when(inviteOrganisationRestServiceMock.getById(inviteOrganisationId)).thenReturn(RestResult.restFailure(new Error("BAD_REQUEST", BAD_REQUEST)));

        service.applicationAndOrganisationIdCombinationIsValid(applicationId, inviteOrganisationId);
    }

    @Test
    public void getInviteIds_foundIdsShouldBeMappedToReturnedList() throws Exception {
        List<ApplicationInviteResource> inviteResources = newApplicationInviteResource().withId(1L,2L,3L,4L,5L).withApplication(2L).build(5);

        InviteOrganisationResource inviteOrganisationResource = newInviteOrganisationResource().withInviteResources(inviteResources).build();

        when(inviteOrganisationRestServiceMock.getById(inviteOrganisationId)).thenReturn(RestResult.restSuccess(inviteOrganisationResource));

        List<Long> result = service.getInviteIds(applicationId, inviteOrganisationId);

        assertTrue(inviteOrganisationResource.getInviteResources().stream().allMatch(invite -> result.contains(invite.getId())));
    }

    @Test
    public void getInviteIds_noIdsFoundShouldReturnEmptyList() throws Exception {
        InviteOrganisationResource inviteOrganisationResource = newInviteOrganisationResource().build();

        when(inviteOrganisationRestServiceMock.getById(inviteOrganisationId)).thenReturn(RestResult.restSuccess(inviteOrganisationResource));

        List<Long> result = service.getInviteIds(applicationId, inviteOrganisationId);

        assertTrue(result.isEmpty());

    }

    @Test(expected=RuntimeException.class)
    public void getInviteIds_organisationNotFoundShouldReturnException() throws Exception {
        when(inviteOrganisationRestServiceMock.getById(inviteOrganisationId)).thenReturn(RestResult.restFailure(new Error("BAD_REQUEST", BAD_REQUEST)));

        service.getInviteIds(applicationId, inviteOrganisationId);
    }
}