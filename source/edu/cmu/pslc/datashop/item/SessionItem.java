/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a Session record of the system.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7789 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2012-07-21 14:48:00 -0400 (Sat, 21 Jul 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SessionItem extends Item
            implements java.io.Serializable, Comparable  {

    /** Completion Code possible value - "QUIT". */
    public static final String COMPLETION_CODE_QUIT = "QUIT";

    /** Completion Code possible value - "DONE". */
    public static final String COMPLETION_CODE_DONE = "DONE";

    /** Completion Code possible value - "TIMEOUT". */
    public static final String COMPLETION_CODE_TIMEOUT = "TIMEOUT";

    /** Completion Code possible value and the default value - "UNKNOWN". */
    public static final String COMPLETION_CODE_UNKNOWN = "UNKNOWN";

    /** The session_id for this item, auto_incremented. */
    private Long sessionId;

    /** The session_tag for this item. */
    private String sessionTag;

    /** The start time  for this session. */
    private Date startTime;

    /** The end time  for this session (optional). */
    private Date endTime;

    /** The end time milliseconds. */
    private Integer endTimeMS;

    /** The start time milliseconds. */
    private Integer startTimeMS;

    /** The completion code for this session. */
    private String completionCode;

    /** Dataset associated with this session. */
    private DatasetItem dataset;

    /** Collection of problem events associated with this session. */
    private Set problemEvents;

    /** Dataset level sequence associated with this session. */
    private DatasetLevelSequenceItem datasetLevelSequence;

    /** School associated with this transaction. */
    private SchoolItem school;

    /** Class associated with this transaction. */
    private ClassItem classItem;

    /** Student associated with this transaction. */
    private StudentItem student;

    /** Default constructor. */
    public SessionItem() {
        this.completionCode = COMPLETION_CODE_UNKNOWN;
    }

    /**
     * Constructor with id.
     * @param sessionId as long, the unique id.
     */
    public SessionItem(Long sessionId) {
        this.sessionId = sessionId;
        this.completionCode = COMPLETION_CODE_UNKNOWN;
    }

    /**
     * Returns SessionId.
     * @return the Long SessionId as a Comparable
     */
    public Comparable getId() {
        return this.sessionId;
    }

    /**
     * Set SessionId.
     * @param sessionId Database generated unique id.
     */
    public void setId(Long sessionId) {
        this.sessionId = sessionId;
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
     * Get start time.
     * @return Date.
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * Set start time.
     * @param startTime as Date.
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Get end time.
     * @return Date.
     */
    public Date getEndTime() {
        return this.endTime;
    }

    /**
     * Set end time.
     * @param endTime as Date.
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /** Returns endTimeMS. @return Returns the endTimeMS. */
    public Integer getEndTimeMS() {
        return endTimeMS;
    }

    /** Set endTimeMS. @param endTimeMS The endTimeMS to set. */
    public void setEndTimeMS(Integer endTimeMS) {
        this.endTimeMS = endTimeMS;
    }

    /** Returns startTimeMS. @return Returns the startTimeMS. */
    public Integer getStartTimeMS() {
        return startTimeMS;
    }

    /** Set startTimeMS. @param startTimeMS The startTimeMS to set. */
    public void setStartTimeMS(Integer startTimeMS) {
        this.startTimeMS = startTimeMS;
    }

    /**
     * Get completionCode.
     * @return String.
     */
    public String getCompletionCode() {
        return this.completionCode;
    }

    /**
     * Set completionCode.
     * @param completionCode as String.
     */
    public void setCompletionCode(String completionCode) {
        this.completionCode = completionCode;
    }

    /**
     * Get dataset.
     * @return the dataset item
     */
    public DatasetItem getDataset() {
        return this.dataset;
    }

    /**
     * Set dataset.
     * @param dataset DatasetItem associated with this item
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Get student.
     * @return the student item
     */
    public StudentItem getStudent() {
        return this.student;
    }

    /**
     * Set student.
     * @param student student item associated with this item
     */
    public void setStudent(StudentItem student) {
        this.student = student;
    }

    /**
     * Get problemEvents.
     * @return the set of problem events for this session
     */
    protected Set getProblemEvents() {
        if (this.problemEvents == null) {
            this.problemEvents = new HashSet();
        }
        return this.problemEvents;
    }

    /**
     * Public method to get ProblemEvents.
     * @return a list instead of a set
     */
    public List getProblemEventsExternal() {
        List sortedList = new ArrayList(getProblemEvents());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a problem event.
     * @param item problem event to add
     */
    public void addProblemEvent(ProblemEventItem item) {
        getProblemEvents().add(item);
        item.setSession(this);
    }

    /**
     * Remove a problem event.
     * @param item problem event to remove
     */
    public void removeProblemEvent(ProblemEventItem item) {
        getProblemEvents().remove(item);
        item.setSession(null);
    }

    /**
     * Set problemEvents.
     * @param problemEvents Collection of problemEvents taught at this school.
     */
    public void setProblemEvents(Set problemEvents) {
        this.problemEvents = problemEvents;
    }

    /**
     * Get datasetLevelSequences.
     * @return the datasetLevelSequences
     */
    public DatasetLevelSequenceItem getDatasetLevelSequence() {
        return datasetLevelSequence;
    }

    /**
     * Set datasetLevelSequence.
     * @param datasetLevelSequence the datasetLevelSequence to set
     */
    public void setDatasetLevelSequence(DatasetLevelSequenceItem datasetLevelSequence) {
        this.datasetLevelSequence = datasetLevelSequence;
    }

    /**
     * Get school.
     * @return the school item
     */
    public SchoolItem getSchool() {
        return this.school;
    }

    /**
     * Set school.
     * @param school School associated with this transaction.
     */
    public void setSchool(SchoolItem school) {
        this.school = school;
    }

    /**
     * Get classItem.
     * @return the class item
     */
    public ClassItem getClassItem() {
        return this.classItem;
    }

    /**
     * Set classItem.
     * @param classItem Class associated with this transaction.
     */
    public void setClassItem(ClassItem classItem) {
        this.classItem = classItem;
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
        buffer.append(objectToString("SessionId", this.getId()));
        buffer.append(objectToString("SessionTag", this.getSessionTag()));
        buffer.append(objectToString("StartTime", this.getStartTime()));
        buffer.append(objectToString("StartTimeMS", this.getStartTimeMS()));
        buffer.append(objectToString("EndTime", this.getEndTime()));
        buffer.append(objectToString("EndTimeMS", this.getEndTimeMS()));
        buffer.append(objectToString("CompletionCode", this.getCompletionCode()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToStringFK("School", getSchool()));
        buffer.append(objectToStringFK("Class", getClassItem()));
        buffer.append(objectToStringFK("DatasetLevelSequence", getDatasetLevelSequence()));
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
        if (obj instanceof SessionItem) {
            SessionItem otherItem = (SessionItem)obj;

            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEquals(this.getSessionTag(), otherItem.getSessionTag())) {
                return false;
            }
            if (!objectEquals(this.getStartTime(), otherItem.getStartTime())) {
                return false;
            }
            if (!objectEquals(this.getStartTimeMS(), otherItem.getStartTimeMS())) {
                return false;
            }
            if (!objectEquals(this.getEndTime(), otherItem.getEndTime())) {
                return false;
            }
            if (!objectEquals(this.getEndTimeMS(), otherItem.getEndTimeMS())) {
                return false;
            }
            if (!objectEquals(this.getCompletionCode(), otherItem.getCompletionCode())) {
                return false;
            }
            if (!objectEqualsFK(this.getSchool(), otherItem.getSchool())) {
                return false;
            }
            if (!objectEqualsFK(this.getClassItem(), otherItem.getClassItem())) {
                return false;
            }
            if (!objectEqualsFK(this.getDatasetLevelSequence(),
                                otherItem.getDatasetLevelSequence())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSessionTag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getStartTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getStartTimeMS());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getEndTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getEndTimeMS());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getCompletionCode());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getSchool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getClassItem());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDatasetLevelSequence());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset</li>
     * <li>sessionTag</li>
     * <li>startTime</li>
     * <li>endTime</li>
     * <li>completionCode</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SessionItem otherItem = (SessionItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSessionTag(), otherItem.getSessionTag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTime(), otherItem.getStartTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTimeMS(), otherItem.getStartTimeMS());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTime(), otherItem.getEndTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTimeMS(), otherItem.getEndTimeMS());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCompletionCode(), otherItem.getCompletionCode());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getSchool(), otherItem.getSchool());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getClassItem(), otherItem.getClassItem());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDatasetLevelSequence(),
                                  otherItem.getDatasetLevelSequence());
        if (value != 0) { return value; }

        return value;
    }
} // end SessionItem.java