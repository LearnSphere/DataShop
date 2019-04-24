/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Project Shareability History item.
 *
 * @author Cindy Tipper
 * @version $Revision: 10513 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-03 12:56:21 -0500 (Mon, 03 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ProjectShareabilityHistoryItem extends Item
        implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this project shareability history . */
    private Integer id;

    /** Project for which this shareability history is related. */
    private ProjectItem project;

    /** User that updated the project status. */
    private UserItem updatedBy;

    /** Date that project status was updated. */
    private Date updatedTime;

    /** String indicating shareability status. */
    private String shareableStatus;

    /** List of ShareableStatus values. */
    private static final List<String> SHAREABLE_STATUS_ENUM = new ArrayList<String>();
    /** ShareableStatus "Not Submitted" value. */
    private static final String SHAREABLE_STATUS_NOT_SUBMITTED = "not_submitted";
    /** ShareableStatus "Waiting for researcher" value. */
    private static final String SHAREABLE_STATUS_WAITING_FOR_RESEARCHER = "waiting_for_researcher";
    /** ShareableStatus "Submitted for review". */
    private static final String SHAREABLE_STATUS_SUBMITTED_FOR_REVIEW = "submitted_for_review";
    /** ShareableStatus "Shareable". */
    private static final String SHAREABLE_STATUS_SHAREABLE = "shareable";
    /** ShareableStatus "Shareable, but cannot be public". */
    public static final String SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC = "shareable_not_public";
    /** ShareableStatus "Not Shareable". */
    private static final String SHAREABLE_STATUS_NOT_SHAREABLE = "not_shareable";

    static {
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_NOT_SUBMITTED);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_WAITING_FOR_RESEARCHER);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_SUBMITTED_FOR_REVIEW);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_SHAREABLE);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_NOT_SHAREABLE);
    }

    /** Default constructor. */
    public ProjectShareabilityHistoryItem() {
        this.shareableStatus = "not_submitted";
    }

    /**
     *  Constructor with id.
     *  @param historyId Database generated unique Id for this history.
     */
    public ProjectShareabilityHistoryItem(Integer historyId) {
        this.id = historyId;
        this.shareableStatus = "not_submitted";
    }

    /**
     * Returns the id.
     * @return the Long id as a Comparable
     */
    public Comparable<Integer> getId() {
        return this.id;
    }

    /**
     * Set historyId.
     * @param historyId Database generated unique Id for this history.
     */
    public void setId(Integer historyId) {
        this.id = historyId;
    }

    /**
     * Get project.
     * @return the project item
     */
    public ProjectItem getProject() {
        return this.project;
    }

    /**
     * Set project.
     * @param project Project for which this terms of use history is associated.
     */
    public void setProject(ProjectItem project) {
        this.project = project;
    }

    /**
     * Get user that updated status.
     * @return UserItem user that updated project status
     */
    public UserItem getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set user that updated status.
     * @param updatedBy user that updated project status
     */
    public void setUpdatedBy(UserItem updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Get time that project status was updated.
     * @return Date time the project status was updated
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Set time that project status was updated.
     * @param updatedTime time that project was updated
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Get shareability status.
     * @return String indicating shareability status
     */
    public String getShareableStatus() {
        return shareableStatus;
    }

    /**
     * Set shareability status.
     * @param shareableStatus String indicating status
     */
    public void setShareableStatus(String shareableStatus) {
        if (SHAREABLE_STATUS_ENUM.contains(shareableStatus)) {
            this.shareableStatus = shareableStatus;
        } else {
            throw new IllegalArgumentException("Invalid ShareableStatus value: " + shareableStatus);
        }
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
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToStringFK("Project", getProject()));
        buffer.append(objectToStringFK("UpdatedBy", getUpdatedBy()));
        buffer.append(objectToString("UpdatedTime", getUpdatedTime()));
        buffer.append(objectToString("ShareableStatus", getShareableStatus()));
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
        if (obj instanceof ProjectShareabilityHistoryItem) {
            ProjectShareabilityHistoryItem otherItem = (ProjectShareabilityHistoryItem)obj;

            if (!objectEqualsFK(this.getProject(), otherItem.getProject())) {
                return false;
            }
            if (!objectEqualsFK(this.getUpdatedBy(), otherItem.getUpdatedBy())) {
                return false;
            }
            if (!objectEquals(this.getUpdatedTime(), otherItem.getUpdatedTime())) {
                return false;
            }
            if (!objectEquals(this.getShareableStatus(), otherItem.getShareableStatus())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getProject());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUpdatedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUpdatedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getShareableStatus());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>project</li>
     * <li>version</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProjectShareabilityHistoryItem otherItem = (ProjectShareabilityHistoryItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getProject(), otherItem.getProject());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUpdatedBy(), otherItem.getUpdatedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUpdatedTime(), otherItem.getUpdatedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getShareableStatus(), otherItem.getShareableStatus());
        if (value != 0) { return value; }

        return value;
    }


}