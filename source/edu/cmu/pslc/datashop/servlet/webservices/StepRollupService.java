/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.select;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.importdata.ImportConstants.PROBLEM_NAME_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.SAMPLE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.STUDENT_HEADING;
import static edu.cmu.pslc.datashop.servlet.
                                    webservices.WebServiceException.invalidParamValueException;

/**
 * Web service for fetching student step rollup.
 * @author jimbokun
 * @version $Revision: 10126 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-10-10 14:36:20 -0400 (Thu, 10 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepRollupService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** KC model columns parameter */
    private static final String KCMS = "kcms";
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(DATASET_ID,
            SAMPLE_ID, ZIP, COLS, CFS_COL, LIMIT, OFFSET, HEADERS, KCMS);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES =
        map(ZIP, TF, HEADERS, TF);
    /** Valid values for the columns parameter. */
    private static final Set<String> COL_VALUES = set(ROW_COL, STUDENT_COL, PROBLEM_HIERARCHY_COL,
            PROBLEM_COL, "problem_view", STEP_COL, "first_transaction_time",
            "correct_transaction_time", "step_end_time", "step_duration", "correct_step_duration",
            "error_step_duration", "first_attempt", "incorrects", "hints", "corrects",
            CONDITION_COL);
    /** maps columns parameters to export file column headings */
    private static final Map<String, String> COL_HEADINGS = map(ROW_COL, ROW_HEADING,
            STUDENT_COL, STUDENT_HEADING, PROBLEM_HIERARCHY_COL, PROBLEM_HIERARCHY_HEADING,
            PROBLEM_COL, PROBLEM_NAME_HEADING, "problem_view", "Problem View",
            STEP_COL, "Step Name", "first_transaction_time", "First Transaction Time",
            "correct_transaction_time", "Correct Transaction Time",
            "step_end_time", "Step End Time", "step_duration", "Step Duration (sec)",
            "correct_step_duration", "Correct Step Duration (sec)",
            "error_step_duration", "Error Step Duration (sec)", "first_attempt", "First Attempt",
            "incorrects", "Incorrects", "hints", "Hints", "corrects", "Corrects");
    /** pattern for identifying KC header columns */
    private static final Pattern KCS_PATTERN =
        Pattern.compile("(KC|Opportunity|Predicted Error Rate) \\(.*\\)");

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public StepRollupService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Whether this is a knowledge component column header.
     * @param header the column header
     * @return whether this is a knowledge component header
     */
    private boolean isKcmHeader(String header) { return KCS_PATTERN.matcher(header).matches(); }

    /** Until the Tx export is changed to use "Sample" instead of "Sample Name", this
     * variables is needed by the step rollup services to parse out the sample column.
     */
    private static final String SAMPLE_HEADING_STEP = "Sample";
    /**
     * Determine which columns to include based on the header row of the export file and the
     * columns parameter.
     * @param headers the header row of the export file
     * @return the indices of the columns to include
     */
    private List<Integer> colIndices(List<String> headers) {
        List<String> cols = colsParam();
        Set<String> includeHeaders = new HashSet<String>();
        List<Integer> indices = new ArrayList<Integer>();
        List<String> kcms = multipleStringParam(KCMS);
        boolean includeKcms = kcms.isEmpty()
            || (kcms.size() == 1 && ALL.equals(kcms.get(0)));

        if (cols.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);

                if (!SAMPLE_HEADING_STEP.equals(header)
                    && !isKcmHeader(header)) { indices.add(i); }
            }
        } else {
            for (String col : cols) {
                String heading = COL_HEADINGS.get(col);

                if (heading != null) {
                    includeHeaders.add(heading);
                } else if (CONDITION_COL.equals(col)) {
                    includeHeaders.add("Condition");
                }
            }
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);

                if (includeHeaders.contains(header)) { indices.add(i); }
            }
        }
        // add KC models at the end
        if (includeKcms) {
            for (int i = 0; i < headers.size(); i++) {
                if (isKcmHeader(headers.get(i))) { indices.add(i); }
            }
        }

        return indices;
    }

    /** Return the requested steps in tab delimited format. */
    public void get(WebServiceUserLog wsUserLog) {
        PrintWriter writer = null;
        ZipOutputStream zip = null;
        int offset = -1;

        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS, VALID_GET_PARAM_VALUES);
            validateMultipleParam(COLS, COL_VALUES);

            offset = offsetParam();
            Iterable<List<String>> rows = helper().stepRows(getAuthenticatedUser(),
                    datasetParam(), sampleParam(), limitParam(), offset);
            List<Integer> colIndices = null;
            boolean isZip = zipParam();
            boolean showHeaders = headerParam();

            if (isZip) {
                zip = initZip("steps");
            } else {
                writer = getResp().getWriter();
            }
            for (List<String> row : rows) {
                boolean isHeaderRow = colIndices == null;

                if (colIndices == null) { colIndices = colIndices(row); }
                if (!colIndices.isEmpty()) { row = select(colIndices, row); }
                if (!isHeaderRow || showHeaders) {
                    if (isZip) {
                        zip.write((buildRow(row, colIndices) + "\n").getBytes("UTF-8"));
                    } else {
                        writer.println(buildRow(row, colIndices));
                    }
                }
            }
            if (zip != null) { finishZip(zip); }
        } catch (IndexOutOfBoundsException iob) {
            writeError(invalidParamValueException("offset", offset));
        } catch (WebServiceException wse) {
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something unexpected went wrong with the web service request.", e);
            writeInternalError();
        } finally {
            if (writer != null) { writer.close(); }
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
