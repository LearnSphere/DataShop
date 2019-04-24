/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A collections of subgoals that combine to make up a problem.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10940 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-04-25 12:59:47 -0400 (Fri, 25 Apr 2014) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ProblemItem extends Item implements java.io.Serializable, Comparable  {

    /** Collection of all allowed items in the tutor flag enumeration. */
    private static final List TUTOR_FLAG_ENUM = new ArrayList();

    //
    // NOTE that the tutor flags are also in the ProblemElement class of the logging library.
    //

    /** Tutor Flag enumeration field value - "tutor". */
    public static final String TUTOR_FLAG_TUTOR = "tutor";
    /** Tutor Flag enumeration field value - "test". */
    public static final String TUTOR_FLAG_TEST = "test";
    /** Tutor Flag enumeration field value - "test". */
    public static final String TUTOR_FLAG_PRE_TEST = "pre-test";
    /** Tutor Flag enumeration field value - "test". */
    public static final String TUTOR_FLAG_POST_TEST = "post-test";
    /** Tutor Flag enumeration field value - "other". */
    public static final String TUTOR_FLAG_OTHER = "other";
    /** Adds each enumerated value to the tutor flag enumeration list. */
    static {
        TUTOR_FLAG_ENUM.add(TUTOR_FLAG_TUTOR);
        TUTOR_FLAG_ENUM.add(TUTOR_FLAG_TEST);
        TUTOR_FLAG_ENUM.add(TUTOR_FLAG_PRE_TEST);
        TUTOR_FLAG_ENUM.add(TUTOR_FLAG_POST_TEST);
        TUTOR_FLAG_ENUM.add(TUTOR_FLAG_OTHER);
    }

    /** Database generated unique Id for this problem. */
    private Long id;
    /** The Dataset Level that this problem exists in. */
    private DatasetLevelItem datasetLevel;
    /** Name of the problem as a string. */
    private String problemName;
    /** Description of the problem as a string. */
    private String problemDescription;
    /** Tutor, Test or Other. */
    private String tutorFlag;
    /** Description of where in between tutored and test this problem is. */
    private String tutorOther;
    /** The PC Problem that this maps to, if set. */
    private PcProblemItem pcProblem;
    /** Collection of Subgoals associated with this problem. */
    private Set subgoals;
    /** Collection of problem events associated with this problem. */
    private Set problemEvents;
    /** Collection of cognitive steps associated with this problem. */
    private Set cognitiveSteps;

    /** Default constructor. */
    public ProblemItem() {
    }

    /**
     *  Constructor with id.
     *  @param problemId Database generated unique Id for this problem.
     */
    public ProblemItem(Long problemId) {
        this.id = problemId;
    }

    /**
     * Get the problem id as a Comparable.
     * @return The Long id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set problem Id.
     * @param problemId Database generated unique Id for this problem.
     */
    public void setId(Long problemId) {
        this.id = problemId;
    }

    /**
     * Get datasetLevel.
     * @return the dataset level item
     */

    public DatasetLevelItem getDatasetLevel() {
        return this.datasetLevel;
    }

    /**
     * Set datasetLevel.
     * @param datasetLevel The Dataset Level that this problem exists in.
     */
    public void setDatasetLevel(DatasetLevelItem datasetLevel) {
        this.datasetLevel = datasetLevel;
    }
    /**
     * Get problemName.
     * @return the name of the problem
     */

    public String getProblemName() {
        return this.problemName;
    }

    /**
     * Set problemName.
     * @param problemName Name of the problem as a string.
     */
    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    /**
     * Get problemDescription.
     * @return the problem description
     */
    public String getProblemDescription() {
        return this.problemDescription;
    }

    /**
     * Set problemDescription.
     * @param problemDescription Description of the problem as a string.
     */
    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    /**
     * Get tutorFlag.
     * @return the tutor flag
     */
    public String getTutorFlag() {
        return tutorFlag;
    }

    /**
     * Checks if the given string is a valid tutor flag.
     * @param tutorFlag the tutor flag
     * @return true if the string is valid
     */
    public static boolean isValidTutorFlag(String tutorFlag) {
        if (TUTOR_FLAG_ENUM.contains(tutorFlag)) {
            return true;
        }
        return false;
    }

    /**
     * Set tutorFlag. Tutor, Test or Other.
     * @param tutorFlag the tutor flag
     */
    public void setTutorFlag(String tutorFlag) {
        if (tutorFlag == null || isValidTutorFlag(tutorFlag)) {
            this.tutorFlag = tutorFlag;
        } else {
            throw new LogException("Problem invalid tutorFlag : " + tutorFlag);
        }
    }

    /**
     * Get tutorOther value.
     * @return the tutor other value
     */
    public String getTutorOther() {
        return tutorOther;
    }

    /**
     * Set tutorOther.
     * @param tutorOther the tutor other value
     */
    public void setTutorOther(String tutorOther) {
        this.tutorOther = tutorOther;
    }

    /**
     * Get PC Problem.
     * @return the PC problem item
     */

    public PcProblemItem getPcProblem() {
        return this.pcProblem;
    }

    /**
     * Set PC Problem.
     * @param pcProblem The PC Problem that this problem is mapped to.
     */
    public void setPcProblem(PcProblemItem pcProblem) {
        this.pcProblem = pcProblem;
    }

    /**
     * Get subgoals.
     * @return the set of subgoal items
     */
    protected Set getSubgoals() {
        if (this.subgoals == null) {
            this.subgoals = new HashSet();
        }
        return this.subgoals;
    }

    /**
     * Public method to get subgoals.
     * @return a list instead of a set
     */
    public List getSubgoalsExternal() {
        List sortedList = new ArrayList(getSubgoals());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set subgoals.
     * @param subgoals Collection of Subgoals associated with this problem.
     */
    protected void setSubgoals(Set subgoals) {
        this.subgoals = subgoals;
    }

    /**
     * Add a Subgoal.
     * @param item to add
     */
    public void addSubgoal(SubgoalItem item) {
        if (!getSubgoals().contains(item)) {
            getSubgoals().add(item);
            item.setProblem(this);
        }
    }

    /**
     * Get problemEvents.
     * @return the set of problem event items
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
        item.setProblem(this);
    }

    /**
     * Remove a problem event.
     * @param item problem event to remove
     */
    public void removeProblemEvent(ProblemEventItem item) {
        getProblemEvents().remove(item);
        item.setProblem(null);
    }

    /**
     * Set problemEvents.
     * @param problemEvents Collection of problemEvents taught at this school.
     */
    public void setProblemEvents(Set problemEvents) {
        this.problemEvents = problemEvents;
    }

    /**
     * Get cognitiveSteps.
     * @return the set of cognitive steps
     */
    protected Set getCognitiveSteps() {
        if (this.cognitiveSteps == null) {
            this.cognitiveSteps = new HashSet();
        }
        return this.cognitiveSteps;
    }

    /**
     * Public method to get CognitiveSteps.
     * @return a list instead of a set
     */
    public List getCognitiveStepsExternal() {
        List sortedList = new ArrayList(getCognitiveSteps());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a problem event.
     * @param item problem event to add
     */
    public void addCognitiveStep(CognitiveStepItem item) {
        getCognitiveSteps().add(item);
        item.setProblem(this);
    }

    /**
     * Remove a problem event.
     * @param item problem event to remove
     */
    public void removeCognitiveStep(CognitiveStepItem item) {
        getCognitiveSteps().remove(item);
        item.setProblem(null);
    }

    /**
     * Set cognitiveSteps.
     * @param cognitiveSteps Collection of cognitive steps associated with this problem.
     */
    public void setCognitiveSteps(Set cognitiveSteps) {
        this.cognitiveSteps = cognitiveSteps;
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
         buffer.append(objectToString("ProblemId", getId()));
         buffer.append(objectToStringFK("DatasetLevelId", getDatasetLevel()));
         buffer.append(objectToString("ProblemName", getProblemName()));
         buffer.append(objectToString("ProblemDescription", getProblemDescription()));
         buffer.append(objectToString("TutorFlag", getTutorFlag()));
         buffer.append(objectToString("TutorOther", getTutorOther()));
         buffer.append(objectToStringFK("PcProblemId", getPcProblem()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
    * Equals function for this class.
    * @param obj Object of any type, should be an Item for equality check
    * @return boolean true if the items are equal, false if not
    */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ProblemItem) {
            ProblemItem otherItem = (ProblemItem)obj;

            if (!Item.objectEquals(this.getProblemName(), otherItem.getProblemName())) {
                return false;
            }

            if (!Item.objectEquals(this.getProblemDescription(),
                    otherItem.getProblemDescription())) {
                return false;
            }

            if (!Item.objectEquals(this.getTutorFlag(), otherItem.getTutorFlag())) {
                return false;
            }

            if (!Item.objectEquals(this.getTutorOther(), otherItem.getTutorOther())) {
                return false;
            }

            if (!Item.objectEqualsFK(this.getDatasetLevel(), otherItem.getDatasetLevel())) {
                return false;
            }

            if (!Item.objectEqualsFK(this.getPcProblem(), otherItem.getPcProblem())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getProblemName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getProblemDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getTutorFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getTutorOther());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDatasetLevel());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getPcProblem());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>name</li>
     * <li>dataset level</li>
     * <li>description</li>
     * <li>pc problem</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProblemItem otherItem = (ProblemItem)obj;
        int value = 0;

        value = objectCompareTo(this.getProblemName(), otherItem.getProblemName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDatasetLevel(), otherItem.getDatasetLevel());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getProblemDescription(),
                otherItem.getProblemDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTutorFlag(), otherItem.getTutorFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getTutorOther(), otherItem.getTutorOther());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getPcProblem(), otherItem.getPcProblem());
        if (value != 0) { return value; }

        return value;
    }

}