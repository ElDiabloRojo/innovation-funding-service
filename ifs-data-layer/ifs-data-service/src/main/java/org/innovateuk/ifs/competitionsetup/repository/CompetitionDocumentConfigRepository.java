package org.innovateuk.ifs.competitionsetup.repository;

import org.innovateuk.ifs.competitionsetup.domain.CompetitionDocument;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface CompetitionDocumentConfigRepository extends PagingAndSortingRepository<CompetitionDocument, Long> {

    List<CompetitionDocument> findByCompetitionId(Long competitionId);
}

