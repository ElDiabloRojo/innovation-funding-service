package org.innovateuk.ifs.testdata.builders;

import org.innovateuk.ifs.address.resource.AddressResource;
import org.innovateuk.ifs.address.resource.OrganisationAddressType;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.testdata.builders.data.OrganisationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;

/**
 * Creates Organisations
 */
public class OrganisationDataBuilder extends BaseDataBuilder<OrganisationData, OrganisationDataBuilder> {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisationDataBuilder.class);

    public OrganisationDataBuilder createOrganisation(String organisationName,
                                                      String companyRegistrationNumber,
                                                      OrganisationTypeEnum organisationType) {

        return with(data -> {

            doAs(systemRegistrar(), () -> {

                OrganisationResource created = organisationService.create(newOrganisationResource().
                        withId().
                        withName(organisationName).
                        withCompaniesHouseNumber(companyRegistrationNumber).
                        withOrganisationType(organisationType.getId()).
                        build()).getSuccess();

                data.setOrganisation(created);
            });
        });
    }

    public OrganisationDataBuilder withAddress(OrganisationAddressType addressType, String addressLine1,
                                               String addressLine2, String addressLine3, String town,
                                               String postcode, String county) {

        return with(data -> {

            doAs(systemRegistrar(), () -> {

                AddressResource address = newAddressResource().
                        withId().
                        withAddressLine1(addressLine1).
                        withAddressLine2(addressLine2).
                        withAddressLine3(addressLine3).
                        withTown(town).
                        withPostcode(postcode).
                        withCounty(county).
                        build();

                organisationService.addAddress(data.getOrganisation().getId(), addressType, address);
            });
        });
    }

    public static OrganisationDataBuilder newOrganisationData(ServiceLocator serviceLocator) {

        return new OrganisationDataBuilder(emptyList(), serviceLocator);
    }

    private OrganisationDataBuilder(List<BiConsumer<Integer, OrganisationData>> multiActions,
                                    ServiceLocator serviceLocator) {

        super(multiActions, serviceLocator);
    }

    @Override
    protected OrganisationDataBuilder createNewBuilderWithActions(List<BiConsumer<Integer, OrganisationData>> actions) {
        return new OrganisationDataBuilder(actions, serviceLocator);
    }

    @Override
    protected OrganisationData createInitial() {
        return new OrganisationData();
    }

    @Override
    protected void postProcess(int index, OrganisationData instance) {
        super.postProcess(index, instance);
        LOG.info("Created Organisation '{}'", instance.getOrganisation().getName());
    }
}
