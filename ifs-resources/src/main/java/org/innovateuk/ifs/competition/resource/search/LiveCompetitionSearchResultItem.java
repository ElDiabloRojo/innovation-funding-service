package org.innovateuk.ifs.competition.resource.search;

import org.innovateuk.ifs.competition.resource.CompetitionStatus;

import java.util.Set;
import java.util.TreeSet;

/**
 * A summary of competition information displayed during competition search
 */
public class LiveCompetitionSearchResultItem extends AbstractCompetitionSearchResultItem {

    private Set<String> innovationAreaNames;
    private String competitionTypeName;
    private int numberOfApplications;

    public LiveCompetitionSearchResultItem(long id, String name, CompetitionStatus competitionStatus, Set<String> innovationAreaNames, String competitionTypeName, int numberOfApplications) {
        super(id, name, competitionStatus);
        this.innovationAreaNames = innovationAreaNames;
        this.competitionTypeName = competitionTypeName;
        this.numberOfApplications = numberOfApplications;
    }

    public Set<String> getInnovationAreaNames() {
        return innovationAreaNames;
    }

    public void setInnovationAreaNames(Set<String> innovationAreaNames) {
        this.innovationAreaNames = new TreeSet<>(innovationAreaNames);
    }

    public int getNumberOfApplications() {
        return numberOfApplications;
    }

    public String getCompetitionTypeName() {
        return competitionTypeName;
    }

}