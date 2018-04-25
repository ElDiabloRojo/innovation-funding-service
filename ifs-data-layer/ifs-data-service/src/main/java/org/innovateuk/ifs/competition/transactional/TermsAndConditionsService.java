package org.innovateuk.ifs.competition.transactional;

import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.SiteTermsAndConditionsResource;

public interface TermsAndConditionsService {

    @SecuredBySpring(value = "TODO", description = "TODO")
    @NotSecured(value = "TODO", mustBeSecuredByOtherServices = false)
    ServiceResult<SiteTermsAndConditionsResource> getLatestSiteTermsAndConditions();

}
