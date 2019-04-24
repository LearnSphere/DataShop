/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */

package edu.cmu.pslc.importdata;

import java.util.ArrayList;

/**
 * Utility class that holds constants used by the DatasetVerificationTool and the
 * DatasetImportTool classes.
 *
 * @author kcunning
 * @version $Revision: 10522 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-02-05 14:10:31 -0500 (Wed, 05 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
  */

public final class ImportConstants {

    /*****************************
     * Message Constants
     *****************************/
    /** Success Message Type. */
    public static final String SUCCESS = "SUCCESS";
    /** Error Message Type. */
    public static final String ERROR = "ERROR";
    /** Warning Message Type. */
    public static final String WARNING = "WARNING";
    /** Error Message Prefix. */
    public static final String MSG_PREFIX = " FlatFileImporter - ";
    /** Documentation Link. */
    public static final String MSG_DOCUMENT_LINK =
        "For documentation, see http://pslcdatashop.org/about/importverify.html ";
    /** Success Message Type. */
    public static final String STATUS_VERIFYING_HEADERS = "verifying headers";
    /** Success Message Type. */
    public static final String STATUS_LOADING = "loading";
    /** Success Message Type. */
    public static final String STATUS_LOADED = "loaded";
    /** Success Message Type. */
    public static final String STATUS_VERIFYING = "verifying";
    /** Success Message Type. */
    public static final String STATUS_PROCESSING = "processing";
    /** Success Message Type. */
    public static final String STATUS_MERGING = "merging";
    /** Success Message Type. */
    public static final String STATUS_IMPORTED = "imported";
    /** Success Message Type. */
    public static final String STATUS_NO_LONGER_EXISTS = "no longer exists";

    /*****************************
     * Column Headings Constants
     *****************************/
    /** Row column heading. */
    public static final String ROW_HEADING = "Row";
    /** Sample column heading. */
    public static final String SAMPLE_HEADING = "Sample Name";
    /** Student column heading. */
    public static final String STUDENT_HEADING = "Anon Student Id";
    /** GUID column heading. */
    public static final String GUID_HEADING = "Transaction Id";
    /** Session column heading. */
    public static final String SESSION_HEADING = "Session Id";
    /** Time column heading. */
    public static final String TIME_HEADING = "Time";
    /** Time Zone column heading. */
    public static final String TIME_ZONE_HEADING = "Time Zone";
    /** Transaction Duration column heading. */
    public static final String DURATION_HEADING = "Duration (sec)";
    /** Student Response Type column heading. */
    public static final String STUDENT_RESPONSE_TYPE_HEADING = "Student Response Type";
    /** Student Response Subtype column heading. */
    public static final String STUDENT_RESPONSE_SUBTYPE_HEADING = "Student Response Subtype";
    /** Tutor Response Type column heading. */
    public static final String TUTOR_RESPONSE_TYPE_HEADING = "Tutor Response Type";
    /** Tutor Response Subtype column heading. */
    public static final String TUTOR_RESPONSE_SUBTYPE_HEADING = "Tutor Response Subtype";
    /** Dataset Level column heading. */
    public static final String DATASET_LEVEL_HEADING = "Level";
    /** Problem Name column heading. */
    public static final String PROBLEM_NAME_HEADING = "Problem Name";
    /** Problem Name column heading. */
    public static final String PROBLEM_VIEW_HEADING = "Problem View";
    /** Problem Name column heading. */
    public static final String PROBLEM_START_TIME_HEADING = "Problem Start Time";
    /** Step Name column heading. */
    public static final String STEP_NAME_HEADING = "Step Name";
    /** Attempt At step column heading. */
    public static final String ATTEMPT_AT_STEP_HEADING = "Attempt At Step";
    /** Outcome column heading. */
    public static final String OUTCOME_HEADING = "Outcome";
    /** Selection column heading. */
    public static final String SELECTION_HEADING = "Selection";
    /** Action column heading. */
    public static final String ACTION_HEADING = "Action";
    /** Input column heading. */
    public static final String INPUT_HEADING = "Input";
    /** Feedback Text column heading. */
    public static final String FEEDBACK_TEXT_HEADING = "Feedback Text";
    /** Feedback Classification heading. */
    public static final String FEEDBACK_CLASSIFICATION_HEADING = "Feedback Classification";
    /** Help Level column heading. */
    public static final String HELP_LEVEL_HEADING = "Help Level";
    /** Total Number of Hints column heading. */
    public static final String TOTAL_HINTS_HEADING = "Total Num Hints";
    /** Condition Name column heading. */
    public static final String CONDITION_NAME_HEADING = "Condition Name";
    /** Condition Type column heading. */
    public static final String CONDITION_TYPE_HEADING = "Condition Type";
    /** Knowledge Component column heading. */
    public static final String KC_HEADING = "KC";
    /** Knowledge Component Category column heading. */
    public static final String KC_CATEGORY_HEADING = "KC Category";
    /** School column heading. */
    public static final String SCHOOL_HEADING = "School";
    /** Class column heading. */
    public static final String CLASS_HEADING = "Class";
    /** Custom Field column heading. */
    public static final String CUSTOM_FIELD_HEADING = "CF";
    /** Maximum custom field value length. */
    public static final int MAX_CUSTOM_FIELD_LENGTH = 255;
    /** Maximum level value length. */
    public static final int MAX_LEVEL_LENGTH = 100;
    /** Maximum KC value length. */
    public static final int MAX_KC_LENGTH = 50;
    /** Maximum KC category value length. */
    public static final int MAX_KC_CATEGORY_LENGTH = 50;

    /** Required headings.
     *  Level() is also required but the way this list
     *  is used we can't match on the variable text between
     *  parens so don't include DATASET_LEVEL_HEADING here.
     */
    public static final ArrayList<String> REQUIRED_HEADINGS =
        new ArrayList<String>() { { add(STUDENT_HEADING);
                                    add(SESSION_HEADING);
                                    add(TIME_HEADING);
                                    add(PROBLEM_NAME_HEADING);
                                    add(SELECTION_HEADING);
                                    add(ACTION_HEADING);
                                    add(INPUT_HEADING); } };
     /** Optional headings. */
     public static final ArrayList<String> OPTIONAL_HEADINGS =
        new ArrayList<String>() { { add(TIME_ZONE_HEADING);
                                    add(STUDENT_RESPONSE_TYPE_HEADING);
                                    add(STUDENT_RESPONSE_SUBTYPE_HEADING);
                                    add(TUTOR_RESPONSE_TYPE_HEADING);
                                    add(TUTOR_RESPONSE_SUBTYPE_HEADING);
                                    add(STEP_NAME_HEADING);
                                    add(OUTCOME_HEADING);
                                    add(PROBLEM_VIEW_HEADING);
                                    add(PROBLEM_START_TIME_HEADING);
                                    add(FEEDBACK_TEXT_HEADING);
                                    add(FEEDBACK_CLASSIFICATION_HEADING);
                                    add(HELP_LEVEL_HEADING);
                                    add(TOTAL_HINTS_HEADING);
                                    add(CONDITION_NAME_HEADING);
                                    add(CONDITION_TYPE_HEADING);
                                    add(KC_HEADING);
                                    add(KC_CATEGORY_HEADING);
                                    add(SCHOOL_HEADING);
                                    add(CLASS_HEADING);
                                    add(CUSTOM_FIELD_HEADING); } };

    /** Running Mode. */
    public static final String MODE_RUN_BY_FFI = "Run By FFI";
    /** Running Mode. */
    public static final String MODE_STANDALONE = "Standalone";

    /**
     * Private Constructor, as this is a utility class.
     */
    private ImportConstants() { };

} // end ImportConstants
