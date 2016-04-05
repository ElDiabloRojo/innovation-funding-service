package com.worth.ifs.application.transactional;

import static com.worth.ifs.user.builder.RoleBuilder.newRole;
import static com.worth.ifs.user.builder.UserBuilder.newUser;
import static com.worth.ifs.user.domain.UserRoleType.COMP_ADMIN;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import com.worth.ifs.BaseServiceSecurityTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.application.resource.ApplicationSummaryPageResource;
import com.worth.ifs.application.resource.ClosedCompetitionApplicationSummaryPageResource;
import com.worth.ifs.application.resource.CompetitionSummaryResource;
import com.worth.ifs.commons.service.ServiceResult;
import com.worth.ifs.user.domain.UserRoleType;

public class ApplicationSummaryServiceSecurityTest extends BaseServiceSecurityTest<ApplicationSummaryService> {

	@Test
	public void test_applicationSummariesByCompetitionId_allowedIfGlobalCompAdminRole() {

		setLoggedInUser(newUser().withRolesGlobal(newRole().withType(COMP_ADMIN).build()).build());
		service.getApplicationSummariesByCompetitionId(123L, 0, null);
	}

	@Test
	public void test_applicationSummariesByCompetitionId_deniedIfNotLoggedIn() {

		setLoggedInUser(null);
		try {
			service.getApplicationSummariesByCompetitionId(123L, 0, null);
			fail("Should not have been able to get application summaries without first logging in");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void test_applicationSummariesByCompetitionId_deniedIfNoGlobalRolesAtAll() {

		try {
			service.getApplicationSummariesByCompetitionId(123L, 0, null);
			fail("Should not have been able to get application summaries without the global comp admin role");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void test_applicationSummariesByCompetitionId_deniedIfNotCorrectGlobalRoles() {

		List<UserRoleType> nonCompAdminRoles = asList(UserRoleType.values()).stream().filter(type -> type != COMP_ADMIN)
				.collect(toList());

		nonCompAdminRoles.forEach(role -> {

			setLoggedInUser(newUser().withRolesGlobal(newRole().withType(role).build()).build());

			try {
				service.getApplicationSummariesByCompetitionId(123L, 0, null);
				fail("Should not have been able to get application summaries without the global Comp Admin role");
			} catch (AccessDeniedException e) {
				// expected behaviour
			}
		});
	}
	
	@Test
	public void test_submittedApplicationSummariesByClosedCompetitionId_allowedIfGlobalCompAdminRole() {

		setLoggedInUser(newUser().withRolesGlobal(newRole().withType(COMP_ADMIN).build()).build());
		service.getSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
	}

	@Test
	public void test_aubmittedApplicationSummariesByClosedCompetitionId_deniedIfNotLoggedIn() {

		setLoggedInUser(null);
		try {
			service.getSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
			fail("Should not have been able to get application summaries without first logging in");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void test_submittedApplicationSummariesByClosedCompeititonId_deniedIfNoGlobalRolesAtAll() {

		try {
			service.getSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
			fail("Should not have been able to get application summaries without the global comp admin role");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void test_submittedApplicationSummariesByClosedCompetitionId_deniedIfNotCorrectGlobalRoles() {

		List<UserRoleType> nonCompAdminRoles = asList(UserRoleType.values()).stream().filter(type -> type != COMP_ADMIN)
				.collect(toList());

		nonCompAdminRoles.forEach(role -> {

			setLoggedInUser(newUser().withRolesGlobal(newRole().withType(role).build()).build());

			try {
				service.getSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
				fail("Should not have been able to get application summaries without the global Comp Admin role");
			} catch (AccessDeniedException e) {
				// expected behaviour
			}
		});
	}
	@Test
	public void test_notSubmittedApplicationSummariesByClosedCompetitionId_allowedIfGlobalCompAdminRole() {

		setLoggedInUser(newUser().withRolesGlobal(newRole().withType(COMP_ADMIN).build()).build());
		service.getNotSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
	}

	@Test
	public void test_notSubmittedApplicationSummariesByClosedCompetitionId_deniedIfNotLoggedIn() {

		setLoggedInUser(null);
		try {
			service.getNotSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
			fail("Should not have been able to get application summaries without first logging in");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void test_notSubmittedApplicationSummariesByClosedCompeititonId_deniedIfNoGlobalRolesAtAll() {

		try {
			service.getNotSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
			fail("Should not have been able to get application summaries without the global comp admin role");
		} catch (AccessDeniedException e) {
			// expected behaviour
		}
	}

	@Test
	public void test_notSubmittedApplicationSummariesByClosedCompetitionId_deniedIfNotCorrectGlobalRoles() {

		List<UserRoleType> nonCompAdminRoles = asList(UserRoleType.values()).stream().filter(type -> type != COMP_ADMIN)
				.collect(toList());

		nonCompAdminRoles.forEach(role -> {

			setLoggedInUser(newUser().withRolesGlobal(newRole().withType(role).build()).build());

			try {
				service.getNotSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(123L, 0, null);
				fail("Should not have been able to get application summaries without the global Comp Admin role");
			} catch (AccessDeniedException e) {
				// expected behaviour
			}
		});
	}

	@Override
	protected Class<? extends ApplicationSummaryService> getServiceClass() {
		return TestApplicationSummaryService.class;
	}

	private static class TestApplicationSummaryService implements ApplicationSummaryService {

		@Override
		public ServiceResult<ApplicationSummaryPageResource> getApplicationSummariesByCompetitionId(Long competitionId,
				int pageIndex, String sortBy) {
			return null;
		}

		@Override
		public ServiceResult<ClosedCompetitionApplicationSummaryPageResource> getSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(
				Long competitionId, int pageIndex, String sortBy) {
			return null;
		}

		@Override
		public ServiceResult<ClosedCompetitionApplicationSummaryPageResource> getNotSubmittedApplicationSummariesForClosedCompetitionByCompetitionId(
				Long competitionId, int pageIndex, String sortBy) {
			return null;
		}

		@Override
		public ServiceResult<CompetitionSummaryResource> getCompetitionSummaryByCompetitionId(Long competitionId) {
			return null;
		}
		
		@Override
		public List<Application> getApplicationSummariesByCompetitionIdAndStatus(Long competitionId, Long applicationStatusId) {
			return null;
		}

	}
}
