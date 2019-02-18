package org.innovateuk.ifs.competitionsetup.transactional;

import org.innovateuk.ifs.BaseUnitTestMocksTest;
import org.innovateuk.ifs.competition.resource.AssessorCountOptionResource;
import org.innovateuk.ifs.competitionsetup.domain.AssessorCountOption;
import org.innovateuk.ifs.competitionsetup.fixtures.AssessorCountOptionFixture;
import org.innovateuk.ifs.competitionsetup.mapper.AssessorCountOptionMapper;
import org.innovateuk.ifs.competitionsetup.repository.AssessorCountOptionRepository;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class AssessorCountOptionServiceImplTest extends BaseUnitTestMocksTest {

	@InjectMocks
	private AssessorCountOptionService assessorCountOptionService = new AssessorCountOptionServiceImpl();

	@Mock
	private AssessorCountOptionRepository assessorCountOptionRepositoryMock;

	@Mock
	private AssessorCountOptionMapper assessorCountOptionMapperMock;

	@Test
	public void testFindAllByCompetitionType() throws Exception {
		List<AssessorCountOption> options = AssessorCountOptionFixture.programmeAssessorOptionsList();
		List<AssessorCountOptionResource> expectedResponse = AssessorCountOptionFixture.programmeAssessorOptionResourcesList();

		when(assessorCountOptionRepositoryMock.findByCompetitionTypeId(anyLong())).thenReturn(options);
		when(assessorCountOptionMapperMock.mapToResource(same(options.get(0)))).thenReturn((expectedResponse.get(0)));
		when(assessorCountOptionMapperMock.mapToResource(same(options.get(1)))).thenReturn((expectedResponse.get(1)));
		when(assessorCountOptionMapperMock.mapToResource(same(options.get(2)))).thenReturn((expectedResponse.get(2)));

		List<AssessorCountOptionResource> actualResponse = assessorCountOptionService.findAllByCompetitionType(1L).getSuccess();

		assertEquals(expectedResponse, actualResponse);
		verify(assessorCountOptionRepositoryMock, only()).findByCompetitionTypeId(1L);
	}
}
