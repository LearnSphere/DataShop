/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.importdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.logging.util.DateTools;


/**
 * Tool used to verify the input format of tab-delimited dataset
 * files to be imported into the DataShop.
 *
 * @author Kyle A Cunningham
 * @version $Revision: 11676 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-03 11:03:12 -0500 (Mon, 03 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetVerificationTool {

    /** The name of this tool, used in displayUsage method. */
    private static final String TOOL_NAME = DatasetVerificationTool.class.getSimpleName();

    /** The output/logging file name. */
    private static final String LOG_FILE_NAME = "datashop-verify.log";
    /** Logger for the tool. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Name of the tab-delimited input file. */
    private static String inputFileName;
    /** default delimiter */
    private static final String DEFAULT_DELIMITER = "\t";
    /** empty string */
    private static final String EMPTY_STRING = "";

    /** Set of option column headings */
    private HashSet columnHeadings = new HashSet();

    /** Number of Student columns */
    private Integer maxStudentCount = new Integer(0);
    /** Number of Dataset columns */
    private Integer maxDatasetLevelCount = new Integer(0);
    /** Number of Selection columns */
    private Integer maxSelectionCount = new Integer(0);
    /** Number of Action Columns */
    private Integer maxActionCount = new Integer(0);
    /** Number of Input columns */
    private Integer maxInputCount = new Integer(0);
    /** Number of Conditions */
    private Integer maxConditionCount = new Integer(0);
    /** Number of Skills */
    private Integer maxSkillCount = new Integer(0);
    /** Number of Custom Field columns */
    private Integer maxCustomFieldCount = new Integer(0);
    /** Line number being processed */
    private Integer lineNumber = new Integer(0);
    /** Number of errors */
    private Integer errorCount = new Integer(0);
    /** Number of warnings */
    private Integer warningCount = new Integer(0);
    /** Number of columns in dataset */
    private Integer columnCount = new Integer(0);

    /** Set of variables to indicate if various optional columns are present */
    /** Indicates if row is included in dataset file */
    private static boolean rowPresent = false;
    /** Indicates if sample is included in dataset file */
    private static boolean samplePresent = false;
    /** Indicates if time zone is included in dataset file */
    private static boolean timeZonePresent = false;
    /** Indicates if transaction duration is included in dataset file */
    private static boolean durationPresent = false;
    /** Indicates if student response type is included in dataset file */
    private static boolean studentResponseTypePresent = false;
    /** Indicates if student response subtype is included in dataset file */
    private static boolean studentResponseSubtypePresent = false;
    /** Indicates if tutor response type is included in dataset file */
    private static boolean tutorResponseTypePresent = false;
    /** Indicates if tutor response subtype is included in dataset file */
    private static boolean tutorResponseSubtypePresent = false;
    /** Indicates if problem view is included in dataset file */
    private static boolean problemViewPresent = false;
    /** Indicates if problem start time is included in dataset file */
    private static boolean problemStartTimePresent = false;
    /** Indicates if step name is included in dataset file */
    private static boolean stepNamePresent = false;
    /** Indicates if attempt at step is included in dataset file */
    private static boolean attemptAtStepPresent = false;
    /** Indicates if outcome is included in dataset file */
    private static boolean outcomePresent = false;
    /** Indicates if feedback text is included in dataset file */
    private static boolean feedbackTextPresent = false;
    /** Indicates if feedback classification is included in dataset file */
    private static boolean feedbackClassificationPresent = false;
    /** Indicates if help level is included in dataset file */
    private static boolean helpLevelPresent = false;
    /** Indicates if total # hints is included in dataset file */
    private static boolean totalNumHintsPresent = false;
    /** Indicates if condition(s) included in dataset file */
    private static boolean conditionPresent = false;
    /** Indicates if knowledge component(s) included in dataset file */
    private static boolean kcPresent = false;
    /** Indicates is KC categories are present */
    private static boolean kcCategoryPresent = false;
    /** Indicates if school is included in dataset file */
    private static boolean schoolPresent = false;
    /** Indicates if class is included in dataset file */
    private static boolean classPresent = false;
    /** Indicates if custom field(s) included in dataset file */
    private static boolean cfPresent = false;
    /** Set of DatasetLevel Titles -- order matters!*/
    private ArrayList datasetLevelTitles = new ArrayList();
    /** Set of Knowledge Component Categories */
    private Set kcCategories = new HashSet();
    /** Set of Custom Field Names */
    private ArrayList customFieldNames = new ArrayList();
    /** Set Skill Model Names */
    private ArrayList skillModelNames = new ArrayList();
    /** Sample name for the dataset file */
    private String sampleName = null;
    /** Flag indicating if sample name has been set */
    private Boolean sampleWarningSet = false;

    /**
     * Default constructor.
     */
    public DatasetVerificationTool() { };

    /**
     * Populate the columnHeadings hash for use during
     * file verification.
     */
    public void populateColumnHeadings() {
        columnHeadings.add(ImportConstants.ROW_HEADING);
        columnHeadings.add(ImportConstants.SAMPLE_HEADING);
        columnHeadings.add(ImportConstants.STUDENT_HEADING);
        columnHeadings.add(ImportConstants.SESSION_HEADING);
        columnHeadings.add(ImportConstants.TIME_HEADING);
        columnHeadings.add(ImportConstants.TIME_ZONE_HEADING);
        columnHeadings.add(ImportConstants.DURATION_HEADING);
        columnHeadings.add(ImportConstants.STUDENT_RESPONSE_TYPE_HEADING);
        columnHeadings.add(ImportConstants.STUDENT_RESPONSE_SUBTYPE_HEADING);
        columnHeadings.add(ImportConstants.TUTOR_RESPONSE_TYPE_HEADING);
        columnHeadings.add(ImportConstants.TUTOR_RESPONSE_SUBTYPE_HEADING);
        columnHeadings.add(ImportConstants.DATASET_LEVEL_HEADING);
        columnHeadings.add(ImportConstants.PROBLEM_NAME_HEADING);
        columnHeadings.add(ImportConstants.PROBLEM_VIEW_HEADING);
        columnHeadings.add(ImportConstants.PROBLEM_START_TIME_HEADING);
        columnHeadings.add(ImportConstants.STEP_NAME_HEADING);
        columnHeadings.add(ImportConstants.ATTEMPT_AT_STEP_HEADING);
        columnHeadings.add(ImportConstants.OUTCOME_HEADING);
        columnHeadings.add(ImportConstants.SELECTION_HEADING);
        columnHeadings.add(ImportConstants.ACTION_HEADING);
        columnHeadings.add(ImportConstants.INPUT_HEADING);
        columnHeadings.add(ImportConstants.FEEDBACK_TEXT_HEADING);
        columnHeadings.add(ImportConstants.FEEDBACK_CLASSIFICATION_HEADING);
        columnHeadings.add(ImportConstants.HELP_LEVEL_HEADING);
        columnHeadings.add(ImportConstants.TOTAL_HINTS_HEADING);
        columnHeadings.add(ImportConstants.CONDITION_NAME_HEADING);
        columnHeadings.add(ImportConstants.CONDITION_TYPE_HEADING);
        columnHeadings.add(ImportConstants.KC_HEADING);
        columnHeadings.add(ImportConstants.KC_CATEGORY_HEADING);
        columnHeadings.add(ImportConstants.SCHOOL_HEADING);
        columnHeadings.add(ImportConstants.CLASS_HEADING);
        columnHeadings.add(ImportConstants.CUSTOM_FIELD_HEADING);
    }

    /**
     * Parse the provided custom field column heading and return the
     * name of the custom field.
     * @param customField the custom field heading to process
     * @return errorString a string containing any error messages produced
     *      during processing
     */
    private String getCustomFieldName(String customField) {
        String errorString = null;
        String[] split = customField.split("CF");
        // the actual name should be in the second position
        String name = split[1];
        if (name.startsWith("(") && name.endsWith(")")) {
            name = name.replace("(", "");
            name = name.replace(")", "").trim();

            if (name == null || name.equals("")) {
                name = "Default_" + (customFieldNames.size() + 1);
                logger.info("getCustomFieldName()::Current custom field has no title,"
                        + "setting to '" + name + "'");
            }
            customFieldNames.add(name);
        } else {    // custom field name was not followed by a ")"
            errorCount++;
            errorString = "Line " + lineNumber
                + ": Was not able to read the Custom Field heading "
                + "'" + name + "'.  Please verify the formatting of the column heading.\n";
        }
        return errorString;
    }

    /**
     * Parse the provided dataset level column heading and return the
     * title of the dataset level.
     * @param datasetLevel the dataset level heading to process
     * @return errorString a string containing any error messages produced
     *      during processing
     */
    private String getDatasetLevelTitle(String datasetLevel) {
        String errorString = null;
        String[] split = datasetLevel.split("Level");
        // the actual name should be in the second position
        String title = split[1];
        if (title.startsWith("(") && title.endsWith(")")) {
            title = title.replace("(", "");
            title = title.replace(")", "").trim();

            if (title == null || title.equals("")) {
                logger.info("getDatasetLevelTitle() :: Current dataset level has no title,"
                        + "setting to 'Default'.");
                title = "Default";
            }
            datasetLevelTitles.add(title);
        } else {
            errorCount++;
            errorString = "Line " + lineNumber
                + ": Was not able to read the Dataset Level heading "
                + "'" + title + "'.  Please verify the formatting of the column heading.\n";
        }
        return errorString;
    }

    /**
     * Parse the provided knowledge component column heading and return the
     * name of the knowledge component.
     * @param skillModelName the name of the skill model to process
     * @return errorString a string containing any error messages produced
     *      during processing
     */
    private String processSkillModelName(String skillModelName) {
        String errorString = null;
        String[] split = skillModelName.split("KC\\(");
        // the actual name should be in the first position
        String name = split[1];
        if (name.endsWith(")")) {
            name = name.replace(")", "").trim();

            if (name == null || name.equals("")) {
                logger.info("getSkillModelName() :: Current KC has no KC Model name,"
                        + "setting to 'Default'.");
                name = "Default";
            }
            skillModelNames.add(name);
        } else {
            errorCount++;
            errorString = "Line " + lineNumber
                + ": Was not able to read the KC Model name "
                + "'" + name + "'.  Please verify the formatting of the column heading.\n";
        }
        return errorString;
    }

    /**
     * Check the first line of the input file for correct column headings.
     * Comb through the array and check for the presence of each column header against
     * constants declared above.  If a column heading is not found, return a useful error message.
     * This method will not opt out after finding the first discrepancy (it keeps going to identify
     * additional errors (if any)).
     * @param line The first line from the input file, which contains the column headings.
     * @return ArrayList result - contains any error messages generated.  If the column headings
     *      are OK, an empty ArrayList is returned.
     */
    private ArrayList checkColumnHeadings(String line) {
        ArrayList result = new ArrayList();
        String currentHeading = new String();
        String[] lineSplit = line.split(DEFAULT_DELIMITER);
        int lineSplitLength = lineSplit.length;
        int position = 0;
        errorCount = 0;
        lineNumber = 1;
        columnCount = 0;

        try {
            // ROW_HEADING and SAMPLE_HEADING are optional
            currentHeading = lineSplit[position];
            if (currentHeading.equals("")) {
                position++;
            } else {
                if (columnHeadings.contains(currentHeading)) {
                    if (currentHeading.equals(ImportConstants.ROW_HEADING)) {
                        setRowPresent(true);
                        position++;
                        columnCount++;
                        String nextHeading = lineSplit[position];
                        if (nextHeading.equals(ImportConstants.SAMPLE_HEADING)) {
                            setSamplePresent(true);
                            position++;
                            columnCount++;
                        }
                    }
                    if (currentHeading.equals(ImportConstants.SAMPLE_HEADING)) {
                        setSamplePresent(true);
                        position++;
                        columnCount++;
                    }
                } else { // there is a heading here we can't process
                    errorCount++;
                    result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
                }
            }

            // STUDENT_HEADING is required.
        if (lineSplit[position].equals(ImportConstants.STUDENT_HEADING)) {
            maxStudentCount++;
            position++;
            columnCount++;
            // We used to allow multiple students... not any more.
            if (lineSplit[position].equals(ImportConstants.STUDENT_HEADING)) {
                result.add("Line " + lineNumber
                        + ": Duplicate " + ImportConstants.STUDENT_HEADING + " was found.\n");
                errorCount++;
                position++;
            }
        } else {
            result.add("Line " + lineNumber + ": " + ImportConstants.STUDENT_HEADING
                + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }
        }

        // SESSION_HEADING is required.
        if (lineSplit[position].equals(ImportConstants.SESSION_HEADING)) {
            position++;
            columnCount++;
        } else {
            result.add("Line " + lineNumber + ": "
                + ImportConstants.SESSION_HEADING + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }
        }

        // TIME_HEADING is required.
        if (lineSplit[position].equals(ImportConstants.TIME_HEADING)) {
            position++;
            columnCount++;
        } else {
            result.add("Line " + lineNumber + ": " + ImportConstants.TIME_HEADING
                + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }
        }

        boolean datasetLevelFound = false;

        // TIME_ZONE_HEADING is optional
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.TIME_ZONE_HEADING)) {
                    setTimeZonePresent(true);
                    position++;
                    columnCount++;
                }
            } else if (currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                    datasetLevelFound = true;
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // DURATION_HEADING is optional
        if (!datasetLevelFound) {
            currentHeading = lineSplit[position];
            if (currentHeading.equals("")) {
                position++;
            } else {
                if (columnHeadings.contains(currentHeading)) {
                    if (currentHeading.equals(ImportConstants.DURATION_HEADING)) {
                        setDurationPresent(true);
                        position++;
                        columnCount++;
                    }
                } else if (currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                        datasetLevelFound = true;
                } else { // there is a heading here we can't process
                    errorCount++;
                    result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
                }
            }
        }

        // STUDENT_RESPONSE_TYPE_HEADING is optional.
        if (!datasetLevelFound) {
            currentHeading = lineSplit[position];
            if (currentHeading.equals("")) {
                position++;
            } else {
                if (columnHeadings.contains(currentHeading)) {
                    if (currentHeading.equals(ImportConstants.STUDENT_RESPONSE_TYPE_HEADING)) {
                        setStudentResponseTypePresent(true);
                        position++;
                        columnCount++;
                    }
                } else if (currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                    datasetLevelFound = true;
                } else { // there is a heading here we can't process
                    errorCount++;
                    result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
                }
            }
        }

        // STUDENT_RESPONSE_SUBTYPE_HEADING is optional
        if (!datasetLevelFound) {
            currentHeading = lineSplit[position];
            if (currentHeading.equals("")) {
                position++;
            } else {
                if (columnHeadings.contains(currentHeading)) {
                    if (currentHeading.equals(ImportConstants.STUDENT_RESPONSE_SUBTYPE_HEADING)) {
                        setStudentResponseSubtypePresent(true);
                        position++;
                        columnCount++;
                    }
                } else if (currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                    datasetLevelFound = true;
                } else { // there is a heading here we can't process
                    errorCount++;
                    result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
                }
            }
        }

        // TUTOR_RESPONSE_TYPE is optional.
        if (!datasetLevelFound) {
            currentHeading = lineSplit[position];
            if (currentHeading.equals("")) {
                position++;
            } else {
                if (columnHeadings.contains(currentHeading)) {
                    if (currentHeading.equals(ImportConstants.TUTOR_RESPONSE_TYPE_HEADING)) {
                        setTutorResponseTypePresent(true);
                        position++;
                        columnCount++;
                    }
                } else if (currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                    datasetLevelFound = true;
                } else { // there is a heading here we can't process
                    errorCount++;
                    result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
                }
            }
        }

        //TUTOR_RESPONSE_SUBTYPE_HEADING is optional
        if (!datasetLevelFound) {
            currentHeading = lineSplit[position];
            if (currentHeading.equals("")) {
                position++;
            } else {
                if (columnHeadings.contains(currentHeading)) {
                    if (currentHeading.equals(ImportConstants.TUTOR_RESPONSE_SUBTYPE_HEADING)) {
                        setTutorResponseSubtypePresent(true);
                        position++;
                        columnCount++;
                    }
                } else if (currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                    datasetLevelFound = true;
                } else { // there is a heading here we can't process
                    errorCount++;
                    result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
                }
            }
        }

        // DATASET_LEVEL_HEADING is required.
        if (lineSplit[position].startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
            // there may be multiple dataset levels, so check for this
            String temp = getDatasetLevelTitle(lineSplit[position]);
            if (temp != null && temp != "") {
                result.add(temp);
            }
            maxDatasetLevelCount++;
            position++;
            columnCount++;
            while (lineSplit[position].startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                temp = getDatasetLevelTitle(lineSplit[position]);
                if (temp != null && temp != "") {
                    result.add(temp);
                }
                position++;
                columnCount++;
                maxDatasetLevelCount++;
            }
        } else {
            result.add("Line " + lineNumber + ": "
                       + ImportConstants.DATASET_LEVEL_HEADING
                       + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }
        }

        // PROBLEM_NAME_HEADING is required.
        if (lineSplit[position].equals(ImportConstants.PROBLEM_NAME_HEADING)) {
            position++;
            columnCount++;
        } else {
            result.add("Line " + lineNumber + ": " + ImportConstants.PROBLEM_NAME_HEADING
                + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }
        }

        // PROBLEM_VIEW_HEADING is optional.
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.PROBLEM_VIEW_HEADING)) {
                    problemViewPresent = true;
                    position++;
                    columnCount++;
                }
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // PROBLEM_START_TIME_HEADING is optional.
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.PROBLEM_START_TIME_HEADING)) {
                    problemStartTimePresent = true;
                    position++;
                    columnCount++;
                }
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // STEP_NAME_HEADING is optional.
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.STEP_NAME_HEADING)) {
                    setStepNamePresent(true);
                    position++;
                    columnCount++;
                }
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        //ATTEMPT_AT_STEP_HEADING is optional
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.ATTEMPT_AT_STEP_HEADING)) {
                    setAttemptAtStepPresent(true);
                    position++;
                    columnCount++;
                }
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // OUTCOME_HEADING is optional.
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.OUTCOME_HEADING)) {
                    setOutcomePresent(true);
                    position++;
                    columnCount++;
                }
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }


        // SELECTION_HEADING is required.
        if (lineSplit[position].equals(ImportConstants.SELECTION_HEADING)) {
            // there may be multiple selections, so check for this
            maxSelectionCount++;
            position++;
            columnCount++;
            while (lineSplit[position].equals(ImportConstants.SELECTION_HEADING)) {
                position++;
                columnCount++;
                maxSelectionCount++;
            }
        } else {
            result.add("Line " + lineNumber + ": " + ImportConstants.SELECTION_HEADING
                + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }

        }

        // ACTION_HEADING is required.
        if (lineSplit[position].equals(ImportConstants.ACTION_HEADING)) {
            // there may be multiple actions, so check for this
            maxActionCount++;
            position++;
            columnCount++;
            while (lineSplit[position].equals(ImportConstants.ACTION_HEADING)) {
                position++;
                columnCount++;
                maxActionCount++;
            }
        } else {
            result.add("Line " + lineNumber + ": " + ImportConstants.ACTION_HEADING
                + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }
        }

        // INPUT_HEADING is required.
        if (lineSplit[position].equals(ImportConstants.INPUT_HEADING)) {
            // there may be multiple inputs, so check for this
            maxInputCount++;
            position++;
            columnCount++;
            while (lineSplit[position].equals(ImportConstants.INPUT_HEADING)) {
                position++;
                columnCount++;
                maxInputCount++;
            }
        } else {
            result.add("Line " + lineNumber + ": " + ImportConstants.INPUT_HEADING
                + " was not found.\n");
            errorCount++;
            if (lineSplit[position].equals("")) { position++; }
        }

        // FEEDBACK_TEXT_HEADING is optional
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.FEEDBACK_TEXT_HEADING)) {
                    setFeedbackTextPresent(true);
                    position++;
                    columnCount++;
                }
            } else if (currentHeading.startsWith(ImportConstants.KC_HEADING)) {
                setKcPresent(true);
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // FEEDBACK_CLASSIFICATION_HEADING is optional
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.FEEDBACK_CLASSIFICATION_HEADING)) {
                    setFeedbackClassificationPresent(true);
                    position++;
                    columnCount++;
                }
            } else if (currentHeading.startsWith(ImportConstants.KC_HEADING)) {
                kcPresent = true;
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // HELP_LEVEL_HEADING is optional
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.HELP_LEVEL_HEADING)) {
                    setHelpLevelPresent(true);
                    position++;
                    columnCount++;
                }
            } else if (currentHeading.startsWith(ImportConstants.KC_HEADING)) {
                kcPresent = true;
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // TOTAL_HINTS_HEADING is optional
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.TOTAL_HINTS_HEADING)) {
                    setTotalNumHintsPresent(true);
                    position++;
                    columnCount++;
                }
            } else if (currentHeading.startsWith(ImportConstants.KC_HEADING)) {
                kcPresent = true;
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // CONDITION_NAME is optional
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                //if (currentHeading.equals(CONDITION_NAME_HEADING)) {
                if (currentHeading.startsWith("Condition")) {
                    setConditionPresent(true);
                    while (lineSplit[position].startsWith("Condition")) {
                        //check for multiple condition/condition type column pairs
                        if (lineSplit[position].equals(ImportConstants.CONDITION_NAME_HEADING)) {
                            position++;
                            columnCount++;
                            if (!lineSplit[position].equals(
                                    ImportConstants.CONDITION_TYPE_HEADING)) {
                                result.add("Line " + lineNumber + ": " + "Each "
                                        + ImportConstants.CONDITION_NAME_HEADING
                                    + " must be paired with a "
                                    + ImportConstants.CONDITION_TYPE_HEADING + ".\n\t"
                                    + "A " + ImportConstants.CONDITION_TYPE_HEADING
                                    + " was not found in the expected position.\n");
                                errorCount++;
                            } else {
                                maxConditionCount++;
                                position++; // move past the CONDITION_TYPE_HEADING
                                columnCount++;
                            }
                        } else if (lineSplit[position].equals(
                                ImportConstants.CONDITION_TYPE_HEADING)) {
                            // Condition Type without a Condition column
                            result.add("Line " + lineNumber + ": " + "Each "
                                    + ImportConstants.CONDITION_TYPE_HEADING
                                + " must be paired with a "
                                + ImportConstants.CONDITION_NAME_HEADING + ".\n\t");
                            errorCount++;
                            position++;
                        }
                    } // end while startsWith("Condition")
                }
            } else if (currentHeading.startsWith(ImportConstants.KC_HEADING)) {
                setKcPresent(true);
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }
        // KC_HEADING is optional
        currentHeading = lineSplit[position];
        if (currentHeading.startsWith(ImportConstants.KC_HEADING)) {
            setKcPresent(true);
        }
        while (lineSplit[position].startsWith("KC")) {
            if (lineSplit[position].startsWith(ImportConstants.KC_HEADING)) {
                String temp = processSkillModelName(lineSplit[position]);
                if (temp != null && temp != "") {
                    result.add(temp);
                }
                position++;
                columnCount++;
                maxSkillCount++;
            }
            // look at next column to see if KC Category is included (have already incremented
            // position)
            if (lineSplit[position].startsWith(ImportConstants.KC_CATEGORY_HEADING)) {
                setKcCategoryPresent(true);
                position++;
                columnCount++;
            }
        } // end while startsWith("KC")

        // SCHOOL_HEADING is optional
        logger.debug("In School section");
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.SCHOOL_HEADING)) {
                    setSchoolPresent(true);
                    position++;
                    columnCount++;
                }
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // CLASS_HEADING is optional
        logger.debug("In Class section");
        currentHeading = lineSplit[position];
        if (currentHeading.equals("")) {
            position++;
        } else {
            if (columnHeadings.contains(currentHeading)) {
                if (currentHeading.equals(ImportConstants.CLASS_HEADING)) {
                    setClassPresent(true);
                    position++;
                    columnCount++;
                }
            } else { // there is a heading here we can't process
                errorCount++;
                result.add(getColumnCheckErrorMsg(lineNumber, currentHeading));
            }
        }

        // CUSTOM_FIELD is optional
        currentHeading = lineSplit[position];
        logger.debug("In CF section");
        if (currentHeading.equals("")) {
            position++;
        } else if (lineSplit[position].startsWith(ImportConstants.CUSTOM_FIELD_HEADING)) {
                String temp = getCustomFieldName(lineSplit[position]);
                if (temp != null && temp != "") {
                    result.add(temp);
                }
                position++;
                maxCustomFieldCount++;
                columnCount++;
                setCfPresent(true);
                while (position < lineSplitLength) {
                    if (lineSplit[position].startsWith(ImportConstants.CUSTOM_FIELD_HEADING)) {
                        temp = getCustomFieldName(lineSplit[position]);
                        if (temp != null && temp != "") {
                            result.add(temp);
                        }
                        position++;
                        columnCount++;
                        maxCustomFieldCount++;
                    } else { // there is a discrepancy with the column heading format - report it.
                        String errorDesc = "Line " + lineNumber + ": There is a problem with a"
                            + " Custom Field heading near \'" + lineSplit[position] + "\'.\n\t"
                            + "Perhaps the column heading is not in the correct format?\n";
                        result.add(errorDesc);
                        position++;
                        errorCount++;
                    }
               } // end while
            } else {
                errorCount++;
                result.add("Line " + lineNumber + ": Was not able to read the Custom Field heading "
                    + "'" + lineSplit[position] + "'"
                    + " Please verify the formatting of the column heading.");
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            logger.info("checkColumnHeadings - reached the end of the column row.");
        }

        // If 'Problem Start Time' present, ignore 'Problem View'.
        if (problemViewPresent && problemStartTimePresent) {
            warningCount++;
            logger.warn("Problem Start Time specified so Problem View is ignored.");
        }

        return result;
    } // end checkColumnHeadings

    /**
     * Helper method to generate messages for errors encountered during column heading
     * processing.
     * @param lineNumber the line number in the file (usually one).
     * @param currentHeading the heading we are currently processing.
     * @return a nicely formatted error message.
     */
    private String getColumnCheckErrorMsg(int lineNumber, String currentHeading) {
        StringBuffer msg = new StringBuffer();
        msg.append("Line " + lineNumber);
        msg.append(": Did not recognize '");
        msg.append(currentHeading);
        msg.append("'- please check the heading for accuracy.\n");
        logger.debug(msg);
        return msg.toString();
    }

    /**
     * Check the body of the dataset line-by-line for accuracy.
     * This method will not opt out after finding the first discrepancy (it keeps going to identify
     * additional errors (if any)).  If the line is in the correct format, return TRUE.
     * Since not all columns are required, check option column flags to see if that column is
     * present.
     * @param line - String representing a line from the dataset file
     * @param result - the LineProcessingResult object to populate
     * @return String result - contains any error messages generated.  If the dataset
     *      id OK, then "OK" is returned.
     */
    private LineProcessingResult checkDatasetLine(String line, LineProcessingResult result) {
        result.setLineNumber(lineNumber);
        result.getErrors().clear();
        result.getWarnings().clear();
        result.getInfo().clear();
        if (line.length() == 0  || line.split(DEFAULT_DELIMITER).length == 0) {
            lineNumber++;
            result.addToInfo("Skipping line " + lineNumber + ": it is blank.");
            return result;
        } else {
            try {
                String[] lineSplit = line.split(DEFAULT_DELIMITER);
                int position = 0;
                lineNumber++;
                //check the line for required values
                // Sample
                if (rowPresent) {
                    position++;
                }
                // Sample
                if (samplePresent) {
                    if (sampleName == null && lineSplit[position] != null) {
                        sampleName = lineSplit[position];
                    } else {
                        if (lineSplit[position] != null
                                && !lineSplit[position].equals(sampleName) && !sampleWarningSet) {
                            result.addToWarnings("Multiple samples have been found in this file."
                                    + "  DataShop expects a single sample per file.");
                            warningCount++;
                            sampleWarningSet = true;
                        }
                    }
                    position++;
                }
                // Anon Student Id
                if (lineSplit[position].equals(EMPTY_STRING)) {
                    result.addToErrors("Line " + lineNumber + ", Column '"
                            + ImportConstants.STUDENT_HEADING + "': "
                            + ImportConstants.STUDENT_HEADING
                            + " value not found.");
                    errorCount++;
                }
                position++;
                // Session Id
                if (lineSplit[position].equals(EMPTY_STRING)) {
                    result.addToErrors("Line " + lineNumber + ", Column '"
                        + ImportConstants.SESSION_HEADING + "': "
                        + ImportConstants.SESSION_HEADING + " value not found.");
                    errorCount++;
                }
                position++;
                // Time
                String timeString = null;
                boolean emptyTime = false;
                if (lineSplit[position].equals(EMPTY_STRING)) {
                    result.addToErrors("Line " + lineNumber + ", Column '"
                        + ImportConstants.TIME_HEADING + "': "
                        + ImportConstants.TIME_HEADING + " value not found.");
                    errorCount++;
                    emptyTime = true;
                } else {
                    timeString = lineSplit[position];
                }
                position++;
                // Time Zone
                String timeZone = null;
                if (timeZonePresent) {
                    timeZone = lineSplit[position];
                    if (timeZone.equals("(null)") || timeZone.equals(EMPTY_STRING)) {
                        timeZone = null;
                    }
                    if (!emptyTime) {
                        boolean lineDate = DateTools.checkDate(timeString, timeZone);
                        if (!lineDate) {
                            result.addToErrors("Line " + lineNumber + ", Column '"
                                + ImportConstants.TIME_HEADING + "': Please verify the Time"
                                + " (and Time Zone if included)"
                                + " are in a valid format.");
                            errorCount++;
                        }
                    }
                    position++;
                } else if (!emptyTime) {
                    boolean timeCheck = DateTools.checkDate(timeString, null);
                    if (!timeCheck) {
                        result.addToErrors("Line " + lineNumber + ", Column '"
                                + ImportConstants.TIME_HEADING + "': Please verify the Time"
                                + " (and Time Zone if included)"
                                + " are in a valid format.");
                            errorCount++;
                    }
                }
                // Duration - we don't care since this is calculated by the extractors.
                if (durationPresent) {
                    position++;
                }
                // Student Response Type
                if (studentResponseTypePresent) {
                    position++;
                }
                // Student Response Subtype
                if (studentResponseSubtypePresent) {
                    position++;
                }
                // Tutor Response Type
                if (tutorResponseTypePresent) {
                    position++;
                }
                // Tutor Response Subtype
                if (tutorResponseSubtypePresent) {
                    position++;
                }
                // Dataset Level
                for (int i = 0; i < maxDatasetLevelCount; i++) {
                    if (lineSplit[position].equals(EMPTY_STRING)) {
                        result.addToErrors("Line " + lineNumber + ", Column '"
                            + ImportConstants.DATASET_LEVEL_HEADING + "': "
                            + ImportConstants.DATASET_LEVEL_HEADING
                                           + " value not found.");
                        errorCount++;
                    }
                    position++;
                }
                // Problem Name
                if (lineSplit[position].equals(EMPTY_STRING)) {
                    result.addToErrors("Line " + lineNumber + ", Column '"
                        + ImportConstants.PROBLEM_NAME_HEADING + "': "
                        + ImportConstants.PROBLEM_NAME_HEADING
                        + " value not found.");
                    errorCount++;
                }
                position++;
                // Problem View
                if (problemViewPresent) {
                    if (position < lineSplit.length) {
                        String value = lineSplit[position];
                        // If 'Problem Start Time' present, ignore 'Problem View'.
                        if (!problemStartTimePresent) {
                            if (value.equals(EMPTY_STRING)) {
                                result.addToErrors("Line " + lineNumber + ", Column "
                                                   + ImportConstants.PROBLEM_VIEW_HEADING
                                                   + " is empty.");
                                errorCount++;
                            } else {
                                try {
                                    Integer.parseInt(value);
                                } catch (NumberFormatException nfe) {
                                    result.addToErrors("Line " + lineNumber + ", Column "
                                                       + ImportConstants.PROBLEM_VIEW_HEADING
                                                       + " value is not a number (" + value + ").");
                                    errorCount++;
                                }
                            }
                        }
                        position++;
                    }
                }
                // Problem Start Time
                if (problemStartTimePresent) {
                    if (position < lineSplit.length) {
                        String value = lineSplit[position];
                        if (value.equals(EMPTY_STRING)) {
                            result.addToErrors("Line " + lineNumber + ", Column "
                                    + ImportConstants.PROBLEM_START_TIME_HEADING
                                    + " is empty.");
                            errorCount++;
                        } else {
                            if (!DateTools.checkDate(value, null)) {
                                result.addToErrors("Line " + lineNumber + ", Column "
                                        + ImportConstants.PROBLEM_START_TIME_HEADING
                                        + " has an invalid format.");
                                errorCount++;
                            }
                        }
                        position++;
                    }
                }
                // Step Name
                if (stepNamePresent) {
                    position++;
                }
                // Attempt at Step
                if (attemptAtStepPresent) {
                    position++;
                }
                // Outcome
                if (outcomePresent) {
                    position++;
                }
                // Selection
                for (int i = 0; i < maxSelectionCount; i++) {
                    position++;
                }
                // Action
                for (int i = 0; i < maxActionCount; i++) {
                    position++;
                }
                // Input
                for (int i = 0; i < maxInputCount; i++) {
                    position++;
                }
                // Feedback Text
                if (feedbackTextPresent) {
                    position++;
                }
                // Feedback Classification
                if (feedbackClassificationPresent) {
                    position++;
                }
                // Help Level - if present, check to make sure it is an integer.
                if (helpLevelPresent) {
                    if (position < lineSplit.length) {
                        String helpLevel = lineSplit[position];
                        if (!helpLevel.equals(EMPTY_STRING)) {
                            try {
                                Integer.parseInt(lineSplit[position]);
                            } catch (NumberFormatException nfe) {
                                result.addToErrors("Line " + lineNumber + ", Column '"
                                    + ImportConstants.HELP_LEVEL_HEADING + "': "
                                    + ImportConstants.HELP_LEVEL_HEADING
                                    + " value is not an integer.");
                                errorCount++;
                            }
                        }
                        position++;
                    }
                }
                // Total Number of Hints - if present, check to make sure it is an integer.
                if (totalNumHintsPresent) {
                    if (position < lineSplit.length) {
                        String totalNumHints = lineSplit[position];
                        if (!totalNumHints.equals(EMPTY_STRING)) {
                            try {
                                Integer.parseInt(lineSplit[position]);
                            } catch (NumberFormatException nfe) {
                                result.addToErrors("Line " + lineNumber + ", Column '"
                                   + ImportConstants.TOTAL_HINTS_HEADING + "': "
                                   + ImportConstants.TOTAL_HINTS_HEADING
                                   + " value is not an integer.");
                                errorCount++;
                            }
                        }
                        position++;
                    }
                }
                // Condition Name & Type
                if (conditionPresent) {
                    for (int i = 0; i < maxConditionCount; i++) {
                        position += 2;
                    }
                }
                // KC Area & Category
                if (kcPresent && kcCategoryPresent) {
                    for (int i = 0; i < maxSkillCount; i++) {
                        position += 2;
                    }
                } else if (kcPresent) {
                    for (int i = 0; i < maxSkillCount; i++) {
                        position++;
                    }
                }
                // School
                if (schoolPresent) {
                    position++;
                }
                // Class
                if (classPresent) {
                    position++;
                }
                // Custom Field(s) - if present make sure they are less than
                // MAX_CUSTOM_FIELD_LENGTH
                if (cfPresent) {
                    for (int i = 0; i < maxCustomFieldCount; i++) {
                        if (position < lineSplit.length) {
                            String cfValue = lineSplit[position];
                            if (cfValue == null
                                  || cfValue.equals(EMPTY_STRING)
                                  || (cfValue.length() < ImportConstants.MAX_CUSTOM_FIELD_LENGTH)) {
                                position++;
                            } else {
                                result.addToWarnings("Line " + lineNumber + ", Column '"
                                        + ImportConstants.CUSTOM_FIELD_HEADING
                                        + customFieldNames.get(i) + "):"
                                        + " length of value is greater than "
                                        + ImportConstants.MAX_CUSTOM_FIELD_LENGTH
                                        + " and will be truncated by the Import Tool.");
                                position++;
                                warningCount++;
                            }
                        } else {
                            break;
                        }
                    }
                }
                return result;
            } catch (ArrayIndexOutOfBoundsException e) {
                lineNumber++;
                logger.error("Unexpectedly reached the end of line " + lineNumber + ". [NPE]");
                return result;
            }
        }
    }

    /**
     * Process the content of the given file.
     * @param inputFileName the name of the file to process
     * @return message indicating if processing was successful
     * @throws UnsupportedEncodingException thrown by handleInputFile method
     * @throws FileNotFoundException thrown by handleInputFile method
     */
    public String process(String inputFileName)
            throws FileNotFoundException, UnsupportedEncodingException {
        BufferedReader inputReader = ImportUtilities.handleInputFile(inputFileName);
        populateColumnHeadings();
        String result = new String();
        ArrayList resultList = new ArrayList();

        // check the first row for correct column headings
        try {
            String line = new String();
            Boolean keepReading = true;
            logger.info("Looking for the column header row ...");
            // eat any whitespace at the top of the file.
            while (keepReading) {
                line = inputReader.readLine();
                if (line.length() != 0) {
                    if (line.startsWith(ImportConstants.ROW_HEADING)
                            || line.startsWith(ImportConstants.SAMPLE_HEADING)
                            || line.startsWith(ImportConstants.STUDENT_HEADING)) {
                        keepReading = false;
                    }
                }
            }
            resultList = checkColumnHeadings(line);
            if (resultList.size() != 0) {
                logger.error(errorCount + " problem(s) have been found");
                for (Iterator it = resultList.iterator(); it.hasNext();) {
                    logger.error((String) it.next());
                }
                logger.info("DatasetVerificationTool done.");
                System.exit(-1);
            } else {
                logger.info("Column Heading OK!  Processed " + columnCount + " columns.");
                logger.info("Processing transactions ...");
                errorCount = 0;
                LineProcessingResult lineResult  = new LineProcessingResult();
                while (line != null) {
                    line = inputReader.readLine();
                    if (line == null) {
                        break;
                    } else {
                        lineResult = checkDatasetLine(line, lineResult);
                        Set errors = lineResult.getErrors();
                        if (errors.size() != 0) {
                            for (Iterator it = errors.iterator(); it.hasNext();) {
                                logger.error((String) it.next());
                            }
                        }
                        Set warnings = lineResult.getWarnings();
                        if (warnings.size() != 0) {
                            for (Iterator it = warnings.iterator(); it.hasNext();) {
                                logger.warn((String) it.next());
                            }
                        }
                        Set info = lineResult.getInfo();
                        if (info.size() != 0) {
                            for (Iterator it = info.iterator(); it.hasNext();) {
                                logger.info((String) it.next());
                            }
                        }
                    }
                }
            }
            if (errorCount == 0) {
                logger.info(lineNumber + " lines processed.");
                if (warningCount != 0) {
                    logger.warn("Warnings found: " + warningCount);
                }
                logger.info("Success! DatasetVerificationTool completed with no errors.");
            } else {
                logger.error("Errors found: " + errorCount);
                if (warningCount != 0) {
                    logger.warn("Warnings found: " + warningCount);
                }
                result = "ERROR";
            }
            inputReader.close();
        } catch (IOException exception) {
            logger.error("IOException: " + exception.getMessage());
            exception.printStackTrace();
        }
        return result;
    }

    /**
     * Handle the command line arguments.
     * @param args command line arguments passed into main
     */
    protected void handleOptions(String[] args) {
        if (args.length == 0 || args == null) {
            displayUsage();
            return;
        }

        java.util.ArrayList argsList = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")
             || args[i].equals("-help")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-v")
                    || args[i].equals("-version")) {
                logger.info(VersionInformation.getReleaseString());
                System.exit(0);
            } else if (args[i].equals("-f")
                    || args[i].equals("-filename")) {
                if (++i < args.length) {
                    inputFileName = args[i];
                } else {
                    logger.error("A file name must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            }
        } // end for loop

        // check for the required arguments
        if (inputFileName == null) {
            logger.error("A filename is required.");
            displayUsage();
            System.exit(1);
        }
    } // end handleOptions

    /**
     * Display the usage of this utility.
     */
    public void displayUsage() {
       logger.info("\nUSAGE: java -classpath ..."
                + TOOL_NAME + "  [-filename input_file_name] [-help] [-version]");
       logger.info("Options:");
       logger.info("\t-f, -filename  \t Verify the given tab-delimited file");
       logger.info("\t-h, -help      \t Display this help and exit");
       logger.info("\t-v, -version   \t Display the version and exit");
       logger.info("Note:");
       logger.info("\tThe log output file is: " + LOG_FILE_NAME);
    }

    /**
     * Run the Dataset Verification Tool.
     * USAGE: java -filename input_file_name
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("DatasetVerificationTool.main");
        DatasetVerificationTool dvt = new DatasetVerificationTool();
        String version = VersionInformation.getReleaseString();
        logger.info("DatasetVerificationTool starting (" + version + ")...");
        try {
            // Handle the command line options
            dvt.handleOptions(args);

            logger.info("Examining " + inputFileName + " for valid structure and content.");
            dvt.process(inputFileName);

        } catch (FileNotFoundException e) {
            logger.error("The specified file name was not found.  "
                    + "Please check your input parameter for accuracy.");

        } catch (IOException e) {
            logger.error("A problem occurred while attempting to open the input file for reading.");
        }
    } // end main()

    /**----------------------------------------
     * Getters and Setters
     **--------------------------------------*/

    /**
     * Get the total number of lines processed.
     * @return the number of lines processed.
     */
    public int getLinesProcessed() {
        return this.lineNumber;
    }

    /**
     * Get AttemptAtStepPresent.
     * @return the attemptAtStepPresent
     */
    public boolean isAttemptAtStepPresent() {
        return attemptAtStepPresent;
    }

    /**
     * Set AttemptAtStepPresent.
     * @param present the attemptAtStepPresent to set
     */
    public void setAttemptAtStepPresent(boolean present) {
        attemptAtStepPresent = present;
    }

    /**
     * Get CfPresent.
     * @return the cfPresent
     */
    public boolean isCfPresent() {
        return cfPresent;
    }

    /**
     * Set CfPresent.
     * @param present the cfPresent to set
     */
    public void setCfPresent(boolean present) {
        cfPresent = present;
    }

    /**
     * Get classPresent.
     * @return the classPresent
     */
    public boolean isClassPresent() {
        return classPresent;
    }

    /**
     * Set classPresent.
     * @param present the classPresent to set
     */
    public void setClassPresent(boolean present) {
        classPresent = present;
    }

    /**
     * Get the column count.
     * @return the columnCount
     */
    public Integer getColumnCount() {
        return columnCount;
    }

    /**
     * Set the column count.
     * @param columnCount the columnCount to set
     */
    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * Get conditionPresent.
     * @return the conditionPresent
     */
    public boolean isConditionPresent() {
        return conditionPresent;
    }

    /**
     * Set conditionPresent.
     * @param present the conditionPresent to set
     */
    public void setConditionPresent(boolean present) {
        conditionPresent = present;
    }

    /**
     * Get feedbackClassificationPresent.
     * @return the feedbackClassificationPresent
     */
    public boolean isFeedbackClassificationPresent() {
        return feedbackClassificationPresent;
    }

    /**
     * Set feedbackClassificationPresent.
     * @param present the feedbackClassificationPresent to set
     */
    public void setFeedbackClassificationPresent(boolean present) {
        feedbackClassificationPresent = present;
    }

    /**
     * Get feedbackTextPresent.
     * @return the feedbackTextPresent
     */
    public boolean isFeedbackTextPresent() {
        return feedbackTextPresent;
    }

    /**
     * set feedbackTextPresent.
     * @param present the feedbackTextPresent to set
     */
    public void setFeedbackTextPresent(boolean present) {
        feedbackTextPresent = present;
    }

    /**
     * Get helpLevelPresent.
     * @return the helpLevelPresent
     */
    public boolean isHelpLevelPresent() {
        return helpLevelPresent;
    }

    /**
     * Set helpLevelPresent.
     * @param present the helpLevelPresent to set
     */
    public void setHelpLevelPresent(boolean present) {
        helpLevelPresent = present;
    }

    /**
     * get KCPresent.
     * @return the kcPresent
     */
    public boolean isKcPresent() {
        return kcPresent;
    }

    /**
     * get KCCategoryPresent.
     * @return the kcCategoryPresent
     */
    public boolean isKcCategoryPresent() {
        return kcCategoryPresent;
    }

    /**
     * Set kcPresent.
     * @param present the kcPresent to set
     */
    public void setKcPresent(boolean present) {
        kcPresent = present;
    }

    /**
     * Set kcCategoryPresent.
     * @param present the kcPresent to set
     */
    public void setKcCategoryPresent(boolean present) {
        kcCategoryPresent = present;
    }

    /**
     * Get the maxActionCount.
     * @return the maxActionCount
     */
    public Integer getMaxActionCount() {
        return maxActionCount;
    }

    /**
     * Set the maxActionCount.
     * @param maxActionCount the maxActionCount to set
     */
    public void setMaxActionCount(Integer maxActionCount) {
        this.maxActionCount = maxActionCount;
    }

    /**
     * Get the maxConditionCount.
     * @return the maxConditionCount
     */
    public Integer getMaxConditionCount() {
        return maxConditionCount;
    }

    /**
     * Set the maxConditionCount.
     * @param maxConditionCount the maxConditionCount to set
     */
    public void setMaxConditionCount(Integer maxConditionCount) {
        this.maxConditionCount = maxConditionCount;
    }

    /**
     * Get the maxCustomFieldCount.
     * @return the maxCustomFieldCount
     */
    public Integer getMaxCustomFieldCount() {
        return maxCustomFieldCount;
    }

    /**
     * Set the maxCustomFieldCount.
     * @param maxCustomFieldCount the maxCustomFieldCount to set
     */
    public void setMaxCustomFieldCount(Integer maxCustomFieldCount) {
        this.maxCustomFieldCount = maxCustomFieldCount;
    }

    /**
     * Get the maxDatasetLevelCount.
     * @return the maxDatasetLevelCount
     */
    public Integer getMaxDatasetLevelCount() {
        return maxDatasetLevelCount;
    }

    /**
     * Set the maxDatasetLevelCount.
     * @param maxDatasetLevelCount the maxDatasetLevelCount to set
     */
    public void setMaxDatasetLevelCount(Integer maxDatasetLevelCount) {
        this.maxDatasetLevelCount = maxDatasetLevelCount;
    }

    /**
     * Get the maxInputCount.
     * @return the maxInputCount
     */
    public Integer getMaxInputCount() {
        return maxInputCount;
    }

    /**
     * Set the maxInputCount.
     * @param maxInputCount the maxInputCount to set
     */
    public void setMaxInputCount(Integer maxInputCount) {
        this.maxInputCount = maxInputCount;
    }

    /**
     * Get the maxSelectionCount.
     * @return the maxSelectionCount
     */
    public Integer getMaxSelectionCount() {
        return maxSelectionCount;
    }

    /**
     * Set the maxSelectionCount.
     * @param maxSelectionCount the maxSelectionCount to set
     */
    public void setMaxSelectionCount(Integer maxSelectionCount) {
        this.maxSelectionCount = maxSelectionCount;
    }

    /**
     * Get the maxSkillCount.
     * @return the maxSkillCount
     */
    public Integer getMaxSkillCount() {
        return maxSkillCount;
    }

    /**
     * Set the maxSkillCount.
     * @param maxSkillCount the maxSkillCount to set
     */
    public void setMaxSkillCount(Integer maxSkillCount) {
        this.maxSkillCount = maxSkillCount;
    }

    /**
     * Get the maxStudentCount.
     * @return the maxStudentCount
     */
    public Integer getMaxStudentCount() {
        return maxStudentCount;
    }

    /**
     * Set the maxStudentCount.
     * @param maxStudentCount the maxStudentCount to set
     */
    public void setMaxStudentCount(Integer maxStudentCount) {
        this.maxStudentCount = maxStudentCount;
    }

    /**
     * Get schoolPresent.
     * @return the schoolPresent
     */
    public boolean isSchoolPresent() {
        return schoolPresent;
    }

    /**
     * Set schoolPresent.
     * @param present the schoolPresent to set
     */
    public void setSchoolPresent(boolean present) {
        schoolPresent = present;
    }

    /**
     * Get studentResponseSubtypePresent.
     * @return the studentResponseSubtypePresent
     */
    public boolean isStudentResponseSubtypePresent() {
        return studentResponseSubtypePresent;
    }

    /**
     * Set studentResponseSubtypePresent.
     * @param present the studentResponseSubtypePresent to set
     */
    public void setStudentResponseSubtypePresent(boolean present) {
        studentResponseSubtypePresent = present;
    }

    /**
     * Get timeZonePresent.
     * @return the timeZonePresent
     */
    public boolean isTimeZonePresent() {
        return timeZonePresent;
    }

    /**
     * Set timeZonePresent.
     * @param present the timeZonePresent to set
     */
    public void setTimeZonePresent(boolean present) {
        timeZonePresent = present;
    }

    /**
     * Get durationPresent.
     * @return the durationPresent.
     */
    public boolean isDurationPresent() {
        return durationPresent;
    }

    /**
     * Set durationPresent.
     * @param present the durationPresent value to set.
     */
    public void setDurationPresent(boolean present) {
        durationPresent = present;
    }

    /**
     * Get totalNumHintsPresent.
     * @return the totalNumHintsPresent
     */
    public boolean isTotalNumHintsPresent() {
        return totalNumHintsPresent;
    }

    /**
     * Set totalNumHintsPresent.
     * @param present the totalNumHintsPresent to set
     */
    public void setTotalNumHintsPresent(boolean present) {
        totalNumHintsPresent = present;
    }

    /**
     *Get tutorResponseSubtypePresent.
     * @return the tutorResponseSubtypePresent
     */
    public boolean isTutorResponseSubtypePresent() {
        return tutorResponseSubtypePresent;
    }

    /**
     * Set tutorResponseSubtypePresent.
     * @param present the tutorResponseSubtypePresent to set
     */
    public void setTutorResponseSubtypePresent(boolean present) {
        tutorResponseSubtypePresent = present;
    }

    /**
     * Gets the set containing the custom field names.
     * @return customFieldNames
     */
    public ArrayList getCustomFieldNames() {
        return this.customFieldNames;
    }

    /**
     * Gets the set containing the dataset level titles.
     * @return the datasetLevelTitles
     */
    public ArrayList getDatasetLevelTitles() {
        return this.datasetLevelTitles;
    }

    /**
     * Gets the set containing the KC names.
     * @return the kcNames
     */
    public ArrayList getSkillModelNames() {
        return this.skillModelNames;
    }

    /**
     * Gets the set containing the KC Category names.
     * @return the kcCategory
     */
    public Set getKCCategoryNames() {
        return this.kcCategories;
    }

    /**
     * Get stepNamePresent.
     * @return value of stepNamePresent
     */
    public boolean isStepNamePresent() {
        return stepNamePresent;
    }

    /**
     * Set stepNamePresent.
     * @param present - if it is present or not
     */
    public void setStepNamePresent(boolean present) {
        stepNamePresent = present;
    }

    /**
     * Get tutorResponseTypeResent.
     * @return value of tutorResponseTypePresent
     */
    public boolean isTutorResponseTypePresent() {
        return tutorResponseTypePresent;
    }

    /**
     * Set tutorResponseTypePresent.
     * @param present - if it is present or not
     */
    public void setTutorResponseTypePresent(boolean present) {
        tutorResponseTypePresent = present;
    }

    /**
     * Get studentResponseTypePresent.
     * @return value of studentResponseTypePresent
     */
    public boolean isStudentResponseTypePresent() {
        return studentResponseTypePresent;
    }

    /**
     * Set studentResponseTypePresent.
     * @param present - if it is present or not
     */
    public void setStudentResponseTypePresent(boolean present) {
        studentResponseTypePresent = present;
    }

    /**
     * Get outcomePresent.
     * @return value of outcomePresent
     */
    public boolean isOutcomePresent() {
        return outcomePresent;
    }

    /**
     * Set outcomePresent.
     * @param present - if it is present or not
     */
    public void setOutcomePresent(boolean present) {
        outcomePresent = present;
    }

    /**
     * Get samplePresent.
     * @return value of studentResponseTypePresent
     */
    public boolean isSamplePresent() {
        return samplePresent;
    }

    /**
     * Set samplePresent.
     * @param present - if it is present or not
     */
    public void setSamplePresent(boolean present) {
        samplePresent = present;
    }
    /**
     * Get rowPresent.
     * @return true if row column is present, otherwise false
     */
    public boolean isRowPresent() {
        return rowPresent;
    }

    /**
     * Set rowPresent.
     * @param present indicates if the row column is present or not
     */
    public void setRowPresent(boolean present) {
        rowPresent = present;
    }
} // end class
