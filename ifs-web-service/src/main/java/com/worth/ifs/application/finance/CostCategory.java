package com.worth.ifs.application.finance;

import com.worth.ifs.application.finance.cost.CostItem;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface CostCategory {
    public List<CostItem> getCosts();

    public void saveCosts(HttpServletRequest request, CostType costType);
    public Double getTotal();
    public void addCost(CostItem costItem);
}
