package org.innovateuk.ifs.project.grantofferletter.transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.UserNotificationTarget;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.mapper.OrganisationMapper;
import org.innovateuk.ifs.organisation.repository.OrganisationRepository;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.project.core.builder.PartnerOrganisationBuilder;
import org.innovateuk.ifs.project.core.domain.PartnerOrganisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.innovateuk.ifs.project.core.workflow.configuration.ProjectWorkflowHandler;
import org.innovateuk.ifs.project.financechecks.repository.CostRepository;
import org.innovateuk.ifs.project.grantofferletter.configuration.workflow.GrantOfferLetterWorkflowHandler;
import org.innovateuk.ifs.project.grantofferletter.model.*;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterApprovalResource;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterEvent;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterState;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterStateResource;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.project.spendprofile.domain.SpendProfile;
import org.innovateuk.ifs.project.spendprofile.repository.SpendProfileRepository;
import org.innovateuk.ifs.project.spendprofile.transactional.SpendProfileService;
import org.innovateuk.ifs.user.builder.UserBuilder;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.util.EmailService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.innovateuk.ifs.address.builder.AddressBuilder.newAddress;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_ALREADY_COMPLETE;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.file.builder.FileEntryBuilder.newFileEntry;
import static org.innovateuk.ifs.file.builder.FileEntryResourceBuilder.newFileEntryResource;
import static org.innovateuk.ifs.finance.builder.ApplicationFinanceResourceBuilder.newApplicationFinanceResource;
import static org.innovateuk.ifs.invite.builder.ProjectInviteBuilder.newProjectInvite;
import static org.innovateuk.ifs.invite.domain.ProjectParticipantRole.*;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.RESEARCH;
import static org.innovateuk.ifs.project.core.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.financecheck.builder.CostBuilder.newCost;
import static org.innovateuk.ifs.project.grantofferletter.builder.GrantOfferLetterAcademicFinanceTableBuilder.newGrantOfferLetterAcademicFinanceTable;
import static org.innovateuk.ifs.project.grantofferletter.builder.GrantOfferLetterFinanceTotalsTableBuilder.newGrantOfferLetterFinanceTotalsTable;
import static org.innovateuk.ifs.project.grantofferletter.builder.GrantOfferLetterIndustrialFinanceTableBuilder.newGrantOfferLetterIndustrialFinanceTable;
import static org.innovateuk.ifs.project.spendprofile.builder.SpendProfileBuilder.newSpendProfile;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GrantOfferLetterServiceImplTest extends BaseServiceUnitTest<GrantOfferLetterService> {

    private static final String GRANT_OFFER_LETTER_DATE_FORMAT = "d MMMM yyyy";

    private Long projectId = 123L;
    private Long applicationId = 456L;
    private Long userId = 7L;
    private Application application;
    private List<Organisation> organisations;
    private Organisation nonAcademicUnfunded;
    private User user;
    private ProcessRole leadApplicantProcessRole;
    private ProjectUser leadPartnerProjectUser;
    private Project project;
    private List<OrganisationResource> organisationResources;
    private GrantOfferLetterIndustrialFinanceTable industrialFinanceTable;
    private GrantOfferLetterAcademicFinanceTable academicFinanceTable;
    private GrantOfferLetterFinanceTotalsTable totalsTable;

    @Mock
    private EmailService projectEmailService;

    private Address address;

    private FileEntryResource fileEntryResource;

    private FileEntry createdFile;

    private String htmlFile;

    private Pair<File, FileEntry> fileEntryPair;

    @Captor
    ArgumentCaptor<Map<String, Object>> templateArgsCaptor;

    @Captor
    ArgumentCaptor<String> templateCaptor;

    @Captor
    ArgumentCaptor<FileEntryResource> fileEntryResCaptor;

    @Captor
    ArgumentCaptor<Supplier<InputStream>> supplierCaptor;

    @Mock
    private OrganisationRepository organisationRepositoryMock;

    @Mock
    private ProjectRepository projectRepositoryMock;

    @Mock
    private OrganisationMapper organisationMapperMock;

    @Mock
    private GrantOfferLetterWorkflowHandler golWorkflowHandlerMock;

    @Mock
    private ProjectWorkflowHandler projectWorkflowHandlerMock;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private SpendProfileService spendProfileServiceMock;

    @Mock
    private NotificationService notificationServiceMock;

    @Mock
    private SpendProfileRepository spendProfileRepositoryMock;

    @Mock
    private CostRepository costRepositoryMock;

    @Mock
    private GrantOfferLetterIndustrialFinanceTablePopulator industrialFinanceTablePopulatorMock;

    @Mock
    private GrantOfferLetterAcademicFinanceTablePopulator academicFinanceTablePopulatorMock;

    @Mock
    private GrantOfferLetterFinanceTotalsTablePopulator financeTotalsTablePopulatorMock;

    @Before
    public void setUp() {
        organisations = newOrganisation().withOrganisationType(RESEARCH).withName("Org1&", "Org2\"", "Org3<").build(3);
        nonAcademicUnfunded = newOrganisation().withOrganisationType(BUSINESS).withName("Org4").build();
        organisationResources = newOrganisationResource().build(4);

        Competition competition = newCompetition().build();

        address = newAddress().withAddressLine1("test1")
                .withAddressLine2("test2")
                .withPostcode("PST")
                .withTown("town").build();

        user = newUser().
                withId(userId).
                build();

        leadApplicantProcessRole = newProcessRole().
                withOrganisationId(organisations.get(0).getId()).
                withRole(Role.LEADAPPLICANT).
                withUser(user).
                build();

        leadPartnerProjectUser = newProjectUser().
                withOrganisation(organisations.get(0)).
                withRole(PROJECT_PARTNER).
                withUser(user).
                build();

        application = newApplication().
                withId(applicationId).
                withCompetition(competition).
                withProcessRoles(leadApplicantProcessRole).
                withName("My Application").
                withDurationInMonths(5L).
                withStartDate(LocalDate.of(2017, 3, 2)).
                build();

        PartnerOrganisation partnerOrganisation = newPartnerOrganisation().withOrganisation(organisations.get(0)).build();
        PartnerOrganisation partnerOrganisation2 = newPartnerOrganisation().withOrganisation(organisations.get(1)).build();
        PartnerOrganisation partnerOrganisation3 = newPartnerOrganisation().withOrganisation(organisations.get(2)).build();

        List<PartnerOrganisation> partnerOrganisations = new ArrayList<>();
        partnerOrganisations.add(partnerOrganisation);
        partnerOrganisations.add(partnerOrganisation2);
        partnerOrganisations.add(partnerOrganisation3);

        project = newProject().
                withId(projectId).
                withPartnerOrganisations(partnerOrganisations).
                withAddress(address).
                withApplication(application).
                withProjectUsers(singletonList(leadPartnerProjectUser)).
                build();

        SpendProfile orgSpendProfile = newSpendProfile()
                .withSpendProfileFigures(singletonList(newCost().build()))
                .build();

        industrialFinanceTable = newGrantOfferLetterIndustrialFinanceTable().build();
        academicFinanceTable = newGrantOfferLetterAcademicFinanceTable().build();
        totalsTable = newGrantOfferLetterFinanceTotalsTable().build();

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(organisationRepositoryMock.findById(organisations.get(0).getId())).thenReturn(Optional.of(organisations.get(0)));
        when(organisationRepositoryMock.findById(organisations.get(1).getId())).thenReturn(Optional.of(organisations.get(1)));
        when(organisationRepositoryMock.findById(organisations.get(2).getId())).thenReturn(Optional.of(organisations.get(2)));
        when(organisationMapperMock.mapToResource(organisations.get(0))).thenReturn(organisationResources.get(0));
        when(organisationMapperMock.mapToResource(organisations.get(1))).thenReturn(organisationResources.get(1));
        when(organisationMapperMock.mapToResource(organisations.get(2))).thenReturn(organisationResources.get(2));
        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(anyLong(), anyLong())).thenReturn(Optional.of(orgSpendProfile));
        when(costRepositoryMock.findByCostGroupId(anyLong())).thenReturn(singletonList(newCost().build()));
        when(industrialFinanceTablePopulatorMock.createTable(anyMap())).thenReturn(industrialFinanceTable);
        when(academicFinanceTablePopulatorMock.createTable(anyMap())).thenReturn(academicFinanceTable);
        when(financeTotalsTablePopulatorMock.createTable(anyMap(), anyLong())).thenReturn(totalsTable);
    }

    @Test
    public void testCreateSignedGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getSignedGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createSignedGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testCreateGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testCreateAdditionalContractFileEntry() {
        assertCreateFile(
                project::getAdditionalContractFile,
                (fileToCreate, inputStreamSupplier) ->
                        service.createAdditionalContractFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void testGetAdditionalContractFileEntryDetails() {
        assertGetFileDetails(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileEntryDetails(123L));
    }

    @Test
    public void testGetGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void testGetSignedGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void testGetAdditionalContractFileContents() {
        assertGetFileContents(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileAndContents(123L));
    }

    @Test
    public void testGetGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void testGetSignedGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntry() {
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.TRUE);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        assertUpdateFile(
                project::getSignedGrantOfferLetter,
                (fileToUpdate, inputStreamSupplier) ->
                        service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntryProjectLive() {

        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(projectWorkflowHandlerMock.getState(any())).thenReturn(ProjectState.LIVE);
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    @Test
    public void testUpdateSignedGrantOfferLetterFileEntryGolNotSent() {

        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(projectWorkflowHandlerMock.getState(any())).thenReturn(ProjectState.SETUP);
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier);
        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), CommonFailureKeys.GRANT_OFFER_LETTER_MUST_BE_SENT_BEFORE_UPLOADING_SIGNED_COPY.toString());
    }

    @Test
    public void testSubmitGrantOfferLetterFailureNoSignedGolFile() {

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.SIGNED_GRANT_OFFER_LETTER_MUST_BE_UPLOADED_BEFORE_SUBMIT));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void testSubmitGrantOfferLetterFailureCannotReachSignedState() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_CANNOT_SET_SIGNED_STATE));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void testSubmitGrantOfferLetterSuccess() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(Boolean.TRUE);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.isSuccess());
        Assert.assertThat(project.getOfferSubmittedDate(), notNullValue());
    }

    @Test
    public void testGenerateGrantOfferLetter() {
        assertGenerateFile(
                fileEntryResource ->
                        service.generateGrantOfferLetter(123L, fileEntryResource));

        verify(spendProfileRepositoryMock, times(3)).findOneByProjectIdAndOrganisationId(anyLong(), anyLong());
        verify(costRepositoryMock, times(3)).findByCostGroupId(nullable(Long.class));
        verify(industrialFinanceTablePopulatorMock).createTable(anyMap());
        verify(academicFinanceTablePopulatorMock).createTable(anyMap());
        verify(financeTotalsTablePopulatorMock).createTable(anyMap(), anyLong());
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntry() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findById(internalUserResource.getId())).thenReturn(Optional.of(internalUser));
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(true);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingGOLFile.getId())).thenReturn(serviceSuccess(existingGOLFile));

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isSuccess());
        assertNull(project.getGrantOfferLetter());

        verify(golWorkflowHandlerMock).removeGrantOfferLetter(project, internalUser);
        verify(fileServiceMock).deleteFileIgnoreNotFound(existingGOLFile.getId());
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntryProjectLive() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findById(internalUserResource.getId())).thenReturn(Optional.of(internalUser));
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(true);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.LIVE);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingGOLFile.getId())).thenReturn(serviceSuccess(existingGOLFile));

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    @Test
    public void testRemoveGrantOfferLetterFileEntryButWorkflowRejected() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findById(internalUserResource.getId())).thenReturn(Optional.of(internalUser));
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(false);

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_CANNOT_BE_REMOVED));
        assertEquals(existingGOLFile, project.getGrantOfferLetter());

        verify(golWorkflowHandlerMock).removeGrantOfferLetter(project, internalUser);
        verify(fileServiceMock, never()).deleteFile(existingGOLFile.getId());
    }

    @Test
    public void testRemoveSignedGrantOfferLetterFileEntry() {

        UserResource externalUser = newUserResource().build();
        setLoggedInUser(externalUser);

        FileEntry existingSignedGOLFile = newFileEntry().build();
        project.setSignedGrantOfferLetter(existingSignedGOLFile);

        when(userRepositoryMock.findById(externalUser.getId())).thenReturn(Optional.of(user));
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingSignedGOLFile.getId())).thenReturn(serviceSuccess(existingSignedGOLFile));
        when(golWorkflowHandlerMock.removeSignedGrantOfferLetter(project, user)).thenReturn(true);

        ServiceResult<Void> result = service.removeSignedGrantOfferLetterFileEntry(123L);
        assertTrue(result.isSuccess());
        assertNull(project.getSignedGrantOfferLetter());

        verify(golWorkflowHandlerMock).removeSignedGrantOfferLetter(project, user);
        verify(fileServiceMock).deleteFileIgnoreNotFound(existingSignedGOLFile.getId());
    }

    @Test
    public void testRemoveSignedGrantOfferLetterFileEntryProjectLive() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingSignedGOLFile = newFileEntry().build();
        project.setSignedGrantOfferLetter(existingSignedGOLFile);

        when(userRepositoryMock.findById(internalUserResource.getId())).thenReturn(Optional.of(internalUser));
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.LIVE);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingSignedGOLFile.getId())).thenReturn(serviceSuccess(existingSignedGOLFile));

        ServiceResult<Void> result = service.removeSignedGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    private final Organisation organisation(OrganisationTypeEnum type, String name) {
        return newOrganisation()
                .withOrganisationType(type)
                .withName(name)
                .build();
    }

    @Test
    public void testGenerateGrantOfferLetterIfReadySuccess() {

        setupGolTemplate();

        Organisation o1 = organisation(BUSINESS, "OrgLeader&");
        Organisation o2 = organisation(BUSINESS, "Org2\"");
        Organisation o3 = organisation(BUSINESS, "Org3<");

        ApplicationFinanceResource applicationFinanceResource = newApplicationFinanceResource()
                .withGrantClaimPercentage(30)
                .withApplication(456L)
                .withOrganisation(3L)
                .build();

        setupOrganisationsForGrantOfferLetter(o1, o2, o3, applicationFinanceResource, applicationFinanceResource, applicationFinanceResource);

        Map<String, Object> templateArgs = setupTemplateArguments();

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);

        verify(rendererMock).renderTemplate(templateCaptor.capture(), templateArgsCaptor.capture());
        verify(fileServiceMock).createFile(fileEntryResCaptor.capture(), supplierCaptor.capture());

        verify(spendProfileRepositoryMock, times(3)).findOneByProjectIdAndOrganisationId(anyLong(), anyLong());
        verify(costRepositoryMock, times(3)).findByCostGroupId(nullable(Long.class));
        verify(industrialFinanceTablePopulatorMock).createTable(anyMap());
        verify(academicFinanceTablePopulatorMock).createTable(anyMap());
        verify(financeTotalsTablePopulatorMock).createTable(anyMap(), anyLong());

        assertTrue(checkGolTemplate());
        assertTrue(result.isSuccess());
        assertTrue(compareTemplate(templateArgs, templateArgsCaptor.getAllValues().get(0)));
    }

    @Test
    public void testGenerateGrantOfferLetterFailureSpendProfilesNotApproved() {
        when(projectRepositoryMock.findById(123L)).thenReturn(Optional.of(project));
        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.REJECTED));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGenerateGrantOfferLetterOtherDocsNotApproved() {

        Competition comp = newCompetition().withName("Test Comp").build();
        Organisation o1 = newOrganisation().withName("OrgLeader").build();
        User u = newUser().withFirstName("ab").withLastName("cd").build();
        ProcessRole leadAppProcessRole = newProcessRole().withOrganisationId(o1.getId()).withUser(u).withRole(Role.LEADAPPLICANT).build();
        Application app = newApplication().withCompetition(comp).withProcessRoles(leadAppProcessRole).withId(3L).build();
        ProjectUser pm = newProjectUser().withRole(PROJECT_MANAGER).withOrganisation(o1).build();
        PartnerOrganisation po = PartnerOrganisationBuilder.newPartnerOrganisation().withOrganisation(o1).withLeadOrganisation(true).build();
        Project project = newProject().withOtherDocumentsApproved(ApprovalType.REJECTED).withApplication(app).withPartnerOrganisations(asList(po)).withProjectUsers(asList(pm)).withDuration(10L).build();

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findById(123L)).thenReturn(Optional.of(project));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testGenerateGrantOfferLetterNoProject() {

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findById(123L)).thenReturn(Optional.empty());

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    private Map<String, Object> setupTemplateArguments() {
        Map<String, Object> templateArgs = new HashMap();
        templateArgs.put("ProjectLength", 10L);
        templateArgs.put("ProjectTitle", "project 1");
        templateArgs.put("LeadContact", "ab cd");
        templateArgs.put("ApplicationNumber", 3L);
        templateArgs.put("LeadOrgName", "OrgLeader&");
        templateArgs.put("CompetitionName", "Test Comp<");
        templateArgs.put("Address1", "InnovateUK>");
        templateArgs.put("Address2", "Northstar House\"");
        templateArgs.put("Address3", "");
        templateArgs.put("TownCity", "Swindon&");
        templateArgs.put("PostCode", "SN1 1AA'");
        templateArgs.put("ProjectStartDate", ZonedDateTime.now().format(DateTimeFormatter.ofPattern(GRANT_OFFER_LETTER_DATE_FORMAT)));
        templateArgs.put("Date", ZonedDateTime.now().toString());
        return templateArgs;
    }

    private boolean compareTemplate(Map<String, Object> expectedTemplateArgs, Map<String, Object> templateArgs) {
        boolean result = true;
        result &= expectedTemplateArgs.get("ProjectLength").equals(templateArgs.get("ProjectLength"));
        result &= expectedTemplateArgs.get("ProjectTitle").equals(templateArgs.get("ProjectTitle"));
        result &= expectedTemplateArgs.get("ProjectStartDate").equals(templateArgs.get("ProjectStartDate"));
        result &= expectedTemplateArgs.get("LeadContact").equals(templateArgs.get("LeadContact"));
        result &= expectedTemplateArgs.get("ApplicationNumber").equals(templateArgs.get("ApplicationNumber"));
        result &= expectedTemplateArgs.get("LeadOrgName").equals(templateArgs.get("LeadOrgName"));
        result &= expectedTemplateArgs.get("CompetitionName").equals(templateArgs.get("CompetitionName"));
        result &= expectedTemplateArgs.get("Address1").equals(templateArgs.get("Address1"));
        result &= expectedTemplateArgs.get("Address2").equals(templateArgs.get("Address2"));
        result &= expectedTemplateArgs.get("Address3").equals(templateArgs.get("Address3"));
        result &= expectedTemplateArgs.get("TownCity").equals(templateArgs.get("TownCity"));
        result &= expectedTemplateArgs.get("PostCode").equals(templateArgs.get("PostCode"));
        result &= ZonedDateTime.parse((String) expectedTemplateArgs.get("Date")).isBefore(ZonedDateTime.parse((String) templateArgs.get("Date"))) || ZonedDateTime.parse((String) expectedTemplateArgs.get("Date")).isEqual(ZonedDateTime.parse((String) templateArgs.get("Date")));
        return result;
    }

    private void setupGolTemplate() {
        fileEntryResource = newFileEntryResource().
                withFilesizeBytes(1024).
                withMediaType("application/pdf").
                withName("grant_offer_letter").
                build();

        createdFile = newFileEntry().build();
        fileEntryPair = Pair.of(new File("blah"), createdFile);

        StringBuilder stringBuilder = new StringBuilder();
        htmlFile = stringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<html dir=\"ltr\" lang=\"en\">\n")
                .append("<head>\n")
                .append("<meta charset=\"UTF-8\"></meta>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("<p>\n")
                .append("${LeadContact}<br/>\n")
                .append("</p>\n")
                .append("</body>\n")
                .append("</html>\n").toString();

        when(rendererMock.renderTemplate(eq("common/grantoffer/grant_offer_letter.html"), any(Map.class))).thenReturn(ServiceResult.serviceSuccess(htmlFile));
        when(fileServiceMock.createFile(any(FileEntryResource.class), any(Supplier.class))).thenReturn(ServiceResult.serviceSuccess(fileEntryPair));
        when(fileEntryMapperMock.mapToResource(createdFile)).thenReturn(fileEntryResource);
    }

    private boolean checkGolTemplate() {
        boolean result = true;
        result &= fileEntryResource.getMediaType().equals(fileEntryResCaptor.getAllValues().get(0).getMediaType());
        result &= (fileEntryResource.getName() + ".pdf").equals(fileEntryResCaptor.getAllValues().get(0).getName());

        String startOfGeneratedFileString = null;
        try {
            int n = supplierCaptor.getAllValues().get(0).get().available();
            byte[] startOfGeneratedFile = new byte[n];
            supplierCaptor.getAllValues().get(0).get().read(startOfGeneratedFile, 0, n < 9 ? n : 9);
            startOfGeneratedFileString = new String(startOfGeneratedFile, StandardCharsets.UTF_8);
        } catch (IOException e) {

        }
        String pdfHeader = "%PDF-1.4\n";
        result &= pdfHeader.equals(startOfGeneratedFileString.substring(0, pdfHeader.length()));
        return result;
    }

    private void setupOrganisationsForGrantOfferLetter(Organisation o1, Organisation o2, Organisation o3, ApplicationFinanceResource af1, ApplicationFinanceResource af2, ApplicationFinanceResource af3) {
        Competition comp = newCompetition()
                .withName("Test Comp<")
                .build();

        User u = newUser()
                .withFirstName("ab")
                .withLastName("cd")
                .build();
        ProcessRole leadAppProcessRole = newProcessRole()
                .withOrganisationId(o1.getId())
                .withUser(u)
                .withRole(Role.LEADAPPLICANT)
                .build();
        Application app = newApplication()
                .withCompetition(comp)
                .withProcessRoles(leadAppProcessRole)
                .withId(3L)
                .build();
        ProjectUser pm = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withOrganisation(o1)
                .build();

        PartnerOrganisation po = newPartnerOrganisation()
                .withOrganisation(o1)
                .withLeadOrganisation(true)
                .build();

        PartnerOrganisation po2 = newPartnerOrganisation()
                .withOrganisation(o2)
                .withLeadOrganisation(false)
                .build();

        PartnerOrganisation po3 = newPartnerOrganisation()
                .withOrganisation(o3)
                .withLeadOrganisation(false)
                .build();

        Address address = newAddress()
                .withAddressLine1("InnovateUK>")
                .withAddressLine2("Northstar House\"")
                .withTown("Swindon&")
                .withPostcode("SN1 1AA'")
                .build();
        Project project = newProject()
                .withOtherDocumentsApproved(ApprovalType.APPROVED)
                .withName("project 1")
                .withApplication(app)
                .withPartnerOrganisations(asList(po3, po, po2))
                .withProjectUsers(asList(pm))
                .withDuration(10L)
                .withAddress(address)
                .withTargetStartDate(LocalDate.now())
                .build();

        when(projectRepositoryMock.findById(123L)).thenReturn(Optional.of(project));

        when(organisationRepositoryMock.findById(o1.getId())).thenReturn(Optional.of(o1));
        when(organisationRepositoryMock.findById(o2.getId())).thenReturn(Optional.of(o2));
        when(organisationRepositoryMock.findById(o3.getId())).thenReturn(Optional.of(o3));

    }

    @Test
    public void testSendGrantOfferLetterNoGol() {

        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        Project p = newProject()
                .withProjectUsers(pu)
                .withPartnerOrganisations(newPartnerOrganisation()
                        .withOrganisation(nonAcademicUnfunded)
                        .build(1))
                .withGrantOfferLetter(null)
                .build();

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(p));
        when(notificationServiceMock.sendNotification(any(), eq(EMAIL))).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testSendGrantOfferLetterSendFails() {

        List<ProjectUser> pu = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withUser(user)
                .withOrganisation(nonAcademicUnfunded)
                .withInvite(newProjectInvite()
                .build())
                .build(1);

        Competition competition = newCompetition()
                .withName("Competition 1")
                .build();

        Application application = newApplication()
                .withName("Application 1")
                .withCompetition(competition)
                .build();

        FileEntry golFile = newFileEntry()
                .withMediaType("application/pdf")
                .withFilesizeBytes(10)
                .build();

        Project project = newProject()
                .withProjectUsers(pu)
                .withPartnerOrganisations(newPartnerOrganisation()
                .withOrganisation(nonAcademicUnfunded)
                .build(1)).withGrantOfferLetter(golFile)
                .withApplication(application)
                .build();

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard",
                "applicationId", projectId,
                "competitionName", "Competition 1"
        );

        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.GRANT_OFFER_LETTER_PROJECT_MANAGER)).thenReturn(serviceFailure(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE));

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testSendGrantOfferLetterNoProject() {

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.empty());

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testSendGrantOfferLetterSuccess() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);

        Competition competition = newCompetition()
                .withName("Competition 1")
                .build();

        Application application = newApplication()
                .withName("Application 1")
                .withCompetition(competition)
                .build();

        Project p = newProject()
                .withProjectUsers(pu)
                .withPartnerOrganisations(newPartnerOrganisation()
                .withOrganisation(nonAcademicUnfunded)
                .build(1))
                .withGrantOfferLetter(golFile)
                .withApplication(application)
                .build();

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(p));

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard",
                "applicationId", projectId,
                "competitionName", "Competition 1"
        );

        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.GRANT_OFFER_LETTER_PROJECT_MANAGER)).thenReturn(serviceSuccess());

        User user = UserBuilder.newUser().build();
        setLoggedInUser(newUserResource().withId(user.getId()).build());
        when(userRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));

        when(golWorkflowHandlerMock.grantOfferLetterSent(p, user)).thenReturn(Boolean.TRUE);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testSendGrantOfferLetterFailure() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);

        Competition competition = newCompetition()
                .withName("Competition 1")
                .build();

        Application application = newApplication()
                .withName("Application 1")
                .withCompetition(competition)
                .build();

        Project project = newProject()
                .withProjectUsers(pu)
                .withPartnerOrganisations(newPartnerOrganisation()
                .withOrganisation(nonAcademicUnfunded)
                .build(1)).withGrantOfferLetter(golFile)
                .withApplication(application)
                .build();

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard",
                "competitionName", "Competition 1",
                "applicationId", projectId
        );

        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.GRANT_OFFER_LETTER_PROJECT_MANAGER)).thenReturn(serviceSuccess());

        User user = UserBuilder.newUser().build();
        setLoggedInUser(newUserResource().withId(user.getId()).build());
        when(userRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));

        when(golWorkflowHandlerMock.grantOfferLetterSent(project, user)).thenReturn(Boolean.FALSE);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterRejectionSuccess() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findById(u.getId())).thenReturn(Optional.of(u));
        when(golWorkflowHandlerMock.grantOfferLetterRejected(project, u)).thenReturn(Boolean.TRUE);

        String rejectionReason = "No signature";
        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, rejectionReason);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        Map<String, Object> expectedNotificationArguments = asMap(
                "competitionName", project.getApplication().getCompetition().getName(),
                "applicationId", project.getApplication().getId()
        );

        verify(projectRepositoryMock).findById(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterRejected(project, u);
        verify(golWorkflowHandlerMock, never()).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock, never()).grantOfferLetterApproved(any(), any());
        verify(projectEmailService, never()).sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE);
        assertNull(project.getOfferSubmittedDate());
        assertEquals(project.getGrantOfferLetterRejectionReason(), rejectionReason);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenGOLRejectionFailure() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findById(u.getId())).thenReturn(Optional.of(u));
        when(golWorkflowHandlerMock.grantOfferLetterRejected(project, u)).thenReturn(Boolean.FALSE);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, "No signature");
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findById(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterRejected(project, u);
        verify(golWorkflowHandlerMock, never()).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock, never()).grantOfferLetterApproved(any(), any());
        verify(projectEmailService, never()).sendEmail(singletonList(to), emptyMap(), GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_UNEXPECTED_ERROR));
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterApprovalSuccess() {

        User user = newUser()
                .withFirstName("A")
                .withLastName("B")
                .withEmailAddress("a@b.com")
                .build();

        setLoggedInUser(newUserResource()
                .withId(user.getId())
                .build());

        List<ProjectUser> projectUsers = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withUser(user).withOrganisation(nonAcademicUnfunded)
                .withInvite(newProjectInvite()
                .build())
                .build(1);

        Competition competition = newCompetition()
                .withName("Competition 1")
                .build();

        Application application = newApplication()
                .withName("Application 1")
                .withCompetition(competition)
                .build();

        Project project = newProject()
                .withId(projectId)
                .withProjectUsers(projectUsers)
                .withPartnerOrganisations(newPartnerOrganisation()
                .withOrganisation(nonAcademicUnfunded)
                .build(1))
                .withApplication(application)
                .build();

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "competitionName", "Competition 1",
                "applicationId", project.getApplication().getId()
        );

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, user)).thenReturn(Boolean.TRUE);
        when(projectWorkflowHandlerMock.grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).thenReturn(Boolean.TRUE);
        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE)).thenReturn(serviceSuccess());

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock, atLeast(2)).findById(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, user);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        verify(projectEmailService).sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterEnsureDuplicateEmailsAreNotSent() {

        User user = newUser()
                .withFirstName("A")
                .withLastName("B")
                .withEmailAddress("a@b.com")
                .build();

        setLoggedInUser(newUserResource()
                .withId(user.getId())
                .build());

        List<ProjectUser> projectUsers = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withUser(user).withOrganisation(nonAcademicUnfunded)
                .withInvite(newProjectInvite()
                .build())
                .build(1);

        List<ProjectUser> financeContact = newProjectUser()
                .withRole(PROJECT_FINANCE_CONTACT)
                .withUser(user)
                .withOrganisation(nonAcademicUnfunded)
                .withInvite(newProjectInvite()
                .build())
                .build(1);

        Competition competition = newCompetition()
                .withName("Competition 1")
                .build();

        Application application = newApplication()
                .withName("Application 1")
                .withCompetition(competition)
                .build();

        projectUsers.addAll(financeContact);

        Project project = newProject()
                .withId(projectId)
                .withProjectUsers(projectUsers)
                .withPartnerOrganisations(newPartnerOrganisation()
                .withOrganisation(nonAcademicUnfunded)
                .build(1))
                .withApplication(application)
                .build();

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        Map<String, Object> expectedNotificationArguments = asMap(
                "competitionName", "Competition 1",
                "applicationId", project.getApplication().getId()
        );

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findById(user.getId())).thenReturn(Optional.of(user));
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, user)).thenReturn(Boolean.TRUE);
        when(projectWorkflowHandlerMock.grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).thenReturn(Boolean.TRUE);
        when(projectEmailService.sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE)).thenReturn(serviceSuccess());

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock, atLeast(2)).findById(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, user);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        verify(projectEmailService).sendEmail(singletonList(to), expectedNotificationArguments, GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE);

        assertTrue(result.isSuccess());
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenProjectGOLApprovalFailure() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(u).withOrganisation(nonAcademicUnfunded).withInvite(newProjectInvite().build()).build(1);
        Project project = newProject().withId(projectId).withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).build();

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findById(u.getId())).thenReturn(Optional.of(u));
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, u)).thenReturn(Boolean.TRUE);
        when(projectWorkflowHandlerMock.grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).thenReturn(Boolean.FALSE);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findById(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        verify(projectEmailService, never()).sendEmail(singletonList(to), emptyMap(), GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_UNEXPECTED_ERROR));
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenGOLApprovalFailure() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.TRUE);
        when(userRepositoryMock.findById(u.getId())).thenReturn(Optional.of(u));
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, u)).thenReturn(Boolean.FALSE);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findById(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock, never()).grantOfferLetterApproved(any(), any());
        verify(projectEmailService, never()).sendEmail(singletonList(to), emptyMap(), GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_UNEXPECTED_ERROR));
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenGOLNotReadyToApprove() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.of(project));
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(Boolean.FALSE);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findById(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_NOT_READY_TO_APPROVE));
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenGOLRejectedButRejectionReasonIsAllWhitespaces() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, "          ");
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenGOLRejectedButRejectionReasonIsEmpty() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, "");
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenGOLRejectedButNoRejectionReason() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void testApproveOrRejectSignedGrantOfferLetterWhenGOLNeitherApprovedNorRejected() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(null, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void testGetGrantOfferLetterStateWhenProjectDoesNotExist() {

        when(projectRepositoryMock.findById(projectId)).thenReturn(Optional.empty());

        ServiceResult<GrantOfferLetterStateResource> result = service.getGrantOfferLetterState(projectId);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Project.class, projectId)));
    }

    @Test
    public void testGetGrantOfferLetterState() {

        GrantOfferLetterStateResource state = GrantOfferLetterStateResource.stateInformationForPartnersView(GrantOfferLetterState.SENT, GrantOfferLetterEvent.GOL_SENT);

        when(golWorkflowHandlerMock.getExtendedState(project)).thenReturn(serviceSuccess(state));
        ServiceResult<GrantOfferLetterStateResource> retrievedState = service.getGrantOfferLetterState(project.getId());
        assertSame(state, retrievedState.getSuccess());
    }

    private static final String webBaseUrl = "https://ifs-local-dev/dashboard";

    @Override
    protected GrantOfferLetterService supplyServiceUnderTest() {

        GrantOfferLetterServiceImpl projectGrantOfferService = new GrantOfferLetterServiceImpl();
        ReflectionTestUtils.setField(projectGrantOfferService, "webBaseUrl", webBaseUrl);
        return projectGrantOfferService;
    }

}
