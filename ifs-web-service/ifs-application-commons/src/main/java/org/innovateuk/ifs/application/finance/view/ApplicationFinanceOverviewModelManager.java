package org.innovateuk.ifs.application.finance.view;

import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.finance.viewmodel.ApplicationFinanceOverviewViewModel;
import org.innovateuk.ifs.application.finance.viewmodel.BaseFinanceOverviewViewModel;
import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.file.service.FileEntryRestService;
import org.innovateuk.ifs.finance.resource.BaseFinanceResource;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.form.service.FormInputRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.Map;

@Component
public class ApplicationFinanceOverviewModelManager extends AbstractFinanceModelPopulator implements FinanceOverviewModelManager {
    private ApplicationFinanceRestService applicationFinanceRestService;
    private SectionService sectionService;
    private FinanceService financeService;
    private FileEntryRestService fileEntryRestService;

    @Autowired
    public ApplicationFinanceOverviewModelManager(
            ApplicationFinanceRestService applicationFinanceRestService,
            SectionService sectionService,
            FinanceService financeService,
            QuestionRestService questionRestService,
            FileEntryRestService fileEntryRestService,
            FormInputRestService formInputRestService
    ) {
        super(sectionService, formInputRestService, questionRestService);
        this.applicationFinanceRestService = applicationFinanceRestService;
        this.sectionService = sectionService;
        this.financeService = financeService;
        this.fileEntryRestService = fileEntryRestService;
    }

    public void addFinanceDetails(Model model, Long competitionId, Long applicationId) {
        addFinanceSections(competitionId, model);
        OrganisationApplicationFinanceOverviewImpl organisationFinanceOverview = new OrganisationApplicationFinanceOverviewImpl(
                financeService,
                fileEntryRestService,
                applicationId
        );

        model.addAttribute("financeTotal", organisationFinanceOverview.getTotal());
        model.addAttribute("financeTotalPerType", organisationFinanceOverview.getTotalPerType());
        Map<Long, BaseFinanceResource> organisationFinances = organisationFinanceOverview.getFinancesByOrganisation();
        model.addAttribute("organisationFinances", organisationFinances);
        model.addAttribute("academicFileEntries", organisationFinanceOverview.getAcademicOrganisationFileEntries());
        model.addAttribute("totalFundingSought", organisationFinanceOverview.getTotalFundingSought());
        model.addAttribute("totalContribution", organisationFinanceOverview.getTotalContribution());
        model.addAttribute("totalOtherFunding", organisationFinanceOverview.getTotalOtherFunding());
        model.addAttribute(
                "researchParticipationPercentage",
                applicationFinanceRestService.getResearchParticipationPercentage(applicationId).getSuccess()
        );
        model.addAttribute("isApplicant", true);
    }

    private void addFinanceSections(Long competitionId, Model model) {
        SectionResource section = sectionService.getFinanceSection(competitionId);

        if (section == null) {
            return;
        }
        model.addAttribute("financeSection", section);
    }

    public BaseFinanceOverviewViewModel getFinanceDetailsViewModel(Long competitionId, Long applicationId) {
        ApplicationFinanceOverviewViewModel viewModel = new ApplicationFinanceOverviewViewModel();

        addFinanceSections(competitionId, viewModel);
        OrganisationApplicationFinanceOverviewImpl organisationFinanceOverview = new OrganisationApplicationFinanceOverviewImpl(
                financeService,
                fileEntryRestService,
                applicationId
        );
        viewModel.setFinanceTotal(organisationFinanceOverview.getTotal());
        viewModel.setFinanceTotalPerType(organisationFinanceOverview.getTotalPerType());
        Map<Long, BaseFinanceResource> organisationFinances = organisationFinanceOverview.getFinancesByOrganisation();
        viewModel.setOrganisationFinances(organisationFinances);
        viewModel.setAcademicFileEntries(organisationFinanceOverview.getAcademicOrganisationFileEntries());
        viewModel.setTotalFundingSought(organisationFinanceOverview.getTotalFundingSought());
        viewModel.setTotalContribution(organisationFinanceOverview.getTotalContribution());
        viewModel.setTotalOtherFunding(organisationFinanceOverview.getTotalOtherFunding());
        viewModel.setResearchParticipationPercentage(
                applicationFinanceRestService.getResearchParticipationPercentage(applicationId).getSuccess()
        );

        return viewModel;
    }

    private void addFinanceSections(Long competitionId, BaseFinanceOverviewViewModel viewModel) {
        SectionResource section = sectionService.getFinanceSection(competitionId);
        if (section == null) {
            return;
        }
        viewModel.setFinanceSection(section);
    }
}
