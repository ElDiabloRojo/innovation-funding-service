package com.worth.ifs.finance.security;

import com.worth.ifs.BasePermissionRulesTest;
import com.worth.ifs.application.domain.Application;
import com.worth.ifs.competition.domain.Competition;
import com.worth.ifs.finance.domain.ApplicationFinance;
import com.worth.ifs.finance.domain.Cost;
import com.worth.ifs.finance.resource.cost.AcademicCost;
import com.worth.ifs.finance.resource.cost.CostItem;
import com.worth.ifs.user.domain.Organisation;
import com.worth.ifs.user.domain.Role;
import com.worth.ifs.user.resource.UserResource;

import org.junit.Before;
import org.junit.Test;

import static com.worth.ifs.BuilderAmendFunctions.id;
import static com.worth.ifs.application.builder.ApplicationBuilder.newApplication;
import static com.worth.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static com.worth.ifs.competition.resource.CompetitionResource.Status.OPEN;
import static com.worth.ifs.finance.builder.ApplicationFinanceBuilder.newApplicationFinance;
import static com.worth.ifs.finance.builder.CostBuilder.newCost;
import static com.worth.ifs.user.builder.OrganisationBuilder.newOrganisation;
import static com.worth.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static com.worth.ifs.user.builder.UserResourceBuilder.newUserResource;
import static com.worth.ifs.user.resource.UserRoleType.COLLABORATOR;
import static com.worth.ifs.user.resource.UserRoleType.LEADAPPLICANT;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CostPermissionRulesTest extends BasePermissionRulesTest<CostPermissionRules> {

    private Cost cost;
    private CostItem costItem;
    private Cost otherCost;
    private UserResource leadApplicant;
    private UserResource collaborator;
    private UserResource compAdmin;
    private UserResource assessor;
    private UserResource otherLeadApplicant;

    @Override
    protected CostPermissionRules supplyPermissionRulesUnderTest() {
        return new CostPermissionRules();
    }

    @Before
    public void setup() throws Exception {

        // Create a compAdmin
        compAdmin = compAdminUser();

        // Set up global role method mocks
        for (Role role : allRoles) {
            when(roleRepositoryMock.findByName(role.getName())).thenReturn(asList(role));
        }
        {
            // Set up users on an organisation and application
            final Long applicationId = 1L;
            final Long organisationId = 2L;
            final Competition openCompetition = newCompetition().withCompetitionStatus(OPEN).withEndDate(now().plusSeconds(1)).build();

            final Application application = newApplication().withId(applicationId).withCompetition(openCompetition).build();
            final Organisation organisation = newOrganisation().with(id(organisationId)).build();
            final ApplicationFinance applicationFinance = newApplicationFinance().withApplication(application).withOrganisation(organisation).build();
            cost = newCost().withApplicationFinance(applicationFinance).build();
            costItem = new AcademicCost(cost.getId(), "", ZERO, "");

            leadApplicant = newUserResource().build();
            collaborator = newUserResource().build();
            when(costRepositoryMock.findOne(cost.getId())).thenReturn(cost);
            when(processRoleRepositoryMock.findByUserIdAndRoleIdAndApplicationIdAndOrganisationId(leadApplicant.getId(), getRole(LEADAPPLICANT).getId(), applicationId, organisationId)).thenReturn(newProcessRole().build());
            when(processRoleRepositoryMock.findByUserIdAndRoleIdAndApplicationIdAndOrganisationId(collaborator.getId(), getRole(COLLABORATOR).getId(), applicationId, organisationId)).thenReturn(newProcessRole().build());
        }
        {
            // Set up different users on an organisation and application to check that there is no bleed through of permissions
            final long otherApplicationId = 3l;
            final long otherOrganisationId = 4l;
            final Organisation otherOrganisation = newOrganisation().with(id(otherOrganisationId)).build();
            final Application otherApplication = newApplication().with(id(otherApplicationId)).build();
            final ApplicationFinance otherApplicationFinance = newApplicationFinance().withOrganisation(otherOrganisation).withApplication(otherApplication).build();
            otherCost = newCost().withApplicationFinance(otherApplicationFinance).build();
            otherLeadApplicant = newUserResource().build();
            when(processRoleRepositoryMock.findByUserIdAndRoleIdAndApplicationIdAndOrganisationId(otherLeadApplicant.getId(), getRole(LEADAPPLICANT).getId(), otherApplicationId, otherOrganisationId)).thenReturn(newProcessRole().build());
        }
    }

    @Test
    public void testConsortiumCanDeleteACostForTheirApplicationAndOrganisation() {
        assertTrue(rules.consortiumCanDeleteACostForTheirApplicationAndOrganisation(cost, leadApplicant));
        assertTrue(rules.consortiumCanDeleteACostForTheirApplicationAndOrganisation(cost, collaborator));

        assertFalse(rules.consortiumCanDeleteACostForTheirApplicationAndOrganisation(cost, otherLeadApplicant));
        assertFalse(rules.consortiumCanDeleteACostForTheirApplicationAndOrganisation(cost, compAdmin));
    }

    @Test
    public void testConsortiumCanUpdateACostForTheirApplicationAndOrganisation() {
        assertTrue(rules.consortiumCanUpdateACostForTheirApplicationAndOrganisation(cost, leadApplicant));
        assertTrue(rules.consortiumCanUpdateACostForTheirApplicationAndOrganisation(cost, collaborator));

        assertFalse(rules.consortiumCanUpdateACostForTheirApplicationAndOrganisation(cost, otherLeadApplicant));
        assertFalse(rules.consortiumCanUpdateACostForTheirApplicationAndOrganisation(cost, compAdmin));
    }


    @Test
    public void testConsortiumCanReadACostForTheirApplicationAndOrganisation() {
        assertTrue(rules.consortiumCanReadACostForTheirApplicationAndOrganisation(cost, leadApplicant));
        assertTrue(rules.consortiumCanReadACostForTheirApplicationAndOrganisation(cost, collaborator));

        assertFalse(rules.consortiumCanReadACostForTheirApplicationAndOrganisation(cost, otherLeadApplicant));
        assertFalse(rules.consortiumCanReadACostForTheirApplicationAndOrganisation(cost, compAdmin));
    }

    @Test
    public void testConsortiumCanReadACostForTheirApplicationAndOrganisation2() {
        assertTrue(rules.consortiumCanReadACostItemForTheirApplicationAndOrganisation(costItem, leadApplicant));
        assertTrue(rules.consortiumCanReadACostItemForTheirApplicationAndOrganisation(costItem, collaborator));

        assertFalse(rules.consortiumCanReadACostItemForTheirApplicationAndOrganisation(costItem, otherLeadApplicant));
        assertFalse(rules.consortiumCanReadACostItemForTheirApplicationAndOrganisation(costItem, compAdmin));
    }

}
