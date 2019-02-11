package org.innovateuk.ifs.project.grantofferletter.transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.commons.error.CommonFailureKeys;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.builder.CompetitionDocumentBuilder;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competitionsetup.domain.CompetitionDocument;
import org.innovateuk.ifs.file.domain.FileEntry;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.grant.service.GrantProcessService;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
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
import org.innovateuk.ifs.project.core.transactional.PartnerOrganisationService;
import org.innovateuk.ifs.project.core.workflow.configuration.ProjectWorkflowHandler;
import org.innovateuk.ifs.project.document.resource.DocumentStatus;
import org.innovateuk.ifs.project.documents.domain.ProjectDocument;
import org.innovateuk.ifs.project.financechecks.repository.CostRepository;
import org.innovateuk.ifs.project.grantofferletter.configuration.workflow.GrantOfferLetterWorkflowHandler;
import org.innovateuk.ifs.project.grantofferletter.model.*;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterApprovalResource;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterEvent;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterState;
import org.innovateuk.ifs.project.grantofferletter.resource.GrantOfferLetterStateResource;
import org.innovateuk.ifs.project.resource.ApprovalType;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
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
import static org.innovateuk.ifs.invite.builder.ProjectUserInviteBuilder.newProjectUserInvite;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.organisation.builder.OrganisationBuilder.newOrganisation;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.RESEARCH;
import static org.innovateuk.ifs.project.builder.PartnerOrganisationResourceBuilder.newPartnerOrganisationResource;
import static org.innovateuk.ifs.project.core.builder.PartnerOrganisationBuilder.newPartnerOrganisation;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.project.core.builder.ProjectUserBuilder.newProjectUser;
import static org.innovateuk.ifs.project.core.domain.ProjectParticipantRole.*;
import static org.innovateuk.ifs.project.documents.builder.ProjectDocumentBuilder.newProjectDocument;
import static org.innovateuk.ifs.project.financecheck.builder.CostBuilder.newCost;
import static org.innovateuk.ifs.project.grantofferletter.builder.GrantOfferLetterAcademicFinanceTableBuilder.newGrantOfferLetterAcademicFinanceTable;
import static org.innovateuk.ifs.project.grantofferletter.builder.GrantOfferLetterFinanceTotalsTableBuilder.newGrantOfferLetterFinanceTotalsTable;
import static org.innovateuk.ifs.project.grantofferletter.builder.GrantOfferLetterIndustrialFinanceTableBuilder.newGrantOfferLetterIndustrialFinanceTable;
import static org.innovateuk.ifs.project.grantofferletter.transactional.GrantOfferLetterServiceImpl.NotificationsGol.GRANT_OFFER_LETTER_PROJECT_MANAGER;
import static org.innovateuk.ifs.project.grantofferletter.transactional.GrantOfferLetterServiceImpl.NotificationsGol.PROJECT_LIVE;
import static org.innovateuk.ifs.project.spendprofile.builder.SpendProfileBuilder.newSpendProfile;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
    private List<PartnerOrganisationResource> partnerOrganisationsResource;

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
    private PartnerOrganisationService partnerOrganisationServiceMock;
    
    @Mock
    private SystemNotificationSource systemNotificationSource;

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

    @Mock
    private GrantProcessService grantProcessService;

    @Before
    public void setUp() {
        organisations = newOrganisation().withOrganisationType(RESEARCH).withName("Org1&", "Org2\"", "Org3<").build(3);
        nonAcademicUnfunded = newOrganisation().withOrganisationType(BUSINESS).withName("Org4").build();
        organisationResources = newOrganisationResource().build(4);

        Competition competition = newCompetition()
                .build();

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

        partnerOrganisationsResource = newPartnerOrganisationResource().build(2);

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

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(organisationRepositoryMock.findOne(organisations.get(0).getId())).thenReturn(organisations.get(0));
        when(organisationRepositoryMock.findOne(organisations.get(1).getId())).thenReturn(organisations.get(1));
        when(organisationRepositoryMock.findOne(organisations.get(2).getId())).thenReturn(organisations.get(2));
        when(organisationMapperMock.mapToResource(organisations.get(0))).thenReturn(organisationResources.get(0));
        when(organisationMapperMock.mapToResource(organisations.get(1))).thenReturn(organisationResources.get(1));
        when(organisationMapperMock.mapToResource(organisations.get(2))).thenReturn(organisationResources.get(2));
        when(spendProfileRepositoryMock.findOneByProjectIdAndOrganisationId(anyLong(), anyLong())).thenReturn(Optional.of(orgSpendProfile));
        when(costRepositoryMock.findByCostGroupId(anyLong())).thenReturn(singletonList(newCost().build()));
        when(industrialFinanceTablePopulatorMock.createTable(anyMap())).thenReturn(industrialFinanceTable);
        when(academicFinanceTablePopulatorMock.createTable(anyMap())).thenReturn(academicFinanceTable);
        when(financeTotalsTablePopulatorMock.createTable(anyMap(), anyLong())).thenReturn(totalsTable);
        when(partnerOrganisationServiceMock.getProjectPartnerOrganisations(anyLong())).thenReturn(serviceSuccess(partnerOrganisationsResource));
    }

    @Test
    public void createSignedGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getSignedGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createSignedGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void createGrantOfferLetterFileEntry() {
        assertCreateFile(
                project::getGrantOfferLetter,
                (fileToCreate, inputStreamSupplier) ->
                        service.createGrantOfferLetterFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void createAdditionalContractFileEntry() {
        assertCreateFile(
                project::getAdditionalContractFile,
                (fileToCreate, inputStreamSupplier) ->
                        service.createAdditionalContractFileEntry(123L, fileToCreate, inputStreamSupplier));
    }

    @Test
    public void getAdditionalContractFileEntryDetails() {
        assertGetFileDetails(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileEntryDetails(123L));
    }

    @Test
    public void getGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void getSignedGrantOfferLetterFileEntryDetails() {
        assertGetFileDetails(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileEntryDetails(123L));
    }

    @Test
    public void getAdditionalContractFileContents() {
        assertGetFileContents(
                project::setAdditionalContractFile,
                () -> service.getAdditionalContractFileAndContents(123L));
    }

    @Test
    public void getGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setGrantOfferLetter,
                () -> service.getGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void getSignedGrantOfferLetterFileContents() {
        assertGetFileContents(
                project::setSignedGrantOfferLetter,
                () -> service.getSignedGrantOfferLetterFileAndContents(123L));
    }

    @Test
    public void updateSignedGrantOfferLetterFileEntry() {
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(true);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.SETUP);
        assertUpdateFile(
                project::getSignedGrantOfferLetter,
                (fileToUpdate, inputStreamSupplier) ->
                        service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier));
    }

    @Test
    public void updateSignedGrantOfferLetterFileEntryProjectLive() {

        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(projectWorkflowHandlerMock.getState(any())).thenReturn(ProjectState.LIVE);
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(false);

        ServiceResult<Void> result = service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier);
        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    @Test
    public void updateSignedGrantOfferLetterFileEntryGolNotSent() {

        FileEntryResource fileToUpdate = newFileEntryResource().build();
        Supplier<InputStream> inputStreamSupplier = () -> null;

        when(projectWorkflowHandlerMock.getState(any())).thenReturn(ProjectState.SETUP);
        when(golWorkflowHandlerMock.isSent(any())).thenReturn(false);

        ServiceResult<Void> result = service.updateSignedGrantOfferLetterFile(123L, fileToUpdate, inputStreamSupplier);
        assertTrue(result.isFailure());
        assertEquals(result.getErrors().get(0).getErrorKey(), CommonFailureKeys.GRANT_OFFER_LETTER_MUST_BE_SENT_BEFORE_UPLOADING_SIGNED_COPY.toString());
    }

    @Test
    public void submitGrantOfferLetterFailureNoSignedGolFile() {

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.SIGNED_GRANT_OFFER_LETTER_MUST_BE_UPLOADED_BEFORE_SUBMIT));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void submitGrantOfferLetterFailureCannotReachSignedState() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(false);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_CANNOT_SET_SIGNED_STATE));
        Assert.assertThat(project.getOfferSubmittedDate(), nullValue());
    }

    @Test
    public void submitGrantOfferLetterSuccess() {
        project.setSignedGrantOfferLetter(mock(FileEntry.class));

        when(golWorkflowHandlerMock.sign(any())).thenReturn(true);

        ServiceResult<Void> result = service.submitGrantOfferLetter(projectId);

        assertTrue(result.isSuccess());
        Assert.assertThat(project.getOfferSubmittedDate(), notNullValue());
    }

    @Test
    public void generateGrantOfferLetter() {
        assertGenerateFile(
                fileEntryResource ->
                        service.generateGrantOfferLetter(123L, fileEntryResource));

        verify(spendProfileRepositoryMock, times(3)).findOneByProjectIdAndOrganisationId(anyLong(), anyLong());
        verify(costRepositoryMock, times(3)).findByCostGroupId(anyLong());
        verify(industrialFinanceTablePopulatorMock).createTable(anyMap());
        verify(academicFinanceTablePopulatorMock).createTable(anyMap());
        verify(financeTotalsTablePopulatorMock).createTable(anyMap(), anyLong());
    }

    @Test
    public void removeGrantOfferLetterFileEntry() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
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
    public void removeGrantOfferLetterFileEntryProjectLive() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
        when(golWorkflowHandlerMock.removeGrantOfferLetter(project, internalUser)).thenReturn(true);
        when(projectWorkflowHandlerMock.getState(project)).thenReturn(ProjectState.LIVE);
        when(fileServiceMock.deleteFileIgnoreNotFound(existingGOLFile.getId())).thenReturn(serviceSuccess(existingGOLFile));

        ServiceResult<Void> result = service.removeGrantOfferLetterFileEntry(123L);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(PROJECT_SETUP_ALREADY_COMPLETE));
    }

    @Test
    public void removeGrantOfferLetterFileEntryButWorkflowRejected() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingGOLFile = newFileEntry().build();
        project.setGrantOfferLetter(existingGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
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
    public void removeSignedGrantOfferLetterFileEntry() {

        UserResource externalUser = newUserResource().build();
        setLoggedInUser(externalUser);

        FileEntry existingSignedGOLFile = newFileEntry().build();
        project.setSignedGrantOfferLetter(existingSignedGOLFile);

        when(userRepositoryMock.findOne(externalUser.getId())).thenReturn(user);
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
    public void removeSignedGrantOfferLetterFileEntryProjectLive() {

        UserResource internalUserResource = newUserResource().build();
        User internalUser = newUser().withId(internalUserResource.getId()).build();
        setLoggedInUser(internalUserResource);

        FileEntry existingSignedGOLFile = newFileEntry().build();
        project.setSignedGrantOfferLetter(existingSignedGOLFile);

        when(userRepositoryMock.findOne(internalUserResource.getId())).thenReturn(internalUser);
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
    public void generateGrantOfferLetterIfReadySuccess() {

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
        verify(costRepositoryMock, times(3)).findByCostGroupId(anyLong());
        verify(industrialFinanceTablePopulatorMock).createTable(anyMap());
        verify(academicFinanceTablePopulatorMock).createTable(anyMap());
        verify(financeTotalsTablePopulatorMock).createTable(anyMap(), anyLong());

        assertTrue(checkGolTemplate());
        assertTrue(result.isSuccess());
        assertTrue(compareTemplate(templateArgs, templateArgsCaptor.getAllValues().get(0)));
    }

    @Test
    public void generateGrantOfferLetterIfReadyWhenProjectDocumentsAndDocsApproved() {

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

        Competition comp = newCompetition().withName("Test Comp<").build();
        CompetitionDocument configuredCompetitionDocument = CompetitionDocumentBuilder
                .newCompetitionDocument()
                .withCompetition(comp)
                .withTitle("Risk Register")
                .withGuidance("Guidance for Risk Register")
                .build();
        comp.setCompetitionDocuments(singletonList(configuredCompetitionDocument));
        project.getApplication().setCompetition(comp);

        ProjectDocument projectDocument =
                newProjectDocument()
                .withProject(project)
                .withStatus(DocumentStatus.APPROVED)
                .build();
        project.setProjectDocuments(singletonList(projectDocument));

        Map<String, Object> templateArgs = setupTemplateArguments();

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);

        verify(rendererMock).renderTemplate(templateCaptor.capture(), templateArgsCaptor.capture());
        verify(fileServiceMock).createFile(fileEntryResCaptor.capture(), supplierCaptor.capture());

        verify(spendProfileRepositoryMock, times(3)).findOneByProjectIdAndOrganisationId(anyLong(), anyLong());
        verify(costRepositoryMock, times(3)).findByCostGroupId(anyLong());
        verify(industrialFinanceTablePopulatorMock).createTable(anyMap());
        verify(academicFinanceTablePopulatorMock).createTable(anyMap());
        verify(financeTotalsTablePopulatorMock).createTable(anyMap(), anyLong());

        assertTrue(checkGolTemplate());
        assertTrue(result.isSuccess());
        assertTrue(compareTemplate(templateArgs, templateArgsCaptor.getAllValues().get(0)));
    }

    @Test
    public void testGenerateGrantOfferLetterFailureSpendProfilesNotApproved() {
        when(projectRepositoryMock.findOne(123L)).thenReturn(project);
        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.REJECTED));

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    @Test
    public void generateGrantOfferLetterProjectDocuments() {

        Competition comp = newCompetition().withName("Test Comp").build();
        CompetitionDocument configuredCompetitionDocument = CompetitionDocumentBuilder
                .newCompetitionDocument()
                .withCompetition(comp)
                .withTitle("Risk Register")
                .withGuidance("Guidance for Risk Register")
                .build();
        comp.setCompetitionDocuments(singletonList(configuredCompetitionDocument));

        Organisation o1 = newOrganisation().withName("OrgLeader").build();
        User u = newUser().withFirstName("ab").withLastName("cd").build();
        ProcessRole leadAppProcessRole = newProcessRole().withOrganisationId(o1.getId()).withUser(u).withRole(Role.LEADAPPLICANT).build();
        Application app = newApplication().withCompetition(comp).withProcessRoles(leadAppProcessRole).withId(3L).build();
        ProjectUser pm = newProjectUser().withRole(PROJECT_MANAGER).withOrganisation(o1).build();
        PartnerOrganisation po = PartnerOrganisationBuilder.newPartnerOrganisation().withOrganisation(o1).withLeadOrganisation(true).build();
        Project project = newProject().withApplication(app).withPartnerOrganisations(asList(po)).withProjectUsers(asList(pm)).withDuration(10L).build();
        ProjectDocument projectDocument =
                newProjectDocument()
                .withProject(project)
                .withStatus(DocumentStatus.REJECTED)
                .build();
        project.setProjectDocuments(singletonList(projectDocument));

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        ServiceResult<Void> result = service.generateGrantOfferLetterIfReady(123L);
        assertTrue(result.isSuccess());
    }

    @Test
    public void generateGrantOfferLetterNoProject() {

        when(spendProfileServiceMock.getSpendProfileStatusByProjectId(123L)).thenReturn(serviceSuccess(ApprovalType.APPROVED));
        when(projectRepositoryMock.findOne(123L)).thenReturn(null);

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

        List<CompetitionDocument> competitionDocuments
                = CompetitionDocumentBuilder.newCompetitionDocument().build(1);

        Competition comp = newCompetition()
                .withName("Test Comp<")
                .withCompetitionDocuments(competitionDocuments)
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
        project = newProject()
                .withName("project 1")
                .withApplication(app)
                .withPartnerOrganisations(asList(po3, po, po2))
                .withProjectUsers(asList(pm))
                .withDuration(10L)
                .withAddress(address)
                .withTargetStartDate(LocalDate.now())
                .build();

        ProjectDocument projectDocument =
                newProjectDocument()
                .withProject(project)
                .withStatus(DocumentStatus.APPROVED)
                .build();

        project.setProjectDocuments(singletonList(projectDocument));

        when(projectRepositoryMock.findOne(123L)).thenReturn(project);

        when(organisationRepositoryMock.findOne(o1.getId())).thenReturn(o1);
        when(organisationRepositoryMock.findOne(o2.getId())).thenReturn(o2);
        when(organisationRepositoryMock.findOne(o3.getId())).thenReturn(o3);

    }

    @Test
    public void sendGrantOfferLetterNoGol() {

        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectUserInvite().build()).build(1);
        Project p = newProject()
                .withProjectUsers(pu)
                .withPartnerOrganisations(newPartnerOrganisation()
                        .withOrganisation(nonAcademicUnfunded)
                        .build(1))
                .withGrantOfferLetter(null)
                .build();

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);
        when(notificationServiceMock.sendNotificationWithFlush(any(), eq(EMAIL))).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void sendGrantOfferLetterSendFails() {

        List<ProjectUser> pu = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withUser(user)
                .withOrganisation(nonAcademicUnfunded)
                .withInvite(newProjectUserInvite()
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

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);

        User projectManagerUser = pu.get(0).getUser();

        NotificationTarget to = new UserNotificationTarget(projectManagerUser.getName(), projectManagerUser.getEmail());

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard",
                "applicationId", application.getId(),
                "competitionName", "Competition 1"
        );

        Notification notification = new Notification(systemNotificationSource, to, GRANT_OFFER_LETTER_PROJECT_MANAGER, expectedNotificationArguments);
        when(notificationServiceMock.sendNotificationWithFlush(notification, EMAIL)).thenReturn(serviceFailure(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE));

        User user = newUser().build();
        setLoggedInUser(newUserResource().withId(user.getId()).build());
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);

        when(golWorkflowHandlerMock.grantOfferLetterSent(project, user)).thenReturn(true);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(NOTIFICATIONS_UNABLE_TO_SEND_MULTIPLE));
    }

    @Test
    public void sendGrantOfferLetterNoProject() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
    }

    @Test
    public void sendGrantOfferLetterSuccess() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectUserInvite().build()).build(1);

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

        when(projectRepositoryMock.findOne(projectId)).thenReturn(p);

        User projectManagerUser = pu.get(0).getUser();

        NotificationTarget to = new UserNotificationTarget(projectManagerUser.getName(), projectManagerUser.getEmail());

        Map<String, Object> expectedNotificationArguments = asMap(
                "dashboardUrl", "https://ifs-local-dev/dashboard",
                "applicationId", application.getId(),
                "competitionName", "Competition 1"
        );

        Notification notification = new Notification(systemNotificationSource, to, GRANT_OFFER_LETTER_PROJECT_MANAGER, expectedNotificationArguments);
        when(notificationServiceMock.sendNotificationWithFlush(notification, EMAIL)).thenReturn(serviceSuccess());

        User user = UserBuilder.newUser().build();
        setLoggedInUser(newUserResource().withId(user.getId()).build());
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);

        when(golWorkflowHandlerMock.grantOfferLetterSent(p, user)).thenReturn(true);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isSuccess());

        verify(golWorkflowHandlerMock).grantOfferLetterSent(p, user);
        verify(notificationServiceMock).sendNotificationWithFlush(notification, EMAIL);
    }

    @Test
    public void sendGrantOfferLetterFailure() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(user).withOrganisation(nonAcademicUnfunded).withInvite(newProjectUserInvite().build()).build(1);

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

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);

        User user = UserBuilder.newUser().build();
        setLoggedInUser(newUserResource().withId(user.getId()).build());
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);

        when(golWorkflowHandlerMock.grantOfferLetterSent(project, user)).thenReturn(false);

        ServiceResult<Void> result = service.sendGrantOfferLetter(projectId);

        assertTrue(result.isFailure());
        verify(golWorkflowHandlerMock).grantOfferLetterSent(project, user);
        verify(notificationServiceMock, never()).sendNotificationWithFlush(any(Notification.class), eq(EMAIL));
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterRejectionSuccess() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(true);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(golWorkflowHandlerMock.grantOfferLetterRejected(project, u)).thenReturn(true);

        String rejectionReason = "No signature";
        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, rejectionReason);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterRejected(project, u);
        verify(golWorkflowHandlerMock, never()).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock, never()).grantOfferLetterApproved(any(), any());
        verify(notificationServiceMock, never()).sendNotificationWithFlush(any(Notification.class), eq(EMAIL));
        assertNull(project.getOfferSubmittedDate());
        assertEquals(project.getGrantOfferLetterRejectionReason(), rejectionReason);

        assertTrue(result.isSuccess());
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterWhenGOLRejectionFailure() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());

        NotificationTarget to = new UserNotificationTarget("A B", "a@b.com");

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(true);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(golWorkflowHandlerMock.grantOfferLetterRejected(project, u)).thenReturn(false);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, "No signature");
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterRejected(project, u);
        verify(golWorkflowHandlerMock, never()).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock, never()).grantOfferLetterApproved(any(), any());
        verify(notificationServiceMock, never()).sendNotificationWithFlush(any(Notification.class), eq(EMAIL));

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_UNEXPECTED_ERROR));
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterApprovalSuccess() {

        ReflectionTestUtils.setField(service, "allocateLiveProjectsRole", true);

        User user = newUser()
                .withFirstName("A")
                .withLastName("B")
                .withEmailAddress("a@b.com")
                .build();

        setLoggedInUser(newUserResource()
                .withId(user.getId())
                .build());

        Organisation organisation1 = newOrganisation().build();
        Organisation organisation2 = newOrganisation().build();

        ProjectUser projectManager = newProjectUser()
                .withRole(PROJECT_MANAGER)
                .withUser(user)
                .withOrganisation(organisation1)
                .build();

        ProjectUser financeContactOrg1 = newProjectUser()
                .withRole(PROJECT_FINANCE_CONTACT)
                .withUser(newUser().build())
                .withOrganisation(organisation1)
                .build();

        ProjectUser normalPartnerOrg1 = newProjectUser()
                .withRole(PROJECT_PARTNER)
                .withUser(newUser().build())
                .withOrganisation(organisation1)
                .build();

        ProjectUser financeContactOrg2 = newProjectUser()
                .withRole(PROJECT_FINANCE_CONTACT)
                .withUser(newUser().build())
                .withOrganisation(organisation2)
                .build();

        ProjectUser normalPartnerOrg2 = newProjectUser()
                .withRole(PROJECT_PARTNER)
                .withUser(newUser().build())
                .withOrganisation(organisation2)
                .build();

        List<ProjectUser> projectUsers =
                asList(projectManager, financeContactOrg1, normalPartnerOrg1, financeContactOrg2, normalPartnerOrg2);

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
                    .withOrganisation(organisation1, organisation2)
                    .build(2)
                )
                .withApplication(application)
                .build();

        List<NotificationTarget> to = asList(
                new UserNotificationTarget(projectManager.getUser().getName(), projectManager.getUser().getEmail()),
                new UserNotificationTarget(financeContactOrg1.getUser().getName(), financeContactOrg1.getUser().getEmail()),
                new UserNotificationTarget(financeContactOrg2.getUser().getName(), financeContactOrg2.getUser().getEmail())
        );

        Map<String, Object> expectedNotificationArguments = asMap(
                "competitionName", "Competition 1",
                "applicationId", project.getApplication().getId()
        );

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(true);
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, user)).thenReturn(true);
        when(projectWorkflowHandlerMock.grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).thenReturn(true);

        Notification notification = new Notification(systemNotificationSource, to, PROJECT_LIVE, expectedNotificationArguments);
        when(notificationServiceMock.sendNotificationWithFlush(notification, EMAIL)).thenReturn(serviceSuccess());

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);

        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);
        assertTrue(result.isSuccess());

        // assert that the Project Manager and the Finance Contacts for each Partner Organisation are granted access to
        // Live Projects
        assertTrue(projectManager.getUser().hasRole(Role.LIVE_PROJECTS_USER));
        assertTrue(financeContactOrg1.getUser().hasRole(Role.LIVE_PROJECTS_USER));
        assertTrue(financeContactOrg2.getUser().hasRole(Role.LIVE_PROJECTS_USER));

        // assert that "normal" Partner users are NOT granted access to Live Projects
        assertFalse(normalPartnerOrg1.getUser().hasRole(Role.LIVE_PROJECTS_USER));
        assertFalse(normalPartnerOrg2.getUser().hasRole(Role.LIVE_PROJECTS_USER));

        verify(projectRepositoryMock, atLeast(2)).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, user);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        verify(notificationServiceMock).sendNotificationWithFlush(notification, EMAIL);
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterEnsureDuplicateEmailsAreNotSent() {

        User user = newUser()
                .withFirstName("A")
                .withLastName("B")
                .withEmailAddress("a@b.com")
                .build();

        setLoggedInUser(newUserResource()
                .withId(user.getId())
                .build());

        List<ProjectUser> projectUsers = newProjectUser()
                .withRole(PROJECT_MANAGER, PROJECT_FINANCE_CONTACT)
                .withUser(user).withOrganisation(nonAcademicUnfunded)
                .withInvite(newProjectUserInvite()
                .build())
                .build(2);

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

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(true);
        when(userRepositoryMock.findOne(user.getId())).thenReturn(user);
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, user)).thenReturn(true);

        when(projectWorkflowHandlerMock.grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).
                thenReturn(true);

        Notification notification = new Notification(systemNotificationSource, to, PROJECT_LIVE, expectedNotificationArguments);
        when(notificationServiceMock.sendNotificationWithFlush(notification, EMAIL)).thenReturn(serviceSuccess());

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);

        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);
        assertTrue(result.isSuccess());

         verify(projectRepositoryMock, atLeast(2)).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, user);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        verify(notificationServiceMock).sendNotificationWithFlush(notification, EMAIL);

    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterWhenProjectGOLApprovalFailure() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());
        List<ProjectUser> pu = newProjectUser().withRole(PROJECT_MANAGER).withUser(u).withOrganisation(nonAcademicUnfunded).withInvite(newProjectUserInvite().build()).build(1);
        Project project = newProject().withId(projectId).withProjectUsers(pu).withPartnerOrganisations(newPartnerOrganisation().withOrganisation(nonAcademicUnfunded).build(1)).build();

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(true);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, u)).thenReturn(true);
        when(projectWorkflowHandlerMock.grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0))).thenReturn(false);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock).grantOfferLetterApproved(project, project.getProjectUsersWithRole(PROJECT_MANAGER).get(0));
        verify(notificationServiceMock, never()).sendNotificationWithFlush(any(Notification.class), eq(EMAIL));

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_UNEXPECTED_ERROR));
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterWhenGOLApprovalFailure() {
        User u = newUser().withFirstName("A").withLastName("B").withEmailAddress("a@b.com").build();
        setLoggedInUser(newUserResource().withId(u.getId()).build());

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(true);
        when(userRepositoryMock.findOne(u.getId())).thenReturn(u);
        when(golWorkflowHandlerMock.grantOfferLetterApproved(project, u)).thenReturn(false);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);
        verify(golWorkflowHandlerMock).grantOfferLetterApproved(project, u);
        verify(projectWorkflowHandlerMock, never()).grantOfferLetterApproved(any(), any());
        verify(notificationServiceMock, never()).sendNotificationWithFlush(any(Notification.class), eq(EMAIL));

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_UNEXPECTED_ERROR));
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterWhenGOLNotReadyToApprove() {

        FileEntry golFile = newFileEntry().withFilesizeBytes(10).withMediaType("application/pdf").build();
        project.setGrantOfferLetter(golFile);

        when(projectRepositoryMock.findOne(projectId)).thenReturn(project);
        when(golWorkflowHandlerMock.isReadyToApprove(project)).thenReturn(false);

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.APPROVED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        verify(projectRepositoryMock).findOne(projectId);
        verify(golWorkflowHandlerMock).isReadyToApprove(project);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GRANT_OFFER_LETTER_NOT_READY_TO_APPROVE));
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterWhenGOLRejectedButRejectionReasonIsAllWhitespaces() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, "          ");
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterWhenGOLRejectedButRejectionReasonIsEmpty() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, "");
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void approveOrRejectSignedGrantOfferLetterWhenGOLRejectedButNoRejectionReason() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(ApprovalType.REJECTED, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void aproveOrRejectSignedGrantOfferLetterWhenGOLNeitherApprovedNorRejected() {

        GrantOfferLetterApprovalResource grantOfferLetterApprovalResource = new GrantOfferLetterApprovalResource(null, null);
        ServiceResult<Void> result = service.approveOrRejectSignedGrantOfferLetter(projectId, grantOfferLetterApprovalResource);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(CommonFailureKeys.GENERAL_INVALID_ARGUMENT));
    }

    @Test
    public void getGrantOfferLetterStateWhenProjectDoesNotExist() {

        when(projectRepositoryMock.findOne(projectId)).thenReturn(null);

        ServiceResult<GrantOfferLetterStateResource> result = service.getGrantOfferLetterState(projectId);

        assertTrue(result.isFailure());
        assertTrue(result.getFailure().is(notFoundError(Project.class, projectId)));
    }

    @Test
    public void getGrantOfferLetterState() {

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
