package org.innovateuk.ifs.survey;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SurveyResource {

    private SurveyType surveyType;

    private SurveyTargetType targetType;

    private Long targetId;

    private Satisfaction satisfaction;

    private String comments;

    public SurveyResource() {
    }

    public SurveyResource(SurveyType surveyType,
                          SurveyTargetType targetType,
                          Long targetId,
                          Satisfaction satisfaction,
                          String comments) {
        this.surveyType = surveyType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.satisfaction = satisfaction;
        this.comments = comments;
    }

    public SurveyType getSurveyType() {
        return surveyType;
    }

    public void setSurveyType(SurveyType surveyType) {
        this.surveyType = surveyType;
    }

    public SurveyTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(SurveyTargetType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Satisfaction getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Satisfaction satisfaction) {
        this.satisfaction = satisfaction;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SurveyResource that = (SurveyResource) o;

        return new EqualsBuilder()
                .append(surveyType, that.surveyType)
                .append(targetType, that.targetType)
                .append(targetId, that.targetId)
                .append(satisfaction, that.satisfaction)
                .append(comments, that.comments)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(surveyType)
                .append(targetType)
                .append(targetId)
                .append(satisfaction)
                .append(comments)
                .toHashCode();
    }
}
