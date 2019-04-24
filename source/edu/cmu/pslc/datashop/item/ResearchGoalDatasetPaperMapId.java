/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the external_tool_file_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalDatasetPaperMapId implements Serializable, Comparable {

    /** The research goal id (FK). */
    private Integer researchGoalId;
    /** The dataset id (FK). */
    private Integer datasetId;
    /** The paper id (FK). */
    private Integer paperId;

    /**
     * Constructor.
     */
    public ResearchGoalDatasetPaperMapId() { };

    /**
     * Full constructor.
     * @param goalItem the tool item for this composite key.
     * @param paperItem the file item for this composite key.
     */
    public ResearchGoalDatasetPaperMapId(ResearchGoalItem goalItem,
            DatasetItem datasetItem, PaperItem paperItem) {
        if (goalItem != null) {
            this.researchGoalId = (Integer)goalItem.getId();
        }
        if (datasetItem != null) {
            this.datasetId = (Integer)datasetItem.getId();
        }
        if (paperItem != null) {
            this.paperId = (Integer)paperItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param goalId the goal ID for this composite key.
     * @param datasetId the dataset ID for this composite key.
     * @param paperId the file ID for this composite key.
     */
    public ResearchGoalDatasetPaperMapId(Integer goalId, Integer datasetId, Integer paperId) {
        this.researchGoalId = goalId;
        this.datasetId = datasetId;
        this.paperId = paperId;
    }

    /**
     * Get the goalId.
     * @return the goalId.
     */
    public Integer getResearchGoalId() {
        return researchGoalId;
    }

    /**
     * Set the goalId.
     * @param goalId the goalId to set.
     */
    public void setResearchGoalId(Integer goalId) {
        this.researchGoalId = goalId;
    }

    /**
     * Get the datasetId.
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }

    /**
     * Set the datasetId.
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Get the paperId.
     * @return the paperId.
     */
    public Integer getPaperId() {
        return paperId;
    }

    /**
     * Set the paperId.
     * @param paperId the paperId to set.
     */
    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
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
         buffer.append("researchGoalId").append("='").
                 append(researchGoalId).append("' ");
         buffer.append("datasetId").append("='").
                 append(datasetId).append("' ");
         buffer.append("paperId").append("='").
                 append(paperId).append("' ");
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj.getClass().equals(this.getClass()))) {
            ResearchGoalDatasetPaperMapId otherItem = (ResearchGoalDatasetPaperMapId)obj;
            if (!this.researchGoalId.equals(otherItem.getResearchGoalId())) {
                return false;
            }
            if (!this.datasetId.equals(otherItem.getDatasetId())) {
                return false;
            }
            if (!this.paperId.equals(otherItem.getPaperId())) {
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
        int hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME
            + (researchGoalId != null ? researchGoalId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (datasetId != null ? datasetId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME
            + (paperId != null ? paperId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>research goal id</li>
     * <li>dataset id</li>
     * <li>file id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ResearchGoalDatasetPaperMapId otherItem = (ResearchGoalDatasetPaperMapId)obj;
        if (this.researchGoalId.compareTo(otherItem.researchGoalId) != 0) {
            return this.researchGoalId.compareTo(otherItem.researchGoalId);
        }
        if (this.datasetId.compareTo(otherItem.datasetId) != 0) {
            return this.datasetId.compareTo(otherItem.datasetId);
        }
        if (this.paperId.compareTo(otherItem.paperId) != 0) {
            return this.paperId.compareTo(otherItem.paperId);
        }
        return 0;
    }

}
