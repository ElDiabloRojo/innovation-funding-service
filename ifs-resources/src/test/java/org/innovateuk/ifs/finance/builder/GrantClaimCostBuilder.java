package org.innovateuk.ifs.finance.builder;

import org.innovateuk.ifs.finance.resource.cost.GrantClaimPercentage;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.idBasedNames;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;

public class GrantClaimCostBuilder extends AbstractCostBuilder<GrantClaimPercentage, GrantClaimCostBuilder> {

    public GrantClaimCostBuilder withId(Long... id) {
        return withArraySetFieldByReflection("id", id);
    }

    public GrantClaimCostBuilder withGrantClaimPercentage(Integer... value) {
        return withArray((v, cost) -> cost.setGrantClaimPercentage(v), value);
    }

    public static GrantClaimCostBuilder newGrantClaim() {
        return new GrantClaimCostBuilder(emptyList()).with(uniqueIds()).with(idBasedNames("Grant Claim "));
    }

    private GrantClaimCostBuilder(List<BiConsumer<Integer, GrantClaimPercentage>> multiActions) {
        super(multiActions);
    }

    @Override
    protected GrantClaimCostBuilder createNewBuilderWithActions(List<BiConsumer<Integer, GrantClaimPercentage>> actions) {
        return new GrantClaimCostBuilder(actions);
    }

    @Override
    protected GrantClaimPercentage createInitial() {
        return newInstance(GrantClaimPercentage.class);
    }
}