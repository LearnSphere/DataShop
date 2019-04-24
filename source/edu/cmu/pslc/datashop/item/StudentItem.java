/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A single student.
 *
 * @author Benjamin K. Billings
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
    /** Collection of alpha scores associated with this student. */
    private Set alphaScores;
    /** Collection of classes in which this student is enrolled. */
    private Set classes;
    /** Collection of sessions in which this student has participated. */
    private Set sessions;

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
     * Get alphaScores.
     * @return java.util.Set
     */
    protected Set getAlphaScores() {
        if (this.alphaScores == null) {
            this.alphaScores = new HashSet();
        }
        return this.alphaScores;
    }

    /**
     * Public method to get tutorTransactions.
     * @return a list instead of a set
     */
    public List getAlphaScoresExternal() {
        List sortedList = new ArrayList(getAlphaScores());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add an alpha score.
     * @param item alpha score to add
     */
    public void addAlphaScore(AlphaScoreItem item) {
        getAlphaScores().add(item);
        item.setStudent(this);
    }

    // No removeAlphaScore needed.  Too funky.

    /**
     * Set alphaScores.
     * @param alphaScores Collection of alpha scores associated with this student.
     */
    public void setAlphaScores(Set alphaScores) {
        this.alphaScores = alphaScores;
    }

    /**
     * Get set of classes.
     * @return java.util.Set
     */
    protected Set getClasses() {
        if (this.classes == null) {
            this.classes = new HashSet();
        }
        return this.classes;
    }

    /**
     * Public method to get Classes.
     * @return a list instead of a set
     */
    public List getClassesExternal() {
        List sortedList = new ArrayList(getClasses());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a class.
     * @param item class to add
     */
    public void addClass(ClassItem item) {
        if (!getClasses().contains(item)) {
            getClasses().add(item);
            item.addStudent(this);
        }
    }

    /**
     * Remove a class.
     * @param item class to remove
     */
    public void removeClass(ClassItem item) {
        if (getClasses().contains(item)) {
            getClasses().remove(item);
            item.removeStudent(this);
        }
    }

    /**
     * Set classes.
     * @param classes Collection of classes in which this student is enrolled.
     */
    public void setClasses(Set classes) {
        this.classes = classes;
    }

    /**
     * Get set of sessions.
     * @return java.util.Set
     */
    protected Set getSessions() {
        if (this.sessions == null) {
            this.sessions = new HashSet();
        }
        return this.sessions;
    }

    /**
     * Public method to get Sessions.
     * @return a sorted list instead of a set
     */
    public List getSessionsExternal() {
        List sortedList = new ArrayList(getSessions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a session.
     * @param item session to add
     */
    public void addSession(SessionItem item) {
        if (!getSessions().contains(item)) {
            getSessions().add(item);
            item.setStudent(this);
        }
    }

    /**
     * Remove a session.
     * @param item session to remove
     */
    public void removeSession(SessionItem item) {
        if (getSessions().contains(item)) {
            getSessions().remove(item);
        }
    }

    /**
     * Set sessions.
     * @param sessions Collection of sessions which this student has participated in.
     */
    public void setSessions(Set sessions) {
        this.sessions = sessions;
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

            if (!objectEquals(this.getAnonymousUserId(), otherItem.getAnonymousUserId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAnonymousUserId());
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

        value = objectCompareTo(this.getAnonymousUserId(), otherItem.getAnonymousUserId());
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