/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a Message record of the system.
 *
 * @author Hui Cheng
 * @version $Revision: 5025 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-08-01 12:06:14 -0400 (Fri, 01 Aug 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MessageItem extends Item
            implements java.io.Serializable, Comparable  {

    /** Message type enumerated field value - "tool". */
    public static final String MSG_TYPE_TOOL = "tool";

    /** Message type enumerated field value - "tutor". */
    public static final String MSG_TYPE_TUTOR = "tutor";

    /** Message type enumerated field value - "context". */
    public static final String MSG_TYPE_CONTEXT = "context";

    /** Message type enumerated field value - "message". */
    public static final String MSG_TYPE_PLAIN_MESSAGE = "message";

    /** Message type enumerated field value - "none". */
    public static final String MSG_TYPE_NONE = "none";

    /** Collection of all allowed items in the level enumeration. */
    private static final List MSG_TYPE_ENUM = new ArrayList();

    /**
     * Adds each message type to the enumerated list.
     */
    static {
        MSG_TYPE_ENUM.add(MSG_TYPE_TOOL);
        MSG_TYPE_ENUM.add(MSG_TYPE_TUTOR);
        MSG_TYPE_ENUM.add(MSG_TYPE_CONTEXT);
        MSG_TYPE_ENUM.add(MSG_TYPE_PLAIN_MESSAGE);
        MSG_TYPE_ENUM.add(MSG_TYPE_NONE);
    }

    /** Imported/Processed flag column value used to indicate that this GUID was imported. */
    public static final String SUCCESS_FLAG = "SUCCESS";

    /**
     * Imported/Processed flag column value used to indicate that this GUID was imported,
     * but that the data was received by the server less than the confidence time before
     * it was processed.
     */
    public static final String SUCCESS_QUESTIONABLE_FLAG = "RECHECK";

    /** Imported/Processed flag column value used to indicate that this GUID was NOT imported. */
    public static final String ERROR_FLAG = "ERROR";

    /** Imported/Processed flag column value used to indicate that this GUID was ignored. */
    public static final String IGNORE_FLAG = "IGNORE";

    /**
     * Check for valid message type.
     * @param messageType the message type, tool, tutor or context/curriculum
     * @return true if message type is valid; false otherwise
     */
    public static boolean isValidMessageType(String messageType) {
        if (MSG_TYPE_ENUM.contains(messageType)) {
            return true;
        }
        return false;
    }

    /**
     * Return a list of the valid message types.
     * @return an unmodifiable list of the valid message types
     */
    public static List getMessageTypes() {
        return Collections.unmodifiableList(MSG_TYPE_ENUM);
    }

    /** The message_id for this message record, db auto_incremented */
    private Long messageId;
    /** The user_id for this message record. */
    private String userId;
    /** The session tag (actual) for this message record. */
    private String sessionTag;
    /** The dataset_message_id for this message record. */
    private String contextMessageId;
    /** The time for this message record. */
    private Date time;
    /** The time zone for this message record. */
    private String timeZone;
    /** The transaction_id for this message record. */
    private String transactionId;
    /** The message_type for this message record. */
    private String messageType;
    /** The info for this message record. */
    private String info;
    /** The imported_time for this message record. */
    private Date importedTime;
    /** The import_source for this message record. */
    private String importSource;
    /** The processed_time for this message record. */
    private Date processedTime;
    /** The processed_flag for this message record. */
    private String processedFlag;
    /** The processed_info for this message record. */
    private String processedInfo;
    /** The GUID for this message record. */
    private String guid;
    /** The version of XML in the info field for this message record. */
    private String xmlVersion;
    /** Server receipt time */
    private Date serverReceiptTime;

    /** Default constructor. */
    public MessageItem() {
    }

    /**
     * Constructor with id.
     * @param messageId as long, the unique id.
     */
    public MessageItem(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * Returns messageId.
     * @return the Long messageId as a Comparable
     */
    public Comparable getId() {
        return this.messageId;
    }

    /**
     * Set messageId.
     * @param messageId Database generated unique id.
     */
    public void setId(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * Get userId.
     * @return String.
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Set userId.
     * @param userId as String.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get sessionTag.
     * @return String.
     */
    public String getSessionTag() {
        return this.sessionTag;
    }

    /**
     * Set sessionTag.
     * @param sessionTag as String.
     */
    public void setSessionTag(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    /**
     * Get contextMessageId.
     * @return String.
     */
    public String getContextMessageId() {
        return this.contextMessageId;
    }

    /**
     * Set contextMessageId.
     * @param contextMessageId as String.
     */
    public void setContextMessageId(String contextMessageId) {
        this.contextMessageId = contextMessageId;
    }

    /**
     * Get time.
     * @return Date.
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * Set time.
     * @param time as Date.
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Get timeZone.
     * @return String.
     */
    public String getTimeZone() {
        return this.timeZone;
    }

    /**
     * Set timeZone.
     * @param timeZone as String.
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Get transactionId.
     * @return String.
     */
    public String getTransactionId() {
        return this.transactionId;
    }

    /**
     * Set transactionId.
     * @param transactionId as String.
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Get messageType.
     * @return String.
     */
    public String getMessageType() {
        return this.messageType;
    }

    /**
     * Set messageType.
     * @param messageType as String.
     */
    public void setMessageType(String messageType) {
        if (isValidMessageType(messageType)) {
            this.messageType = messageType;
        } else {
            throw new LogException("Message type can only be "
                + getMessageTypes() + " and not : " + messageType);
        }
    }

    /**
     * Get info.
     * @return String.
     */
    public String getInfo() {
        return this.info;
    }

    /**
     * Set info.
     * @param info as String.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Get importedTime.
     * @return Date
     */
    public Date getImportedTime() {
        return this.importedTime;
    }

    /**
     * Set importedTime.
     * @param importedTime as Date
     */
    public void setImportedTime(Date importedTime) {
        this.importedTime = importedTime;
    }

    /**
     * Get improtSource.
     * @return the importSource
     */
    public String getImportSource() {
        return importSource;
    }

    /**
     * Set improtSource.
     * @param importSource the importSource to set
     */
    public void setImportSource(String importSource) {
        this.importSource = importSource;
    }

    /**
     * Get processedTime.
     * @return Date
     */
    public Date getProcessedTime() {
        return this.processedTime;
    }

    /**
     * Set processedTime.
     * @param processedTime as Date
     */
    public void setProcessedTime(Date processedTime) {
        this.processedTime = processedTime;
    }

    /**
     * Get processedFlag.
     * @return String.
     */
    public String getProcessedFlag() {
        return this.processedFlag;
    }

    /**
     * Set processedFlag.
     * @param processedFlag as String.
     */
    public void setProcessedFlag(String processedFlag) {
        this.processedFlag = processedFlag;
    }

    /**
     * Get processedInfo.
     * @return String.
     */
    public String getProcessedInfo() {
        return this.processedInfo;
    }

    /**
     * Set processedInfo.
     * @param processedInfo as String.
     */
    public void setProcessedInfo(String processedInfo) {
        this.processedInfo = processedInfo;
    }

    /**
     * Get GUID.
     * @return String.
     */
    public String getGuid() {
        return this.guid;
    }

    /**
     * Set GUID.
     * @param guid as String.
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * Get xmlVersion.
     * @return String.
     */
    public String getXmlVersion() {
        return this.xmlVersion;
    }

    /**
     * Set xmlVersion.
     * @param xmlVersion as String.
     */
    public void setXmlVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    /**
     * Get server receipt time.
     * @return the server receipt time as a Date.
     */
    public Date getServerReceiptTime() {
        return this.serverReceiptTime;
    }

    /**
     * Set server receipt time.
     * @param serverReceiptTime for this action.
     */
    public void setServerReceiptTime(Date serverReceiptTime) {
        this.serverReceiptTime = serverReceiptTime;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToString("messageId", this.getId()));
        buffer.append(objectToString("userId", this.getUserId()));
        buffer.append(objectToString("sessionTag", this.getSessionTag()));
        buffer.append(objectToString("contextMessageId", this.getContextMessageId()));
        buffer.append(objectToString("time", this.getTime()));
        buffer.append(objectToString("timeZone", this.getTimeZone()));
        buffer.append(objectToString("trasactionId", this.getTransactionId()));
        buffer.append(objectToString("messageType", this.getMessageType()));
        buffer.append(objectToString("info", this.getInfo()));
        buffer.append(objectToString("importedTime", this.getImportedTime()));
        buffer.append(objectToString("processedTime", this.getProcessedTime()));
        buffer.append(objectToString("processedFlag", this.getProcessedFlag()));
        buffer.append(objectToString("processedInfo", this.getProcessedInfo()));
        buffer.append(objectToString("GUID", this.getGuid()));
        buffer.append(objectToString("xmlVersion", this.getXmlVersion()));
        buffer.append(objectToString("serverReceiptTime", this.getServerReceiptTime()));
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Returns a shorter string representation of this item, does not include everything.
     * @return a shorter string representation of this item
     */
    public String debugToString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" [");
        buffer.append(objectToString("messageId", this.getId()));
        buffer.append(objectToString("userId", this.getUserId()));
        buffer.append(objectToString("sessionTag", this.getSessionTag()));
        buffer.append(objectToString("contextMessageId", this.getContextMessageId()));
        buffer.append(objectToString("time", this.getTime()));
        buffer.append(objectToString("timeZone", this.getTimeZone()));
        buffer.append(objectToString("trasactionId", this.getTransactionId()));
        buffer.append(objectToString("messageType", this.getMessageType()));
        buffer.append(objectToString("importedTime", this.getImportedTime()));
        buffer.append(objectToString("processedTime", this.getProcessedTime()));
        buffer.append(objectToString("processedFlag", this.getProcessedFlag()));
        buffer.append(objectToString("processedInfo", this.getProcessedInfo()));
        buffer.append(objectToString("GUID", this.getGuid()));
        buffer.append(objectToString("serverReceiptTime", this.getServerReceiptTime()));
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MessageItem) {
            MessageItem otherItem = (MessageItem)obj;

            if (!objectEquals(this.getUserId(), otherItem.getUserId())) {
                return false;
            }
            if (!objectEquals(this.getSessionTag(), otherItem.getSessionTag())) {
                return false;
            }
            if (!objectEquals(this.getContextMessageId(), otherItem.getContextMessageId())) {
                return false;
            }
            if (!objectEquals(this.getTime(), otherItem.getTime())) {
                return false;
            }
            if (!objectEquals(this.getTimeZone(), otherItem.getTimeZone())) {
                return false;
            }
            if (!objectEquals(this.getTransactionId(), otherItem.getTransactionId())) {
                return false;
            }
            if (!objectEquals(this.getMessageType(), otherItem.getMessageType())) {
                return false;
            }
            if (!objectEquals(this.getInfo(), otherItem.getInfo())) {
                return false;
            }
            if (!objectEquals(this.getImportedTime(), otherItem.getImportedTime())) {
                return false;
            }
            if (!objectEquals(this.getImportSource(), otherItem.getImportSource())) {
                return false;
            }
            if (!objectEquals(this.getProcessedTime(), otherItem.getProcessedTime())) {
                return false;
            }
            if (!objectEquals(this.getProcessedFlag(), otherItem.getProcessedFlag())) {
                return false;
            }
            if (!objectEquals(this.getProcessedInfo(), otherItem.getProcessedInfo())) {
                return false;
            }
            if (!objectEquals(this.getGuid(), otherItem.getGuid())) {
                return false;
            }
            if (!objectEquals(this.getXmlVersion(), otherItem.getXmlVersion())) {
                return false;
            }
            if (!objectEquals(this.getServerReceiptTime(), otherItem.getServerReceiptTime())) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getUserId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSessionTag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getContextMessageId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getTimeZone());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getTransactionId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getMessageType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getInfo());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getImportedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getImportSource());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getProcessedFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getProcessedInfo());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getProcessedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getXmlVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getServerReceiptTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>userId</li>
     * <li>sessionTag</li>
     * <li>contextMessageId</li>
     * <li>time</li>
     * <li>timeZone</li>
     * <li>transactionId</li>
     * <li>messageType</li>
     * <li>info</li>
     * <li>importedTime</li>
     * <li>processedTime</li>
     * <li>processedFlag</li>
     * <li>processedInfo</li>
     * <li>GUID</li>
     * <li>xmlVersion</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        MessageItem otherItem = (MessageItem)obj;

        int value = 0;

        value = objectCompareTo(this.getUserId(), otherItem.getUserId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSessionTag(), otherItem.getSessionTag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getContextMessageId(), otherItem.getContextMessageId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTime(), otherItem.getTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeZone(), otherItem.getTimeZone());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionId(), otherItem.getTransactionId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getMessageType(), otherItem.getMessageType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInfo(), otherItem.getInfo());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProcessedTime(), otherItem.getProcessedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProcessedFlag(), otherItem.getProcessedFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProcessedInfo(), otherItem.getProcessedInfo());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getImportedTime(), otherItem.getImportedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getImportSource(), otherItem.getImportSource());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getGuid(), otherItem.getGuid());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getXmlVersion(), otherItem.getXmlVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getServerReceiptTime(), otherItem.getServerReceiptTime());
        if (value != 0) { return value; }

        return value;
    }
}