package org.innovateuk.ifs.question.transactional;

import com.google.common.collect.Lists;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupQuestionType;
import org.innovateuk.ifs.competition.resource.GuidanceRowResource;
import org.innovateuk.ifs.file.resource.FileTypeCategory;
import org.innovateuk.ifs.form.domain.FormInput;
import org.innovateuk.ifs.form.domain.GuidanceRow;
import org.innovateuk.ifs.form.domain.Question;
import org.innovateuk.ifs.form.mapper.GuidanceRowMapper;
import org.innovateuk.ifs.form.repository.FormInputRepository;
import org.innovateuk.ifs.form.repository.QuestionRepository;
import org.innovateuk.ifs.form.resource.FormInputScope;
import org.innovateuk.ifs.form.resource.FormInputType;
import org.innovateuk.ifs.transactional.BaseTransactionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.CollectionFunctions.forEachWithIndex;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleToLinkedHashSet;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * Service for operations around the usage and processing of Competitions questions in setup.
 */
@Service
public class QuestionSetupCompetitionServiceImpl extends BaseTransactionalService implements QuestionSetupCompetitionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private FormInputRepository formInputRepository;

    @Autowired
    private GuidanceRowMapper guidanceRowMapper;

    @Autowired
    private QuestionSetupTemplateService questionSetupTemplateService;

    @Override
    public ServiceResult<CompetitionSetupQuestionResource> getByQuestionId(Long questionId) {
        return find(questionRepository.findOne(questionId), notFoundError(Question.class, questionId))
                .andOnSuccess(question -> mapQuestionToSuperQuestionResource(question));
    }

    private ServiceResult<CompetitionSetupQuestionResource> mapQuestionToSuperQuestionResource(Question question) {
        CompetitionSetupQuestionResource setupResource = new CompetitionSetupQuestionResource();

        question.getFormInputs().forEach(formInput -> {
            if (FormInputScope.ASSESSMENT.equals(formInput.getScope())) {
                mapAssessmentFormInput(formInput, setupResource);
            } else {
                mapApplicationFormInput(formInput, setupResource);
            }
        });

        setupResource.setScoreTotal(question.getAssessorMaximumScore());
        setupResource.setNumber(question.getQuestionNumber());
        setupResource.setShortTitle(question.getShortName());
        setupResource.setTitle(question.getName());
        setupResource.setSubTitle(question.getDescription());
        setupResource.setQuestionId(question.getId());
        setupResource.setType(question.getQuestionSetupType());

        return serviceSuccess(setupResource);
    }

    private void mapApplicationFormInput(FormInput formInput, CompetitionSetupQuestionResource setupResource) {
        switch (formInput.getType()) {
            case FILEUPLOAD:
                setupResource.setAppendix(formInput.getActive());
                setupResource.setAllowedFileTypes(
                        simpleToLinkedHashSet(StringUtils.commaDelimitedListToStringArray(formInput.getAllowedFileTypes()),
                                FileTypeCategory::fromDisplayName));
                setupResource.setAppendixGuidance(formInput.getGuidanceAnswer());
                break;
            case TEXTAREA:
                setupResource.setGuidanceTitle(formInput.getGuidanceTitle());
                setupResource.setGuidance(formInput.getGuidanceAnswer());
                setupResource.setMaxWords(formInput.getWordCount());
                break;
        }
    }

    private void mapAssessmentFormInput(FormInput formInput, CompetitionSetupQuestionResource setupResource) {
        switch (formInput.getType()) {
            case TEXTAREA:
                setupResource.setWrittenFeedback(formInput.getActive());
                setupResource.setAssessmentMaxWords(formInput.getWordCount());
                setupResource.setAssessmentGuidance(formInput.getGuidanceAnswer());
                setupResource.setGuidanceRows(sortByPriority((guidanceRowMapper.mapToResource(formInput.getGuidanceRows()))));
                setupResource.setAssessmentGuidanceTitle(formInput.getGuidanceTitle());
                break;
            case ASSESSOR_SCORE:
                setupResource.setScored(formInput.getActive());
                break;
            case ASSESSOR_APPLICATION_IN_SCOPE:
                setupResource.setScope(formInput.getActive());
                break;
            case ASSESSOR_RESEARCH_CATEGORY:
                setupResource.setResearchCategoryQuestion(formInput.getActive());
                break;
        }
    }

    private List<GuidanceRowResource> sortByPriority(Iterable<GuidanceRowResource> guidanceRowResources) {
        List<GuidanceRowResource> resources = Lists.newArrayList(guidanceRowResources);
        Collections.sort(resources, (o1, o2) -> o1.getPriority().compareTo(o2.getPriority()));
        return resources;
    }

    @Override
    @Transactional
    public ServiceResult<CompetitionSetupQuestionResource> createByCompetitionId(Long competitionId) {
        return find(competitionRepository.findById(competitionId), notFoundError(Competition.class, competitionId))
                .andOnSuccess(competition -> questionSetupTemplateService.addDefaultAssessedQuestionToCompetition(competition))
                .andOnSuccess(question -> mapQuestionToSuperQuestionResource(question));
    }

    @Override
    @Transactional
    public ServiceResult<Void> delete(Long questionId) {
        questionSetupTemplateService.deleteQuestionInCompetition(questionId);

        return serviceSuccess();
    }

    @Override
    @Transactional
    public ServiceResult<CompetitionSetupQuestionResource> update(CompetitionSetupQuestionResource competitionSetupQuestionResource) {
        Long questionId = competitionSetupQuestionResource.getQuestionId();
        Question question = questionRepository.findOne(questionId);

        if (question.getQuestionSetupType() != CompetitionSetupQuestionType.APPLICATION_DETAILS) {
            question.setShortName(competitionSetupQuestionResource.getShortTitle());
        }
        question.setName(competitionSetupQuestionResource.getTitle());
        question.setDescription(competitionSetupQuestionResource.getSubTitle());
        question.setAssessorMaximumScore(competitionSetupQuestionResource.getScoreTotal());

        FormInput questionFormInput = formInputRepository.findByQuestionIdAndScopeAndType(questionId, FormInputScope.APPLICATION, FormInputType.TEXTAREA);
        questionFormInput.setGuidanceTitle(competitionSetupQuestionResource.getGuidanceTitle());
        questionFormInput.setGuidanceAnswer(competitionSetupQuestionResource.getGuidance());
        questionFormInput.setWordCount(competitionSetupQuestionResource.getMaxWords());

        markAppendixAsActiveOrInactive(questionId, competitionSetupQuestionResource);
        markScoredAsActiveOrInactive(questionId, competitionSetupQuestionResource);
        markWrittenFeedbackAsActiveOrInactive(questionId, competitionSetupQuestionResource);
        markResearchCategoryQuestionAsActiveOrInactive(questionId, competitionSetupQuestionResource);
        markScopeAsActiveOrInactive(questionId, competitionSetupQuestionResource);

        return serviceSuccess(competitionSetupQuestionResource);
    }

    private void markAppendixAsActiveOrInactive(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource) {
        FormInput appendixFormInput = formInputRepository.findByQuestionIdAndScopeAndType(questionId,
                FormInputScope.APPLICATION,
                FormInputType.FILEUPLOAD);
        if (appendixFormInput != null && competitionSetupQuestionResource.getAppendix() != null) {
            appendixFormInput.setActive(competitionSetupQuestionResource.getAppendix());

            if(competitionSetupQuestionResource.getAppendix()) {
                setAppendixSubOptions(appendixFormInput, competitionSetupQuestionResource );
            }
            else {
                resetAppendixSubOptions(appendixFormInput);
            }
        }
    }

    private void setAppendixSubOptions(FormInput appendixFormInput, CompetitionSetupQuestionResource competitionSetupQuestionResource) {
        appendixFormInput.setAllowedFileTypes(
                StringUtils.collectionToDelimitedString(
                        simpleMap(competitionSetupQuestionResource.getAllowedFileTypes(), FileTypeCategory::getDisplayName),
                        ","));
        if(competitionSetupQuestionResource.getAppendixGuidance() != null) {
            appendixFormInput.setGuidanceAnswer(competitionSetupQuestionResource.getAppendixGuidance());
        }
    }

    private void resetAppendixSubOptions(FormInput appendixFormInput) {
        appendixFormInput.setAllowedFileTypes(null);
        appendixFormInput.setGuidanceAnswer(null);
    }

    private void markScoredAsActiveOrInactive(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource) {

        FormInput scoredFormInput = formInputRepository.findByQuestionIdAndScopeAndType(questionId, FormInputScope.ASSESSMENT, FormInputType.ASSESSOR_SCORE);

        if (scoredFormInput != null && competitionSetupQuestionResource.getScored() != null) {
            scoredFormInput.setActive(competitionSetupQuestionResource.getScored());
        }
    }

    private void markResearchCategoryQuestionAsActiveOrInactive(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource) {

        FormInput researchCategoryQuestionFormInput = formInputRepository.findByQuestionIdAndScopeAndType(questionId, FormInputScope.ASSESSMENT, FormInputType.ASSESSOR_RESEARCH_CATEGORY);

        if (researchCategoryQuestionFormInput != null && competitionSetupQuestionResource.getResearchCategoryQuestion() != null) {
            researchCategoryQuestionFormInput.setActive(competitionSetupQuestionResource.getResearchCategoryQuestion());
        }
    }

    private void markScopeAsActiveOrInactive(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource) {

        FormInput scopeFormInput = formInputRepository.findByQuestionIdAndScopeAndType(questionId, FormInputScope.ASSESSMENT, FormInputType.ASSESSOR_APPLICATION_IN_SCOPE);

        if (scopeFormInput != null && competitionSetupQuestionResource.getScope() != null) {
            scopeFormInput.setActive(competitionSetupQuestionResource.getScope());
        }
    }

    private void markWrittenFeedbackAsActiveOrInactive(Long questionId, CompetitionSetupQuestionResource competitionSetupQuestionResource) {

        FormInput writtenFeedbackFormInput = formInputRepository.findByQuestionIdAndScopeAndType(questionId, FormInputScope.ASSESSMENT, FormInputType.TEXTAREA);

        if (writtenFeedbackFormInput != null && competitionSetupQuestionResource.getWrittenFeedback() != null) {
            writtenFeedbackFormInput.setActive(competitionSetupQuestionResource.getWrittenFeedback());

            writtenFeedbackFormInput.setGuidanceAnswer(competitionSetupQuestionResource.getAssessmentGuidance());
            writtenFeedbackFormInput.setGuidanceTitle(competitionSetupQuestionResource.getAssessmentGuidanceTitle());
            writtenFeedbackFormInput.setWordCount(competitionSetupQuestionResource.getAssessmentMaxWords());

            // Delete all existing guidance rows and replace with new list
            List<GuidanceRow> newRows = newArrayList(guidanceRowMapper.mapToDomain(competitionSetupQuestionResource.getGuidanceRows()));
            // Ensure form input and priority set against newly added rows
            forEachWithIndex(newRows, (index, row) -> {
                row.setFormInput(writtenFeedbackFormInput);
                row.setPriority(index);
            });
            writtenFeedbackFormInput.getGuidanceRows().clear();
            writtenFeedbackFormInput.getGuidanceRows().addAll(newRows);
            formInputRepository.save(writtenFeedbackFormInput);
        }
    }
}
