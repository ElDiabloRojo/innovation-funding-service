package org.innovateuk.ifs.application.populator.section;

import org.innovateuk.ifs.applicant.resource.ApplicantSectionResource;
import org.innovateuk.ifs.application.populator.ApplicationNavigationPopulator;
import org.innovateuk.ifs.application.populator.OpenSectionModelPopulator;
import org.innovateuk.ifs.application.populator.forminput.FormInputViewModelGenerator;
import org.innovateuk.ifs.application.viewmodel.OpenSectionViewModel;
import org.innovateuk.ifs.application.viewmodel.section.FinanceOverviewSectionViewModel;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.form.resource.SectionType;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Optional;

/**
 * Finance overview section view models.
 */
@Component
public class FinanceOverviewSectionPopulator extends AbstractSectionPopulator<FinanceOverviewSectionViewModel> {

    private OpenSectionModelPopulator openSectionModelPopulator;
    private FormInputViewModelGenerator formInputViewModelGenerator;

    public FinanceOverviewSectionPopulator(final ApplicationNavigationPopulator navigationPopulator,
                                           final OpenSectionModelPopulator openSectionModelPopulator,
                                           final FormInputViewModelGenerator formInputViewModelGenerator) {
        super(navigationPopulator);
        this.openSectionModelPopulator = openSectionModelPopulator;
        this.formInputViewModelGenerator = formInputViewModelGenerator;
    }

    @Override
    protected void populateNoReturn(ApplicantSectionResource section,
                                    ApplicationForm form,
                                    FinanceOverviewSectionViewModel viewModel,
                                    Model model,
                                    BindingResult bindingResult,
                                    Boolean readOnly,
                                    Optional<Long> applicantOrganisationId) {
        viewModel.setOpenSectionViewModel((OpenSectionViewModel) openSectionModelPopulator.populateModel(form, model,
                bindingResult, section));
    }

    @Override
    protected FinanceOverviewSectionViewModel createNew(ApplicantSectionResource section,
                                                        ApplicationForm form,
                                                        Boolean readOnly,
                                                        Optional<Long> applicantOrganisationId,
                                                        Boolean readOnlyAllApplicantApplicationFinances) {
        return new FinanceOverviewSectionViewModel(section, formInputViewModelGenerator.fromSection(section, section,
                form, readOnly), getNavigationViewModel(section), readOnly, applicantOrganisationId,
                readOnlyAllApplicantApplicationFinances);
    }

    @Override
    public SectionType getSectionType() {
        return SectionType.OVERVIEW_FINANCES;
    }

}

