package org.innovateuk.ifs.finance.service;

import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.service.BaseRestService;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.costItemListType;

/**
 * FinanceRowRestServiceImpl is a utility for CRUD operations on
 * {@link org.innovateuk.ifs.finance.resource.FinanceRowResource}.
 */
public abstract class BaseFinanceRowRestServiceImpl extends BaseRestService implements FinanceRowRestService {

    private String costRestUrl;

    protected BaseFinanceRowRestServiceImpl(String costRestUrl) {
        this.costRestUrl = costRestUrl;
    }

    @Override
    public RestResult<ValidationMessages> add(Long financeId, Long questionId, FinanceRowItem costItem) {
        return postWithRestResult(costRestUrl + "/add/" + financeId + "/" + questionId, costItem,
                ValidationMessages.class);
    }

    @Override
    public RestResult<FinanceRowItem> addWithResponse(long financeId, FinanceRowItem costItem) {
        return postWithRestResult(costRestUrl + "/add-with-response/" + financeId, costItem,
                FinanceRowItem.class);
    }

    @Override
    public RestResult<List<FinanceRowItem>> getCosts(Long financeId) {
        return getWithRestResult(costRestUrl + "/get/" + financeId, costItemListType());
    }

    @Override
    public RestResult<ValidationMessages> update(FinanceRowItem costItem) {
        return putWithRestResult(costRestUrl + "/update/" + costItem.getId(), costItem, ValidationMessages.class);
    }

    @Override
    public RestResult<FinanceRowItem> findById(Long id) {
        return getWithRestResult(costRestUrl + "/" + id, FinanceRowItem.class);
    }

    @Override
    public RestResult<Void> delete(long costId) {
        return deleteWithRestResult(costRestUrl + "/delete/" + costId);
    }

    protected String getCostRestUrl() {
        return costRestUrl;
    }
}
