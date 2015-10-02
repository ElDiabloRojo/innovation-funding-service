package com.worth.ifs;

import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dwatson on 02/10/15.
 */
@Transactional
public abstract class BaseRepositoryIntegrationTest<RepositoryType> extends BaseIntegrationTest {

    protected RepositoryType repository;

    @Autowired
    protected abstract void setRepository(RepositoryType repository);
}