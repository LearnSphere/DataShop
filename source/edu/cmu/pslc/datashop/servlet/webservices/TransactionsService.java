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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.LogUtils;

import static java.lang.String.format;

import static edu.cmu.pslc.importdata.ImportConstants.ACTION_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.ATTEMPT_AT_STEP_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.CLASS_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.CONDITION_NAME_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.CONDITION_TYPE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.STUDENT_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.SESSION_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.TIME_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.TIME_ZONE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.DURATION_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.STUDENT_RESPONSE_TYPE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.STUDENT_RESPONSE_SUBTYPE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.TUTOR_RESPONSE_TYPE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.TUTOR_RESPONSE_SUBTYPE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.PROBLEM_NAME_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.PROBLEM_VIEW_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.PROBLEM_START_TIME_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.STEP_NAME_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.OUTCOME_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.SELECTION_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.INPUT_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.FEEDBACK_TEXT_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.FEEDBACK_CLASSIFICATION_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.HELP_LEVEL_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.TOTAL_HINTS_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.SCHOOL_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.SAMPLE_HEADING;
import static edu.cmu.pslc.importdata.ImportConstants.GUID_HEADING;

import static edu.cmu.pslc.datashop.servlet.
              webservices.WebServiceException.invalidParamValueException;
import static edu.cmu.pslc.datashop.servlet.
                webservices.WebServiceException.INVALID_CUSTOM_FIELD_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.CollectionUtils.select;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Web service for fetching transactions.
 * @author jimbokun
 * @version $Revision: 10125 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-10-10 14:16:27 -0400 (Thu, 10 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TransactionsService extends WebService {
    /** feedback column parameter */
    private static final String FEEDBACK_COL = "feedback";
    /** class column parameter */
    private static final String CLASS_COL = "class";
    /** school column parameter */
    private static final String SCHOOL_COL = "school";
    /** total number of hints column parameter */
    private static final String TOTAL_HINTS_COL = "total_num_hints";
    /** help level column parameter */
    private static final String HELP_COL = "help_level";
    /** input column parameter */
    private static final String INPUT_COL = "input";
    /** action column parameter */
    private static final String ACTION_COL = "action";
    /** selection column parameter */
    private static final String SELECTION_COL = "selection";
    /** outcome column parameter */
    private static final String OUTCOME_COL = "outcome";
    /** problem view column parameter */
    private static final String PROBLEM_VIEW_COL = "problem_view";
    /** problem start time column parameter */
    private static final String PROBLEM_START_TIME_COL = "problem_start_time";
    /** attempt at step column parameter */
    private static final String ATTEMPT_COL = "attempt_at_step";
    /** tutor response subtype column parameter */
    private static final String TUTOR_RESPONSE_SUB_COL = "tutor_response_subtype";
    /** tutor response type column parameter */
    private static final String TUTOR_RESPONSE_COL = "tutor_response_type";
    /** student response subtype column parameter */
    private static final String STUDENT_RESPONSE_SUB_COL = "student_response_subtype";
    /** student response type column parameter */
    private static final String STUDENT_RESPONSE_COL = "student_response_type";
    /** duration column parameter */
    private static final String DURATION_COL = "duration";
    /** time zone column parameter */
    private static final String TIME_ZONE_COL = "time_zone";
    /** time column parameter */
    private static final String TIME_COL = "time";
    /** session id column parameter */
    private static final String SESSION_ID_COL = "session_id";
    /** guid column parameter */
    private static final String GUID_COL = "tx_id";
    /** tab delimited format parameter */
    private static final String TD = "td";
    /** xml format parameter */
    private static final String XML = "xml";
    /** the format parameter */
    private static final String FORMAT = "format";
    /** pattern for extracting level names from level header columns */
    private static final Pattern LEVELS_PATTERN = Pattern.compile("Level \\((.*)\\)");

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(VERBOSE, DATASET_ID, SAMPLE_ID,
            FORMAT, ZIP, COLS, CFS_COL, LIMIT, OFFSET, HEADERS);
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS_EXCLUDE = set(CFS_COL);
    /** maps parameter key to valid values for that parameter */
    private static final Map<String, Set<String>> VALID_GET_PARAM_VALUES =
        map(ACCESS, ACCESS_PARAMS, VERBOSE, TF, ZIP, TF, FORMAT, set(XML, TD), HEADERS, TF);
    /** Valid values for the columns parameter. */
    private static final Set<String> COL_VALUES = set(STUDENT_COL, GUID_COL,
            SESSION_ID_COL, TIME_COL, TIME_ZONE_COL, DURATION_COL, STUDENT_RESPONSE_COL,
            STUDENT_RESPONSE_SUB_COL, TUTOR_RESPONSE_COL, TUTOR_RESPONSE_SUB_COL,
            PROBLEM_COL,
            PROBLEM_VIEW_COL,
            PROBLEM_START_TIME_COL,
            STEP_COL, ATTEMPT_COL, OUTCOME_COL, SELECTION_COL, ACTION_COL, INPUT_COL,
            FEEDBACK_COL, HELP_COL, TOTAL_HINTS_COL, CONDITION_COL, KCS_COL, SCHOOL_COL,
            CLASS_COL, PROBLEM_HIERARCHY_COL, ROW_COL);
    /** maps columns parameters to export file column headings */
    private static final Map<String, String> COL_HEADINGS = map(STUDENT_COL, STUDENT_HEADING,
            SESSION_ID_COL, SESSION_HEADING, TIME_COL, TIME_HEADING,
            TIME_ZONE_COL, TIME_ZONE_HEADING, DURATION_COL, DURATION_HEADING,
            STUDENT_RESPONSE_COL, STUDENT_RESPONSE_TYPE_HEADING,
            STUDENT_RESPONSE_SUB_COL, STUDENT_RESPONSE_SUBTYPE_HEADING,
            TUTOR_RESPONSE_COL, TUTOR_RESPONSE_TYPE_HEADING,
            TUTOR_RESPONSE_SUB_COL, TUTOR_RESPONSE_SUBTYPE_HEADING,
            PROBLEM_COL, PROBLEM_NAME_HEADING,
            PROBLEM_VIEW_COL, PROBLEM_VIEW_HEADING,
            PROBLEM_START_TIME_COL, PROBLEM_START_TIME_HEADING,
            STEP_COL, STEP_NAME_HEADING,
            ATTEMPT_COL, ATTEMPT_AT_STEP_HEADING,
            OUTCOME_COL, OUTCOME_HEADING, SELECTION_COL, SELECTION_HEADING,
            ACTION_COL, ACTION_HEADING, INPUT_COL, INPUT_HEADING, HELP_COL, HELP_LEVEL_HEADING,
            TOTAL_HINTS_COL, TOTAL_HINTS_HEADING, SCHOOL_COL, SCHOOL_HEADING,
            CLASS_COL, CLASS_HEADING, PROBLEM_HIERARCHY_COL, PROBLEM_HIERARCHY_HEADING,
            ROW_COL, ROW_HEADING, GUID_COL, GUID_HEADING);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public TransactionsService(HttpServletRequest req, HttpServletResponse resp,
            Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Determine which columns to include based on the header row of the export file and the
     * columns parameter.
     * @param headers the header row of the export file
     * @return the indices of the columns to include
     * @throws WebServiceException the web service exception
     */
    private List<Integer> colIndices(List<String> headers) throws WebServiceException {
        List<String> cols = colsParam();
        List<String> cfs = multipleStringParam(CFS_COL);
        boolean includeAllCfs = false;
        boolean includeNoneCfs = false;
        if (cfs.size() == 1 && ALL.equals(cfs.get(0))) {
            includeAllCfs = true;
        }
        if (cfs.size() == 1 && NONE.equals(cfs.get(0))) {
            includeNoneCfs = true;
        }
        if (includeAllCfs || includeNoneCfs) {
            cfs = emptyList();
        }
        List<String> cfNames = helper().cfNames(datasetParam(), getAuthenticatedUser(), cfs);
        if (cfs.size() > 0 && cfNames.size() == 0) {
            throw new WebServiceException(INVALID_CUSTOM_FIELD_ERR,
                    "Custom field " + stringParam(CFS_COL)
                    + " is not valid.");
        }
        Set<String> includeHeaders = new HashSet<String>();
        List<Integer> indices = new ArrayList<Integer>();
        //decide cf is all, none, or specified

        boolean includeKcs = false;
        // IF which columns to include is specified on the command line
        boolean colsSpecified = false;
        for (String col : cols) {
            colsSpecified = true;
            String heading = COL_HEADINGS.get(col);

            if (heading != null) {
                includeHeaders.add(heading);
            } else if (FEEDBACK_COL.equals(col)) {
                includeHeaders.add(FEEDBACK_CLASSIFICATION_HEADING);
                includeHeaders.add(FEEDBACK_TEXT_HEADING);
            } else if (CONDITION_COL.equals(col)) {
                includeHeaders.add(CONDITION_NAME_HEADING);
                includeHeaders.add(CONDITION_TYPE_HEADING);
            } else if (KCS_COL.equals(col)) {
                includeKcs = true;
            }
        }

        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (includeHeaders.contains(header)
             || (includeKcs && isKcHeader(header))
             || (colsSpecified && (includeAllCfs || cfNames.contains(stripCF(header)))
                             && isCfHeader(header))) {
                    indices.add(i);
            }
        }
        // Eliminate the Sample Name column
        if (cols.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                if (!SAMPLE_HEADING.equals(header)
                 && !isLevelHeader(header)
                 && (includeAllCfs || cfNames.contains(stripCF(header)) || !isCfHeader(header))) {
                    indices.add(i);
                }
            }
        }

        return indices;
    }

    /**
     * Strip the CF() out of the header string.
     * @param header the original header
     * @return the original header minus the CF( and ).
     */
    private String stripCF(String header) {
        if (!isCfHeader(header)) {
            return header;
        } else {
            //CF header is in format of CF (some_custom_field_name)
            return header.substring(header.indexOf("(") + 1, header.indexOf(")"));
        }
    }

    /**
     * Whether to include the column in the export.
     * @param col the column
     * @return whether to include the column in the export
     */
    private boolean includeColumn(String col) {
        List<String> inclCols = colsParam();
        return inclCols.isEmpty() || inclCols.contains(col);
    }

    /**
     * Whether this is a custom field column header.
     * @param header the column header
     * @return whether this is a custom field header
     */
    private boolean isCfHeader(String header) { return header.startsWith("CF"); }

    /**
     * Whether this is a knowledge component column header.
     * @param header the column header
     * @return whether this is a knowledge component header
     */
    private boolean isKcHeader(String header) { return header.startsWith("KC"); }

    /**
     * Whether this is a level column header.
     * @param header the column header
     * @return whether this is a level column header
     */
    private boolean isLevelHeader(String header) { return header.startsWith("Level"); }

    /** A data structure holding a start and end value. */
    private class Range {
        /** range start */
        private int start;
        /** range end */
        private int end;

        /**
         * Create a new range.
         * @param start the range start
         * @param end the range end
         */
        Range(int start, int end) { this.start = start; this.end = end; }

        /** Range start. @return range start */
        public int start() { return start; }

        /** Range end. @return range end */
        public int end() { return end; }

        /** Printable representation. @return printable representation */
        public String toString() { return format("[start::%s end::%s]", start, end); }
    }

    /**
     * Get the range from the first level column to the last
     * (level columns must appear consecutively).
     * @param headers the column headers
     * @return range from the first level column to the last
     */
    private Range levelRange(List<String> headers) {
        int start = 0, end;

        while (!isLevelHeader(headers.get(start))) { start++; }
        end = start;
        while (isLevelHeader(headers.get(end))) { end++; }

        return new Range(start, end);
    }

    /**
     * Parse out the name of each level from the headers.
     * @param headers the column headers
     * @return a list of level names
     */
    private List<String> levelNames(final List<String> headers) {
        return new ArrayList<String>() { {
            Range r = levelRange(headers);

            for (String header : headers.subList(r.start(), r.end())) {
                Matcher matcher = LEVELS_PATTERN.matcher(header);

                if (matcher.matches()) { add(matcher.group(1)); }
            }
        } };
    }

    /**
     * Construct problem hierarchy string from the level columns in row.
     * @param levelCols range of level columns
     * @param levelNames level names extracted from header columns
     * @param row the current row
     * @return problem hierarchy for level columns in current row
     */
    private String problemHierarchy(Range levelCols, List<String> levelNames,
            List<String> row) {
        List<String> levels = new ArrayList<String>();
        for (int i = levelCols.start(); i < levelCols.end(); i++) {
            levels.add(levelNames.get(i - levelCols.start()) + " " + row.get(i));
        }
        return join(", ", levels);
    }

    /**
     * Replace items in range with the given items.
     * @param <E> the type of the items
     * @param r range of items to remove
     * @param list the list in which we are replacing items
     * @param replaceWith the items to substitute for the removed items
     * @return a new list containing the result of the replace operation
     */
    private <E> List<E> replace(final Range r, final List<E> list, final List<E> replaceWith) {
        return new ArrayList<E>() { {
            for (E item : list.subList(0, r.start())) { add(item); }
            for (E item : replaceWith) { add(item); }
            for (E item : list.subList(r.end(), list.size())) { add(item); }
        } };
    }

    /**
     * Replace items in range with the given item.
     * @param <E> the type of the items
     * @param r range of items to remove
     * @param list the list in which we are replacing items
     * @param replaceWith the item to substitute for the removed items
     * @return a new list containing the result of the replace operation
     */
    private <E> List<E> replace(final Range r, final List<E> list, final E replaceWith) {
        return replace(r, list, singletonList(replaceWith));
    }

    /**
     * Replace items in range from the list.
     * @param <E> the type of the items
     * @param r range of items to remove
     * @param list the list in which we are replacing items
     * @return the list with the items in range removed
     */
    private <E> List<E> remove(final Range r, final List<E> list) {
        List<E> empty = emptyList();
        return replace(r, list, empty);
    }

    /** Return the requested transactions in tab delimited format. */
    public void get(WebServiceUserLog wsUserLog) {
        PrintWriter writer = null;
        ZipOutputStream zip = null;
        int offset = -1;

        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS,
                    VALID_GET_PARAM_VALUES, VALID_GET_PARAMS_EXCLUDE);
            validateMultipleParam(COLS, COL_VALUES);
            validateCFParam();
            offset = offsetParam();
            Iterable<List<String>> rows = helper().transactionRows(getAuthenticatedUser(),
                    datasetParam(), sampleParam(), limitParam(), offset);
            List<Integer> colIndices = null;
            Range levelCols = null;
            List<String> levelNames = null;
            boolean isZip = zipParam();
            boolean includeProbHierarchy = includeColumn(PROBLEM_HIERARCHY_COL);
            boolean includeRow = includeColumn(ROW_COL);
            boolean showHeaders = headerParam();

            if (isZip) {
                zip = initZip("transactions");
            } else {
                writer = getResp().getWriter();
            }

            int rowno = offset;
            String hierarchy;

            for (List<String> row : rows) {
                // header row
                if (rowno == offset) {
                    // check whether the Row header is already in the cached file
                    if (!row.isEmpty() && ROW_HEADING.equals(row.get(0))) {
                        includeRow = false;
                    }
                    // add the Row header
                    if (includeRow) { row.add(0, ROW_HEADING); }
                    // replace the Level headers with Problem Hierarchy header
                    levelCols = levelRange(row);
                    levelNames = levelNames(row);
                    hierarchy = PROBLEM_HIERARCHY_HEADING;
                } else {
                    // add the current row number
                    if (includeRow) { row.add(0, Integer.toString(rowno)); }
                    // replace the level columns with the Problem Hierarchy string
                    hierarchy = problemHierarchy(levelCols, levelNames, row);
                }
                if (includeProbHierarchy) {
                    row = replace(levelCols, row, hierarchy);
                } else {
                    row = remove(levelCols, row);
                }
                // determine column indices AFTER replacing level headers
                if (rowno == offset) { colIndices = colIndices(row); }

                if (!colIndices.isEmpty()) { row = select(colIndices, row); }
                if (rowno > offset || showHeaders) {
                    if (isZip) {
                        zip.write((buildRow(row, colIndices) + "\n").getBytes("UTF-8"));
                    } else {
                        writer.println(buildRow(row, colIndices));
                    }
                }
                rowno++;
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
