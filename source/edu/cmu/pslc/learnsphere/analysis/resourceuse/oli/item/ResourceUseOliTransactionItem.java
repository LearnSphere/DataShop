/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a single transaction.
 *
 * @author 
 * @version $Revision: 12889 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:56:51 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResourceUseOliTransactionItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this resource use transaction. */
    private Long id;
    /** resource use transactin file associated with this transaction. */
    private ResourceUseOliTransactionFileItem resourceUseOliTransactionFileItem;
    /** Identifier from source*/
    private String guid;
    /** user sess from source*/
    private String userSess;
    /** source*/
    private String source;
    /** The timestamp for when this transaction occurred. */
    private Date transactionTime;
    /** Time zone the timestamp was taken in. */
    private String timeZone;
    /** action */
    private String action;
    /** external_object_id. */
    private String externalObjectId;
    /** container. */
    private String container;
    /** concept_this. */
    private String conceptThis;
    /** concept_req. */
    private String conceptReq;
    /** The timestamp for eastern_time. */
    private Date easternTime;
    /** The timestamp for server_receipt_time. */
    private Date serverReceiptTime;
    /** infoType. */
    private String infoType;
    /** info. */
    private String info;

    /** Default constructor. */
    public ResourceUseOliTransactionItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id for this tutor transaction.
     */
    public ResourceUseOliTransactionItem(Long id) {
        this.id = id;
    }

    /**
     * Get the id.
     * @return id as a Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param transactionId Database generated unique Id for this tutor transaction.
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Get resourceUseItem.
     * @return edu.cmu.learnsphere.analysis.resourceuse.item.ResourceUseItem
     */
    public ResourceUseOliTransactionFileItem getResourceUseOliTransactionFileItem() {
        return this.resourceUseOliTransactionFileItem;
    }

    /**
     * Set resourceUseTransactionFileItem.
     * @param resourceUseOliTransactionFile Resource Use Oli Transaction file associated with this transaction.
     */
    public void setResourceUseOliTransactionFileItem(ResourceUseOliTransactionFileItem resourceUseOliTransactionFileItem) {
        this.resourceUseOliTransactionFileItem = resourceUseOliTransactionFileItem;
    }

    /** Returns GUID. @return Returns the GUID. */
    public String getGuid() {
        return guid;
    }

    /** Set GUID. @param guid The GUID to set. */
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    /** Returns source. @return Returns the source. */
    public String getSource() {
        return source;
    }

    /** Set source. @param source The source to set. */
    public void setSource(String source) {
        this.source = source;
    }
    
    /** Returns userSess. @return Returns the userSess. */
    public String getUserSess() {
        return userSess;
    }

    /** Set userSess. @param userSess The userSess to set. */
    public void setUserSess(String userSess) {
        this.userSess = userSess;
    }
    
    /**
     * Get transactionTime.
     * @return java.util.Date
     */
    public Date getTransactionTime() {
        return this.transactionTime;
    }

    /**
     * Set transactionTime.
     * @param transactionTime The timestamp for when this transaction occurred.
     */
    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    /** Returns timeZone. @return Returns the timeZone. */
    public String getTimeZone() {
        return timeZone;
    }

    /** Set timeZone. @param timeZone The timeZone to set. */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    /** Returns action. @return Returns the action. */
    public String getAction() {
        return action;
    }

    /** Set action. @param action The action to set. */
    public void setAction(String action) {
        this.action = action;
    }
    
    /** Returns externalObjectId. @return Returns the externalObjectId. */
    public String getExternalObjectId() {
        return externalObjectId;
    }

    /** Set externalObjectId. @param externalObjectId The externalObjectId to set. */
    public void setExternalObjectId(String externalObjectId) {
        this.externalObjectId = externalObjectId;
    }
    
    /** Returns container. @return container the container. */
    public String getContainer() {
        return container;
    }

    /** Set container. @param container The container to set. */
    public void setContainer(String container) {
        this.container = container;
    }
    
    /** Returns conceptThis. @return conceptThis the container. */
    public String getConceptThis() {
        return conceptThis;
    }
    
    /** Set conceptThis. @param conceptThis The conceptThis to set. */
    public void setConceptThis(String conceptThis) {
        this.conceptThis = conceptThis;
    }
    
    /** Returns conceptReq. @return conceptReq the conceptReq. */
    public String getConceptReq() {
        return conceptReq;
    }

    /** Set conceptReq. @param conceptReq The conceptReq to set. */
    public void setConceptReq(String conceptReq) {
        this.conceptReq = conceptReq;
    }
    
    /**
     * Set easternTime.
     * @param easternTime.
     */
    public void setEasternTime(Date easternTime) {
        this.easternTime = easternTime;
    }

    /** Returns easternTime. @return Returns the easternTime. */
    public Date getEasternTime() {
        return easternTime;
    }
    
    /**
     * Set serverReceiptTime.
     * @param serverReceiptTime.
     */
    public void setServerReceiptTime(Date serverReceiptTime) {
        this.serverReceiptTime = serverReceiptTime;
    }

    /** Returns serverReceiptTime. @return Returns the serverReceiptTime. */
    public Date getServerReceiptTime() {
        return serverReceiptTime;
    }
    
    /** Returns infoType. @return Returns the infoType. */
    public String getInfoType() {
        return infoType;
    }

    /** Set infoType. @param infoType The infoType to set. */
    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }
    
    /** Returns info. @return Returns the info. */
    public String getInfo() {
        return info;
    }

    /** Set info. @param info The info to set. */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToStringFK("ResourceUseOliTransactionFileItem", getResourceUseOliTransactionFileItem()));
        buffer.append(objectToString("Guid", getGuid()));
        buffer.append(objectToString("UserSess", getUserSess()));
        buffer.append(objectToString("Source", getSource()));
        buffer.append(objectToString("TransactionTime", getTransactionTime()));
        buffer.append(objectToString("TimeZone", getTimeZone()));
        buffer.append(objectToString("Action", getAction()));
        buffer.append(objectToString("externalObjectId", getExternalObjectId()));
        buffer.append(objectToString("Container", getContainer()));
        buffer.append(objectToString("ServerReceiptTime;", getServerReceiptTime()));
        buffer.append(objectToString("InfoType", getInfoType()));
        buffer.append(objectToString("Info", getInfo()));
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
        if (obj instanceof ResourceUseOliTransactionItem) {
            ResourceUseOliTransactionItem otherItem = (ResourceUseOliTransactionItem)obj;

            if (!objectEqualsFK(this.getResourceUseOliTransactionFileItem(), otherItem.getResourceUseOliTransactionFileItem())) {
                return false;
            }
            if (!objectEquals(this.getGuid(), otherItem.getGuid())) {
                return false;
            }
            if (!objectEquals(this.getUserSess(), otherItem.getUserSess())) {
                return false;
            }
            if (!objectEquals(this.getSource(), otherItem.getSource())) {
                return false;
            }
            if (!objectEquals(this.getTransactionTime(), otherItem.getTransactionTime())) {
                 return false;
            }
            if (!objectEquals(this.getTimeZone(), otherItem.getTimeZone())) {
                return false;
            }
            if (!objectEquals(this.getTimeZone(), otherItem.getTimeZone())) {
                 return false;
            }
            if (!objectEquals(this.getAction(), otherItem.getAction())) {
                 return false;
            }
            if (!objectEquals(this.getExternalObjectId(), otherItem.getExternalObjectId())) {
                 return false;
            }
            if (!objectEquals(this.getContainer(), otherItem.getContainer())) {
                return false;
           }
           if (!objectEquals(this.getServerReceiptTime(), otherItem.getServerReceiptTime())) {
                return false;
           }
            if (!objectEquals(this.getInfoType(), otherItem.getInfoType())) {
                 return false;
            }
            if (!objectEquals(this.getInfo(), otherItem.getInfo())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getResourceUseOliTransactionFileItem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getGuid());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUserSess());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSource());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTransactionTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTimeZone());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAction());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getExternalObjectId());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getContainer());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getServerReceiptTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfoType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfo());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResourceUseOliTransactionItem otherItem = (ResourceUseOliTransactionItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getResourceUseOliTransactionFileItem(), otherItem.getResourceUseOliTransactionFileItem());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getGuid(), otherItem.getGuid());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUserSess(), otherItem.getUserSess());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSource(), otherItem.getSource());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTransactionTime(), otherItem.getTransactionTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTimeZone(), otherItem.getTimeZone());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAction(), otherItem.getAction());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getExternalObjectId(), otherItem.getExternalObjectId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getContainer(), otherItem.getContainer());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getServerReceiptTime(), otherItem.getServerReceiptTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInfoType(), otherItem.getInfoType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getInfo(), otherItem.getInfo());
        if (value != 0) { return value; }

        return value;
    }
}
