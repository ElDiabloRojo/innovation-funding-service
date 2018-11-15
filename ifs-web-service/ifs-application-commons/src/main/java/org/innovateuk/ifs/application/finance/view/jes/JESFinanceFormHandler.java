package org.innovateuk.ifs.application.finance.view.jes;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.innovateuk.ifs.application.finance.model.FinanceFormField;
import org.innovateuk.ifs.application.finance.service.FinanceService;
import org.innovateuk.ifs.application.finance.view.FinanceFormHandler;
import org.innovateuk.ifs.application.finance.view.item.FinanceRowHandler;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.exception.UnableToReadUploadedFile;
import org.innovateuk.ifs.file.resource.FileEntryResource;
import org.innovateuk.ifs.finance.resource.ApplicationFinanceResource;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.innovateuk.ifs.finance.service.ApplicationFinanceRestService;
import org.innovateuk.ifs.finance.service.DefaultFinanceRowRestService;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static org.innovateuk.ifs.commons.error.Error.fieldError;
import static org.innovateuk.ifs.commons.error.ValidationMessages.noErrors;
import static org.innovateuk.ifs.form.resource.FormInputType.*;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

@Component
public class JESFinanceFormHandler implements FinanceFormHandler {
    private static final Log LOG = LogFactory.getLog(JESFinanceFormHandler.class);
    @Autowired
    private DefaultFinanceRowRestService financeRowRestService;
    @Autowired
    private ApplicationFinanceRestService applicationFinanceRestService;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private QuestionService questionService;
    public static final String REMOVE_FINANCE_DOCUMENT = "remove_finance_document";
    public static final String UPLOAD_FINANCE_DOCUMENT = "upload_finance_document";
    public static final String NON_DECIMAL_MESSAGE = "validation.standard.integer.non.decimal.format";
    public static final String NON_NEGATIVE_MESSAGE = "validation.standard.non.negative.integer.format";
    public static final String BLANK_FIELD_MESSAGE = "validation.field.must.not.be.blank";
    public static final String TSB_REFERENCE = "tsb_reference";

    @Override
    public ValidationMessages update(HttpServletRequest request, Long userId, Long applicationId, Long competitionId) {
        ValidationMessages validationMessages = new ValidationMessages();
        validationMessages.addAll(storeFinanceRowItems(request, userId, applicationId, competitionId));
        validationMessages.addAll(storeJESUpload(request, userId, applicationId));
        return validationMessages;
    }

    private ValidationMessages storeFinanceRowItems(HttpServletRequest request, Long userId, Long applicationId, Long competitionId) {

        ValidationMessages validationMessages = new ValidationMessages();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String parameter = parameterNames.nextElement();
            String[] parameterValues = request.getParameterValues(parameter);
            if (parameterValues.length > 0) {
                validationMessages.addAll(storeCost(userId, applicationId, parameter, parameterValues[0], competitionId));
            }
        }
        return validationMessages;
    }

    @Override
    public ValidationMessages storeCost(Long userId, Long applicationId, String fieldName, String value, Long competitionId) {
        if (fieldName != null && value != null && fieldName.startsWith("cost-")) {
            return storeField(fieldName.replace("cost-", ""), value, userId, applicationId, competitionId);
        }
        return null;
    }

    private ValidationMessages storeField(String fieldName, String value, Long userId, Long applicationId, Long competitionId) {

        ValidationMessages validationMessages = new ValidationMessages();

        FinanceFormField financeFormField = getCostFormField(competitionId, fieldName, value);
        if (financeFormField == null) {
            return null;
        }

        Long costFormFieldId = 0L;
        if (financeFormField.getId() != null && !"null".equals(financeFormField.getId())) {
            costFormFieldId = Long.parseLong(financeFormField.getId());
        }

        if (!financeFormField.getCostName().equals(TSB_REFERENCE)) {
            validationMessages.addAll(validateLong(value, financeFormField));
        }

        FinanceRowHandler financeRowHandler = new AcademicFinanceHandler();

        FinanceRowItem costItem = financeRowHandler.toFinanceRowItem(costFormFieldId, Arrays.asList(financeFormField));
        validationMessages.addAll(storeFinanceRowItem(costItem, userId, applicationId, financeFormField.getQuestionId()));

        if (value.isEmpty() && financeFormField.getCostName().equals(TSB_REFERENCE)) {
            validationMessages.addError(fieldError("formInput[cost-" + financeFormField.getId() + "-item]",
                    financeFormField, BLANK_FIELD_MESSAGE));
        }

        return ValidationMessages.noErrors();
    }

    private ValidationMessages validateLong(String value, FinanceFormField financeFormField) {
            // if this is a project cost we test for 1) fractional values, 2) negative values - empty values are okay.
            if (!inputIsLong(value)) {
                return new ValidationMessages(fieldError("formInput[cost-" + financeFormField.getId() + "-cost]",
                        financeFormField, NON_DECIMAL_MESSAGE));
            } else if (!StringUtils.isEmpty(value) && Long.parseLong(value) < 0) {
                return new ValidationMessages(fieldError("formInput[cost-" + financeFormField.getId() + "-cost]",
                        financeFormField, NON_NEGATIVE_MESSAGE));
            }
        return null;
    }

    private boolean inputIsLong(String input) {
        // empty value is valid
        if (StringUtils.isEmpty(input)) {
            return true;
        }
        try {
            Long.parseLong(input);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private FinanceFormField getCostFormField(Long competitionId, String costTypeKey, String value) {
        // check for question id
        String[] keyParts = costTypeKey.split("-");
        if (keyParts.length == 2) {
            Long questionId = getQuestionId(competitionId, keyParts[1]);
            return new FinanceFormField(costTypeKey, value, keyParts[0], String.valueOf(questionId), keyParts[1], "");
        }
        return null;
    }

    private ValidationMessages storeFinanceRowItem(FinanceRowItem costItem, Long userId, Long applicationId, String question) {
        if (costItem.getId().equals(0L)) {
            addFinanceRowItem(costItem, userId, applicationId, question);
        } else {
            RestResult<ValidationMessages> messages = financeRowRestService.update(costItem);
            ValidationMessages validationMessages = messages.getSuccess();
            if (validationMessages == null || validationMessages.getErrors() == null || validationMessages.getErrors().isEmpty()) {
                LOG.debug("no validation errors on cost items");
                return messages.getSuccess();
            } else {
                messages.getSuccess().getErrors()
                        .forEach(e -> LOG.debug(String.format("Got cost item Field error: %s", e.getErrorKey())));
                return messages.getSuccess();
            }
        }
        return null;
    }

    private void addFinanceRowItem(FinanceRowItem costItem, Long userId, Long applicationId, String question) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);
        if (question != null && !question.isEmpty()) {
            Long questionId = Long.parseLong(question);
            financeRowRestService.add(applicationFinanceResource.getId(), questionId, costItem).getSuccess();
        }
    }

    private Long getQuestionId(Long competitionId, String costFieldName) {
        QuestionResource question;
        switch (costFieldName) {
            case "tsb_reference":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, YOUR_FINANCE).getSuccess();
                break;
            case "incurred_staff":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, LABOUR).getSuccess();
                break;
            case "incurred_travel_subsistence":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, TRAVEL).getSuccess();
                break;
            case "incurred_other_costs":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, MATERIALS).getSuccess();
                break;
            case "allocated_investigators":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, LABOUR).getSuccess();
                break;
            case "allocated_estates_costs":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, OTHER_COSTS).getSuccess();
                break;
            case "allocated_other_costs":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, OTHER_COSTS).getSuccess();
                break;
            case "indirect_costs":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, OVERHEADS).getSuccess();
                break;
            case "exceptions_staff":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, LABOUR).getSuccess();
                break;
            case "exceptions_other_costs":
                question = questionService.getQuestionByCompetitionIdAndFormInputType(competitionId, OTHER_COSTS).getSuccess();
                break;
            default:
                question = null;
                break;
        }
        if (question != null) {
            return question.getId();
        } else {
            return null;
        }
    }

    private ValidationMessages storeJESUpload(HttpServletRequest request, Long userId, Long applicationId) {
        final Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey(REMOVE_FINANCE_DOCUMENT)) {
            ApplicationFinanceResource applicationFinance = financeService.getApplicationFinance(userId, applicationId);
            financeService.removeFinanceDocument(applicationFinance.getId()).getSuccess();
        } else if (params.containsKey(UPLOAD_FINANCE_DOCUMENT)) {
            final Map<String, MultipartFile> fileMap = ((StandardMultipartHttpServletRequest) request).getFileMap();
            final MultipartFile file = fileMap.get("jesFileUpload");
            if (file != null && !file.isEmpty()) {
                ApplicationFinanceResource applicationFinance = financeService.getApplicationFinance(userId, applicationId);
                try {
                    RestResult<FileEntryResource> result = financeService.addFinanceDocument(applicationFinance.getId(),
                            file.getContentType(),
                            file.getSize(),
                            file.getOriginalFilename(),
                            file.getBytes());
                    if (result.isFailure()) {
                        List<Error> errors = simpleMap(result.getFailure().getErrors(),
                                e -> fieldError("jesFileUpload", e.getFieldRejectedValue(), e.getErrorKey(), e.getArguments()));
                        return new ValidationMessages(errors);
                    }
                } catch (IOException e) {
                    LOG.error(e);
                    throw new UnableToReadUploadedFile();
                }
            }
        }
        return noErrors();
    }

    @Override
    public RestResult<ByteArrayResource> getFile(Long applicationFinanceId) {
        return financeService.getFinanceDocumentByApplicationFinance(applicationFinanceId);
    }

    @Override
    public void updateFinancePosition(Long userId, Long applicationId, String fieldName, String value, Long competitionId) {
        ApplicationFinanceResource applicationFinanceResource = financeService.getApplicationFinanceDetails(userId, applicationId);
        updateFinancePosition(applicationFinanceResource, fieldName, value);
        applicationFinanceRestService.update(applicationFinanceResource.getId(), applicationFinanceResource);
    }

    private void updateFinancePosition(ApplicationFinanceResource applicationFinance, String fieldName, String value) {
        String fieldNameReplaced = fieldName.replace("financePosition-", "");
        if (fieldNameReplaced.equals("projectLocation")) {
            applicationFinance.setWorkPostcode(value);
        } else {
            LOG.error(String.format("value not saved: %s / %s", fieldNameReplaced, value));
        }
    }

    @Override
    public ValidationMessages addCost(Long applicationId, Long userId, Long questionId) {
        // not to be implemented, can't add extra rows of finance to the JES form
        throw new NotImplementedException("Can't add extra rows of finance to the JES form");
    }

    @Override
    public FinanceRowItem addCostWithoutPersisting(Long applicationId, Long userId, Long questionId) {
        // not to be implemented, can't add extra rows of finance to the JES form
        throw new NotImplementedException("Can't add extra rows of finance to the JES form");
    }
}