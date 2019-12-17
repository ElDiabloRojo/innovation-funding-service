package org.innovateuk.ifs.interview.transactional;

import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.interview.repository.InterviewAssignmentRepository;
import org.innovateuk.ifs.interview.repository.InterviewInviteRepository;
import org.innovateuk.ifs.interview.repository.InterviewParticipantRepository;
import org.innovateuk.ifs.interview.resource.InterviewAssignmentKeyStatisticsResource;
import org.innovateuk.ifs.interview.resource.InterviewAssignmentState;
import org.innovateuk.ifs.interview.resource.InterviewInviteStatisticsResource;
import org.innovateuk.ifs.interview.resource.InterviewStatisticsResource;
import org.innovateuk.ifs.invite.domain.Invite;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.domain.CompetitionParticipantRole.INTERVIEW_ASSESSOR;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.SENT;
import static org.innovateuk.ifs.util.CollectionFunctions.asLinkedSet;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * Service to get statistics related to Interview Panels.
 */
@Service
@Transactional(readOnly = true)
public class InterviewStatisticsServiceImpl implements InterviewStatisticsService{

    private InterviewInviteRepository interviewInviteRepository;
    private InterviewParticipantRepository interviewParticipantRepository;
    private ApplicationRepository applicationRepository;
    private InterviewAssignmentRepository interviewAssignmentRepository;

    public InterviewStatisticsServiceImpl() {
    }

    @Autowired
    public InterviewStatisticsServiceImpl(InterviewInviteRepository interviewInviteRepository,
                                          InterviewParticipantRepository interviewParticipantRepository,
                                          ApplicationRepository applicationRepository,
                                          InterviewAssignmentRepository interviewAssignmentRepository) {
        this.interviewInviteRepository = interviewInviteRepository;
        this.interviewParticipantRepository = interviewParticipantRepository;
        this.applicationRepository = applicationRepository;
        this.interviewAssignmentRepository = interviewAssignmentRepository;
    }

    @Override
    public ServiceResult<InterviewAssignmentKeyStatisticsResource> getInterviewAssignmentPanelKeyStatistics(long competitionId) {
        int applicationsInCompetition = applicationRepository.countByCompetitionIdAndApplicationProcessActivityState(competitionId, ApplicationState.SUBMITTED);
        int applicationsAssigned = interviewAssignmentRepository.
                countByTargetCompetitionIdAndActivityStateIn(competitionId, asLinkedSet(InterviewAssignmentState.assignedStates()));

        return serviceSuccess(new InterviewAssignmentKeyStatisticsResource(applicationsInCompetition, applicationsAssigned));
    }

    @Override
    public ServiceResult<InterviewInviteStatisticsResource> getInterviewInviteStatistics(long competitionId) {
        List<Long> interviewPanelInviteIds = simpleMap(interviewInviteRepository.getByCompetitionId(competitionId), Invite::getId);

        int totalAssessorsInvited = interviewInviteRepository.countByCompetitionIdAndStatusIn(competitionId, EnumSet.of(OPENED, SENT));
        int assessorsAccepted = getInterviewParticipantCountStatistic(competitionId, ParticipantStatus.ACCEPTED, interviewPanelInviteIds);
        int assessorsDeclined = getInterviewParticipantCountStatistic(competitionId, ParticipantStatus.REJECTED, interviewPanelInviteIds);

        return serviceSuccess(
                new InterviewInviteStatisticsResource(
                        totalAssessorsInvited,
                        assessorsAccepted,
                        assessorsDeclined
                )
        );
    }

    @Override
    public ServiceResult<InterviewStatisticsResource> getInterviewStatistics(long competitionId) {
        List<Long> interviewPanelInviteIds = simpleMap(interviewInviteRepository.getByCompetitionId(competitionId), Invite::getId);

        int applicationsAssigned = interviewAssignmentRepository.
                countByTargetCompetitionIdAndActivityStateIn(competitionId, asLinkedSet(InterviewAssignmentState.assignedStates()));
        int interviewResponses = interviewAssignmentRepository.
                countByTargetCompetitionIdAndActivityStateIn(competitionId, asLinkedSet(InterviewAssignmentState.SUBMITTED_FEEDBACK_RESPONSE));
        int assessorsAccepted = getInterviewParticipantCountStatistic(competitionId, ParticipantStatus.ACCEPTED, interviewPanelInviteIds);

        return serviceSuccess(
                new InterviewStatisticsResource(
                        applicationsAssigned,
                        interviewResponses,
                        assessorsAccepted
                )
        );
    }

    private int getInterviewParticipantCountStatistic(long competitionId, ParticipantStatus status, List<Long> inviteIds) {
        return interviewParticipantRepository.countByCompetitionIdAndRoleAndStatusAndInviteIdIn(competitionId, INTERVIEW_ASSESSOR, status, inviteIds);
    }
}