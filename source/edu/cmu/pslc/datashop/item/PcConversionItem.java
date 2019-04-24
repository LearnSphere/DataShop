/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Problem Content Conversion Meta-Data.
 *
 * @author Cindy Tipper
 * @version $Revision: 11123 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-05 11:00:50 -0400 (Thu, 05 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */

public class PcConversionItem extends Item implements java.io.Serializable, Comparable  {

    /** Constant for TutorShop Converter conversion tool name. */
    public static final String TUTORSHOP_CONVERTER = "TutorShop Converter";
    /** Constant for OLI Converter conversion tool name. */
    public static final String OLI_CONVERTER = "OLI Converter";

    /** Database generated unique id. */
    private Long id;
    /** Name of the conversion tool. */
    private String conversionTool;
    /** Revision of the conversion tool. */
    private String toolVersion;
    /** DataShop Version Number, for the tool. */
    private String datashopVersion;
    /** Date the conversion tool was run. */
    private Date conversionDate;
    /** Problem content version. */
    private String contentVersion;
    /** Problem content date. */
    private Date contentDate;
    /** Problem content description. */
    private String contentDescription;
    /** Path to converted problem content. */
    private String path;

    /** Format for the date range method, getDateRangeString. */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMM d, yyyy");

    /** Default constructor. */
    public PcConversionItem() {
    }

    /**
     * Constructor with id.
     * @param pcConversionId Database generated unique id
     */
    public PcConversionItem(Long pcConversionId) {
        this.id = pcConversionId;
    }

    /**
     * Get pcConversionId
     * @return Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set pcConversionId.
     * @param pcConversionId Database generated unique id
     */
    public void setId(Long pcConversionId) {
        this.id = pcConversionId;
    }

    /**
     * Get the name of the conversion tool.
     * @return the conversionTool
     */
    public String getConversionTool() {
        return conversionTool;
    }

    /**
     * Set the name of the conversion tool.
     * @param conversionTool the tool name
     */
    public void setConversionTool(String conversionTool) {
        this.conversionTool = conversionTool;
    }

    /**
     * Get the revision number of the tool.
     * @return the toolVersion
     */
    public String getToolVersion() {
        return toolVersion;
    }

    /**
     * Set the revision number of the tool.
     * @param toolVersion the revision
     */
    public void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    /**
     * Get the DataShop version number.
     * @return the datashopVersion
     */
    public String getDatashopVersion() {
        return datashopVersion;
    }

    /**
     * Set the DataShop version number.
     * @param datashopVersion the number
     */
    public void setDatashopVersion(String datashopVersion) {
        this.datashopVersion = datashopVersion;
    }

    /**
     * Get the date the conversion was generated.
     * @return the conversionDate
     */
    public Date getConversionDate() {
        return conversionDate;
    }

    /**
     * Set the date the conversion was generated.
     * @param conversionDate the date
     */
    public void setConversionDate(Date conversionDate) {
        this.conversionDate = conversionDate;
    }

    /**
     * Get the date the conversion was generated, formatted for display.
     * @return the conversionDate
     */
    public String getConversionDateStr() {
        if (conversionDate == null) {
            return "";
        } else {
            return DATE_FORMAT.format(conversionDate);
        }
    }

    /**
     * Get the problem content version.
     * @return the contentVersion
     */
    public String getContentVersion() {
        return contentVersion;
    }

    /**
     * Set the problem content version.
     * @param contentVersion the version
     */
    public void setContentVersion(String contentVersion) {
        this.contentVersion = contentVersion;
    }

    /**
     * Get the date associated with this problem content.
     * @return the contentDate
     */
    public Date getContentDate() {
        return contentDate;
    }

    /**
     * Set the date associated with this problem content.
     * @param contentDate the date
     */
    public void setContentDate(Date contentDate) {
        this.contentDate = contentDate;
    }

    /**
     * Get the date associated with this problem content, formatted for display.
     * @return the contentDate
     */
    public String getContentDateStr() {
        if (contentDate == null) {
            return "";
        } else {
            return DATE_FORMAT.format(contentDate);
        }
    }

    /**
     * Get the problem content description.
     * @return the contentDescription
     */
    public String getContentDescription() {
        return this.contentDescription;
    }

    /**
     * Set the problem content description.
     * @param contentDescription the description
     */
    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    /**
     * Get the path to the problem content.
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Set the path to the problem content.
     * @param path the content path
     */
    public void setPath(String path) {
        this.path = path;
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
        buffer.append(objectToString("ConversionTool",  getConversionTool()));
        buffer.append(objectToString("ToolVersion", getToolVersion()));
        buffer.append(objectToString("DataShopVersion", getDatashopVersion()));
        buffer.append(objectToString("ConversionDate", getConversionDate()));
        buffer.append(objectToString("ContentVersion", getContentVersion()));
        buffer.append(objectToString("ContentDate", getContentDate()));
        buffer.append(objectToString("ContentDescription", getContentDescription()));
        buffer.append(objectToString("Path", getPath()));
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
        if (obj instanceof PcConversionItem) {
            PcConversionItem otherItem = (PcConversionItem)obj;

            if (!objectEquals(this.getConversionTool(), otherItem.getConversionTool())) {
                return false;
            }
            if (!objectEquals(this.getToolVersion(), otherItem.getToolVersion())) {
                return false;
            }
            if (!objectEquals(this.getDatashopVersion(), otherItem.getDatashopVersion())) {
                return false;
            }
            if (!objectEquals(this.getConversionDate(), otherItem.getConversionDate())) {
                return false;
            }
            if (!objectEquals(this.getContentVersion(), otherItem.getContentVersion())) {
                return false;
            }
            if (!objectEquals(this.getContentDate(), otherItem.getContentDate())) {
                    return false;
                }
            if (!objectEquals(this.getContentDescription(), otherItem.getContentDescription())) {
                    return false;
            }
            if (!objectEquals(this.getPath(), otherItem.getPath())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getConversionTool());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getToolVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDatashopVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getConversionDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getContentVersion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getContentDate());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getContentDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPath());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        PcConversionItem otherItem = (PcConversionItem)obj;
        int value = 0;

        value = objectCompareTo(this.getConversionTool(), otherItem.getConversionTool());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getToolVersion(), otherItem.getToolVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopVersion(), otherItem.getDatashopVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getConversionDate(), otherItem.getConversionDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getContentVersion(), otherItem.getContentVersion());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getContentDate(), otherItem.getContentDate());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getContentDescription(), otherItem.getContentDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPath(), otherItem.getPath());
        if (value != 0) { return value; }

        return value;
    }
}