package org.innovateuk.ifs.competitionsetup.completionstage.sectionupdater;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.competition.resource.MilestoneResource;
import org.innovateuk.ifs.competition.resource.MilestoneType;
import org.innovateuk.ifs.competition.service.MilestoneRestService;
import org.innovateuk.ifs.competitionsetup.application.sectionupdater.AbstractSectionUpdater;
import org.innovateuk.ifs.competitionsetup.completionstage.form.CompletionStageForm;
import org.innovateuk.ifs.competitionsetup.core.form.CompetitionSetupForm;
import org.innovateuk.ifs.competitionsetup.core.form.GenericMilestoneRowForm;
import org.innovateuk.ifs.competitionsetup.core.form.MilestoneTime;
import org.innovateuk.ifs.competitionsetup.core.sectionupdater.CompetitionSetupSectionUpdater;
import org.innovateuk.ifs.competitionsetup.core.service.CompetitionSetupMilestoneService;
import org.innovateuk.ifs.util.CollectionFunctions;
import org.innovateuk.ifs.util.TimeZoneUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;

/**
 * TODO DW - comment
 */
@Service
public class CompletionStageSectionUpdater extends AbstractSectionUpdater implements CompetitionSetupSectionUpdater {

    private static Log LOG = LogFactory.getLog(CompletionStageSectionUpdater.class);

    @Autowired
    private MilestoneRestService milestoneRestService;

    @Autowired
    private CompetitionSetupMilestoneService competitionSetupMilestoneService;

    @Override
    public CompetitionSetupSection sectionToSave() {
        return CompetitionSetupSection.COMPLETION_STAGE;
    }

    @Override
    protected ServiceResult<Void> doSaveSection(CompetitionResource competition, CompetitionSetupForm competitionSetupForm) {
//        MilestonesForm milestonesForm = (MilestonesForm) competitionSetupForm;
//        LinkedMap<String, GenericMilestoneRowForm> milestoneEntries = milestonesForm.getMilestoneEntries();
//
//        List<Error> errors = returnErrorsFoundOnSave(milestoneEntries, competition);
//        if (!errors.isEmpty()) {
//            competitionSetupMilestoneService.sortMilestones(milestonesForm);
//            return serviceFailure(errors);
//        }

        return serviceSuccess();
    }

    private List<Error> returnErrorsFoundOnSave(LinkedMap<String, GenericMilestoneRowForm> milestoneEntries, CompetitionResource competition) {
        List<MilestoneResource> milestones = milestoneRestService.getAllMilestonesByCompetitionId(competition.getId()).getSuccess();
        Map<String, GenericMilestoneRowForm> filteredMilestoneEntries = milestoneEntries;

        //If competition is already set up only allow to save of future milestones.
        if (TRUE.equals(competition.getSetupComplete())) {
            List<MilestoneType> futureTypes = milestones.stream()
                    .filter(milestoneResource -> milestoneResource.getDate() == null || ZonedDateTime.now().isBefore(milestoneResource.getDate()))
                    .map(MilestoneResource::getType)
                    .collect(Collectors.toList());

            filteredMilestoneEntries = CollectionFunctions.simpleFilter(milestoneEntries, (name, form) -> futureTypes.contains(form.getMilestoneType()));
        }

        List<Error> errors = competitionSetupMilestoneService.validateMilestoneDates(filteredMilestoneEntries);
        if (!errors.isEmpty()) {
            return errors;
        }

        ServiceResult<Void> result = competitionSetupMilestoneService.updateMilestonesForCompetition(milestones, filteredMilestoneEntries, competition.getId());
        if (result.isFailure()) {
            return result.getErrors();
        }

        return Collections.emptyList();
    }

    @Override
    public boolean supportsForm(Class<? extends CompetitionSetupForm> clazz) {
        return CompletionStageForm.class.equals(clazz);
    }

    @Override
    protected ServiceResult<Void> handleIrregularAutosaveCase(CompetitionResource competitionResource,
                                                              String fieldName,
                                                              String value,
                                                              Optional<Long> questionId) {
        List<Error> errors = updateMilestoneWithValueByFieldname(competitionResource, fieldName, value);
        if (!errors.isEmpty()) {
            return serviceFailure(errors);
        }
        return serviceSuccess();
    }


    private List<Error> updateMilestoneWithValueByFieldname(CompetitionResource competitionResource, String fieldName, String value) {
        List<Error> errors = new ArrayList<>();
        try {
            MilestoneResource milestone = milestoneRestService.getMilestoneByTypeAndCompetitionId(
                    MilestoneType.valueOf(getMilestoneTypeFromFieldName(fieldName)), competitionResource.getId())
                    .getSuccess();

            errors.addAll(validateMilestoneDateOnAutosave(milestone, fieldName, value));

            if (!errors.isEmpty()) {
                return errors;
            }
            milestoneRestService.updateMilestone(milestone).getSuccess();
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return makeErrorList();
        }
        return errors;
    }

    private List<Error> validateMilestoneDateOnAutosave(MilestoneResource milestone, String fieldName, String value) {
        Integer day = null, month = null, year = null, hour = 0;
        ZonedDateTime currentDate = milestone.getDate();

        if (isTimeField(fieldName)) {
            if (null != currentDate) {
                day = currentDate.getDayOfMonth();
                month = currentDate.getMonthValue();
                year = currentDate.getYear();
                hour = MilestoneTime.valueOf(value).getHour();
            }
        } else {
            String[] dateParts = value.split("-");
            day = Integer.parseInt(dateParts[0]);
            month = Integer.parseInt(dateParts[1]);
            year = Integer.parseInt(dateParts[2]);

            if (null != currentDate) {
                hour = milestone.getDate().getHour();
            }
        }

        String fieldValidationError = milestone.getType().getMilestoneDescription();

        if (!competitionSetupMilestoneService.isMilestoneDateValid(day, month, year)) {
            return singletonList(fieldError(fieldName, fieldName, "error.milestone.invalid", fieldValidationError));
        } else {
            milestone.setDate(TimeZoneUtil.fromUkTimeZone(year, month, day, hour));
        }

        return Collections.emptyList();
    }

    private boolean isTimeField(String fieldName) {
        return fieldName.endsWith(".time");
    }

    private List<Error> makeErrorList() {
        return singletonList(fieldError("", null, "error.milestone.autosave.unable"));
    }

    private String getMilestoneTypeFromFieldName(String fieldName) {
        Pattern typePattern = Pattern.compile("\\[(.*?)\\]");
        Matcher typeMatcher = typePattern.matcher(fieldName);
        typeMatcher.find();
        return typeMatcher.group(1);
    }

}
