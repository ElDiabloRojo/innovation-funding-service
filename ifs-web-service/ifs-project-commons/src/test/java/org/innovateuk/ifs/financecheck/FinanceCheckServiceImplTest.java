package org.innovateuk.ifs.financecheck;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.project.finance.resource.FinanceCheckResource;
import org.innovateuk.ifs.project.finance.service.FinanceCheckRestService;
import org.innovateuk.ifs.project.finance.service.ProjectFinanceQueriesRestService;
import org.innovateuk.ifs.project.resource.ProjectOrganisationCompositeId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.project.finance.builder.FinanceCheckResourceBuilder.newFinanceCheckResource;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FinanceCheckServiceImplTest {
    @InjectMocks
    private FinanceCheckServiceImpl service;

    @Mock
    private FinanceCheckRestService financeCheckRestServiceMock;

    @Mock
    private ProjectFinanceQueriesRestService projectFinanceQueriesRestServiceMock;

    @Test
    public void testGet(){
        Long projectId = 123L;
        Long organisationId = 456L;
        ProjectOrganisationCompositeId key = new ProjectOrganisationCompositeId(projectId, organisationId);

        FinanceCheckResource financeCheckResource = newFinanceCheckResource().build();

        when(financeCheckRestServiceMock.getByProjectAndOrganisation(projectId, organisationId)).thenReturn(restSuccess(financeCheckResource));

        FinanceCheckResource result = service.getByProjectAndOrganisation(key);

        assertEquals(financeCheckResource, result);
    }

    @Test
    public void testCloseQuery(){

        Long queryId = 1L;
        when(projectFinanceQueriesRestServiceMock.close(queryId)).thenReturn(restSuccess());

        ServiceResult<Void> result = service.closeQuery(queryId);
        assertTrue(result.isSuccess());
        verify(projectFinanceQueriesRestServiceMock).close(queryId);
    }
}
