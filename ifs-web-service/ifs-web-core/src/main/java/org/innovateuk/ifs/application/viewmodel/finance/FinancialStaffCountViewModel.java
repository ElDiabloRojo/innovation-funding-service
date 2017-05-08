package org.innovateuk.ifs.application.viewmodel.finance;

import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;
import org.innovateuk.ifs.form.resource.FormInputType;

/**
 * Created by luke.harper on 08/05/2017.
 */
public class FinancialStaffCountViewModel extends AbstractFormInputViewModel {
    @Override
    protected FormInputType formInputType() {
        return FormInputType.STAFF_COUNT;
    }
}
