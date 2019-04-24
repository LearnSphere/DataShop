/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * MetaElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetaElement {

    /** Class attribute. */
    private String userId = null;
    /** Class attribute. */
    private Boolean anonFlag = null;
    /** Class attribute. */
    private String sessionId = null;
    /** Class attribute. */
    private String time = null;
    /** Class attribute. */
    private String timeZone = null;

    /**
     * Constructor.
     * @param userId userId
     * @param sessionId sessionId
     * @param time time
     * @param timeZone timeZone
     */
    public MetaElement(String userId, String sessionId, String time, String timeZone) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.time = time;
        this.timeZone = timeZone;
    }

    /**
     * Constructor.
     * @param userId userId
     * @param sessionId sessionId
     * @param time time
     * @param timeZone timeZone
     * @param anonFlag anonFlag
     */
    public MetaElement(String userId, Boolean anonFlag,
            String sessionId, String time, String timeZone) {
        this.userId = userId;
        this.anonFlag = anonFlag;
        this.sessionId = sessionId;
        this.time = time;
        this.timeZone = timeZone;
    }

    /**
     * Gets userId.
     * @return userId
     */
    public String getUserId() {
        return userId;
    }
    /**
     * Gets sessionId.
     * @return sessionId
     */
    public String getSessionId() {
        return sessionId;
    }
    /**
     * Gets time.
     * @return time
     */
    public String getTime() {
        return time;
    }
    /**
     * Gets timeZone.
     * @return timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }
    /**
     * Sets time.
     * @param time time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<meta>\n");

        buffer.append("\t\t<user_id");
        if (anonFlag != null) {
            buffer.append(" anonFlag=\"");
            buffer.append(anonFlag);
            buffer.append("\"");
        }
        buffer.append(">");
        buffer.append(LogFormatUtils.escapeElement(userId));
        buffer.append("</user_id>\n");

        buffer.append("\t\t<session_id>");
        buffer.append(LogFormatUtils.escapeElement(sessionId));
        buffer.append("</session_id>\n");

        buffer.append("\t\t<time>");
        buffer.append(LogFormatUtils.escapeElement(time));
        buffer.append("</time>\n");

        buffer.append("\t\t<time_zone>");
        buffer.append(LogFormatUtils.escapeElement(timeZone));
        buffer.append("</time_zone>\n");

        buffer.append("\t</meta>");
        buffer.append("\n");

        return buffer.toString();
    }
}
