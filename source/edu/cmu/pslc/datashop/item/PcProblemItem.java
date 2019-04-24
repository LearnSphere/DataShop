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

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Problem Content Problem.
 *
 * @author Cindy Tipper
 * @version $Revision: 10848 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-28 13:20:06 -0400 (Fri, 28 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */

public class PcProblemItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique id. */
    private Long id;
    /** Problem name, used as the anchor within the generated HTML. */
    private String problemName;
    /** Reference to the PcConversion that this problem belongs to. */
    private PcConversionItem pcConversion;
    /** Reference to the HTML file. */
    private FileItem htmlFile;

    /** Default constructor. */
    public PcProblemItem() {
    }

    /**
     * Constructor with id.
     * @param pcProblemId Database generated unique id
     */
    public PcProblemItem(Long pcProblemId) {
        this.id = pcProblemId;
    }

    /**
     * Get pcProblemId
     * @return Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set pcProblemId.
     * @param pcProblemId Database generated unique id
     */
    public void setId(Long pcProblemId) {
        this.id = pcProblemId;
    }

    /**
     * Get the problem name.
     * @return the problem name
     */
    public String getProblemName() {
        return problemName;
    }

    /**
     * Set the problem name.
     * @param problemName the problem name
     */
    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    /**
     * Get the PcConversionItem for this problem.
     * @return PcConversionItem
     */
    public PcConversionItem getPcConversion() {
        return pcConversion;
    }

    /**
     * Set the PcConversionItem for this problem.
     * @param pcConversion the PcConversionItem
     */
    public void setPcConversion(PcConversionItem pcConversion) {
        this.pcConversion = pcConversion;
    }

    /**
     * Get the FileItem for this problem's HTML.
     * @return FileItem
     */
    public FileItem getHtmlFile() {
        return htmlFile;
    }

    /**
     * Set the FileItem for this problem's HTML.
     * @param htmlFile the FileItem
     */
    public void setHtmlFile(FileItem htmlFile) {
        this.htmlFile = htmlFile;
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
        buffer.append(objectToString("ProblemName",  getProblemName()));
        buffer.append(objectToStringFK("PcConversion", getPcConversion()));
        buffer.append(objectToStringFK("HtmlFile", getHtmlFile()));
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
        if (obj instanceof PcProblemItem) {
            PcProblemItem otherItem = (PcProblemItem)obj;

            if (!objectEquals(this.getProblemName(), otherItem.getProblemName())) {
                return false;
            }
            if (!objectEqualsFK(this.getPcConversion(), otherItem.getPcConversion())) {
                return false;
            }
            if (!objectEqualsFK(this.getHtmlFile(), otherItem.getHtmlFile())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProblemName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getPcConversion());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getHtmlFile());
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
        PcProblemItem otherItem = (PcProblemItem)obj;
        int value = 0;

        value = objectCompareTo(this.getProblemName(), otherItem.getProblemName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getPcConversion(), otherItem.getPcConversion());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getHtmlFile(), otherItem.getHtmlFile());
        if (value != 0) { return value; }

        return value;
    }
}