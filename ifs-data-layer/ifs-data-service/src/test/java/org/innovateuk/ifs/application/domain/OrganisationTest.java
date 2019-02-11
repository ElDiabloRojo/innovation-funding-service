package org.innovateuk.ifs.application.domain;

import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.domain.OrganisationType;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OrganisationTest {
    private Organisation organisation;

    private String name;

    private List<ProcessRole> processRoles;

    private OrganisationType organisationType;

    @Before
    public void setUp() throws Exception {
        name = "test organisation";
        processRoles = new ArrayList<>();
        organisation = new Organisation(name);
        organisationType = new OrganisationType("Business", "Description of the current organisationtype", null);
        organisation.setOrganisationType(organisationType);
    }

    @Test
    public void organisationShouldReturnCorrectAttributeValues() {
        Assert.assertEquals(organisation.getName(), name);
        Assert.assertEquals(organisation.getProcessRoles(), processRoles);
        Assert.assertEquals(organisation.getOrganisationType(), organisationType);
    }
}