package org.innovateuk.ifs.management.competition.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.competition.resource.AssessorFinanceView;
import org.innovateuk.ifs.competition.resource.CompetitionFunderResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;

import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.competition.resource.CompetitionStatus.*;

/**
 * Holder of model attributes for the Competition Management 'in flight' Dashboard
 */
public class CompetitionInFlightViewModel {

    private Long competitionId;
    private String competitionName;
    private CompetitionStatus competitionStatus;
    private boolean horizon2020Competition;
    private String competitionType;
    private String innovationSector;
    private String innovationArea;
    private String executive;
    private String lead;
    private BigInteger funding;
    private List<MilestonesRowViewModel> milestones;
    private long changesSinceLastNotify;
    private CompetitionInFlightStatsViewModel keyStatistics;
    private boolean readOnly;
    private boolean assessmentPanelEnabled;
    private boolean interviewPanelEnabled;
    private AssessorFinanceView assessorFinanceView;

    public CompetitionInFlightViewModel(CompetitionResource competitionResource,
                                        List<MilestonesRowViewModel> milestones,
                                        long changesSinceLastNotify,
                                        CompetitionInFlightStatsViewModel keyStatistics,
                                        boolean readOnly) {
        this.competitionId = competitionResource.getId();
        this.competitionName = competitionResource.getName();
        this.competitionStatus = competitionResource.getCompetitionStatus();
        this.competitionType = competitionResource.getCompetitionTypeName();
        this.horizon2020Competition = competitionResource.isH2020();
        this.innovationSector = competitionResource.getInnovationSectorName();
        this.innovationArea = StringUtils.join(competitionResource.getInnovationAreaNames(), ", ");
        this.executive = competitionResource.getExecutiveName();
        this.lead = competitionResource.getLeadTechnologistName();
        this.funding = competitionResource.getFunders().stream().map(CompetitionFunderResource::getFunderBudget).reduce(BigInteger.ZERO, BigInteger::add);
        this.keyStatistics = keyStatistics;
        this.milestones = milestones;
        this.changesSinceLastNotify = changesSinceLastNotify;
        this.readOnly = readOnly;
        this.assessmentPanelEnabled = competitionResource.isHasAssessmentPanel() != null ? competitionResource.isHasAssessmentPanel() : false;
        this.interviewPanelEnabled = competitionResource.isHasInterviewStage() != null ? competitionResource.isHasInterviewStage() : false;
        this.assessorFinanceView = competitionResource.getAssessorFinanceView();
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public CompetitionStatus getCompetitionStatus() {
        return competitionStatus;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public String getInnovationSector() {
        return innovationSector;
    }

    public String getInnovationArea() {
        return innovationArea;
    }

    public String getExecutive() {
        return executive;
    }

    public String getLead() {
        return lead;
    }

    public BigInteger getFunding() {
        return funding;
    }

    public List<MilestonesRowViewModel> getMilestones() {
        return milestones;
    }

    public long getChangesSinceLastNotify() {
        return changesSinceLastNotify;
    }

    public CompetitionInFlightStatsViewModel getKeyStatistics() {
        return keyStatistics;
    }

    public boolean isHorizon2020Competition() {
        return horizon2020Competition;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isAssessmentPanelEnabled() {
        return assessmentPanelEnabled && competitionStatus != READY_TO_OPEN &&
                competitionStatus != OPEN && competitionStatus != ASSESSOR_FEEDBACK;
    }

    public boolean isInterviewPanelEnabled() {
        return interviewPanelEnabled && competitionStatus != READY_TO_OPEN &&
                competitionStatus != OPEN && competitionStatus != ASSESSOR_FEEDBACK;
    }

    public AssessorFinanceView getAssessorFinanceView() {
        return assessorFinanceView;
    }

    public boolean isFundingDecisionEnabled() {
        return horizon2020Competition
                || !asList(READY_TO_OPEN, OPEN, CLOSED, IN_ASSESSMENT).contains(competitionStatus);
    }

    public boolean isFundingNotificationDisplayed() {
        return horizon2020Competition
                || asList(FUNDERS_PANEL, ASSESSOR_FEEDBACK).contains(competitionStatus);
    }
}
