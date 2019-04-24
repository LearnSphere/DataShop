/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.Serializable;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * This class represents a composite primary key for the pcConversion_irb_map relation.
 * It is necessary to use this class in order to get the Hibernate interaction to work
 * correctly.
 *
 * @author Cindy Tipper
 * @version $Revision: 10940 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-04-25 12:59:47 -0400 (Fri, 25 Apr 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PcConversionDatasetMapId implements Serializable, Comparable {

    /** The pcConversion id (FK). */
    private Long pcConversionId;
    /** The dataset id (FK). */
    private Integer datasetId;

    /**
     * Constructor.
     */
    public PcConversionDatasetMapId() { };

    /**
     * Full constructor.
     * @param PcConversionItem the PcConversion item for this composite key.
     * @param DatasetItem the Dataset item for this composite key.
     */
    public PcConversionDatasetMapId(PcConversionItem pcConversionItem, DatasetItem datasetItem) {
        if (pcConversionItem != null) {
            this.pcConversionId = (Long)pcConversionItem.getId();
        }
        if (datasetItem != null) {
            this.datasetId = (Integer)datasetItem.getId();
        }
    }

    /**
     * Full constructor.
     * @param pcConversionId the PcConversion ID for this composite key.
     * @param datasetId the Dataset ID for this composite key.
     */
    public PcConversionDatasetMapId(Long pcConversionId, Integer datasetId) {
        this.pcConversionId = pcConversionId;
        this.datasetId = datasetId;
    }

    /**
     * Get the pcConversionId.
     * @return the pcConversionId
     */
    public Long getPcConversionId() {
        return pcConversionId;
    }

    /**
     * Set the pcConversionId.
     * @param pcConversionId the pcConversionId to set
     */
    public void setPcConversionId(Long pcConversionId) {
        this.pcConversionId = pcConversionId;
    }

    /**
     * Get the datasetId.
     * @return the datasetId.
     */
    public Integer getDatasetId() {
        return datasetId;
    }

    /**
     * Set the datasetId.
     * @param datasetId the datasetId to set.
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
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
         buffer.append("pcConversionId").append("='").append(pcConversionId).append("' ");
         buffer.append("datasetId").append("='").append(datasetId).append("' ");
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
            PcConversionDatasetMapId otherItem = (PcConversionDatasetMapId)obj;
            if (!this.pcConversionId.equals(otherItem.getPcConversionId())) {
                return false;
            }
            if (!this.datasetId.equals(otherItem.getDatasetId())) {
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
            + (pcConversionId != null ? pcConversionId.hashCode() : 0);
        hash = hash * UtilConstants.HASH_PRIME + (datasetId != null ? datasetId.hashCode() : 0);
        return hash;
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>pcConversion id</li>
     * <li>dataset id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        PcConversionDatasetMapId otherItem = (PcConversionDatasetMapId)obj;
        if (this.pcConversionId.compareTo(otherItem.pcConversionId) != 0) {
            return this.pcConversionId.compareTo(otherItem.pcConversionId);
        }
        if (this.datasetId.compareTo(otherItem.datasetId) != 0) {
            return this.datasetId.compareTo(otherItem.datasetId);
        }
        return 0;
    }

}
