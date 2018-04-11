package org.innovateuk.ifs.dashboard.viewmodel;


import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.util.TimeZoneUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class InProgressDashboardRowViewModel extends AbstractApplicantDashboardRowViewModel {

    private final boolean assignedToMe;
    private final ApplicationState applicationState;
    private final boolean leadApplicant;
    private final ZonedDateTime endDate;
    private final long daysLeft;
    private final int applicationProgress;
    private final boolean assignedToInterview;

    public InProgressDashboardRowViewModel(String title, Long applicationId, String competitionTitle, boolean assignedToMe, ApplicationState applicationState, boolean leadApplicant, ZonedDateTime endDate, long daysLeft, int applicationProgress, boolean assignedToInterview) {
        super(title, applicationId, competitionTitle);
        this.assignedToMe = assignedToMe;
        this.applicationState = applicationState;
        this.leadApplicant = leadApplicant;
        this.endDate = endDate;
        this.daysLeft = daysLeft;
        this.applicationProgress = applicationProgress;
        this.assignedToInterview = assignedToInterview;
    }

    public boolean isAssignedToMe() {
        return assignedToMe;
    }

    public boolean isLeadApplicant() {
        return leadApplicant;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public long getDaysLeft() {
        return daysLeft;
    }

    public int getApplicationProgress() {
        return applicationProgress;
    }

    public boolean isAssignedToInterview() {
        return assignedToInterview;
    }

    /* view logic */
    public boolean isSubmitted() {
        return ApplicationState.SUBMITTED.equals(applicationState) ||
                ApplicationState.INELIGIBLE.equals(applicationState);
    }

    public boolean isCreated() {
        return ApplicationState.CREATED.equals(applicationState);
    }

    public boolean isWithin24Hours() {
        Long hoursLeft = getHoursLeftBeforeSubmit();
        return hoursLeft >= 0 && hoursLeft < 24;
    }

    public Long getHoursLeftBeforeSubmit() {
        return Duration.between(ZonedDateTime.now(), endDate).toHours();
    }

    public boolean isClosingToday() {
        LocalDate endDay = TimeZoneUtil.toUkTimeZone(endDate).toLocalDate();
        LocalDate today = TimeZoneUtil.toUkTimeZone(ZonedDateTime.now()).toLocalDate();

        return today.equals(endDay);
    }

    @Override
    public String getLinkUrl() {
        if (isSubmitted()) {
            if (assignedToInterview) {
                return String.format("/application/%s/track", getApplicationNumber());
            } else {
                return String.format("/application/%s/track", getApplicationNumber());
            }
        } else if (isCreated() && leadApplicant) {
            return String.format("/application/%s/team", getApplicationNumber());
        } else {
            return String.format("/application/%s", getApplicationNumber());
        }
    }

}
