/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Date;

/**
 * This is a DTO for Logging Activity reports.
 * It contains the session, time stamps and message counts.
 * @author alida
 * @version $Revision: 5737 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2009-09-21 12:21:00 -0400 (Mon, 21 Sep 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LoggingActivitySession extends DTO {
    /** The session tag in the log act table. */
    private String sessionId;
    /** The minimum server receipt time for this session. */
    private Date minServerReceiptTime;
    /** The maximum server receipt time for this session. */
    private Date maxServerReceiptTime;
    /** The total number of logs in the tutor message format. */
    private int numTotalMessages;
    /** The number of logs with a context message. */
    private int numContextMessages;
    /** The number of logs with a tool message. */
    private int numToolMessages;
    /** The number of logs with a tutor message. */
    private int numTutorMessages;
    /** The number of logs with a plain message. */
    private int numPlainMessages;

    /**
     * The constructor which takes some of the fields.
     * @param sessionId the session id
     * @param minServerReceiptTime the minimum server receipt time
     * @param maxServerReceiptTime the maximum server receipt time
     */
    public LoggingActivitySession(String sessionId, Date minServerReceiptTime,
            Date maxServerReceiptTime) {
        super();
        this.sessionId = sessionId;
        this.minServerReceiptTime = minServerReceiptTime;
        this.maxServerReceiptTime = maxServerReceiptTime;
        this.numTotalMessages = 0;
        this.numContextMessages = 0;
        this.numToolMessages = 0;
        this.numTutorMessages = 0;
        this.numPlainMessages = 0;
    }

    /**
     * Returns the session id.
     * @return the session id
     */
    public String getSessionId() {
        return sessionId;
    }
    /**
     * Sets the session id.
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    /**
     * Returns the minimum server receipt time.
     * @return the minServerReceiptTime
     */
    public Date getMinServerReceiptTime() {
        return minServerReceiptTime;
    }
    /**
     * Sets the minimum server receipt time.
     * @param minServerReceiptTime the minServerReceiptTime to set
     */
    public void setMinServerReceiptTime(Date minServerReceiptTime) {
        this.minServerReceiptTime = minServerReceiptTime;
    }
    /**
     * Returns the max server receipt time.
     * @return the maxServerReceiptTime
     */
    public Date getMaxServerReceiptTime() {
        return maxServerReceiptTime;
    }
    /**
     * Sets the maximum server receipt time.
     * @param maxServerReceiptTime the maxServerReceiptTime to set
     */
    public void setMaxServerReceiptTime(Date maxServerReceiptTime) {
        this.maxServerReceiptTime = maxServerReceiptTime;
    }
    /**
     * Returns the number of logs with tutor format messages.
     * @return the numTotalMessages
     */
    public int getNumTotalMessages() {
        return numTotalMessages;
    }
    /**
     * Sets the number of logs with tutor format messages.
     * @param numTotalMessages the numTotalMessages to set
     */
    public void setNumTotalMessages(int numTotalMessages) {
        this.numTotalMessages = numTotalMessages;
    }
    /**
     * Returns the number of logs with context messages.
     * @return the numContextMessages
     */
    public int getNumContextMessages() {
        return numContextMessages;
    }
    /**
     * Sets the number of logs with context messages.
     * @param numContextMessages the numContextMessages to set
     */
    public void setNumContextMessages(int numContextMessages) {
        this.numContextMessages = numContextMessages;
    }
    /**
     * Returns the number of logs with tool messages.
     * @return the numToolMessages
     */
    public int getNumToolMessages() {
        return numToolMessages;
    }
    /**
     * Sets the number of logs with tool messages.
     * @param numToolMessages the numToolMessages to set
     */
    public void setNumToolMessages(int numToolMessages) {
        this.numToolMessages = numToolMessages;
    }
    /**
     * Returns the number of logs with tutor messages.
     * @return the numTutorMessages
     */
    public int getNumTutorMessages() {
        return numTutorMessages;
    }
    /**
     * Sets the number of logs with tutor messages.
     * @param numTutorMessages the numTutorMessages to set
     */
    public void setNumTutorMessages(int numTutorMessages) {
        this.numTutorMessages = numTutorMessages;
    }
    /**
     * Returns the number of logs with plain messages.
     * @return the numPlainMessages
     */
    public int getNumPlainMessages() {
        return numPlainMessages;
    }
    /**
     * Sets the number of logs with plain messages.
     * @param numPlainMessages the numPlainMessages to set
     */
    public void setNumPlainMessages(int numPlainMessages) {
        this.numPlainMessages = numPlainMessages;
    }
}
