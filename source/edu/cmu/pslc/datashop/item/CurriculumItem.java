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

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A collection of datasets that all fall under the same curricula (a.k.a. AlgerbraI).
 * Datasets should be similar enough that comparisons across datasets makes sense.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6363 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-14 09:52:39 -0400 (Thu, 14 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */

public class CurriculumItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this curriculum. */
    private Integer id;
    /** Name of this curriculum as a string. */
    private String curriculumName;
    /** Datasets under this curriculum. */
    private Set datasets;

    /** Default constructor. */
    public CurriculumItem() {
    }

    /**
     *  Constructor with id.
     *  @param curriculumId Database generated unique Id for this curriculum.
     */
    public CurriculumItem(Integer curriculumId) {
        this.id = curriculumId;
    }

    /**
     * Get curriculumId.
     * @return the Integer id as a Comparable
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set curriculumId.
     * @param curriculumId Database generated unique Id for this curriculum.
     */
    public void setId(Integer curriculumId) {
        this.id = curriculumId;
    }
    /**
     * Get curriculumName.
     * @return java.lang.String
     */

    public String getCurriculumName() {
        return this.curriculumName;
    }

    /**
     * Set curriculumName.
     * @param curriculumName Name of this curriculum as a string.
     */
    public void setCurriculumName(String curriculumName) {
        this.curriculumName = curriculumName;
    }

    /**
     * Get datasets.
     * @return java.util.Set
     */
    protected Set getDatasets() {
        if (this.datasets == null) {
            this.datasets = new HashSet();
        }
        return this.datasets;
    }

    /**
     * Public method to get Datasets.
     * @return a list instead of a set
     */
    public List getDatasetsExternal() {
        List sortedList = new ArrayList(getDatasets());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set datasets.
     * @param datasets Datasets under this curriculum.
     */
    protected void setDatasets(Set datasets) {
        this.datasets = datasets;
    }

    /**
     * Add a dataset.
     * @param item to add
     */
    public void addDataset(DatasetItem item) {
        if (!getDatasets().contains(item)) {
            getDatasets().add(item);
            item.setCurriculum(this);
        }
    }

    /**
     * Remove a dataset.
     * @param item to remove
     */
    public void removeDataset(DatasetItem item) {
        if (getDatasets().contains(item)) {
            getDatasets().remove(item);
            item.setCurriculum(null);
        }
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
         buffer.append(objectToString("curriclumId", getId()));
         buffer.append(objectToString("curriclumName", getCurriculumName()));
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
        if (obj instanceof CurriculumItem) {
            CurriculumItem otherItem = (CurriculumItem)obj;

            if (!objectEquals(this.getCurriculumName(), otherItem.getCurriculumName())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCurriculumName());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Name</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        CurriculumItem otherItem = (CurriculumItem)obj;

        int value = 0;

        value = objectCompareTo(this.getCurriculumName(), otherItem.getCurriculumName());
        if (value != 0) { return value; }

        return value;
    }
}