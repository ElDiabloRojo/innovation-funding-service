package org.innovateuk.ifs.organisation.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.organisation.domain.OrganisationType;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.*;

/**
 * Builder for OrganisationResource entities.
 */
public class OrganisationTypeBuilder extends BaseBuilder<OrganisationType, OrganisationTypeBuilder> {

    public OrganisationTypeBuilder withOrganisationType(OrganisationTypeEnum... organisationTypeEnums) {
        return withArray((organisationTypeEnum, organisationType) -> {
            setField("id", organisationTypeEnum.getId(), organisationType);
            setField("name", organisationTypeEnum.name(), organisationType);
        }, organisationTypeEnums);
    }

    private OrganisationTypeBuilder(List<BiConsumer<Integer, OrganisationType>> multiActions) {
        super(multiActions);
    }

    public static OrganisationTypeBuilder newOrganisationType() {
        return new OrganisationTypeBuilder(emptyList()).
                with(uniqueIds()).
                with(idBasedNames("OrganisationType "));
    }

    @Override
    protected OrganisationTypeBuilder createNewBuilderWithActions(List<BiConsumer<Integer, OrganisationType>> actions) {
        return new OrganisationTypeBuilder(actions);
    }

    @Override
    protected OrganisationType createInitial() {
        return new OrganisationType();
    }
}
