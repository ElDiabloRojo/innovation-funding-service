package org.innovateuk.ifs.application.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.assessment.domain.Assessment;
import org.innovateuk.ifs.assessment.repository.AssessmentRepository;
import org.innovateuk.ifs.assessment.resource.AssessmentState;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.interview.domain.InterviewAssignment;
import org.innovateuk.ifs.interview.repository.InterviewAssignmentRepository;
import org.innovateuk.ifs.interview.resource.InterviewAssignmentState;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.repository.ProjectRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.resource.ApplicationState.*;
import static org.innovateuk.ifs.assessment.builder.AssessmentBuilder.newAssessment;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.interview.builder.InterviewAssignmentBuilder.newInterviewAssignment;
import static org.innovateuk.ifs.project.core.builder.ProjectBuilder.newProject;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.junit.Assert.assertEquals;

@Rollback
public class ApplicationRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<ApplicationRepository> {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private InterviewAssignmentRepository interviewAssignmentRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    @Override
    protected void setRepository(ApplicationRepository repository) {
        this.repository = repository;
    }

    @Test
    public void findByApplicationProcessActivityStateStateIn() {
        List<Application> applicationList = simpleMap(ApplicationState.values(), this::createApplicationByState);

        long initial = repository.findByApplicationProcessActivityStateIn(submittedAndFinishedStates).count();

        repository.saveAll(applicationList);

        Stream<Application> applications = repository.findByApplicationProcessActivityStateIn(submittedAndFinishedStates);

        assertEquals(initial + 6, applications.count());
    }

    @Test
    public void findSubmittedApplicationsNotOnInterviewPanel() {
        loginCompAdmin();
        Competition competition = competitionRepository.save(newCompetition().with(id(null)).build());

        List<Application> applications = newApplication()
                .withCompetition(competition)
                .withActivityState(CREATED, SUBMITTED)
                .with(id(null))
                .build(2);

        applicationRepository.saveAll(applications);

        Pageable pageable = new PageRequest(0, 20);

        Page<Application> invitableApplications = repository.findSubmittedApplicationsNotOnInterviewPanel(competition
                .getId(), pageable);

        assertEquals(1, invitableApplications.getTotalElements());
    }

    @Test
    public void findSubmittedApplicationsNotOnInterviewPanel_noApplications() {
        loginCompAdmin();
        Competition competition = competitionRepository.save(newCompetition().with(id(null)).build());

        Pageable pageable = new PageRequest(0, 20);

        Page<Application> applications = repository.findSubmittedApplicationsNotOnInterviewPanel(competition.getId(),
                pageable);

        assertEquals(0, applications.getTotalElements());
    }

    @Test
    public void findSubmittedApplicationsNotOnInterviewPanel_staged() {
        loginCompAdmin();
        Competition competition = competitionRepository.save(newCompetition().with(id(null)).build());

        List<Application> applications = newApplication()
                .withCompetition(competition)
                .withActivityState(SUBMITTED)
                .with(id(null))
                .build(2);

        applicationRepository.saveAll(applications);

        InterviewAssignment interviewPanel = newInterviewAssignment()
                .with(id(null))
                .withState(InterviewAssignmentState.CREATED)
                .withTarget(applications.get(0))
                .build();

        interviewAssignmentRepository.save(interviewPanel);

        Pageable pageable = new PageRequest(1, 20);

        Page<Application> invitableApplications = repository.findSubmittedApplicationsNotOnInterviewPanel(competition
                .getId(), pageable);

        assertEquals(1, invitableApplications.getTotalElements());
    }

    @Test
    public void findSubmittedApplicationsNotOnInterviewPanel_inviteSent() {
        loginCompAdmin();
        Competition competition = competitionRepository.save(newCompetition().with(id(null)).build());

        List<Application> applications = newApplication()
                .with(id(null))
                .withCompetition(competition)
                .withActivityState(SUBMITTED)
                .build(2);

        applicationRepository.saveAll(applications);

        InterviewAssignment interviewAssignment = newInterviewAssignment()
                .with(id(null))
                .withState(InterviewAssignmentState.AWAITING_FEEDBACK_RESPONSE)
                .withTarget(applications.get(0))
                .build();

        interviewAssignmentRepository.save(interviewAssignment);

        Pageable pageable = new PageRequest(1, 20);

        Page<Application> invitableApplications = repository.findSubmittedApplicationsNotOnInterviewPanel(competition
                .getId(), pageable);

        assertEquals(1, invitableApplications.getTotalElements());
    }

    @Test
    public void findByProjectId() {
        Application application = applicationRepository.save(newApplication().withId(17L).build());
        Project project = projectRepository.save(newProject()
                .withApplication(application)
                .withId(17L)
                .withName("Project Name")
                .build()
        );

        Application retrieved = repository.findByProjectId(project.getId());

        assertEquals(application, retrieved);
    }

    @Test
    public void findByAssessmentId() {
        Application application = repository.save(newApplication().build());

        Assessment assessment = assessmentRepository.save(newAssessment()
                .with(id(null))
                .withApplication(application)
                .withProcessState(AssessmentState.SUBMITTED)
                .build()
        );

        Application retrieved = repository.findByAssessmentId(assessment.getId());

        assertEquals(application, retrieved);
    }

    @Test
    public void findTopByCompetitionIdOrderByManageFundingEmailDateDesc() {
        loginCompAdmin();

        List<Competition> competitions = stream(
                competitionRepository.saveAll(newCompetition()
                        .with(id(null))
                        .build(2)).spliterator(), false).collect(toList());

        ZonedDateTime[] zonedDateTimes = IntStream.rangeClosed(1, 6).mapToObj(i ->
                ZonedDateTime.of(2018, 8, 1, i, 0, 0, 0, ZoneId.systemDefault()))
                .toArray(ZonedDateTime[]::new);

        Competition competition1 = competitions.get(0);
        Competition competition2 = competitions.get(1);

        List<Application> applications = newApplication()
                .with(id(null))
                .withCompetition(competition1, competition1, competition1, competition2, competition2, competition2)
                .withManageFundingEmailDate(zonedDateTimes)
                .build(6);

        List<Application> saved = stream(repository.saveAll(applications).spliterator(), false).collect(toList());

        Application expectedApplicationComp2WithMaxDate = saved.get(5);

        assertEquals(expectedApplicationComp2WithMaxDate, repository
                .findTopByCompetitionIdOrderByManageFundingEmailDateDesc(competition2.getId()));
    }

    private Application createApplicationByState(ApplicationState applicationState) {
        Application application = newApplication()
                .with(id(null))
                .withApplicationState(applicationState)
                .build();
        application.getApplicationProcess().setProcessState(applicationState);
        return application;
    }
}