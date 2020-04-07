package org.innovateuk.ifs.organisation.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.address.domain.AddressType;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.organisation.domain.OrganisationApplicationAddress;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;

public class OrganisationAddressBuilder extends BaseBuilder<OrganisationApplicationAddress, OrganisationAddressBuilder> {
    private OrganisationAddressBuilder(List<BiConsumer<Integer, OrganisationApplicationAddress>> multiActions) {
        super(multiActions);
    }

    public static OrganisationAddressBuilder newOrganisationAddress() {
        return new OrganisationAddressBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected OrganisationAddressBuilder createNewBuilderWithActions(List<BiConsumer<Integer, OrganisationApplicationAddress>> actions) {
        return new OrganisationAddressBuilder(actions);
    }

    @Override
    protected OrganisationApplicationAddress createInitial() {
        return new OrganisationApplicationAddress();
    }

    public OrganisationAddressBuilder withAddressType(AddressType... addressTypes) {
        return withArray((addressType, orgAddress) -> setField("addressType", addressType, orgAddress), addressTypes);
    }

    public OrganisationAddressBuilder withAddress(Address... addresses) {
        return withArray((address, orgAddress) -> setField("address", address, orgAddress), addresses);
    }

    public OrganisationAddressBuilder withOrganisation(Organisation organisation){
        return with(orgAddress -> orgAddress.setOrganisation(organisation));
    }
}
