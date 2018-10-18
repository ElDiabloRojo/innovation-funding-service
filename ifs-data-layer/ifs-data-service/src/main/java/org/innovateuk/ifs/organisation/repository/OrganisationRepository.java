package org.innovateuk.ifs.organisation.repository;

import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganisationRepository extends PagingAndSortingRepository<Organisation, Long> {

    Organisation findByProcessRoles(@Param("processRoles") ProcessRole processRole);
    Organisation findOneByName(String name);
    List<Organisation> findDistinctByUsers(User user);
    List<Organisation> findDistinctByUsersId(long userId);
    List<Organisation> findByNameOrderById(String name);
    List<Organisation> findByCompaniesHouseNumberOrderById(String companiesHouseNumber);
    Organisation findByProcessRolesUserIdAndProcessRolesApplicationId(long userId, long applicationId);

    @Query("SELECT o FROM Organisation o " +
            "JOIN ProcessRole pr ON o.id = pr.organisationId " +
            "JOIN Project p ON pr.applicationId=p.application.id " +
            "WHERE pr.user.id = :userId " +
            "AND p.id = :projectId")
    Organisation findByUserAndProjectId(long userId, long projectId);
    List<Organisation> findAll(Iterable<Long> ids);
}