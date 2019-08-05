package org.innovateuk.ifs.finance.validator;

import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.domain.ApplicationFinanceRow;
import org.innovateuk.ifs.finance.domain.FinanceRow;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRowRepository;
import org.innovateuk.ifs.finance.resource.cost.GrantClaimPercentage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.innovateuk.ifs.commons.error.ValidationMessages.rejectValue;

/**
 * This class validates the GrantClaim.
 */
@Component
public class GrantClaimValidator implements Validator {

    @Autowired
    private ApplicationFinanceRowRepository financeRowRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return GrantClaimPercentage.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        GrantClaimPercentage response = (GrantClaimPercentage) target;

        if(response.getGrantClaimPercentage() == null) {
            rejectValue(errors, "grantClaimPercentage", "org.hibernate.validator.constraints.NotBlank.message");
            return;
        }

        FinanceRow cost = financeRowRepository.findById(response.getId()).get();
        ApplicationFinance applicationFinance = ((ApplicationFinanceRow)cost).getTarget();
        Integer max = applicationFinance.getMaximumFundingLevel();
        if (max == null) {
            rejectValue(errors, "grantClaimPercentage", "validation.grantClaimPercentage.maximum.not.defined");
            return;
        }

        if (response.getGrantClaimPercentage() > max) {
            rejectValue(errors, "grantClaimPercentage", "validation.finance.grant.claim.percentage.max", max);
        } else if(response.getGrantClaimPercentage() < 0) {
            rejectValue(errors, "grantClaimPercentage", "validation.field.percentage.max.value.or.higher", 0);
        }
    }

}
