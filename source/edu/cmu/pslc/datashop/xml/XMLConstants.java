/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

/**
 * Utility class that contains useful constants for DataShop XML processing.
 * @author kcunning
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class XMLConstants {
    /** Private constructor as this is a utility class. */
    private XMLConstants() { }
    /** Action. */
    public static final String ACTION_ELEMENT = "action";
    /** Action Evaluation. */
    public static final String ACTION_EVALUATION_ELEMENT = "action_evaluation";
    /** Class. */
    public static final String CLASS_ELEMENT = "class";
    /** Condition. */
    public static final String CONDITION_ELEMENT = "condition";
    /** Custom Field. */
    public static final String CUSTOM_FIELD_ELEMENT = "custom_field";
    /** Dataset. */
    public static final String DATASET_ELEMENT = "dataset";
    /** Description. */
    public static final String DESCRIPTION_ELEMENT = "description";
    /** Event Descriptor. */
    public static final String EVENT_DESCRIPTOR_ELEMENT = "event_descriptor";
    /** Input. */
    public static final String INPUT_ELEMENT = "input";
    /** Instructor. */
    public static final String INSTRUCTOR_ELEMENT = "instructor";
    /** Interpretation. */
    public static final String INTERPRETATION_ELEMENT = "interpretation";
    /** Level. */
    public static final String LEVEL_ELEMENT = "level";
    /** Meta. */
    public static final String META_ELEMENT = "meta";
    /** Name. */
    public static final String NAME_ELEMENT = "name";
    /** Period. */
    public static final String PERIOD_ELEMENT = "period";
    /** Problem. */
    public static final String PROBLEM_ELEMENT = "problem";
    /** Problem Name. */
    public static final String PROBLEM_NAME_ELEMENT = "problem_name";
    /** Replay. */
    public static final String REPLAY_ELEMENT = "replay";
    /** School. */
    public static final String SCHOOL_ELEMENT = "school";
    /** Selection. */
    public static final String SELECTION_ELEMENT = "selection";
    /** Semantic Event. */
    public static final String SEMANTIC_EVENT_ELEMENT = "semantic_event";
    /** Session ID. */
    public static final String SESSION_ID_ELEMENT = "session_id";
    /** Skill. */
    public static final String SKILL_ELEMENT = "skill";
    /** Time. */
    public static final String TIME_ELEMENT = "time";
    /** Time Zone. */
    public static final String TIME_ZONE_ELEMENT = "time_zone";
    /** UI Event. */
    public static final String UI_EVENT_ELEMENT = "ui_event";
    /** User ID. */
    public static final String USER_ID_ELEMENT = "user_id";
    /** Value. */
    public static final String VALUE_ELEMENT = "value";
    /** Context Message ID. */
    public static final String CONTEXT_MESSAGE_ID = "context_message_id";
    /** Discourse. */
    public static final String DISCOURSE_ELEMENT = "discourse";
    /** Empty String. */
    public static final String EMPTY_STRING = "";
    /** XML Suffix. */
    public static final String XML_SUFFIX = ".xml";
    /** Schema feature. */
    public static final String SCHEMA_FEATURE = "http://apache.org/xml/features/validation/schema";
    /** Schema Location property. */
    public static final String SCHEMA_LOCATION_PROPERTY =
        "http://apache.org/xml/properties/schema/external-schemaLocation";
    /** Schema location. */
    public static final String SCHEMA_LOCATION =
        "http://pslcdatashop.org/dtd/tutor_message_v4.xsd";

    /** Some constants for string lengths */
    /** Condition Name length. */
    public static final Integer CONDITION_NAME_LENGTH = 80;
    /** Custom Field Name length. */
    public static final Integer CUSTOM_FIELD_NAME_LENGTH = 255;
    /** Dataset Level Name length. */
    public static final Integer DATASET_LEVEL_NAME_LENGTH = 100;
    /** Dataset Name length. */
    public static final Integer DATASET_NAME_LENGTH = 100;
    /** Problem Name length. */
    public static final Integer PROBLEM_NAME_LENGTH = 255;

    /** Length constant. */
    public static final Integer CLASS_NAME_LENGTH = 75;
    /** Length constant. */
    public static final Integer SCHOOL_NAME_LENGTH = 100;
    /** Length constant. */
    public static final Integer PERIOD_NAME_LENGTH = 50;
    /** Length constant. */
    public static final Integer DESCRIPTION_LENGTH = 255;
    /** Length constant. */
    public static final Integer INSTRUCTOR_NAME_LENGTH = 55;
}
