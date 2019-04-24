/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.mapping.item;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A single student in the mapping_db database.
 *
 * @version $Revision: 14294 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-29 09:42:01 -0400 (Fri, 29 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this student. */
    private Long studentId;

    /** Actual user id of this student as a string.  Must be unique. */
    private String actualUserId;
    /** Anonymized user id for this student (from OLI db). */
    private String anonymousUserId;
    /** Id for corresponding student in analysis_db database. */
    private Long originalId;

    /** Default constructor. */
    public StudentItem() {
    }

    /**
     *  Constructor with id.
     *  @param studentId Database generated unique Id for this student.
     */
    public StudentItem(Long studentId) {
        this.studentId = studentId;
    }

    /**
     * Returns the id.
     * @return the id as a Long
     */
    public Comparable getId() {
        return this.studentId;
    }

    /**
     * Set studentId.
     * @param studentId Database generated unique Id for this student.
     */
    public void setId(Long studentId) {
        this.studentId = studentId;
    }

    /**
     * Get actualUserId.
     * @return java.lang.String
     */
    public String getActualUserId() {
        return this.actualUserId;
    }

    /**
     * Set actualUserId.
     * @param userId actual user id for this student
     */
    public void setActualUserId(String userId) {
        this.actualUserId = userId;
    }

    /**
     * Get anonymousUserId.
     * @return java.lang.String
     */
    public String getAnonymousUserId() {
        return this.anonymousUserId;
    }

    /**
     * Set anonymousUserId.
     * @param userId actual user id for this student
     */
    public void setAnonymousUserId(String userId) {
        this.anonymousUserId = userId;
    }

    /**
     * Returns the original id.
     * @return the id as a Long
     */
    public Long getOriginalId() {
        return this.originalId;
    }

    /**
     * Set original student id.
     * @param studentId Database generated unique Id for this student.
     */
    public void setOriginalId(Long studentId) {
        this.originalId = studentId;
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
         buffer.append(objectToString("ActualUserId", getActualUserId()));
         buffer.append(objectToString("AnonymousUserId", getAnonymousUserId()));
         buffer.append(objectToString("OriginalId", getOriginalId()));
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
        if (obj instanceof StudentItem) {
            StudentItem otherItem = (StudentItem)obj;

            if (!objectEquals(this.getActualUserId(), otherItem.getActualUserId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getActualUserId());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>AnonymousUserId</li>
     * <li>school</li>
     * <li>name</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        StudentItem otherItem = (StudentItem)obj;
        int value = 0;

        value = objectCompareTo(this.getActualUserId(), otherItem.getActualUserId());
        if (value != 0) { return value; }


        return value;
    }

    /** String constant "weirdcmu". */
    private static final String WEIRDCMU_PREFIX = "weirdcmu";
    /** String constant "fire_". */
    private static final String FIRE_PREFIX = "fire_";
    /** Length constant 37. */
    private static final int FIRE_LEN = 37;
    /** String constant "Stu_". */
    private static final String STU_PREFIX = "Stu_";
    /** String constant "Test_". */
    private static final String TEST_PREFIX = "Test_";

    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        return bytes;
    }
    /**
    *  Encrypts the current actualUserId and stores the result in the anonymousUserId.
    */
   public void encryptUserId() {
       String lowerCase = actualUserId.toLowerCase();
       String prefix = STU_PREFIX;
       if (lowerCase.startsWith(WEIRDCMU_PREFIX)) {
           prefix = TEST_PREFIX;
       }
       if (lowerCase.startsWith(FIRE_PREFIX) && lowerCase.length() == FIRE_LEN) {
           anonymousUserId = actualUserId;
       } else {
           String salt = generateSalt().toString();
           anonymousUserId = prefix + DigestUtils.md5Hex(actualUserId + salt);
       }
   }

}