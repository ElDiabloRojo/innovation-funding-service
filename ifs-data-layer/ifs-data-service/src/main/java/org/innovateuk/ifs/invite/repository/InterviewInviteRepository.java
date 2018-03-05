package org.innovateuk.ifs.invite.repository;

import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.competition.InterviewInvite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Set;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface InterviewInviteRepository extends PagingAndSortingRepository<InterviewInvite, Long> {

    InterviewInvite getByEmailAndCompetitionId(String email, long competitionId);

    List<InterviewInvite> getByCompetitionId(long competitionId);

    List<InterviewInvite> getByCompetitionIdAndStatus(long competitionId, InviteStatus status);

    Page<InterviewInvite> getByCompetitionIdAndStatus(long competitionId, InviteStatus status, Pageable pageable);

    int countByCompetitionIdAndStatusIn(long competitionId, Set<InviteStatus> statuses);

    List<InterviewInvite> getByUserId(long userId);

    List<InterviewInvite> getByIdIn(List<Long> inviteIds);

    InterviewInvite getByHash(String hash);

    void deleteByCompetitionIdAndStatus(long competitionId, InviteStatus status);
}
