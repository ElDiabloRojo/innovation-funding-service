package org.innovateuk.ifs.management.dashboard.viewmodel;

import org.innovateuk.ifs.competition.resource.CompetitionCountResource;
import org.innovateuk.ifs.competition.resource.search.CompetitionSearchResultItem;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;

import java.util.List;
import java.util.Map;

/**
 * View model for showing the non-IFS competitions
 */
public class NonIFSDashboardViewModel extends DashboardViewModel {
    public NonIFSDashboardViewModel(Map<CompetitionStatus, List<CompetitionSearchResultItem>> competitions,
                                  CompetitionCountResource counts,
                                  DashboardTabsViewModel tabs) {
        this.competitions = competitions;
        this.counts = counts;
        this.tabs = tabs;
    }
}
