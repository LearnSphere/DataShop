/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.item;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A numeric report on metric by learnlab.
 *
 * @author Shanwen Yu
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricByLearnlabItem extends Item
            implements java.io.Serializable, Comparable {

    /** Database generated unique Id for this report. */
    private Integer id;
    /** Numbers of files for this report. */
    private Integer files;
    /** Numbers of papers for this report. */
    private Integer papers;
    /** Numbers of datasets for this report. */
    private Integer datasets;
    /** Numbers of students for this report. */
    private Integer students;
    /** Numbers of actions for this report. */
    private Integer actions;
    /** Numbers of hours for this report. */
    private Double hours;

    /** Learnlab which this report uses */
    private LearnlabItem learnlab;
    /**
     * Get the metricByDomain id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set metricByLearnlab Id.
     * @param metricByLearnlabId Database generated unique Id for this report.
     */
    public void setId(Integer metricByLearnlabId) {
        this.id = metricByLearnlabId;
    }
    /**
     * Get number of files.
     * @return number of files.
     */
    public Integer getFiles() {
        return files;
    }

    /**
     * Set files.
     * @param files The files to set.
     */
    public void setFiles(Integer files) {
        this.files = files;
    }

    /**
     * Get number of Papers.
     * @return number of Papers.
     */
    public Integer getPapers() {
        return papers;
    }

    /**
     * Set Papers.
     * @param papers The Papers to set.
     */
    public void setPapers(Integer papers) {
        this.papers = papers;
    }

    /**
     * Get number of datasets.
     * @return number of datasets.
     */
    public Integer getDatasets() {
        return datasets;
    }

    /**
     * Set datasets.
     * @param datasets The datasets to set.
     */
    public void setDatasets(Integer datasets) {
        this.datasets = datasets;
    }
    /**
     * Get number of students.
     * @return number of students.
     */
    public Integer getStudents() {
        return students;
    }

    /**
     * Set students.
     * @param students The students to set.
     */
    public void setStudents(Integer students) {
        this.students = students;
    }
    /**
     * Get number of files.
     * @return number of files.
     */
    public Integer getActions() {
        return actions;
    }

    /**
     * Set actions.
     * @param actions The actions to set.
     */
    public void setActions(Integer actions) {
        this.actions = actions;
    }
    /**
     * Get number of hours.
     * @return number of hours.
     */
    public Double getHours() {
        return hours;
    }

    /**
     * Set hours.
     * @param hours The hours to set.
     */
    public void setHours(Double hours) {
        this.hours = hours;
    }

    /**
     * Get learnlab.
     * @return learnlab The learnlab that associated with this item.
     */
    public LearnlabItem getLearnlab() {
        return this.learnlab;
    }

    /**
     * Set learnlab.
     * @param learnlab The learnlab that associated with this item.
     */
    public void setLearnlab(LearnlabItem learnlab) {
        this.learnlab = learnlab;
    }
    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
     public String toString() {
         return super.toString("metricByLearnlabId", getId(),
                 "files", getFiles(),
                 "papers", getPapers(),
                 "datasets", getDatasets(),
                 "students", getStudents(),
                 "actions", getActions(),
                 "hours", getHours(),
                 "learnlabId", getLearnlab());
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
          if (obj instanceof MetricByLearnlabItem) {
              MetricByLearnlabItem otherItem = (MetricByLearnlabItem)obj;

            if (!Item.objectEquals(this.getFiles(), otherItem.getFiles())) {
                return false;
            }
            if (!Item.objectEquals(this.getPapers(), otherItem.getPapers())) {
                return false;
            }
            if (!Item.objectEquals(this.getDatasets(), otherItem.getDatasets())) {
                return false;
            }
            if (!Item.objectEquals(this.getStudents(), otherItem.getStudents())) {
                return false;
            }
            if (!Item.objectEquals(this.getActions(), otherItem.getActions())) {
                return false;
            }
            if (!Item.objectEquals(this.getHours(), otherItem.getHours())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getLearnlab(), otherItem.getLearnlab())) {
                return false;
            }
            return true;
        }
        return false;
    }

      /**
       * Returns the hash code for this item.
       * @return int the hash code as an int.
       */
       public int hashCode() {
           long hash = UtilConstants.HASH_INITIAL;
           hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getLearnlab());
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFiles());
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPapers());
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDatasets());
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStudents());
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getActions());
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHours());
           return (int)(hash % Integer.MAX_VALUE);
       }

       /**
        * Compares two objects using each attribute of this class except
        * the assigned id, if it has an assigned id.
        * <ul>
        * <li>learnlab</li>
        * <li>files</li>
        * <li>papers</li>
        * <li>datasets</li>
        * <li>students</li>
        * <li>actions</li>
        * <li>hours</li>
        * </ul>
        * @param obj the object to compare this to.
        * @return the value 0 if equal; a value less than 0 if it is less than;
        * a value greater than 0 if it is greater than
        */
       public int compareTo(Object obj) {
           MetricByLearnlabItem otherItem = (MetricByLearnlabItem)obj;
           int value = 0;

           value = objectCompareToFK(this.getLearnlab(), otherItem.getLearnlab());
           if (value != 0) { return value; }
           value = objectCompareTo(this.getFiles(), otherItem.getFiles());
           if (value != 0) { return value; }
           value = objectCompareTo(this.getPapers(), otherItem.getPapers());
           if (value != 0) { return value; }
           value = objectCompareTo(this.getDatasets(), otherItem.getDatasets());
           if (value != 0) { return value; }
           value = objectCompareTo(this.getStudents(), otherItem.getStudents());
           if (value != 0) { return value; }
           value = objectCompareTo(this.getActions(), otherItem.getActions());
           if (value != 0) { return value; }
           value = objectCompareTo(this.getHours(), otherItem.getHours());
           if (value != 0) { return value; }

           return value;
       }
}
