/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.SessionItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.TransactionItem;

/**
 * This class is a constants class that holds the mapping between
 * what is displayed on the screen (UI), what is held in the database,
 * and what is the actual name (in HQL code) of the sample items.  When
 * a filter is created for a sample on a field and an attribute the text
 * saved in the database matches the DB_CLASS and DB_ATTRIBUTE fields.  The
 * CLASS and ATTRIBUTE fields match the HQL code version.  The UI_CLASS
 * and UI_ATTRIBUTE are human readable versions of the same.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class SampleMapper {

    /** logger for this class. */
    private static final Logger LOGGER =
        Logger.getLogger("edu.cmu.pslc.datashop.dao.hiberante.SampleMapper");

    /** Index number for UI display. */
    public static final int UI = 0;
    /** Index number for the HQL Code value. */
    public static final int HQL = 1;
    /** Index number for Database value. */
    public static final int DB = 2;
    /** Index number fore the HQL abbreviation for the class. */
    public static final int HQL_ABBREV = 3;
    /** Index number fore the Java type of an attribute. */
    public static final int JAVA_TYPE = 3;

    /** Private construct to prevent instantiation */
    private SampleMapper() { };

    /** Mapping information for the problem class. */
    public static final List<String> PROBLEM;
    static {
        PROBLEM = new ArrayList<String>();
        PROBLEM.add(UI, "Problem");
        PROBLEM.add(HQL, ProblemItem.class.getName());
        PROBLEM.add(DB, "problem");
        PROBLEM.add(HQL_ABBREV, "prob");
    }

    /** Mapping information for the problem name attribute. */
    public static final List<String> PROBLEM_NAME;
    static {
        PROBLEM_NAME = new ArrayList<String>();
        PROBLEM_NAME.add(UI, "Name");
        PROBLEM_NAME.add(HQL, "problemName");
        PROBLEM_NAME.add(DB, "problemName");
    }

    /** Mapping information for the problem description attribute. */
    public static final List<String> PROBLEM_DESCRIPTION = new ArrayList<String>();
    static {
        PROBLEM_DESCRIPTION.add(UI, "Description");
        PROBLEM_DESCRIPTION.add(HQL, "problemDescription");
        PROBLEM_DESCRIPTION.add(DB, "problemDescription");
    }

    /** Mapping information for the condition class. */
    public static final List<String> CONDITION = new ArrayList<String>();
    static {
        CONDITION.add(UI, "Condition");
        CONDITION.add(HQL, ConditionItem.class.getName());
        CONDITION.add(DB, "condition");
        CONDITION.add(HQL_ABBREV, "cond");
    }

    /** Mapping information for the condition name attribute. */
    public static final List<String> CONDITION_NAME = new ArrayList<String>();
    static {
        CONDITION_NAME.add(UI, "Name");
        CONDITION_NAME.add(HQL, "conditionName");
        CONDITION_NAME.add(DB, "conditionName");
    }

    /** Mapping information for the condition type attribute. */
    public static final List<String> CONDITION_TYPE = new ArrayList<String>();
    static {
        CONDITION_TYPE.add(UI, "Type");
        CONDITION_TYPE.add(HQL, "type");
        CONDITION_TYPE.add(DB, "type");
    }

    /** Mapping information for the dataset level class. */
    public static final List<String> DATASET_LEVEL = new ArrayList<String>();
    static {
        DATASET_LEVEL.add(UI, "Dataset Level");
        DATASET_LEVEL.add(HQL, DatasetLevelItem.class.getName());
        DATASET_LEVEL.add(DB, "datasetLevel");
        DATASET_LEVEL.add(HQL_ABBREV, "lev");
    }

    /** Mapping information for the dataset level name attribute. */
    public static final List<String> DATASET_LEVEL_NAME = new ArrayList<String>();
    static {
        DATASET_LEVEL_NAME.add(UI, "Name");
        DATASET_LEVEL_NAME.add(HQL, "levelName");
        DATASET_LEVEL_NAME.add(DB, "levelName");
    }

    /** Mapping information for the dataset level title attribute. */
    public static final List<String> DATASET_LEVEL_TITLE = new ArrayList<String>();
    static {
        DATASET_LEVEL_TITLE.add(UI, "Title");
        DATASET_LEVEL_TITLE.add(HQL, "levelTitle");
        DATASET_LEVEL_TITLE.add(DB, "levelTitle");
    }

    /** Mapping information for the school class. */
    public static final List<String> SCHOOL = new ArrayList<String>();
    static {
        SCHOOL.add(UI, "School");
        SCHOOL.add(HQL, SchoolItem.class.getName());
        SCHOOL.add(DB, "school");
        SCHOOL.add(HQL_ABBREV, "scho");
    }

    /** Mapping information for the school name attribute. */
    public static final List<String> SCHOOL_NAME = new ArrayList<String>();
    static {
        SCHOOL_NAME.add(UI, "Name");
        SCHOOL_NAME.add(HQL, "schoolName");
        SCHOOL_NAME.add(DB, "schoolName");
    }

    /** Mapping information for the student class. */
    public static final List<String> STUDENT = new ArrayList<String>();
    static {
        STUDENT.add(UI, "Student");
        STUDENT.add(HQL, StudentItem.class.getName());
        STUDENT.add(DB, "student");
        STUDENT.add(HQL_ABBREV, "stud");
    }

    /** Mapping information for the student name attribute. */
    public static final List<String> STUDENT_NAME = new ArrayList<String>();
    static {
        STUDENT_NAME.add(UI, "Anon Id");
        STUDENT_NAME.add(HQL, "anonymousUserId");
        STUDENT_NAME.add(DB, "anonymousUserId");
    }

    /** Mapping information for the transaction class. */
    public static final List<String> TRANSACTION = new ArrayList<String>();
    static {
        TRANSACTION.add(UI, "Tutor Transaction");
        TRANSACTION.add(HQL, TransactionItem.class.getName());
        TRANSACTION.add(DB, "transaction");
        TRANSACTION.add(HQL_ABBREV, "trans");
    }

    /** Mapping information for the transaction time attribute. */
    public static final List<String> TRANSACTION_TIME = new ArrayList<String>();
    static {
        TRANSACTION_TIME.add(UI, "Time Stamp");
        TRANSACTION_TIME.add(HQL, "transactionTime");
        TRANSACTION_TIME.add(DB, "transactionTime");
    }

    /** Mapping information for the transaction subgoal attempt number. */
    public static final List<String> TRANSACTION_SUBGOAL_ATTEMPT = new ArrayList<String>();
    static {
        TRANSACTION_SUBGOAL_ATTEMPT.add(UI, "Attempt Number");
        TRANSACTION_SUBGOAL_ATTEMPT.add(HQL, "attemptAtSubgoal");
        TRANSACTION_SUBGOAL_ATTEMPT.add(DB, "attemptAtSubgoal");
    }

    /** Mapping information for the session class. */
    public static final List<String> SESSION = new ArrayList<String>();
    static {
        SESSION.add(UI, "Session");
        SESSION.add(HQL, SessionItem.class.getName());
        SESSION.add(DB, "session");
        SESSION.add(HQL_ABBREV, "sess");
    }

    /** Mapping information for the custom field class. */
    public static final List<String> CUSTOM_FIELD = new ArrayList<String>();
    static {
        CUSTOM_FIELD.add(UI, "Custom Field");
        CUSTOM_FIELD.add(HQL, CustomFieldItem.class.getName());
        CUSTOM_FIELD.add(DB, "customField");
        CUSTOM_FIELD.add(HQL_ABBREV, "cf");
    }

    /** Mapping information for the custom field name attribute. */
    public static final List<String> CUSTOM_FIELD_NAME = new ArrayList<String>();
    static {
        CUSTOM_FIELD_NAME.add(UI, "Name");
        CUSTOM_FIELD_NAME.add(HQL, "customFieldName");
        CUSTOM_FIELD_NAME.add(DB, "customFieldName");
        CUSTOM_FIELD_NAME.add(HQL_ABBREV, "cf");
    }

    /** Mapping information for the custom field value attribute. */
    public static final List<String> CUSTOM_FIELD_VALUE = new ArrayList<String>();
    static {
        CUSTOM_FIELD_VALUE.add(UI, "Value");
        CUSTOM_FIELD_VALUE.add(HQL, "value");
        CUSTOM_FIELD_VALUE.add(DB, "customFieldValue");
        CUSTOM_FIELD_VALUE.add(HQL_ABBREV, "cfTxLev");
    }

    /**
     * Takes in the database string value and returns the UI display string.
     * @param databaseText the item string as saved in the DB.
     * @return a String for displaying the item to a UI.
     */
    public static String getDisplayText(String databaseText) {

        if (databaseText.equals(PROBLEM.get(DB))) {
            return (String)PROBLEM.get(UI);
        } else if (databaseText.equals(PROBLEM_NAME.get(DB))) {
            return (String)PROBLEM_NAME.get(UI);
        } else if (databaseText.equals(PROBLEM_DESCRIPTION.get(DB))) {
            return (String)PROBLEM_DESCRIPTION.get(UI);
        } else if (databaseText.equals(CONDITION.get(DB))) {
            return (String)CONDITION.get(UI);
        } else if (databaseText.equals(CONDITION_NAME.get(DB))) {
            return (String)CONDITION_NAME.get(UI);
        } else if (databaseText.equals(CONDITION_TYPE.get(DB))) {
            return (String)CONDITION_TYPE.get(UI);
        } else if (databaseText.equals(DATASET_LEVEL.get(DB))) {
            return (String)DATASET_LEVEL.get(UI);
        } else if (databaseText.equals(DATASET_LEVEL_NAME.get(DB))) {
            return (String)DATASET_LEVEL_NAME.get(UI);
        } else if (databaseText.equals(DATASET_LEVEL_TITLE.get(DB))) {
            return (String)DATASET_LEVEL_TITLE.get(UI);
        } else if (databaseText.equals(SCHOOL.get(DB))) {
            return (String)SCHOOL.get(UI);
        } else if (databaseText.equals(SCHOOL_NAME.get(DB))) {
            return (String)SCHOOL_NAME.get(UI);
        } else if (databaseText.equals(STUDENT.get(DB))) {
            return (String)STUDENT.get(UI);
        } else if (databaseText.equals(STUDENT_NAME.get(DB))) {
            return (String)STUDENT_NAME.get(UI);
        } else if (databaseText.equals(TRANSACTION.get(DB))) {
            return (String)TRANSACTION.get(UI);
        } else if (databaseText.equals(TRANSACTION_TIME.get(DB))) {
            return (String)TRANSACTION_TIME.get(UI);
        } else if (databaseText.equals(TRANSACTION_SUBGOAL_ATTEMPT.get(DB))) {
            return (String)TRANSACTION_SUBGOAL_ATTEMPT.get(UI);
        } else if (databaseText.equals(CUSTOM_FIELD.get(DB))) {
            return (String)CUSTOM_FIELD.get(UI);
        } else if (databaseText.equals(CUSTOM_FIELD_NAME.get(DB))) {
            return (String)CUSTOM_FIELD_NAME.get(UI);
        } else if (databaseText.equals(CUSTOM_FIELD_VALUE.get(DB))) {
            return (String)CUSTOM_FIELD_VALUE.get(UI);
        } else {
            LOGGER.error("No matching UI string found for '" + databaseText + "'");
            return "";
        }
    }

    /** attributeInfo and classInfo contain UI and HQL mappings for a filter attribute and its
     *  class, respectively. */
    private List<String> attributeInfo, classInfo;

    /**
     * Create a SampleMapper with class and attribute mappings.
     * private because SampleMapper's can only be created statically within this class.
     * @param classInfo class mappings
     * @param attributeInfo attribute mappings
     */
    private SampleMapper(List<String> classInfo, List<String> attributeInfo) {
        this.attributeInfo = attributeInfo;
        this.classInfo = classInfo;
    }

    /**
     * The attribute UI String.
     * @return the attribute UI String
     */
    public String getUI() {
        return attributeInfo.get(UI);
    }

    /**
     * The attribute HQL String.
     * @return the attribute HQL String
     */
    public String getHQL() {
        return attributeInfo.get(HQL);
    }

    /**
     * The attribute DB String.
     * @return the attribute DB String
     */
    public String getAttributeDB() {
        return attributeInfo.get(DB);
    }

    /**
     * The class DB String.
     * @return the class DB String
     */
    public String getClassDB() {
        return classInfo.get(DB);
    }

    /**
     * The class HQL abbreviation.
     * @return the class HQL abbreviation
     */
    public String getHQLAbbrev() {
        return classInfo.get(HQL_ABBREV);
    }

    /**
     * The attribute Java type.
     * @return the attribute Java type
     */
    public String getJavaType() {
        return attributeInfo.get(JAVA_TYPE);
    }

    /** Problem Name mapper. */
    public static final SampleMapper PROBLEM_NAME_MAPPER =
        new SampleMapper(PROBLEM, PROBLEM_NAME);
    /** Problem Description mapper. */
    public static final SampleMapper PROBLEM_DESCRIPTION_MAPPER =
        new SampleMapper(PROBLEM, PROBLEM_DESCRIPTION);
    /** Condition Name mapper. */
    public static final SampleMapper CONDITION_NAME_MAPPER =
        new SampleMapper(CONDITION, CONDITION_NAME);
    /** Condition Type mapper. */
    public static final SampleMapper CONDITION_TYPE_MAPPER =
        new SampleMapper(CONDITION, CONDITION_TYPE);
    /** Dataset Level Name mapper. */
    public static final SampleMapper DATASET_LEVEL_NAME_MAPPER =
        new SampleMapper(DATASET_LEVEL, DATASET_LEVEL_NAME);
    /** Dataset Level Title mapper. */
    public static final SampleMapper DATASET_LEVEL_TITLE_MAPPER =
        new SampleMapper(DATASET_LEVEL, DATASET_LEVEL_TITLE);
    /** School Name mapper. */
    public static final SampleMapper SCHOOL_NAME_MAPPER =
        new SampleMapper(SCHOOL, SCHOOL_NAME);
    /** Student Name mapper. */
    public static final SampleMapper STUDENT_NAME_MAPPER =
        new SampleMapper(STUDENT, STUDENT_NAME);
    /** Transaction Time mapper. */
    public static final SampleMapper TRANSACTION_TIME_MAPPER =
        new SampleMapper(TRANSACTION, TRANSACTION_TIME);
    /** Transaction Subgoal Attempt mapper. */
    public static final SampleMapper TRANSACTION_SUBGOAL_ATTEMPT_MAPPER =
        new SampleMapper(TRANSACTION, TRANSACTION_SUBGOAL_ATTEMPT);
    /** Custom Field name mapper. */
    public static final SampleMapper CUSTOM_FIELD_NAME_MAPPER =
        new SampleMapper(CUSTOM_FIELD, CUSTOM_FIELD_NAME);
    /** Custom Field value mapper. */
    public static final SampleMapper CUSTOM_FIELD_VALUE_MAPPER =
        new SampleMapper(CUSTOM_FIELD, CUSTOM_FIELD_VALUE);
}
