package org.innovateuk.ifs.application.forms.sections.yourfunding.viewmodel;

public class ManagementYourFundingViewModel extends YourFundingViewModel {

    public ManagementYourFundingViewModel(long applicationId,
                                          String competitionName,
                                          long sectionId,
                                          long organisationId,
                                          long competitionId,
                                          String applicationName,
                                          String url) {
        super(
                applicationId,
                competitionName,
                sectionId,
                organisationId,
                competitionId,
                true,
                false,
                false,
                true,
                applicationName,
                false,
                false,
                false,
                0L,
                0,
                0,
                url,
                false
        );
    }
}