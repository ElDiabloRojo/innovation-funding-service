package org.innovateuk.ifs.invite.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.invite.domain.InviteOrganisation;
import org.innovateuk.ifs.invite.mapper.InviteOrganisationMapper;
import org.innovateuk.ifs.invite.repository.InviteOrganisationRepository;
import org.innovateuk.ifs.invite.resource.InviteOrganisationResource;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.Optional;

import static org.innovateuk.ifs.invite.builder.InviteOrganisationBuilder.newInviteOrganisation;
import static org.innovateuk.ifs.invite.builder.InviteOrganisationResourceBuilder.newInviteOrganisationResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

public class InviteOrganisationServiceImplTest extends BaseServiceUnitTest<InviteOrganisationServiceImpl> {

    @Override
    protected InviteOrganisationServiceImpl supplyServiceUnderTest() {
        return new InviteOrganisationServiceImpl();
    }

    @Mock
    private InviteOrganisationRepository inviteOrganisationRepositoryMock;

    @Mock
    private InviteOrganisationMapper inviteOrganisationMapperMock;

    @Test
    public void getById() throws Exception {
        InviteOrganisation inviteOrganisation = newInviteOrganisation().build();
        InviteOrganisationResource inviteOrganisationResource = newInviteOrganisationResource().build();

        when(inviteOrganisationRepositoryMock.findById(inviteOrganisation.getId())).thenReturn(Optional.of(inviteOrganisation));
        when(inviteOrganisationMapperMock.mapToResource(inviteOrganisation)).thenReturn(inviteOrganisationResource);

        ServiceResult<InviteOrganisationResource> result = service.getById(inviteOrganisation.getId());
        assertEquals(inviteOrganisationResource, result.getSuccess());

        InOrder inOrder = inOrder(inviteOrganisationRepositoryMock, inviteOrganisationMapperMock);
        inOrder.verify(inviteOrganisationRepositoryMock).findById(inviteOrganisation.getId());
        inOrder.verify(inviteOrganisationMapperMock).mapToResource(inviteOrganisation);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void getByOrganisationIdWithInvitesForApplication() throws Exception {
        long organisationId = 1L;
        long applicationId = 2L;

        InviteOrganisation inviteOrganisation = newInviteOrganisation().build();
        InviteOrganisationResource inviteOrganisationResource = newInviteOrganisationResource().build();

        when(inviteOrganisationRepositoryMock.findOneByOrganisationIdAndInvitesApplicationId(organisationId, applicationId)).thenReturn(inviteOrganisation);
        when(inviteOrganisationMapperMock.mapToResource(inviteOrganisation)).thenReturn(inviteOrganisationResource);

        ServiceResult<InviteOrganisationResource> result = service.getByOrganisationIdWithInvitesForApplication(organisationId, applicationId);
        assertEquals(inviteOrganisationResource, result.getSuccess());

        InOrder inOrder = inOrder(inviteOrganisationRepositoryMock, inviteOrganisationMapperMock);
        inOrder.verify(inviteOrganisationRepositoryMock).findOneByOrganisationIdAndInvitesApplicationId(organisationId, applicationId);
        inOrder.verify(inviteOrganisationMapperMock).mapToResource(inviteOrganisation);
        inOrder.verifyNoMoreInteractions();
    }
}