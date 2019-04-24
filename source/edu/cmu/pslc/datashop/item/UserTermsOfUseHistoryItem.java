/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A history data of user's association with a term of use.
 *
 * @author Shanwen Yu
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserTermsOfUseHistoryItem extends Item implements java.io.Serializable, Comparable   {

    /** Database generated unique Id for this record. */
    private Integer id;
    /** Terms of Use item associated with this record. */
    private TermsOfUseVersionItem termsOfUseVersion;
    /** User item associated with this record. */
    private UserItem user;
    /** Date that is associated with this record. */
    private Date date;

    /** Default constructor. */
    public UserTermsOfUseHistoryItem() {  }

    /**
     * Constructor that takes the fileId.
     * @param termsOfUseHistoryId the database Id of this termsOfUseHistoryItem.
     */
    public UserTermsOfUseHistoryItem(Integer termsOfUseHistoryId) { this.id = termsOfUseHistoryId; }

    /**
     * Get the problem id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set file Id.
     * @param userTermsOfUseHistoryId Database generated unique Id for this terms of use history.
     */
    public void setId(Integer userTermsOfUseHistoryId) {
        this.id = userTermsOfUseHistoryId;
    }

    /**
     * Returns termsOfUseVersion.
     * @return Returns the termsOfUseVersion.
     */
    public TermsOfUseVersionItem getTermsOfUseVersion() {
        return termsOfUseVersion;
    }

    /**
     * Set termsOfUseVersion.
     * @param termsOfUseVersion The termsOfUseVersion to set.
     */
    public void setTermsOfUseVersion(TermsOfUseVersionItem termsOfUseVersion) {
        this.termsOfUseVersion = termsOfUseVersion;
    }


    /**
     * Returns user.
     * @return Returns the user.
     */
    public UserItem getUser() {
        return user;
    }

    /**
     * Set user.
     * @param user The user to set.
     */
    public void setUser(UserItem user) {
        this.user = user;
    }

    /**
     * Returns date.
     * @return Returns the date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set date.
     * @param date The date to set.
     */
    public void setDate(Date date) {
        this.date = date;
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
         buffer.append(objectToString("UserTermsOfUseHistoryId", getId()));
         buffer.append(objectToString("Date", getDate()));
         buffer.append(objectToStringFK("TermsOfUseVersion", getTermsOfUseVersion()));
         buffer.append(objectToStringFK("User", getUser()));
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
        if (obj instanceof UserTermsOfUseHistoryItem) {
            UserTermsOfUseHistoryItem otherItem = (UserTermsOfUseHistoryItem)obj;

            if (!objectEquals(this.getDate(), otherItem.getDate())) {
                return false;
            }
            if (!objectEqualsFK(this.getTermsOfUseVersion(), otherItem.getTermsOfUseVersion())) {
                return false;
            }
            if (!objectEqualsFK(this.getUser(), otherItem.getUser())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getTermsOfUseVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getUser());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>Date</li>
     * <li>Terms of Use Version</li>
     * <li>User</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        UserTermsOfUseHistoryItem otherItem = (UserTermsOfUseHistoryItem)obj;
        int value = 0;

        value = objectCompareTo(this.getDate(), otherItem.getDate());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getTermsOfUseVersion(), otherItem.getTermsOfUseVersion());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getUser(), otherItem.getUser());
        if (value != 0) { return value; }

        return value;
    }
}
