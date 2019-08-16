package org.innovateuk.ifs.finance.resource.cost;

import javax.validation.constraints.Digits;
import java.math.BigDecimal;

public class GrantClaimPercentage extends AbstractFinanceRowItem implements GrantClaim {
    private Long id;

    @Digits(integer = MAX_DIGITS, fraction = 0, message = MAX_DIGITS_MESSAGE)
    private Integer percentage;

    private GrantClaimPercentage() {
        this(null);
    }

    public GrantClaimPercentage(Long targetId) {
        super(targetId);
    }

    public GrantClaimPercentage(Long id, Integer percentage, Long targetId) {
        this(targetId);
        this.id = id;
        this.percentage = percentage;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public BigDecimal getTotal() {
        if (percentage == null) {
            return null;
        }
        return new BigDecimal(percentage);
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    @Override
    public String getName() {
        return getCostType().getType();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getMinRows() {
        return 0;
    }

    @Override
    public FinanceRowType getCostType() {
        return FinanceRowType.FINANCE;
    }

    @Override
    public void reset() {
        percentage = null;
    }

    @Override
    public Integer calculateClaimPercentage(BigDecimal total, BigDecimal totalOtherFunding) {
        return percentage;
    }

    @Override
    public boolean isRequestingFunding() {
        return percentage != null && !percentage.equals(0);
    }

    @Override
    public BigDecimal calculateFundingSought(BigDecimal total, BigDecimal otherFunding) {
        if (percentage == null) {
            return BigDecimal.ZERO;
        }
        return total.multiply(new BigDecimal(percentage))
                .divide(new BigDecimal(100))
                .subtract(otherFunding);
    }
}
