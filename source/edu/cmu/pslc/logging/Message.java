/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.rmi.server.UID;

import edu.cmu.pslc.logging.element.MetaElement;
/**
 * The message class is an abstract class to describe
 * any type of message that DataShop accepts.
 * @author Alida Skogsholm
 * @version $Revision: 9303 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-05-30 11:30:09 -0400 (Thu, 30 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class Message {
    /** . */
    public static final String MSG_SEQUENCE_ELEMENT = "tutor_related_message_sequence";
    /** . */
    public static final String VERSION_NUMBER_ATTR = "version_number";
    /** . */
    public static final String VERSION_NUMBER = "2";
    /** . */
    public static final String TOOL_MSG_ELEMENT = "tool_message";
    /** . */
    public static final String TUTOR_MSG_ELEMENT = "tutor_message";
    /** . */
    public static final String CONTEXT_MSG_ELEMENT = "context_message"; //DTDv4
    /** . */
    public static final String CURRICULUM_MSG_ELEMENT = "curriculum_message"; //DTDv2
    /** . */
    public static final String MSG_ELEMENT = "message";
    /** . */
    public static final String ATTEMPT = "ATTEMPT";
    /** . */
    public static final String RESULT = "RESULT";
    /** . */
    public static final String HINT_REQUEST = "HINT_REQUEST";
    /** . */
    public static final String HINT_MESSAGE = "HINT_MSG";
    /** . */
    private String contextMessageId;
    /** . */
    private MetaElement metaElement;
    /** . */
    private String source = "DataShop";
    /**
     * Constructor.
     * @param contextMessageId the context message id
     * @param meta the meta element
     */
    Message(String contextMessageId, MetaElement meta) {
        this.contextMessageId = contextMessageId;
        this.metaElement = meta;
    }
    /**
     * To string method.
     * @param logMetaFlag whether or not to display the meta element
     * @return the string to display
     */
    public abstract String toString(boolean logMetaFlag);

    /**
     * Generate a unique identifier with a prefix. Returns toString() of a new instance of
     * java.rmi.server.UID.
     *
     * @param prefix the prefix to prepend to the GUID
     * @return  prefix plus the UID.toString() result
     */
    public static String generateGUID(String prefix) {
        UID uid = new UID();
        return prefix + uid.toString();
    }
    /**
     * Get context message id.
     * @return the context message id
     */
    public String getContextMessageId() {
        return contextMessageId;
    }
    /**
     * Set context message id.
     * @param id the context message id
     */
    public void setContextMessageId(String id) {
        this.contextMessageId = id;
    }
    /**
     * Get meta element.
     * @return the meta element
     */
    public MetaElement getMetaElement() {
        return metaElement;
    }
    /**
     * Get user id as a string.
     * @return the user id as a string
     */
    public String getUserId() {
        return metaElement.getUserId();
    }
    /**
     * Get session id.
     * @return the session id as a string
     */
    public String getSessionId() {
        return metaElement.getSessionId();
    }
    /**
     * Get time as a string.
     * @return the time as a string
     */
    public String getTimeString() {
        return metaElement.getTime();
    }
    /**
     * Set time as a string.
     * @param timeString time as a string
     */
    public void setTimeString(String timeString) {
        this.metaElement.setTime(timeString);
    }
    /**
     * Get time zone.
     * @return the time zone
     */
    public String getTimeZone() {
        return metaElement.getTimeZone();
    }
    /**
     * Get source.
     * @return the source
     */
    public String getSource() {
        return source;
    }
    /**
     * Set source.
     * @param source the source
     */
    public void setSource(String source) {
        this.source = source;
    }
}
