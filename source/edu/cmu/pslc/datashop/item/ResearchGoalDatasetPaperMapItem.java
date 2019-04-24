/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a mapping between research goal and a paper/dataset.
 *
 * @author alida
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalDatasetPaperMapItem extends Item
    implements Serializable, Comparable {

    /** The class the serves as the primary key (composite-key) for this item. */
    private ResearchGoalDatasetPaperMapId id;

    /** The research goal item associated with this item. */
    private ResearchGoalItem researchGoal;
    /** The dataset item associated with this item. */
    private DatasetItem dataset;
    /** The paper item associated with this item. */
    private PaperItem paper;

    /** The order to show this paper for this research goal. */
    private Integer paperOrder;

    /**
     * Get the id.
     * @return the id.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param id the id.
     */
    public void setId(ResearchGoalDatasetPaperMapId id) {
        this.id = id;
    }

    /**
     * Get the researchGoal.
     * @return the researchGoal
     */
    public ResearchGoalItem getResearchGoal() {
        return researchGoal;
    }

    /**
     * Set the researchGoal.
     * @param researchGoal the researchGoal to set
     */
    protected void setResearchGoal(ResearchGoalItem researchGoal) {
        this.researchGoal = researchGoal;
    }

    /**
     * Public set researchGoal method to update the composite key as well.
     * @param researchGoal Part of the composite key - FK
     */
    public void setResearchGoalExternal(ResearchGoalItem researchGoal) {
        setResearchGoal(researchGoal);
        this.id = new ResearchGoalDatasetPaperMapId(this.researchGoal, this.dataset, this.paper);
    }

    /**
     * Get the dataset.
     * @return the dataset
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set the dataset.
     * @param dataset the dataset to set
     */
    protected void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Public set dataset method to update the composite key as well.
     * @param dataset Part of the composite key - FK
     */
    public void setDatasetExternal(DatasetItem dataset) {
        setDataset(dataset);
        this.id = new ResearchGoalDatasetPaperMapId(this.researchGoal, this.dataset, this.paper);
    }

    /**
     * Get the paper.
     * @return the paper
     */
    public PaperItem getPaper() {
        return paper;
    }

    /**
     * Set the paper.
     * @param paper the paper to set
     */
    protected void setPaper(PaperItem paper) {
        this.paper = paper;
    }

    /**
     * Public set paper method to update the composite key as well.
     * @param paper Part of the composite key - FK
     */
    public void setPaperExternal(PaperItem paper) {
        setPaper(paper);
        this.id = new ResearchGoalDatasetPaperMapId(this.researchGoal, this.dataset, this.paper);
    }

    /**
     * Get the paperOrder.
     * @return the paperOrder
     */
    public Integer getPaperOrder() {
        return paperOrder;
    }

    /**
     * Set the paperOrder.
     * @param paperOrder the paperOrder to set
     */
    public void setPaperOrder(Integer paperOrder) {
        this.paperOrder = paperOrder;
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
        buffer.append(objectToStringFK("ResearchGoal", getResearchGoal()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToStringFK("Paper", getPaper()));
        buffer.append(objectToString("PaperOrder", this.getPaperOrder()));
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
        if (obj instanceof ResearchGoalDatasetPaperMapItem) {
            ResearchGoalDatasetPaperMapItem otherItem = (ResearchGoalDatasetPaperMapItem)obj;

            if (!objectEqualsFK(this.getResearchGoal(), otherItem.getResearchGoal())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEqualsFK(this.getPaper(), otherItem.getPaper())) {
                return false;
            }
            if (!objectEquals(this.getPaperOrder(), otherItem.getPaperOrder())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getResearchGoal());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getPaper());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getPaperOrder());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>researchGoal</li>
     * <li>dataset</li>
     * <li>paper</li>
     * <li>paperOrder</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResearchGoalDatasetPaperMapItem otherItem = (ResearchGoalDatasetPaperMapItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getResearchGoal(), otherItem.getResearchGoal());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getPaper(), otherItem.getPaper());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPaperOrder(), otherItem.getPaperOrder());
        if (value != 0) { return value; }

        return value;
    }
}
