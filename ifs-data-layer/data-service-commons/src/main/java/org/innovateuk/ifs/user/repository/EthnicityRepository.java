package org.innovateuk.ifs.user.repository;

import org.innovateuk.ifs.commons.ZeroDowntime;
import org.innovateuk.ifs.user.domain.Ethnicity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
@ZeroDowntime(description = "delete", reference = "IFS-4105")
public interface EthnicityRepository extends CrudRepository<Ethnicity, Long> {
    List<Ethnicity> findByActiveTrueOrderByPriorityAsc();

    Ethnicity findOneByDescription(String description);
}
