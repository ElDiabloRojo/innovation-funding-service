package org.innovateuk.ifs.project.financecheck.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.project.financechecks.domain.Cost;
import org.innovateuk.ifs.project.financechecks.domain.CostGroup;
import org.innovateuk.ifs.project.financechecks.repository.CostGroupRepository;
import org.innovateuk.ifs.project.financechecks.repository.CostRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.junit.Assert.*;

/**
 * Repository Integration tests for Costs.
 */
public class CostGroupRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<CostGroupRepository> {

    @Override
    @Autowired
    protected void setRepository(CostGroupRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private CostRepository costRepository;

    @Test
    @Rollback
    public void test_createCostGroup() {

        List<Cost> costs = asList(
                new Cost("12.6"),
                new Cost("13"),
                new Cost("14.567")
        );

        CostGroup newCostGroup = new CostGroup("My collection of costs", costs);

        // save a new CostGroup
        CostGroup saved = repository.save(newCostGroup);

        // clear the Hibernate cache
        flushAndClearSession();

        // and retrieve from the db again - ensure its value is retained
        CostGroup retrieved = repository.findById(saved.getId()).get();
        assertNotSame(saved, retrieved);
        assertEquals("My collection of costs", retrieved.getDescription());

        List<BigDecimal> expectedCosts = asList(
                new BigDecimal("12.60"),
                new BigDecimal("13.00"),
                new BigDecimal("14.57")
        );

        assertEquals(expectedCosts, simpleMap(retrieved.getCosts(), Cost::getValue));
        retrieved.getCosts().forEach(c -> assertEquals(retrieved, c.getCostGroup()));

        // now individually retrieve the costs and assert that they are as expected when retrieved individually as well
        retrieved.getCosts().forEach(c -> {
            Cost individual = costRepository.findById(c.getId()).get();
            assertEquals(c.getValue(), individual.getValue());
            assertEquals(c.getCostGroup(), individual.getCostGroup());
        });
    }

    @Test
    @Rollback
    public void test_updateCostGroup() {

        List<Cost> costs = asList(
                new Cost("12.6"),
                new Cost("13"),
                new Cost("14.567")
        );

        CostGroup newCostGroup = new CostGroup("My collection of costs", costs);

        // save a new CostGroup
        CostGroup saved = repository.save(newCostGroup);

        // clear the Hibernate cache
        flushAndClearSession();

        // and update the set of costs
        CostGroup retrieved = repository.findById(saved.getId()).get();
        List<Cost> updatedCosts = new ArrayList<>();
        updatedCosts.add(retrieved.getCosts().get(0));
        updatedCosts.get(0).setValue(new BigDecimal("1"));
        updatedCosts.add(retrieved.getCosts().get(2));

        // update the CostGroup
        newCostGroup.setCosts(updatedCosts);
        repository.save(newCostGroup);

        // clear the Hibernate cache
        flushAndClearSession();

        CostGroup retrievedAgain = repository.findById(saved.getId()).get();

        List<BigDecimal> expectedCosts = asList(
                new BigDecimal("1.00"),
                new BigDecimal("14.57")
        );

        assertEquals(expectedCosts, simpleMap(retrievedAgain.getCosts(), Cost::getValue));
        retrievedAgain.getCosts().forEach(c -> assertEquals(retrievedAgain, c.getCostGroup()));
    }

    @Test
    @Rollback
    public void test_deleteCostGroupWithOrphanRemoval() {

        List<Cost> costs = asList(
                new Cost("12.6"),
                new Cost("13"),
                new Cost("14.567")
        );

        CostGroup newCostGroup = new CostGroup("My collection of costs", costs);

        // save a new CostGroup
        CostGroup saved = repository.save(newCostGroup);

        // clear the Hibernate cache
        flushAndClearSession();

        // delete the CostGroup
        List<Long> costIds = simpleMap(saved.getCosts(), Cost::getId);
        repository.deleteById(saved.getId());

        // and assert that the individual costs were deleted as a part of the delete
        costIds.forEach(id -> assertFalse(costRepository.findById(id).isPresent()));
    }
}
