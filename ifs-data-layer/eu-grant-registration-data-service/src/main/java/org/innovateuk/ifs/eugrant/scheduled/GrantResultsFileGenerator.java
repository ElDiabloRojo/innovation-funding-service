package org.innovateuk.ifs.eugrant.scheduled;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.eugrant.EuGrantResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.eugrant.scheduled.CsvUtils.readDataFromCsv;
import static org.innovateuk.ifs.eugrant.scheduled.CsvUtils.writeDataToCsv;
import static org.innovateuk.ifs.eugrant.scheduled.ScheduledEuGrantFileImporter.getUriFromString;
import static org.innovateuk.ifs.util.CollectionFunctions.*;

/**
 * A component to create a results file based upon a source EU Grant csv.
 *
 * The output results file will contain the contents of the original source file with
 * the addition of 2 extra columns - a "Short code" column for recording the Short codes
 * of the successfully imported rows, and an "Import failure reason" column for
 * recording any import failure reasons for rows that failed to import.
 */
@Component
public class GrantResultsFileGenerator {

    private static final DateTimeFormatter RESULTS_FILE_SUFFIX_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");

    private static final List<String> ADDITIONAL_COLUMN_HEADERS = asList("Short code", "Import failure reason");

    private URI resultsFileUri;

    GrantResultsFileGenerator(@Value("${ifs.eu.data.service.grant.importer.results.file.location.uri}") String resultsFileUri)
                        throws URISyntaxException {

        ServiceResult<URI> uri = getUriFromString(resultsFileUri);

        if (uri.isFailure()) {
            throw new URISyntaxException(resultsFileUri, uri.getFailure().getErrors().get(0).getErrorKey());
        }

        this.resultsFileUri = uri.getSuccess();
    }

    ServiceResult<File> generateResultsFile(List<ServiceResult<EuGrantResource>> importResults, File originalFile) {

        return readDataFromCsv(originalFile).
                andOnSuccess(this::filterOutEmptyRows).
                andOnSuccess(originalData -> addImportResultsToOriginalData(originalData, importResults)).
                andOnSuccess(data -> createResultsFile(data, originalFile));
    }

    private ServiceResult<List<List<String>>> addImportResultsToOriginalData(List<List<String>> originalData, List<ServiceResult<EuGrantResource>> importResults) {

        List<String> newHeaders = combineLists(originalData.get(0), ADDITIONAL_COLUMN_HEADERS);

        List<List<String>> originalDataMinusHeaders = originalData.subList(1, originalData.size());

        List<List<String>> originalDataPlusImportStatusColumns = zipAndMap(originalDataMinusHeaders, importResults, (originalRow, importResultForRow) -> {

            List<String> importResultsColumns = importResultForRow.handleSuccessOrFailure(
                    failure -> asList("", failure.getErrors().get(0).getErrorKey()),
                    success -> asList(success.getShortCode(), ""));

            return combineLists(originalRow, importResultsColumns);
        });

        List<List<String>> finalSetOfData = combineLists(newHeaders, originalDataPlusImportStatusColumns);

        return serviceSuccess(finalSetOfData);
    }

    private ServiceResult<File> createResultsFile(List<List<String>> data, File originalFile) {
        String originalName = FilenameUtils.removeExtension(originalFile.getName());
        File resultsFileFolder = new File(resultsFileUri);
        if (!resultsFileFolder.exists()) {
            resultsFileFolder.mkdir();
        }
        File resultsFile = new File(resultsFileFolder, originalName + "-results" + ".csv");
        return writeDataToCsv(data, resultsFile);
    }

    private ServiceResult<List<List<String>>> filterOutEmptyRows(List<List<String>> headersAndDataRows) {
        Predicate<List<String>> emptyRow = row -> simpleAllMatch(row, StringUtils::isEmpty);
        List<List<String>> filteredList = simpleFilterNot(headersAndDataRows, emptyRow);
        return serviceSuccess(filteredList);
    }
}
