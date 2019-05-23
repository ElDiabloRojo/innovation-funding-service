package org.innovateuk.ifs.project.status.populator;

import org.apache.commons.lang3.tuple.Pair;
import org.innovateuk.ifs.BaseUnitTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.async.generation.AsyncFuturesGenerator;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.builder.CompetitionDocumentResourceBuilder;
import org.innovateuk.ifs.competition.resource.CompetitionDocumentResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.bankdetails.resource.BankDetailsResource;
import org.innovateuk.ifs.project.bankdetails.service.BankDetailsRestService;
import org.innovateuk.ifs.project.builder.ProjectResourceBuilder;
import org.innovateuk.ifs.project.document.resource.DocumentStatus;
import org.innovateuk.ifs.project.document.resource.ProjectDocumentResource;
import org.innovateuk.ifs.project.monitoring.resource.MonitoringOfficerResource;
import org.innovateuk.ifs.project.monitoring.service.MonitoringOfficerRestService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.service.ProjectRestService;
import org.innovateuk.ifs.project.status.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.project.status.viewmodel.SetupStatusViewModel;
import org.innovateuk.ifs.sections.SectionAccess;
import org.innovateuk.ifs.sections.SectionStatus;
import org.innovateuk.ifs.status.StatusService;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.innovateuk.ifs.AsyncTestExpectationHelper.setupAsyncExpectations;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.rest.RestResult.restFailure;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionDocumentResourceBuilder.newCompetitionDocumentResource;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.competition.resource.CompetitionDocumentResource.COLLABORATION_AGREEMENT_TITLE;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.project.bankdetails.builder.BankDetailsResourceBuilder.newBankDetailsResource;
import static org.innovateuk.ifs.project.builder.MonitoringOfficerResourceBuilder.newMonitoringOfficerResource;
import static org.innovateuk.ifs.project.builder.ProjectPartnerStatusResourceBuilder.newProjectPartnerStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectTeamStatusResourceBuilder.newProjectTeamStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.project.constant.ProjectActivityStates.*;
import static org.innovateuk.ifs.project.documents.builder.ProjectDocumentResourceBuilder.newProjectDocumentResource;
import static org.innovateuk.ifs.project.resource.ProjectState.LIVE;
import static org.innovateuk.ifs.sections.SectionStatus.TICK;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SetupStatusViewModelPopulatorTest extends BaseUnitTest {

    @InjectMocks
    private SetupStatusViewModelPopulator populator;

    @Mock
    private ProjectService projectService;

    @Mock
    private ProjectRestService projectRestService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private MonitoringOfficerRestService monitoringOfficerService;

    @Mock
    private BankDetailsRestService bankDetailsRestService;

    @Mock
    private StatusService statusService;

    @Mock
    private AsyncFuturesGenerator futuresGeneratorMock;

    private static final boolean monitoringOfficerExpected = true;

    List<CompetitionDocumentResource> projectDocumentConfig =
            newCompetitionDocumentResource()
            .withTitle("Risk Register", "Plan Document")
            .build(2);

    private CompetitionResource competition = newCompetitionResource()
            .withLocationPerPartner(false)
            .withProjectDocument(projectDocumentConfig)
            .build();
    private ApplicationResource application = newApplicationResource().withCompetition(competition.getId()).build();
    private ProjectResourceBuilder projectBuilder = newProjectResource().withApplication(application);

    private ProjectResource project = projectBuilder.build();
    private OrganisationResource organisationResource = newOrganisationResource().build();
    private OrganisationResource partnerOrganisationResource = newOrganisationResource().build();

    private BankDetailsResource bankDetailsResource = newBankDetailsResource().build();
    private RestResult<BankDetailsResource> bankDetailsFoundResult = restSuccess(bankDetailsResource);
    private RestResult<BankDetailsResource> bankDetailsNotFoundResult = restFailure(notFoundError(BankDetailsResource.class, 123L));

    private MonitoringOfficerResource monitoringOfficer = newMonitoringOfficerResource().build();
    private RestResult<MonitoringOfficerResource> monitoringOfficerFoundResult = restSuccess(monitoringOfficer);
    private RestResult<MonitoringOfficerResource> monitoringOfficerNotFoundResult = restFailure(HttpStatus.NOT_FOUND);

    private Map<String, SectionStatus> partnerStatusFlagChecks = new HashMap<>();

    private UserResource loggedInUser = newUserResource().withId(1L)
            .withFirstName("James")
            .withLastName("Watts")
            .withEmail("james.watts@email.co.uk")
            .withRolesGlobal(singletonList(Role.APPLICANT))
            .withUID("2aerg234-aegaeb-23aer").build();

    @Before
    public void setUpDefaults() {
        partnerStatusFlagChecks.put("projectDetailsStatus", SectionStatus.FLAG);
        partnerStatusFlagChecks.put("projectTeamStatus", SectionStatus.FLAG);
        partnerStatusFlagChecks.put("monitoringOfficerStatus", SectionStatus.EMPTY);
        partnerStatusFlagChecks.put("bankDetailsStatus", SectionStatus.EMPTY);
        partnerStatusFlagChecks.put("financeChecksStatus", SectionStatus.EMPTY);
        partnerStatusFlagChecks.put("spendProfileStatus", SectionStatus.EMPTY);
        partnerStatusFlagChecks.put("documentsStatus", SectionStatus.EMPTY);
        partnerStatusFlagChecks.put("grantOfferLetterStatus", SectionStatus.EMPTY);
    }

    @Before
    public void setupExpectations() {
        setupAsyncExpectations(futuresGeneratorMock);
    }

    @Test
    public void viewProjectSetupStatus() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withFinanceChecksStatus(NOT_STARTED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);

        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
        assertTrue(viewModel.isCompetitionDocuments());

    }

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedButFinanceContactNotYetSubmittedAsProjectManager() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        ProjectUserResource partnerUser = newProjectUserResource()
                .withUser(loggedInUser.getId())
                .withOrganisation(organisationResource.getId())
                .withRole(PARTNER)
                .build();

        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of(partnerUser));

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("documentsStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
        assertTrue(viewModel.isProjectManager());
    }

    @Test
    public void viewProjectSetupStatusForNonLeadPartnerWithFinanceContactNotSubmitted() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(999L)
                        .withProjectDetailsStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedAndFinanceContactSubmitted() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .build(1))
                .withProjectState(LIVE).
                        build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedAndFinanceContactSubmittedNotFinanceContact() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .build(1))
                .withProjectState(LIVE).
                        build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));
        assertEquals(viewModel.getFinanceChecksSection(), SectionAccess.ACCESSIBLE);

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedAndFinanceContactSubmittedAsFinanceContact() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withBankDetailsStatus(COMPLETE)
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .build(1))
                .withProjectState(LIVE).
                        build();

        when(applicationService.getById(application.getId())).thenReturn(application);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(competitionRestService.getCompetitionById(application.getCompetition())).thenReturn(restSuccess(competition));
        when(monitoringOfficerService.findMonitoringOfficerForProject(project.getId())).thenReturn(monitoringOfficerNotFoundResult);
        when(projectRestService.getOrganisationByProjectAndUser(project.getId(), loggedInUser.getId())).thenReturn(restSuccess(organisationResource));
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(Arrays.asList(newProjectUserResource()
                        .withUser(loggedInUser.getId())
                        .withOrganisation(organisationResource.getId())
                        .withRole(FINANCE_CONTACT).build(),
                newProjectUserResource()
                        .withUser(loggedInUser.getId())
                        .withOrganisation(organisationResource.getId())
                        .withRole(PARTNER).build()));

        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of((newProjectUserResource()
                .withUser(loggedInUser.getId())
                .withOrganisation(organisationResource.getId())
                .withRole(FINANCE_CONTACT).build())));

        ProjectUserResource partnerUser = newProjectUserResource()
                .withUser(loggedInUser.getId() + 1000L)
                .withOrganisation(organisationResource.getId())
                .withRole(PARTNER).build();
        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of(partnerUser));

        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(bankDetailsFoundResult);
        when(statusService.getProjectTeamStatus(project.getId(), Optional.empty())).thenReturn(teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));
        assertEquals(SectionAccess.ACCESSIBLE, viewModel.getFinanceChecksSection());

        assertFalse(viewModel.isProjectComplete());
        assertFalse(viewModel.isProjectManager());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsLeadWhenPDSubmittedFCNotYetSubmittedAndPLRequiredAndNotYetSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.EMPTY));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsLeadWhenPDSubmittedFCSubmittedAndPLRequiredAndNotYetSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.EMPTY),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsLeadWhenPDSubmittedFCNotSubmittedAndPLRequiredAndSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("financeChecksStatus", SectionStatus.EMPTY));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsLeadWhenPDSubmittedFCSubmittedAndPLRequiredAndSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsNonLeadWhenPDSubmittedFCNotYetSubmittedAndPLRequiredAndNotYetSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(999L)
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.EMPTY));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsNonLeadWhenPDSubmittedFCSubmittedAndPLRequiredAndNotYetSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(999L)
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.EMPTY),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsNonLeadWhenPDSubmittedFCNotSubmittedAndPLRequiredAndSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(999L)
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("financeChecksStatus", SectionStatus.EMPTY));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsNonLeadWhenPDSubmittedFCSubmittedAndPLRequiredAndSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(999L)
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, FC = Finance Contact, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsNonLeadWhenPDSubmittedAndOnlyNonLeadFCSubmittedAndPLRequiredAndSubmitted() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(999L)
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", SectionStatus.EMPTY),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWhenAwaitingProjectDetailsActionFromOtherPartners() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS),
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    // PD = Project Details, PL = Project Location
    @Test
    public void viewProjectSetupStatusAsLeadWhenPLRequiredAndAwaitingPDActionFromOtherPartners() throws Exception {

        competition.setLocationPerPartner(true);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withPartnerProjectLocationStatus(COMPLETE)
                        .withFinanceChecksStatus(PENDING)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .withPartnerProjectLocationStatus(ACTION_REQUIRED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS),
                Pair.of("monitoringOfficerStatus", SectionStatus.EMPTY));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithMonitoringOfficerAssigned() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withSpendProfileStatus(NOT_REQUIRED).
                        withFinanceChecksStatus(PENDING).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build())
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS),
                Pair.of("bankDetailsStatus", SectionStatus.EMPTY));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithBankDetailsEntered() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(PENDING).
                        withSpendProfileStatus(NOT_REQUIRED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build())
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithAllBankDetailsCompleteOrNotRequired() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(PENDING).
                        withSpendProfileStatus(NOT_REQUIRED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(NOT_REQUIRED).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithQueryAwaitingResponseNonFinanceContact() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(ACTION_REQUIRED).
                        withSpendProfileStatus(NOT_REQUIRED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(NOT_REQUIRED).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
        assertTrue(viewModel.isShowFinanceChecksPendingQueryWarning());
    }

    @Test
    public void viewProjectSetupStatusWithQueryAwaitingResponseAsFinanceContact() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withBankDetailsStatus(COMPLETE)
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(COMPLETE)
                        .withFinanceChecksStatus(ACTION_REQUIRED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withOrganisationId(organisationResource.getId())
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(COMPLETE)
                        .build(1))
                .withProjectState(LIVE).
                        build();

        when(applicationService.getById(application.getId())).thenReturn(application);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(competitionRestService.getCompetitionById(application.getCompetition())).thenReturn(restSuccess(competition));
        when(monitoringOfficerService.findMonitoringOfficerForProject(project.getId())).thenReturn(monitoringOfficerFoundResult);
        when(projectRestService.getOrganisationByProjectAndUser(project.getId(), loggedInUser.getId())).thenReturn(restSuccess(organisationResource));
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(Arrays.asList(newProjectUserResource()
                        .withUser(loggedInUser.getId())
                        .withOrganisation(organisationResource.getId())
                        .withRole(FINANCE_CONTACT).build(),
                newProjectUserResource()
                        .withUser(loggedInUser.getId())
                        .withOrganisation(organisationResource.getId())
                        .withRole(PARTNER).build()));

        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of((newProjectUserResource()
                .withUser(loggedInUser.getId() + 1000L)
                .withOrganisation(organisationResource.getId())
                .withRole(FINANCE_CONTACT).build())));
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(bankDetailsFoundResult);
        when(statusService.getProjectTeamStatus(project.getId(), Optional.empty())).thenReturn(teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
        assertTrue(viewModel.isShowFinanceChecksPendingQueryWarning());
    }

    @Test
    public void viewProjectSetupStatusWithAllFinanceChecksApproved() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(NOT_REQUIRED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithSpendProfile() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(ACTION_REQUIRED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(ACTION_REQUIRED).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithSpendProfilePartnerComplete() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(ACTION_REQUIRED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithSpendAwaitingApproval() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(PENDING).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithSpendApproved() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", TICK));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithGOLNotSent() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withGrantOfferStatus(NOT_STARTED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withProjectDetailsStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", TICK),
                Pair.of("grantOfferLetterStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithGOLSent() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withProjectTeamStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withGrantOfferStatus(ACTION_REQUIRED).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withProjectDetailsStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE)
                .withProjectManagerAssigned(true)
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", TICK),
                Pair.of("grantOfferLetterStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithGOLReturned() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withGrantOfferStatus(PENDING).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withProjectDetailsStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", TICK),
                Pair.of("grantOfferLetterStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithGOLApproved() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withProjectDetailsStatus(COMPLETE).
                        withProjectTeamStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withFinanceChecksStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withGrantOfferStatus(COMPLETE).
                        withOrganisationId(organisationResource.getId()).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withFinanceChecksStatus(COMPLETE).
                        withBankDetailsStatus(COMPLETE).
                        withSpendProfileStatus(COMPLETE).
                        withProjectDetailsStatus(COMPLETE).
                        build(1))
                .withProjectState(LIVE).
                build();

        setupLookupProjectDetailsExpectations(monitoringOfficerFoundResult, bankDetailsFoundResult, teamStatus);

        SetupStatusViewModel viewModel = populator.populateViewModel(project.getId(), loggedInUser, "origin").get();
        assertStandardViewModelValuesCorrect(viewModel, monitoringOfficerExpected);

        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("projectDetailsStatus", TICK),
                Pair.of("monitoringOfficerStatus", TICK),
                Pair.of("bankDetailsStatus", TICK),
                Pair.of("financeChecksStatus", TICK),
                Pair.of("spendProfileStatus", TICK),
                Pair.of("grantOfferLetterStatus", TICK));

        assertTrue(viewModel.isProjectComplete());
    }

    // Uncomment when ApprovalType conversation has finished.

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedButFinanceContactNotYetSubmittedWithOtherDocumentsSubmittedAsProjectManager() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        project = newProjectResource().withApplication(application).withDocumentsSubmittedDate(ZonedDateTime.now()).build();
        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedButFinanceContactNotYetSubmittedWithOtherDocumentsSubmittedAndRejectedAsProjectManager() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        project = newProjectResource().withApplication(application).withDocumentsSubmittedDate(ZonedDateTime.now()).build();
        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        List<ProjectUserResource> projectUsers = newProjectUserResource()
                .withUser(loggedInUser.getId(), loggedInUser.getId())
                .withOrganisation(organisationResource.getId(), organisationResource.getId())
                .withRole(PARTNER, PROJECT_MANAGER)
                .build(2);

        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(projectUsers);

        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of((newProjectUserResource()
                .withUser(loggedInUser.getId())
                .withOrganisation(organisationResource.getId())
                .withRole(PROJECT_MANAGER).build())));

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.FLAG),
                Pair.of("documentsStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedButFinanceContactNotYetSubmittedWithOtherDocumentsSubmittedAndRejectedAsPartner() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        project = newProjectResource().withApplication(application).withDocumentsSubmittedDate(ZonedDateTime.now()).build();
        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.HOURGLASS));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWithProjectDetailsSubmittedButFinanceContactNotYetSubmittedWithOtherDocumentsSubmittedAndApprovedAsPartner() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        project = newProjectResource().withApplication(application).withDocumentsSubmittedDate(ZonedDateTime.now()).build();
        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", TICK));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWhenAllDocumentsNotYetUploaded() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        project = newProjectResource().withApplication(application).build();
        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of((newProjectUserResource()
                .withUser(loggedInUser.getId())
                .withOrganisation(organisationResource.getId())
                .withRole(PROJECT_MANAGER).build())));

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.FLAG),
                Pair.of("documentsStatus", SectionStatus.FLAG));

        assertFalse(viewModel.isProjectComplete());
    }

    @Test
    public void viewProjectSetupStatusWhenAllDocumentsUploadedButNotSubmitted() throws Exception {

        SetupStatusViewModel viewModel = performDocumentsTest(DocumentStatus.UPLOADED, DocumentStatus.UPLOADED);

        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.FLAG),
                Pair.of("documentsStatus", SectionStatus.FLAG));
    }

    @Test
    public void viewProjectSetupStatusWhenOnlyOneDocumentSubmitted() throws Exception {

        SetupStatusViewModel viewModel = performDocumentsTest(DocumentStatus.UPLOADED, DocumentStatus.SUBMITTED);

        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.FLAG),
                Pair.of("documentsStatus", SectionStatus.FLAG));
    }

    @Test
    public void viewProjectSetupStatusWhenAllDocumentsSubmitted() throws Exception {

        SetupStatusViewModel viewModel = performDocumentsTest(DocumentStatus.SUBMITTED, DocumentStatus.SUBMITTED);

        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.FLAG),
                Pair.of("documentsStatus", SectionStatus.HOURGLASS));
    }

    @Test
    public void viewProjectSetupStatusWhenAnyDocumentRejected() throws Exception {

        SetupStatusViewModel viewModel = performDocumentsTest(DocumentStatus.REJECTED, DocumentStatus.SUBMITTED);

        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.FLAG),
                Pair.of("documentsStatus", SectionStatus.FLAG));
    }

    @Test
    public void viewProjectSetupStatusWhenAllApproved() throws Exception {

        SetupStatusViewModel viewModel = performDocumentsTest(DocumentStatus.APPROVED, DocumentStatus.APPROVED);

        assertPartnerStatusFlagsCorrect(viewModel,
                Pair.of("monitoringOfficerStatus", SectionStatus.HOURGLASS),
                Pair.of("otherDocumentsStatus", SectionStatus.FLAG),
                Pair.of("documentsStatus", TICK));
    }

    private SetupStatusViewModel performDocumentsTest(DocumentStatus document1Status, DocumentStatus document2Status) throws Exception {
        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        List<ProjectDocumentResource> projectDocumentResources = newProjectDocumentResource()
                .withStatus(document1Status, document2Status)
                .build(2);

        project = newProjectResource()
                .withApplication(application)
                .withProjectDocuments(projectDocumentResources)
                .build();
        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);

        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of((newProjectUserResource()
                .withUser(loggedInUser.getId())
                .withOrganisation(organisationResource.getId())
                .withRole(PROJECT_MANAGER).build())));

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);
        assertFalse(viewModel.isProjectComplete());

        return viewModel;
    }


    @Test
    public void viewProjectSetupStatusCollaborationAgreementNotNeeded() throws Exception {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource()
                .withProjectLeadStatus(newProjectPartnerStatusResource()
                        .withOrganisationId(organisationResource.getId())
                        .withProjectDetailsStatus(COMPLETE)
                        .withFinanceContactStatus(NOT_STARTED)
                        .withSpendProfileStatus(NOT_REQUIRED)
                        .withIsLeadPartner(true)
                        .build())
                .withPartnerStatuses(newProjectPartnerStatusResource()
                        .withFinanceContactStatus(NOT_STARTED)
                        .build(1))
                .withProjectState(LIVE)
                .build();

        List<CompetitionDocumentResource> competitionDocuments = CompetitionDocumentResourceBuilder.newCompetitionDocumentResource()
                .withTitle(COLLABORATION_AGREEMENT_TITLE, "Other Document")
                .withCompetition(competition.getId())
                .build(2);

        competition.setCompetitionDocuments(competitionDocuments);

        List<ProjectDocumentResource> projectDocuments = newProjectDocumentResource()
                .withCompetitionDocument(competitionDocuments.get(1))
                .withStatus(DocumentStatus.APPROVED)
                .build(1);

        project = newProjectResource()
                .withApplication(application)
                .withProjectDocuments(projectDocuments)
                .withCompetition(competition.getId())
                .build();

        setupLookupProjectDetailsExpectations(monitoringOfficerNotFoundResult, bankDetailsNotFoundResult, teamStatus);
        when(projectService.getPartnerOrganisationsForProject(project.getId())).thenReturn(asList(organisationResource));

        SetupStatusViewModel viewModel = performPopulateView(project.getId(), loggedInUser);

        assertEquals(false, viewModel.isCollaborationAgreementRequired());
        assertEquals(true, viewModel.getDocumentsStatus().equals(TICK));

        assertFalse(viewModel.isProjectComplete());
        assertFalse(viewModel.isMonitoringOfficer());

    }

    private SetupStatusViewModel performPopulateView(Long projectId, UserResource loggedInUser) throws Exception {
        return populator.populateViewModel(projectId, loggedInUser, "origin").get();
    }

    private void setupLookupProjectDetailsExpectations(RestResult<MonitoringOfficerResource> monitoringOfficerResult, RestResult<BankDetailsResource> bankDetailsResult, ProjectTeamStatusResource teamStatus) {

        ProjectUserResource pmUser = newProjectUserResource()
                .withUser(loggedInUser.getId() + 1000L)
                .withOrganisation(organisationResource.getId())
                .withRole(PROJECT_MANAGER)
                .build();

        when(applicationService.getById(application.getId())).thenReturn(application);
        when(projectService.getById(project.getId())).thenReturn(project);
        when(competitionRestService.getCompetitionById(application.getCompetition())).thenReturn(restSuccess(competition));
        when(monitoringOfficerService.findMonitoringOfficerForProject(project.getId())).thenReturn(monitoringOfficerResult);
        when(projectRestService.getOrganisationByProjectAndUser(project.getId(), loggedInUser.getId())).thenReturn(restSuccess(organisationResource));
        when(projectService.getProjectUsersForProject(project.getId())).thenReturn(newProjectUserResource().
                withUser(loggedInUser.getId())
                .withOrganisation(organisationResource.getId())
                .withRole(PARTNER).build(1));

        when(projectService.getProjectManager(project.getId())).thenReturn(Optional.of(pmUser));
        when(bankDetailsRestService.getBankDetailsByProjectAndOrganisation(project.getId(), organisationResource.getId())).thenReturn(bankDetailsResult);
        when(statusService.getProjectTeamStatus(project.getId(), Optional.empty())).thenReturn(teamStatus);
        when(projectService.getPartnerOrganisationsForProject(project.getId())).thenReturn(asList(organisationResource, partnerOrganisationResource));
    }

    private void assertStandardViewModelValuesCorrect(SetupStatusViewModel viewModel, boolean existingMonitoringOfficerExpected) {
        assertEquals(project.getId(), viewModel.getProjectId());
        assertEquals(project.getName(), viewModel.getProjectName());
        assertEquals(competition.getName(), viewModel.getCompetitionName());
        assertEquals(application.getId(), viewModel.getApplicationId());
        assertEquals(organisationResource.getId(), viewModel.getOrganisationId());
        assertEquals(true, viewModel.isCollaborationAgreementRequired());

        if (existingMonitoringOfficerExpected) {
            assertEquals(monitoringOfficer.getFullName(), viewModel.getMonitoringOfficerName());
        } else {
            assertEquals("", viewModel.getMonitoringOfficerName());
        }
    }

    private final void assertPartnerStatusFlagsCorrect(SetupStatusViewModel viewModel, Pair<String, SectionStatus>... expectedTrueFlags) {
        for (Pair<String, SectionStatus> section : expectedTrueFlags) {
            partnerStatusFlagChecks.replace(section.getLeft(), section.getRight());
        }
        assertStatuses(viewModel);
    }

    private void assertStatuses(SetupStatusViewModel viewModel) {
        assertTrue(partnerStatusFlagChecks.get("projectDetailsStatus") == viewModel.getProjectDetailsStatus());
        assertTrue(partnerStatusFlagChecks.get("projectTeamStatus") == viewModel.getProjectTeamStatus());
        assertTrue(partnerStatusFlagChecks.get("monitoringOfficerStatus") == viewModel.getMonitoringOfficerStatus());
        assertTrue(partnerStatusFlagChecks.get("bankDetailsStatus") == viewModel.getBankDetailsStatus());
        assertTrue(partnerStatusFlagChecks.get("financeChecksStatus") == viewModel.getFinanceChecksStatus());
        assertTrue(partnerStatusFlagChecks.get("spendProfileStatus") == viewModel.getSpendProfileStatus());
        assertTrue(partnerStatusFlagChecks.get("documentsStatus") == viewModel.getDocumentsStatus());
        assertTrue(partnerStatusFlagChecks.get("grantOfferLetterStatus") == viewModel.getGrantOfferLetterStatus());
    }
}
