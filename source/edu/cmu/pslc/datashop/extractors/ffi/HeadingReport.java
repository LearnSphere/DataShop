/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.ffi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.sourcedb.dao.FlatFileImporterDao;
import edu.cmu.pslc.datashop.sourcedb.dao.SourceDbDaoFactory;
import edu.cmu.pslc.datashop.importdb.dao.ImportDbDaoFactory;
import edu.cmu.pslc.datashop.importdb.dao.ImportStatusDao;
import edu.cmu.pslc.datashop.importdb.item.ImportStatusItem;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.importdata.ImportConstants;
import edu.cmu.pslc.importdata.ReservedKeywordConstants;

/**
 * This class validates the column headings
 * given the first line of the first input file.
 *
 * @author Shanwen Yu
 * @version $Revision: 14036 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-04-07 16:37:14 -0400 (Fri, 07 Apr 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class HeadingReport {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Invalid Heading Message Prefix. */
    private static final String HEADING_ISSUE_PREFIX =
        "Heading issue: ";
    /** Invalid Heading Message Prefix. */
    private static final String MSG_PREFIX =
        "The headings are not valid: ";

    /** Invalid Heading Message. */
    private static final String EMPTY_HEADING_MSG =
        " empty heading(s) found.";
    /** Invalid Heading Message. */
    private static final String MISSING_REQUIRED_HEADING_MSG =
        "cannot find required heading ";
    /** Invalid Heading Message. */
    private static final String MISSING_PAIR_MSG =
        "cannot find paired heading(s) ";
    /** Invalid Heading Message. */
    private static final String DUPLICATE_HEADING_MSG =
        "duplicate heading(s) found: ";
    /** Invalid Heading Message. */
    private static final String EMPTY_CONTENT_MSG =
        "missing content between parentheses in heading ";
    /** Invalid Heading Message. */
    private static final String EXCEED_MAX_LENGTH_MSG =
        "content exceeds the maximum length for heading ";
    /** Invalid Heading Message. */
    private static final String INVALID_CHAR_MSG =
        "Invalid character(s) in heading ";
    /** String constant. */
    private static final String INVALID_CHARS_BEFORE =
        "content between parentheses for heading ";
    /** String constant. */
    private static final String INVALID_CHARS_AFTER  =
        " includes an invalid character. ";
    /** Valid Characters Message. */
    private static final String VALID_CHARS =
        "Valid characters for headings include space, dash, underscore, letters, and numbers. ";
    /** Invalid Heading Message. */
    private static final String INCORRECT_KC_ORDER_MSG =
        "corresponding columns must be next to each other: ";
    /** Reserved Keyword Heading Message. */
    private static final String RESERVED_KEYWORD_HEADING_MSG =
        "Reserved keyword: ";

    /** Warning Message.
     *  Note that if this message is changed here,
     *  the code in the ffi_drop_ignored_columns stored procedure of
     *  ffi_verify_data_sp SQL file needs to be changed accordingly. */
    private static final String IGNORED_HEADING_MSG =
        "The following heading(s) were ignored: ";

    /** Default Delimiter */
    private static final String DEFAULT_DELIMITER = "\\t";
    /** Empty String */
    private static final String EMPTY_STRING = "";
    /** Left Parenthesis */
    private static final String LEFT_PARENTHESES = "(";
    /** Right Parenthesis */
    private static final String RIGHT_PARENTHESES = ")";
    /** Underscore */
    private static final String UNDERSCORE = "_";
    /** Max number of characters of a heading content */
    private static final int MAX_CHAR_NUM = 30;
    /** Max number to be used in the round function for empty heading column name */
    private static final int MAX_RANDOM_NUM = 1000;
    /** Regular expression of all the not_allowed characters */
    private static final String REGEX_NOT_ALLOWED_CHARS = "[^\\sA-Za-z0-9_-]";

    /** Number of Selection columns */
    private int maxSelectionCount = 0;
    /** Number of Action Columns */
    private int maxActionCount = 0;
    /** Number of Input columns */
    private int maxInputCount = 0;
    /** Number of Conditions */
    private int maxConditionCount = 0;
    /** Number of Level columns */
    private int maxLevelCount = 0;
    /** Number of KC columns */
    private int maxKcCount = 0;
    /** Number of CustomField columns */
    private int maxCustomFieldCount = 0;

    /** Number of empty headings */
    private int  numOfEmptyHeadings = 0;
    /** List of headings */
    private ArrayList<String> arrHeadings = new ArrayList<String>();
    /** List of required headings that are missing */
    private ArrayList<String>  missingHeadings = new ArrayList<String>();
    /** List of headings that exceed max length */
    private ArrayList<String>  exceedMaxLengthHeadings = new ArrayList<String>();
    /** List of headings which content is empty */
    private ArrayList<String>  emptyContentHeadings = new ArrayList<String>();
    /** List of headings which have duplicates */
    private ArrayList<String>  duplicateHeadings = new ArrayList<String>();
    /** List of headings whose content contains invalid character(s) */
    private ArrayList<String>  invalidCharHeadings = new ArrayList<String>();
    /** List of headings that failed to be paired with the other heading */
    private ArrayList<String>  missingPairHeadings = new ArrayList<String>();
    /** List of headings that has wrong orders */
    private ArrayList<String>  incorrectOrderHeadings = new ArrayList<String>();
    /** List of headings that are to be ignored */
    private ArrayList<String>  ignoredHeadings = new ArrayList<String>();
    /** List of optional headings. Initialized with clone */
    private ArrayList<String> optionalHeadings =
        (ArrayList<String>) ImportConstants.OPTIONAL_HEADINGS.clone();
    /** List of reserved keyword headings. */
    private ArrayList<String> reservedKeywordHeadings = new ArrayList<String>();

    /** List of headings that starts with "Level" */
    private ArrayList<String>  levelHeadings = new ArrayList<String>();
    /** List of headings that starts with "KC(" */
    private ArrayList<String>  kcHeadings = new ArrayList<String>();
    /** List of headings that starts with "KC Category" */
    private ArrayList<String>  kcCategoryHeadings = new ArrayList<String>();

    /** Number of occurrence of Condition Name */
    private int  numOfConditionName = 0;
    /** Number of occurrence of Condition Type */
    private int  numOfConditionType = 0;
    /** Number of occurrence of Feedback Classification Name */
    private int  numOfFeedbackClassification = 0;
    /** Number of occurrence of Feedback Text */
    private int  numOfFeedbackText = 0;

    /** Flag indicating whether the headings are valid. */
    private boolean headingsValid = false;
    private Integer defaultKcColumnId = null;

    /** List of warnings for this HeadingReport object. */
    private List<String> myWarnings = new ArrayList<String>();

    /** Liset of errors for this HeadingReport object. */
    private List<String> myErrors = new ArrayList<String>();

    /**
     * Create a heading report object given the first line of an input file.
     * The object will contain all the necessary information about the headings.
     * Also, the warning and error messages will be saved in the database.
     * @param importStatusItem the high level status row in the database
     * @param headings the first line of the first input file with just the headings
     * @param intThreshold threshold for error checking
     * @param includeUserKCMs whether to include KCMs
     * @return a heading report object
     */
    public static HeadingReport create(ImportStatusItem importStatusItem,
            String headings, int intThreshold, Boolean includeUserKCMs) {
        includeUserKCMs = includeUserKCMs == null ? true : includeUserKCMs;
        HeadingReport report = new HeadingReport();
        report.checkHeadings(importStatusItem, headings, includeUserKCMs);
        return report;
    }

    /**
     * Create a heading report object given the first line of an input file.
     * The object will contain all the necessary information about the headings.
     * The warning and error messages will not be written to the database.
     * @param headings the first line of the first input file with just the headings
     * @return a heading report object
     */
    public static HeadingReport create(String headings) {
        HeadingReport report = new HeadingReport();
        report.checkHeadings(null, headings, false);
        return report;
    }

    /**
     * Constructor.
     */
    public HeadingReport() { };

    /**
     * Validate the string of headings,
     * save errors in the database,
     * sets the report's arrHeadings field, and
     * sets the report's headingsValid flag.
     * @param importStatusItem the high level status row in the database
     * @param headings the first line of the first file which is just the headings
     * @param includeUserKCMs whether to include KCMs
     */
    private void checkHeadings(ImportStatusItem importStatusItem, String headings,
                               Boolean includeUserKCMs)
    {
        logDebug("checkHeadings(", headings, ")");
        boolean isHeadingValid = true;
        Pattern pattern = Pattern.compile(REGEX_NOT_ALLOWED_CHARS);
        Matcher matcher = null;
        // convert heading to an ArrayList
        Collections.addAll(arrHeadings, headings.split(DEFAULT_DELIMITER));

        arrHeadings = standardizeHeadings(arrHeadings, includeUserKCMs);

        String currentHeading = "";
        String previousHeading = "";
        String currentHeadingWithoutParentheses = "";
        // loop through all the heading
        for (int i = 0; i < arrHeadings.size(); i++) {

            currentHeading = arrHeadings.get(i).toString();
            // check for invalid characters
            if (((!currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING))
              && (!currentHeading.startsWith(ImportConstants.KC_HEADING))
              && (!currentHeading.startsWith(ImportConstants.KC_CATEGORY_HEADING))
              && (!currentHeading.startsWith(ImportConstants.CUSTOM_FIELD_HEADING)))) {
                currentHeadingWithoutParentheses =
                    currentHeading.replaceAll("\\(", "_").replaceAll("\\)", "_");
                matcher = pattern.matcher(currentHeadingWithoutParentheses);
                if (matcher.find()) {
                    invalidCharHeadings.add(MSG_PREFIX
                            + "heading " + currentHeading + INVALID_CHARS_AFTER);
                }
            }
            if (currentHeading.equals(EMPTY_STRING)) {
               // replace an empty heading with a custom name
               // this heading will be put into ignoreHeadings list
               // then being dropped from the ffi_import_file_data table
               // later in the stored procedure
               arrHeadings.set(i, "Empty Heading "
                       + Math.round(Math.random() * MAX_RANDOM_NUM));
               numOfEmptyHeadings++;
            } else {
                if (currentHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                    //add to the list so it can be removed from ignored heading list.
                    levelHeadings.add(currentHeading);
                } else if (currentHeading.startsWith(ImportConstants.KC_CATEGORY_HEADING)) {
                    //since KC category heading is dynamic,
                    //grab it and put it in the optionalHeadings list
                    //add it to KC category heading list for later verification

                    if (includeUserKCMs || currentHeading.matches(ImportConstants.KC_CATEGORY_HEADING + DEFAULT_KC_STRING)) {
                        optionalHeadings.add(currentHeading);
                        kcCategoryHeadings.add(currentHeading);
                    }

                    //check for order with KC heading
                    if (i > 1
                            && (includeUserKCMs
                                || currentHeading.matches(ImportConstants.KC_CATEGORY_HEADING + DEFAULT_KC_STRING))) {
                        previousHeading = arrHeadings.get(i - 1);
                        // if the previous heading is not a KC,
                        // or the previous heading is a KC but the content does not equals to
                        // the content of the current KC category, add the current heading to list
                        if (((previousHeading.startsWith(ImportConstants.KC_HEADING))
                          && (!getContent(previousHeading).equals(getContent(currentHeading))))
                          || (!previousHeading.startsWith(ImportConstants.KC_HEADING))) {
                            incorrectOrderHeadings.add(ImportConstants.KC_HEADING
                                    + LEFT_PARENTHESES
                                    + getContent(currentHeading)
                                    + RIGHT_PARENTHESES
                                    + " and " + currentHeading);
                        }
                    }
                } else if (currentHeading.startsWith(ImportConstants.KC_HEADING)) {
                    //since KC heading is dynamic, grab it and put it in the optionalHeadings list
                    //add it to KC heading list for later verification
                    if (includeUserKCMs || currentHeading.matches(ImportConstants.KC_HEADING + DEFAULT_KC_STRING)) {
                        optionalHeadings.add(currentHeading);
                        kcHeadings.add(currentHeading);
                    }


                } else if (currentHeading.startsWith(ImportConstants.CUSTOM_FIELD_HEADING)) {
                    //since custom field heading is dynamic,
                    //grab it and put it in the optionalHeadings list
                    optionalHeadings.add(currentHeading);
                    validateCFHeading(currentHeading);
                } else if (currentHeading.equals(ImportConstants.FEEDBACK_CLASSIFICATION_HEADING)) {
                    numOfFeedbackClassification++;
                    //check for order
                    if (i > 1) {
                        previousHeading = arrHeadings.get(i - 1);
                        // if the previous heading is not Feedback Text,
                        // put the current heading to the list.
                        if (!previousHeading.equals(ImportConstants.FEEDBACK_TEXT_HEADING)) {
                            incorrectOrderHeadings.add(ImportConstants.FEEDBACK_TEXT_HEADING
                                    + " and " + currentHeading);
                        }
                    }
                } else if (currentHeading.equals(ImportConstants.FEEDBACK_TEXT_HEADING)) {
                    numOfFeedbackText++;
                } else if (currentHeading.equals(ImportConstants.CONDITION_TYPE_HEADING)) {
                    numOfConditionType++;
                    //check for order
                    if (i > 1) {
                        previousHeading = arrHeadings.get(i - 1);
                        // if the previous heading is not Condition Name,
                        // put the current heading to the list.
                        if (!previousHeading.equals(ImportConstants.CONDITION_NAME_HEADING)) {
                            incorrectOrderHeadings.add(ImportConstants.CONDITION_NAME_HEADING
                                    + " and " + currentHeading);
                        }
                    }
                } else if (currentHeading.equals(ImportConstants.CONDITION_NAME_HEADING)) {
                    numOfConditionName++;
                } else if (ReservedKeywordConstants.RESERVED_KEYWORDS.contains(currentHeading)) {
                    reservedKeywordHeadings.add(currentHeading);
                } // end if
            } // end if
        } // end of for loop

        // if there is empty heading found, issue warning
        if (numOfEmptyHeadings > 0)  {
            issueWarning(importStatusItem, numOfEmptyHeadings + EMPTY_HEADING_MSG);
        }
        // verify required headings. Put missing headings in the list
        missingHeadings = getMissingHeadings(arrHeadings);
        if (!missingHeadings.isEmpty()) {
            issueError(importStatusItem,
                MSG_PREFIX + MISSING_REQUIRED_HEADING_MSG, missingHeadings);
            isHeadingValid = false;
        }

        // check for duplicate headings
        duplicateHeadings = getDuplicateHeadings(arrHeadings);
        if (!duplicateHeadings.isEmpty()) {
            issueError(importStatusItem,
                    MSG_PREFIX + DUPLICATE_HEADING_MSG, duplicateHeadings);
            isHeadingValid = false;
        }
        //each condition type should be paired with a condition name
        if (numOfConditionType < numOfConditionName) {
            missingPairHeadings.add(ImportConstants.CONDITION_TYPE_HEADING);
            // if it is in the missing pair list, then remove it from the incorrect order list
            incorrectOrderHeadings.remove(ImportConstants.CONDITION_NAME_HEADING
                                    + " and " + ImportConstants.CONDITION_TYPE_HEADING);
        }
        //each condition type should be paired with a condition name
        if (numOfConditionType > numOfConditionName) {
            missingPairHeadings.add(ImportConstants.CONDITION_NAME_HEADING);
            incorrectOrderHeadings.remove(ImportConstants.CONDITION_NAME_HEADING
                                    + " and " + ImportConstants.CONDITION_TYPE_HEADING);
        }
        //each feedback classification should be paired with a feedback text
        if (numOfFeedbackClassification > numOfFeedbackText) {
            missingPairHeadings.add(ImportConstants.FEEDBACK_TEXT_HEADING);
        }

        validateLevelHeading(importStatusItem, levelHeadings);
        validateKCAndKCCateogryHeading(importStatusItem, kcHeadings, kcCategoryHeadings);

        if (!missingPairHeadings.isEmpty()) {
            issueError(importStatusItem,
                    MSG_PREFIX + MISSING_PAIR_MSG, missingPairHeadings);
            isHeadingValid = false;
        }

        if (!exceedMaxLengthHeadings.isEmpty()) {
            issueError(importStatusItem,
                    EXCEED_MAX_LENGTH_MSG, exceedMaxLengthHeadings);
            isHeadingValid = false;
        }

        if (!emptyContentHeadings.isEmpty()) {
            issueError(importStatusItem,
                    MSG_PREFIX + EMPTY_CONTENT_MSG, emptyContentHeadings);
            isHeadingValid = false;
        }

        if (!invalidCharHeadings.isEmpty()) {
            int lastIdx = invalidCharHeadings.size() - 1;
            String lastMsg = invalidCharHeadings.get(lastIdx);
            lastMsg += "\n" + VALID_CHARS;
            invalidCharHeadings.remove(lastIdx);
            invalidCharHeadings.add(lastMsg);
            issueError(importStatusItem,
                    INVALID_CHAR_MSG, invalidCharHeadings);
            isHeadingValid = false;
        }

        if (!incorrectOrderHeadings.isEmpty()) {
            issueError(importStatusItem,
                    MSG_PREFIX + INCORRECT_KC_ORDER_MSG, incorrectOrderHeadings);
            isHeadingValid = false;
        }

        if (!reservedKeywordHeadings.isEmpty()) {
            issueError(importStatusItem,
                       MSG_PREFIX + RESERVED_KEYWORD_HEADING_MSG, reservedKeywordHeadings);
            isHeadingValid = false;
        }

        // handle other headings (ignored)
        ignoredHeadings = getIgnoredHeadings(arrHeadings);
        // issue warning message for ignored headings
        if (!ignoredHeadings.isEmpty()) {
            issueWarning(importStatusItem,
                    IGNORED_HEADING_MSG + ignoredHeadings.toString());
        }

        //figure out if the headings are valid
        setHeadingsValid(isHeadingValid);

        logDebug("checkHeadings(): valid: ", headingsValid);
    }


    /** Constant of warning message. */
    private static final String MSG_TOO_MANY_WARNINGS = "Too many warnings have occurred.";

    /**
     * Save a single warning message in the database and report to command line.
     * @param importStatusItem the high level status row in the database
     * @param message message detail
     */
    private void issueWarning(ImportStatusItem importStatusItem, String message) {
        if (importStatusItem == null) {
            saveWarning(message);
        } else {
            ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
            if (importStatusDao.checkWarningMessageLength(importStatusItem, message)) {
                message = MSG_TOO_MANY_WARNINGS;
            }
            importStatusDao.saveWarningMessage(importStatusItem, message);
        }
        logWarn(message);
    }

    /**
     * Save the warning message in the database and report to command line.
     * @param importStatusItem the high level status row in the database
     * @param msgPrefix prefix of message
     * @param warningHeadings headings that need to be warned
     */
    private void issueWarning(ImportStatusItem importStatusItem, String msgPrefix,
                              ArrayList<String> warningHeadings) {
        String message = "";
        for (String myWarningHeading : warningHeadings) {
            message += msgPrefix + myWarningHeading;

            if (importStatusItem == null) {
                saveWarning(myWarningHeading);
                logWarn(myWarningHeading);
            }
        }

        if (importStatusItem == null) {
            return;
        }

        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();
        if (importStatusDao.checkWarningMessageLength(importStatusItem, message)) {
            message = MSG_TOO_MANY_WARNINGS;
            importStatusDao.saveWarningMessage(importStatusItem, message);
            logWarn(message);
        } else {
            for (String myWarningHeading : warningHeadings) {
                message = msgPrefix + myWarningHeading;
                importStatusDao.saveWarningMessage(importStatusItem, message);
                logWarn(message);
            } // end for loop
        }
    }

    /** Constant of error message. */
    private static final String MSG_TOO_MANY_ERRORS = "Too many errors have occurred.";
    /** Regular expression to match variations of '(Default)'. */
    private static final String DEFAULT_KC_STRING = "[ ]*\\([ ]*Default[ ]*\\)[ ]*";

    /**
     * Save the error message in the database and report to command line.
     * @param importStatusItem the high level status row in the database
     * @param msgPrefix message prefix
     * @param invalidHeadings invalid heading list
     */
    private void issueError(ImportStatusItem importStatusItem, String msgPrefix,
            ArrayList<String> invalidHeadings) {
        String message = "";
        // construct error message
        // for max length and invalid characters,
        // the list contains messages instead of headings
        if ((msgPrefix.equals(EXCEED_MAX_LENGTH_MSG))
            || (msgPrefix.equals(INVALID_CHAR_MSG))) {
            for (String myInvalidHeading : invalidHeadings) {
                message += myInvalidHeading;

                if (importStatusItem == null) {
                    saveError(myInvalidHeading);
                    logError(myInvalidHeading);
                }
            }
        } else {
            if (importStatusItem == null) {
                int count = invalidHeadings.size();
                StringBuffer sb = new StringBuffer(msgPrefix);
                for (String myInvalidHeading : invalidHeadings) {
                    sb.append(myInvalidHeading);
                    count--;
                    if (count > 0) { sb.append(", "); }
                }
                saveError(sb.toString());
                logError(sb.toString());
            } else {
                for (String myInvalidHeading : invalidHeadings) {
                    message += msgPrefix + myInvalidHeading;
                }
            }
        }

        if (importStatusItem == null) {
            return;
        }

        ImportStatusDao importStatusDao = ImportDbDaoFactory.DEFAULT.getImportStatusDao();

        // check total length of new message and existing message.
        // issue one error if total length exceeds max length,
        // otherwise, issue individual message.
        if (importStatusDao.checkErrorMessageLength(importStatusItem, message)) {
            message = MSG_TOO_MANY_ERRORS;
            importStatusDao.saveErrorMessage(importStatusItem, message);
            logError(message);
        } else {
            if ((msgPrefix.equals(EXCEED_MAX_LENGTH_MSG))
                    || (msgPrefix.equals(INVALID_CHAR_MSG))) {
               for (String myHeading : invalidHeadings) {
                   message = myHeading;
                   importStatusDao.saveErrorMessage(importStatusItem, message);
                   logError(message);
               } // end for loop
            } else {
                for (String myHeading : invalidHeadings) {
                    message = msgPrefix + myHeading;
                    importStatusDao.saveErrorMessage(importStatusItem, message);
                    logError(message);
                } // end for loop
            } // end if
           logError(ImportConstants.MSG_DOCUMENT_LINK);
        } // end if
    } // end issueError

    /**
     * Get a list of standard headings given a list of headings.
     * Things that have been done here:
     * 1. strip extra double quotes added by java
     * For example, Level("A") becomes "Level(""A"")" in the arrHeading
     * and is reversed here
     * 2. remove extra white space
     * 3. construct the new heading according to the standard names
     * @param headings headings to standardize
     * @param includeUserKCMs whether to include KCMs
     * @return a list of standard headings
     */
    private ArrayList<String> standardizeHeadings(ArrayList<String> headings, Boolean includeUserKCMs) {

        ArrayList<String> newHeadings = new ArrayList<String>();
        // build standard headings
        ArrayList<String> standardHeadings = new ArrayList<String>();
        // add all required and optional headings to the list
        standardHeadings.addAll(ImportConstants.REQUIRED_HEADINGS);
        standardHeadings.addAll(ImportConstants.OPTIONAL_HEADINGS);
        standardHeadings.add(ImportConstants.ROW_HEADING);
        standardHeadings.add(ImportConstants.DURATION_HEADING);
        standardHeadings.add(ImportConstants.ATTEMPT_AT_STEP_HEADING);

        // remove those that check for prefix, we do it separately
        standardHeadings.remove(ImportConstants.KC_HEADING);
        standardHeadings.remove(ImportConstants.KC_CATEGORY_HEADING);
        standardHeadings.remove(ImportConstants.DATASET_LEVEL_HEADING);
        standardHeadings.remove(ImportConstants.CUSTOM_FIELD_HEADING);

        // build a map to store the original heading
        // and the heading that has been converted to lower case and removed all white spaces.
        Map<String, String> headingMap =
            new HashMap<String, String>();

        ArrayList<String> tempHeadings = new ArrayList<String>();

        for (String myHeading : headings) {
            myHeading = stripExtraQuotes(myHeading.trim());
            headingMap.put(myHeading.toLowerCase().replaceAll("\\s", ""), myHeading);
            tempHeadings.add(myHeading.toLowerCase().replaceAll("\\s", ""));
        }

        // build a map to store the original standard heading
        // and the heading that has been converted to lower case and removed all white spaces.
        Map<String, String> standardHeadingMap =
            new HashMap<String, String>();

        ArrayList<String> tempStandardHeadings = new ArrayList<String>();

        for (String myStandardHeading : standardHeadings) {
            standardHeadingMap.put(myStandardHeading.toLowerCase().replaceAll("\\s", ""),
                    myStandardHeading);
            tempStandardHeadings.add(myStandardHeading.toLowerCase().replaceAll("\\s", ""));
        }

        // length of the standard heading prefix
        int len = 0;

        String myNewHeading = null;
        // loop through all the headings to check if they match the standard
        for (String myTempHeading : tempHeadings) {
            // if the heading is one of the standard headings, then add the standard version
            if (tempStandardHeadings.contains(myTempHeading)) {

                newHeadings.add(standardHeadingMap.get(myTempHeading).toString());

            } else {
                // check for prefix headings
                if (myTempHeading
                        .startsWith(ImportConstants.KC_CATEGORY_HEADING
                                .toLowerCase().replaceAll("\\s", ""))) {
                    len = ImportConstants.KC_CATEGORY_HEADING.length();
                    myNewHeading = ImportConstants.KC_CATEGORY_HEADING
                            + headingMap.get(myTempHeading).toString()
                                    .substring(len);
                    if (includeUserKCMs || myNewHeading.matches(ImportConstants.KC_CATEGORY_HEADING + DEFAULT_KC_STRING)) {
                        newHeadings.add(myNewHeading);
                    }

                } else if (myTempHeading.startsWith(ImportConstants.KC_HEADING
                        .toLowerCase().replaceAll("\\s", ""))) {
                    len = ImportConstants.KC_HEADING.length();
                    myNewHeading = ImportConstants.KC_HEADING
                            + headingMap.get(myTempHeading).toString()
                                    .substring(len);
                    if (includeUserKCMs || myNewHeading.matches(ImportConstants.KC_HEADING + DEFAULT_KC_STRING)) {
                                    newHeadings.add(myNewHeading);
                    }

                } else if (myTempHeading
                        .startsWith(ImportConstants.DATASET_LEVEL_HEADING
                                .toLowerCase().replaceAll("\\s", ""))) {
                    len = ImportConstants.DATASET_LEVEL_HEADING.length();
                    myNewHeading = ImportConstants.DATASET_LEVEL_HEADING
                            + headingMap.get(myTempHeading).toString()
                                    .substring(len);
                    newHeadings.add(myNewHeading);
                } else if (myTempHeading
                        .startsWith(ImportConstants.CUSTOM_FIELD_HEADING
                                .toLowerCase().replaceAll("\\s", ""))) {
                    len = ImportConstants.CUSTOM_FIELD_HEADING.length();
                    myNewHeading = ImportConstants.CUSTOM_FIELD_HEADING
                            + headingMap.get(myTempHeading).toString()
                                    .substring(len);
                    newHeadings.add(myNewHeading);
                } else {
                    // add ignored headings here
                    newHeadings.add(headingMap.get(myTempHeading).toString());
                }
            }
        }

        logDebug("Standardized Headings: ", newHeadings.toString());
        return newHeadings;
    }

    /**
     * Get a list of missing headings.
     * Put missing headings into the missingHeadings list.
     * @param headings headings to validate
     * @return a list of missing headings
     */
    private ArrayList<String> getMissingHeadings(ArrayList<String> headings) {
        //use a clone version of the headings to avoid messing up with the original.
        ArrayList<String> myMissingHeadings =
            (ArrayList<String>) ImportConstants.REQUIRED_HEADINGS.clone();

        // Remove the required headings found in 'headings'.
        for (String s : ImportConstants.REQUIRED_HEADINGS) {
            if (headings.contains(s)) { myMissingHeadings.remove(s); }
        }

        // Trac #701:
        // If 'Step Name' is present, 'Selection', 'Action' and 'Input' aren't required.
        if (headings.contains(ImportConstants.STEP_NAME_HEADING)) {
            myMissingHeadings.remove(ImportConstants.SELECTION_HEADING);
            myMissingHeadings.remove(ImportConstants.ACTION_HEADING);
            myMissingHeadings.remove(ImportConstants.INPUT_HEADING);
        }
        
        //if a level heading does not exist, add it to the missingHeadings list
        if (levelHeadings.isEmpty()) {
            myMissingHeadings.add(ImportConstants.DATASET_LEVEL_HEADING);
        }
        return myMissingHeadings;
    }

    /**
     * Get a list of duplicate headings.
     * @param headings to be checked
     * @return a list of duplicate headings
     */
    public ArrayList<String> getDuplicateHeadings(ArrayList<String> headings) {
       //order is important, use a clone to retain it.
       ArrayList<String> sortedHeadings = (ArrayList<String>) headings.clone();
       Collections.sort(sortedHeadings);
       String temp = "";
       ArrayList<String> myDuplicateHeadings = new ArrayList<String>();
       ArrayList<String> cfContentList = new ArrayList<String>();
       // loop through the headings to find out duplicates
       for (int i = 1; i < sortedHeadings.size(); i++) {
           temp = sortedHeadings.get(i).toString();
           // add duplicate headings to the list except those can be multiple and duplicate
           if ((temp.equals(sortedHeadings.get(i - 1)))
                   && (!temp.startsWith(ImportConstants.DATASET_LEVEL_HEADING))
                   && (!temp.equals(ImportConstants.CONDITION_NAME_HEADING))
                   && (!temp.equals(ImportConstants.CONDITION_TYPE_HEADING))
                   && (!temp.startsWith(ImportConstants.KC_HEADING))
                   && (!temp.startsWith(ImportConstants.KC_CATEGORY_HEADING))
                   && (!temp.equals(ImportConstants.SELECTION_HEADING))
                   && (!temp.equals(ImportConstants.ACTION_HEADING))
                   && (!temp.equals(ImportConstants.INPUT_HEADING))) {
               myDuplicateHeadings.add(temp);
           } else if (temp.startsWith(ImportConstants.CUSTOM_FIELD_HEADING)) {
               // custom fields are dynamic, it can be multiple but not duplicate
               if (cfContentList.contains(temp)) {
                   myDuplicateHeadings.add(temp);
               } else {
                   cfContentList.add(temp);
               }
           } // end if
       } // end for loop

       return myDuplicateHeadings;
    }

    /**
     * Check the length of a given heading.
     * @param content heading to check
     * @param maxLength max length of this heading
     * @return boolean true if the content exceeds max length
     */
    private boolean exceedMaxLength(String content, int maxLength) {
        if (content.length() > maxLength) {
            return true;
        }
        return false;
    }


    /**
     * Validate Custom Field heading against empty string, max length and special characters.
     * @param heading the heading to be checked
     */
    public void validateCFHeading(String heading) {
        String content = getContent(heading);
        Pattern pattern = Pattern.compile(REGEX_NOT_ALLOWED_CHARS);
        Matcher matcher = pattern.matcher(content);
        // validate content and add invalid ones in different list
        if (content.equals(EMPTY_STRING)) {
            emptyContentHeadings.add(ImportConstants.CUSTOM_FIELD_HEADING);
        } else if (exceedMaxLength(content, ImportConstants.MAX_CUSTOM_FIELD_LENGTH)) {
            exceedMaxLengthHeadings.add(MSG_PREFIX + "content exceeds the maximum length ("
                    + ImportConstants.MAX_CUSTOM_FIELD_LENGTH + " characters) "
                    + "for heading " + ImportConstants.CUSTOM_FIELD_HEADING + " "
                    + LEFT_PARENTHESES
                    + content.substring(0, MAX_CHAR_NUM)
                    + RIGHT_PARENTHESES + " where the \"" + content.substring(0, MAX_CHAR_NUM)
                    + "\" is the value truncated to " + MAX_CHAR_NUM + " characters.");
        } else if (matcher.find()) {
            invalidCharHeadings.add(MSG_PREFIX
                    + INVALID_CHARS_BEFORE + heading + INVALID_CHARS_AFTER);
        }
    }

    /**
     * Validate the level headings.
     * @param importStatusItem the high level status row in the database
     * @param lstLevelHeading a list of level headings
     */
    public void validateLevelHeading(ImportStatusItem importStatusItem,
            ArrayList<String> lstLevelHeading) {

        String heading = "", content = "";
        Pattern pattern = Pattern.compile(REGEX_NOT_ALLOWED_CHARS);
        Matcher matcher = pattern.matcher(content);

        ArrayList<String> levelContentList = new ArrayList<String>();

        ArrayList<String> emptyContentLevelHeadingList = new ArrayList<String>();
        ArrayList<String> duplicateLevelList = new ArrayList<String>();
        for (int i = 0; i < lstLevelHeading.size(); i++) {
            heading = lstLevelHeading.get(i).toString();
            content = getContent(heading);
            matcher = pattern.matcher(content);
            // only warnings are issued for empty string in Level,
            // add invalid ones in a different list than emptyContentHeading list
            if (content.equals(EMPTY_STRING)) {
                emptyContentLevelHeadingList.add(heading);
            }
            // only warnings are issued for duplicates in Level,
            // add invalid ones in a different list than duplicateHeading list
            if (levelContentList.contains(content)) {
                duplicateLevelList.add(heading);
            } else {
                levelContentList.add(content);
            }

            if (exceedMaxLength(content, ImportConstants.MAX_LEVEL_LENGTH)) {
                exceedMaxLengthHeadings.add(MSG_PREFIX + "content exceeds the maximum length ("
                        + ImportConstants.MAX_LEVEL_LENGTH + " characters) "
                        + "for heading " + ImportConstants.DATASET_LEVEL_HEADING + " "
                        + LEFT_PARENTHESES
                        + content.substring(0, MAX_CHAR_NUM)
                        + RIGHT_PARENTHESES + " where the \"" + content.substring(0, MAX_CHAR_NUM)
                        + "\" is the value truncated to " + MAX_CHAR_NUM + " characters.");
            }

            if (matcher.find()) {
                invalidCharHeadings.add(MSG_PREFIX
                        + INVALID_CHARS_BEFORE + heading + INVALID_CHARS_AFTER);
            }
        }
        issueWarning(importStatusItem, HEADING_ISSUE_PREFIX + EMPTY_CONTENT_MSG,
                emptyContentLevelHeadingList);
        issueWarning(importStatusItem, HEADING_ISSUE_PREFIX + DUPLICATE_HEADING_MSG,
                duplicateLevelList);
    }

    /**
     * Validate the KC and KC Category headings.
     * @param importStatusItem the high level status row in the database
     * @param lstKcHeading a list of KC headings
     * @param lstKcCatHeading a list of KC Category headings
     */
    public void validateKCAndKCCateogryHeading(ImportStatusItem importStatusItem,
            ArrayList<String> lstKcHeading, ArrayList<String> lstKcCatHeading) {

        String content = "";
        Pattern pattern = Pattern.compile(REGEX_NOT_ALLOWED_CHARS);
        Matcher matcher = pattern.matcher(content);
        ArrayList<String> kcContentList = new ArrayList<String>();
        ArrayList<String> emptyContentKcHeadingList = new ArrayList<String>();
        ArrayList<String> kcCategoryContentList = new ArrayList<String>();

        //get KC content list, check for empty string, max length, and invalid characters
        for (String myKcHeading : lstKcHeading) {
            content = getContent(myKcHeading);
            matcher = pattern.matcher(content);
            kcContentList.add(content);
            if (content.equals(EMPTY_STRING)) {
                emptyContentKcHeadingList.add(myKcHeading);
            }
            if (exceedMaxLength(content, ImportConstants.MAX_KC_LENGTH)) {
                exceedMaxLengthHeadings.add(MSG_PREFIX + "content exceeds the maximum length ("
                        + ImportConstants.MAX_KC_LENGTH + " characters) "
                        + "for heading " + ImportConstants.KC_HEADING + " "
                        + LEFT_PARENTHESES
                        + content.substring(0, MAX_CHAR_NUM)
                        + RIGHT_PARENTHESES + " where the \"" + content.substring(0, MAX_CHAR_NUM)
                        + "\" is the value truncated to " + MAX_CHAR_NUM + " characters.");
            }

            if (matcher.find()) {
                invalidCharHeadings.add(MSG_PREFIX
                        + INVALID_CHARS_BEFORE + myKcHeading + INVALID_CHARS_AFTER);
            }
        }

        //get KC Category content list, check for empty string, max length, and invalid characters
        for (String myKcCatHeading : lstKcCatHeading) {
            content = getContent(myKcCatHeading);
            kcCategoryContentList.add(content);
            if (exceedMaxLength(content, ImportConstants.MAX_KC_CATEGORY_LENGTH)) {
                exceedMaxLengthHeadings.add(MSG_PREFIX + "content exceeds the maximum length ("
                        + ImportConstants.MAX_KC_CATEGORY_LENGTH + " characters) "
                        + "for heading " + ImportConstants.KC_CATEGORY_HEADING + " "
                        + LEFT_PARENTHESES
                        + content.substring(0, MAX_CHAR_NUM)
                        + RIGHT_PARENTHESES + " where the \"" + content.substring(0, MAX_CHAR_NUM)
                        + "\" is the value truncated to " + MAX_CHAR_NUM + " characters.");
            }

            if (matcher.find()) {
                invalidCharHeadings.add(MSG_PREFIX
                        + INVALID_CHARS_BEFORE + myKcCatHeading + INVALID_CHARS_AFTER);
            }
        }

        //compare two list to find missing pairs, add to missing pair list if found any
        kcCategoryContentList.removeAll(kcContentList);
        for (String myKcCategoryContent : kcCategoryContentList) {
            missingPairHeadings.add(ImportConstants.KC_HEADING + " " + LEFT_PARENTHESES
                    + myKcCategoryContent + RIGHT_PARENTHESES);
        }

        //if all KCs are empty, issue warning
        if ((emptyContentKcHeadingList.size() == lstKcHeading.size())
                && (lstKcHeading.size() > 0)) {
             issueWarning(importStatusItem,
                     "Missing content between parentheses in each of the "
                     + ImportConstants.KC_HEADING
                     + " headings.");
             emptyContentKcHeadingList.clear();
        } else if (lstKcHeading.size() > 0) {
             //if some KCs are empty, add to emptyContentHeading list
             emptyContentHeadings.addAll(emptyContentKcHeadingList);
        }
    }

    /**
     * Return the content in the parenthesis of a given heading.
     * @param heading the heading to be processed
     * @return String value of the content
     */
    private String getContent(String heading) {
        int startIndex = heading.indexOf(LEFT_PARENTHESES);
        int endIndex = heading.lastIndexOf(RIGHT_PARENTHESES);
        if ((startIndex > 0) && (endIndex > startIndex)) {
            return heading.substring(startIndex + 1, endIndex).trim();
        }
        return EMPTY_STRING;
    }

    /**
     * Return the heading with extra quotes stripped.
     * @param heading the heading to be processed
     * @return String processed heading
     */
    private String stripExtraQuotes(String heading) {
        if (heading.startsWith("\"")) {
            heading = heading.replaceFirst("\"", "");
            heading = heading.replace("\"\"", "\"");
            if (heading.endsWith("\"")) {
                heading =
                    heading.substring(0, heading.lastIndexOf("\""));
            }
        }
        return heading;
    }

    /**
     * Get a list of ignored Headings given a list of headings.
     * @param headings the full list of headings
     * @return a list of ignored headings
     */
    private ArrayList<String> getIgnoredHeadings(ArrayList<String> headings) {
        ArrayList<String> tempAllHeadings =
            (ArrayList<String>) headings.clone();
        ArrayList<String> allHeadings = new ArrayList<String>();
        // strip the extra double quotes first,
        // otherwise those required headings with double quotes in the content will be ignored.
        for (String currentHeading : tempAllHeadings) {
            currentHeading = stripExtraQuotes(currentHeading);
            allHeadings.add(currentHeading);
        }
        ArrayList<String> tempRequired =
            (ArrayList<String>) allHeadings.clone();
        tempRequired.retainAll(ImportConstants.REQUIRED_HEADINGS);
        ArrayList<String> tempOptional =
            (ArrayList<String>) allHeadings.clone();
        tempOptional.retainAll(optionalHeadings);

        // start with existing headings, remove all those required and optional ones
        ArrayList<String> myIgnoredHeadings = (ArrayList<String>) allHeadings.clone();
        myIgnoredHeadings.removeAll(tempRequired);
        myIgnoredHeadings.removeAll(tempOptional);
        myIgnoredHeadings.removeAll(levelHeadings);
        myIgnoredHeadings.remove(EMPTY_STRING);

        // Reserved keywords already tagged as errors. Remove them from here.
        myIgnoredHeadings.removeAll(reservedKeywordHeadings);

        return myIgnoredHeadings;
    }

    /**
     * Convert headings to column names.
     * @param includeUserKCMs whether to include KCMs
     * @return a list of column names
     */
    public ArrayList<String> convertHeadingsToColumnNames(Boolean includeUserKCMs) {
        FlatFileImporterDao ffiDao = SourceDbDaoFactory.DEFAULT.getFlatFileImporterDao();
        String columnName = "", origHeading = "", standardName = "", columnValue = "";
        ArrayList<String> myColumnNames = new ArrayList<String>();

        // Create the temporary table to map the headings in the file to actual column names
        // in the temporary table, import_file_data
        ffiDao.createHeadingColumnMap();

        // loop through the heading list and convert the heading one by one
        for (String myHeading : arrHeadings) {
            //start defaultSequence with default.
            //DB Merge only cares about the defaultSequence in level and condition.
            origHeading = myHeading;
            // trim and lowercase
            columnName = origHeading.toLowerCase().trim();

            // remove parenthesis
            if (columnName.indexOf(RIGHT_PARENTHESES) > 0) {
                columnName = columnName.replaceAll("\\)", "");
            }

            if (columnName.indexOf(LEFT_PARENTHESES) > 0) {
                columnName = columnName.replaceAll("\\(", UNDERSCORE);
            }
            // replace space with underscore
            columnName = columnName.replaceAll("\\s", UNDERSCORE).replaceAll("-", UNDERSCORE);

            // if level name is not provided, use "Default"
            if ((getContent(origHeading).equals(EMPTY_STRING))
                    && origHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                columnValue = "Default";
             } else if ((getContent(origHeading).equals(EMPTY_STRING))
                      && origHeading.startsWith(ImportConstants.KC_CATEGORY_HEADING)) {
                  columnValue = "Default";
             } else if ((getContent(origHeading).equals(EMPTY_STRING))
                     && origHeading.startsWith(ImportConstants.KC_HEADING)) {
                 columnValue = "Default";
             } else {
                // get column value and replace the white space with underscore
                columnValue = getContent(origHeading);
             }
            // store the mapping in ffi_heading_column_map
            if (origHeading.startsWith(ImportConstants.DATASET_LEVEL_HEADING)) {
                maxLevelCount++;
                standardName = ImportConstants.DATASET_LEVEL_HEADING;
                columnName = "level_" + maxLevelCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxLevelCount);
            } else if (origHeading.startsWith(ImportConstants.KC_CATEGORY_HEADING)
                    && (includeUserKCMs
                        || origHeading.matches(ImportConstants.KC_CATEGORY_HEADING + DEFAULT_KC_STRING))) {
                standardName = ImportConstants.KC_CATEGORY_HEADING;
                // since KC Category must be paired with a KC, use the sequence of KC in KC Category
                columnName = "kc_category_" + maxKcCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxKcCount);
            } else if (origHeading.startsWith(ImportConstants.KC_HEADING)
                    && (includeUserKCMs
                        || origHeading.matches(ImportConstants.KC_HEADING + DEFAULT_KC_STRING))) {
                standardName = ImportConstants.KC_HEADING;


                setDefaultKcColumnId(++maxKcCount);

                columnName = "kc_" + maxKcCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxKcCount);
            } else if (origHeading.startsWith(ImportConstants.CUSTOM_FIELD_HEADING)) {
                standardName = ImportConstants.CUSTOM_FIELD_HEADING;
                maxCustomFieldCount++;
                columnName = "cf_" + maxCustomFieldCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxCustomFieldCount);
            } else if (origHeading.equals(ImportConstants.SELECTION_HEADING)) {
                standardName = ImportConstants.SELECTION_HEADING;
                maxSelectionCount++;
                columnName = columnName + "_" + maxSelectionCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxSelectionCount);
            } else if (origHeading.equals(ImportConstants.ACTION_HEADING)) {
                standardName = ImportConstants.ACTION_HEADING;
                maxActionCount++;
                columnName = columnName + "_" + maxActionCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxActionCount);
            } else if (origHeading.equals(ImportConstants.INPUT_HEADING)) {
                standardName = ImportConstants.INPUT_HEADING;
                maxInputCount++;
                columnName = columnName + "_" + maxInputCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxInputCount);
            } else if (origHeading.equals(ImportConstants.CONDITION_NAME_HEADING)) {
                standardName = ImportConstants.CONDITION_NAME_HEADING;
                maxConditionCount++;
                columnName = columnName + "_" + maxConditionCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxConditionCount);
            } else if (origHeading.equals(ImportConstants.CONDITION_TYPE_HEADING)) {
                standardName = ImportConstants.CONDITION_TYPE_HEADING;
                // since Condition Type must be paired with a Condition Name,
                // use the sequence of Name in Type
                columnName = columnName + "_" + maxConditionCount;
                ffiDao.insertIntoHeadingColumnMap(
                        standardName, origHeading, columnName, columnValue, maxConditionCount);
            }  // end if
            myColumnNames.add(columnName);
        } //end for loop

        return myColumnNames;
    }

    /** Check if headings are valid. @return the validHeadingsFlag */
    public boolean areHeadingsValid() {
        return headingsValid;
    }

    /** Set headingsValid flag. @param headingsValid the headingsValid to set */
    private void setHeadingsValid(boolean headingsValid) {
        this.headingsValid = headingsValid;
    }

    /**
     * Helper method to keep track of warning messages in the HeadingReport object.
     * @param message the warning message
     */
    private void saveWarning(String message) {
        myWarnings.add(message);
    }

    /**
     * Helper method to keep track of error messages in the HeadingReport object.
     * @param message the error message
     */
    private void saveError(String message) {
        myErrors.add(message);
    }

    /**
     * Getter for warning messages in this HeadingReport object.
     * @return list of warning messages
     */
    public List<String> getWarnings() { return myWarnings; }

    /**
     * Getter for error messages in this HeadingReport object.
     * @return list of error messages
     */
    public List<String> getErrors() { return myErrors; }

    /** Only log if debugging is enabled. @param args concatenate objects into one string */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
    /** Only log if info is enabled. @param args concatenate objects into one string */
    private void logWarn(Object... args) {
        LogUtils.logWarn(logger, args);
    }
    /** Log error message. @param args concatenate objects into one string */
    private void logError(Object... args) {
        LogUtils.logErr(logger, args);
    }

    public Integer getDefaultKcColumnId() {
        return defaultKcColumnId;
    }

    public void setDefaultKcColumnId(Integer defaultKcColumnId) {
        this.defaultKcColumnId = defaultKcColumnId;
    }
}
