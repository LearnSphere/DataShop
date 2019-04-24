/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.Date;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Computed metric for a sample.
 * These metrics are only to make other tasks faster and not to store a history.
 * History type information should be stored in the Dataset System Log table
 * using the SystemLogger class.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6317 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-01 11:31:14 -0400 (Fri, 01 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SampleMetricItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this sampleMetric. */
    private Comparable id;
    /** The sample this sampleMetric is associated with. */
    private SampleItem sample;
    /** The skillModel this sampleMetric is associated with. */
    private SkillModelItem skillModel;
    /** The metric being calculated */
    private String metric;
    /** The value of the metric stored as a string */
    private Long value;
    /** The datetime that the metric was last calculated */
    private Date calculatedTime;

    /** Metric: Maximum Skills for a single transaction. */
    public static final String MAX_SKILLS = "Max Skills";
    /** Metric: Maximum Students for a single transaction. */
    public static final String MAX_STUDENTS = "Max Students";
    /** Metric: Maximum Conditions for a single transaction. */
    public static final String MAX_CONDITIONS = "Max Conditions";

    /** Metric: Maximum Selections for a single transaction. */
    public static final String MAX_SELECTIONS = "Max Selections";
    /** Metric: Maximum Actions for a single transaction. */
    public static final String MAX_ACTIONS = "Max Actions";
    /** Metric: Maximum Inputs for a single transaction. */
    public static final String MAX_INPUTS = "Max Inputs";

    /** Metric: Total number of transactions in a sample. */
    public static final String TOTAL_TRANSACTIONS = "Total Transactions";
    /** Metric: Total number of steps in a sample. */
    public static final String TOTAL_STEPS = "Total Steps";
    /** Metric: Total number of steps in a sample. */
    public static final String TOTAL_UNIQUE_STEPS = "Total Unique Steps";
    /** Metric: Total number of steps in a sample. */
    public static final String TOTAL_STUDENTS = "Total Students";
    /** Metric: Total number of student milliseconds in a sample. */
    public static final String TOTAL_STUDENT_MILLISECONDS = "Total Student Milliseconds";
    /** Metric: Max distinct skills across steps. */
    public static final String MAX_DISTINCT_SKILLS_ACROSS_STEPS =
        "Max Distinct Skills Across Steps";

    /** Default constructor. */
    public SampleMetricItem() { }

    /**
     *  Constructor with id.
     *  @param sampleMetricId Database generated unique Id for this sampleMetric.
     */
    public SampleMetricItem(Integer sampleMetricId) {
        this.id = sampleMetricId;
    }

    /** Returns id. @return Returns the Integer id as a Comparable. */
    public Comparable getId() {
        return id;
    }

    /** Set id. @param id The Integer id to set as a Comparable. */
    public void setId(Comparable id) {
        this.id = id;
    }

    /** Returns sample. @return Returns the sample. */
    public SampleItem getSample() {
        return sample;
    }

    /** Set sample. @param sample The sample to set. */
    public void setSample(SampleItem sample) {
        this.sample = sample;
    }

    /** Returns skillModel. @return Returns the skillModel. */
    public SkillModelItem getSkillModel() {
        return skillModel;
    }

    /** Set skillModel. @param skillModel The skillModel to set. */
    public void setSkillModel(SkillModelItem skillModel) {
        this.skillModel = skillModel;
    }

    /** Returns metric. @return Returns the metric. */
    public String getMetric() {
        return metric;
    }

    /** Set metric. @param metric The metric to set. */
    public void setMetric(String metric) {
        this.metric = metric;
    }

    /** Returns value. @return Returns the value. */
    public Long getValue() {
        return value;
    }

    /** Set value. @param value The value to set. */
    public void setValue(Long value) {
        this.value = value;
    }

    /** Returns calculatedTime. @return Returns the calculatedTime. */
    public Date getCalculatedTime() {
        return calculatedTime;
    }

    /** Set calculatedTime. @param calculatedTime The calculatedTime to set. */
    public void setCalculatedTime(Date calculatedTime) {
        this.calculatedTime = calculatedTime;
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
         buffer.append(objectToString("sampleMetricId", getId()));
         buffer.append(objectToString("metric", getMetric()));
         buffer.append(objectToString("value", getValue()));
         buffer.append(objectToString("calculatedTime", getCalculatedTime()));
         buffer.append(objectToStringFK("sampleId", getSample()));
         buffer.append(objectToStringFK("skillModelId", getSkillModel()));
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
        if (obj instanceof SampleMetricItem) {
            SampleMetricItem otherItem = (SampleMetricItem)obj;

            if (!objectEquals(this.getMetric(), otherItem.getMetric())) {
                return false;
            }
            if (!objectEquals(this.getValue(), otherItem.getValue())) {
                return false;
            }
            if (!objectEqualsFK(this.getSample(), otherItem.getSample())) {
                return false;
            }
            if (!objectEqualsFK(this.getSkillModel(), otherItem.getSkillModel())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getMetric());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getValue());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSample());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSkillModel());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
    * Compares two objects using each attribute of this class except
    * the assigned id, if it has an assigned id.
    * <ul>
    *   <li>Sample</li>
    *   <li>Skill Model</li>
    *   <li>metric</li>
    *   <li>value</li>
    * </ul>
    * @param obj the object to compare this to.
    * @return the value 0 if equal; a value less than 0 if it is less than;
    * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        SampleMetricItem otherItem = (SampleMetricItem)obj;
        int intValue = 0;

        intValue = objectCompareToFK(this.getSample(), otherItem.getSample());
        if (intValue != 0) { return intValue; }

        intValue = objectCompareToFK(this.getSkillModel(), otherItem.getSkillModel());
        if (intValue != 0) { return intValue; }

        intValue = objectCompareTo(this.getMetric(), otherItem.getMetric());
        if (intValue != 0) { return intValue; }

        intValue = objectCompareTo(this.getValue(), otherItem.getValue());
        if (intValue != 0) { return intValue; }

        return intValue;
    }
}