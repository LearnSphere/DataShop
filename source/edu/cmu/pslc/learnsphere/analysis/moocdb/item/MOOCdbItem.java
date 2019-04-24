/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a MOOCdb instance.
 *
 * @author 
 * @version $Revision: 14214 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-07-06 14:48:39 -0400 (Thu, 06 Jul 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MOOCdbItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this MOOCdb. */
    private Long id;
    /** MOOCdb name, usually moocdb_courseName. */
    private String MOOCdbName;
    /** earliest submission timestamp from submission table of MOOCdb*/
    private Date earliestSubmissionTimestamp;
    /** user id of the creator*/
    private String createdBy;
    /** username to access this database*/
    private String username;
    /** password to access this database*/
    private String password;
    /** current progress*/
    private String currentProgress;
    /** hash_mapping_file*/
    private String hashMappingFile;
    /** hash_mapping_file_md5_hash_value*/
    private String hashMappingFileMd5HashValue;
    /** general_file*/
    private String generalFile;
    /** general_file_md5_hash_value*/
    private String generalFileMd5HashValue;
    /** forum_file*/
    private String forumFile;
    /** forum_file_md5_hash_value*/
    private String forumFileMd5HashValue;
    /** moocdb_file*/
    private String moocdbFile;
    /** moocdb_file_md5_hash_value*/
    private String moocdbFileMd5HashValue;
    /** last progress*/
    private String lastProgress;
    /** The timestamp for when last progress is done. */
    private Date lastProgressEndTimestamp;
    /** The timestamp for when this moocdb created. */
    private Date startTimestamp;
    /** The timestamp for when the process of creating this moocdb ended. */
    private Date endTimestamp;
    
    /**all allowed values for progress*/
    public static String PROGRESS_CREATE_DBS = "create_dbs";
    public static String PROGRESS_RESTORE_MOOCDB = "restore_moocdb";
    public static String PROGRESS_TRANSLATE_PREPROCESS_CURATE = "translate-preprocess-curate";
    public static String PROGRESS_DONE = "done";
    public static String DB_READ = "SELECT";
    //public static String DB_READ_WRITE = "SELECT,INSERT,UPDATE,ADD,ALTER,DELETE,CREATE,DROP";
    public static String DB_READ_WRITE = "ALL PRIVILEGES ";
    
    /**column header of the WF MOOCdb output file */
    public static String MOOCdb_PROPERTY_NAME = "MOOCdbName";
    /** Default constructor. */
    public MOOCdbItem() {
    }

    /**
     *  Constructor with id.
     *  @param id Database generated unique Id for this tutor transaction.
     */
    public MOOCdbItem(Long id) {
        this.id = id;
    }

    /**
     * Get the id.
     * @return id as a Long
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set the id.
     * @param Database generated unique Id for this moocdb.
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Get moocdbName.
     * @return String
     */
    public String getMOOCdbName() {
        return this.MOOCdbName;
    }

    /**
     * Set MOOCdbName.
     * @param string name for MOOCdb, usually moocdb_courseName.
     */
    public void setMOOCdbName(String name) {
        this.MOOCdbName = name;
    }

    /** Returns createdBy. @return Returns createdBy. */
    public String getCreatedBy() {
        return createdBy;
    }

    /** Set createdBy. @param createdBy The createdBy to set. */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    /** Returns username. @return Returns username. */
    public String getUsername() {
        return username;
    }

    /** Set username. @param username The username to set. */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /** Returns password. @return Returns password. */
    public String getPassword() {
        return password;
    }

    /** Set password. @param password The password to set. */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /** Returns currentProgress. @return Returns the currentProgress. */
    public String getCurrentProgress() {
        return currentProgress;
    }
    
    /** Set currentProgress. @param currentProgress The currentProgress to set. */
    public void setCurrentProgress(String progress) {
        this.currentProgress = progress;
    }
    
    /** Returns hashMappingFile. @return Returns the hashMappingFile. */
    public String getHashMappingFile() {
        return hashMappingFile;
    }

    /** Set hashMappingFile. @param hashMappingFile The hashMappingFile to set. */
    public void setHashMappingFile(String hashMappingFile) {
        this.hashMappingFile = hashMappingFile;
    }
    
    /** Returns hashMappingFileMd5HashValue. @return Returns the hashMappingFileMd5HashValue. */
    public String getHashMappingFileMd5HashValue() {
        return hashMappingFileMd5HashValue;
    }

    /** Set hashMappingFileMd5HashValue. @param hashMappingFileMd5HashValue The hashMappingFileMd5HashValue to set. */
    public void setHashMappingFileMd5HashValue(String hashMappingFileMd5HashValue) {
        this.hashMappingFileMd5HashValue = hashMappingFileMd5HashValue;
    }
    
    /** Returns generalFile. @return Returns the generalFile. */
    public String getGeneralFile() {
        return generalFile;
    }

    /** Set generalFile. @param generalFile The generalFile to set. */
    public void setGeneralFile(String generalFile) {
        this.generalFile = generalFile;
    }
    
    /** Returns generalFileMd5HashValue. @return Returns the generalFileMd5HashValue. */
    public String getGeneralFileMd5HashValue() {
        return generalFileMd5HashValue;
    }

    /** Set generalFileMd5HashValue. @param generalFileMd5HashValue The generalFileMd5HashValue to set. */
    public void setGeneralFileMd5HashValue(String generalFileMd5HashValue) {
        this.generalFileMd5HashValue = generalFileMd5HashValue;
    }
    
    /** Returns forumFile. @return Returns the forumFile. */
    public String getForumFile() {
        return forumFile;
    }

    /** Set forumFile. @param forumFile The forumFile to set. */
    public void setForumFile(String forumFile) {
        this.forumFile = forumFile;
    }
    
    /** Returns forumFileMd5HashValue. @return Returns the forumFileMd5HashValue. */
    public String getForumFileMd5HashValue() {
        return forumFileMd5HashValue;
    }

    /** Set forumFileMd5HashValue. @param forumFileMd5HashValue The forumFileMd5HashValue to set. */
    public void setForumFileMd5HashValue(String forumFileMd5HashValue) {
        this.forumFileMd5HashValue = forumFileMd5HashValue;
    }
    
    /** Returns moocdbFile. @return Returns the moocdbFile. */
    public String getMoocdbFile() {
        return moocdbFile;
    }

    /** Set moocdbFile. @param moocdbFile The moocdbFile to set. */
    public void setMoocdbFile(String moocdbFile) {
        this.moocdbFile = moocdbFile;
    }
    
    /** Returns moocdbFileMd5HashValue. @return Returns the mocdbFileMd5HashValue. */
    public String getMoocdbFileMd5HashValue() {
        return moocdbFileMd5HashValue;
    }

    /** Set moocdbFileMd5HashValue. @param moocdbFileMd5HashValue The moocdbFileMd5HashValue to set. */
    public void setMoocdbFileMd5HashValue(String moocdbFileMd5HashValue) {
        this.moocdbFileMd5HashValue = moocdbFileMd5HashValue;
    }
    
    /** Returns lastProgress. @return Returns the lastProgress. */
    public String getLastProgress() {
        return lastProgress;
    }

    /** Set lastProgress. @param lastProgress The lastProgress to set. */
    public void setLastProgress(String progress) {
        this.lastProgress = progress;
    }
    
    /** Returns lastProgressEndTimestamp. @return Returns the lastProgressEndTimestamp. */
    public Date getLastProgressEndTimestamp() {
        return lastProgressEndTimestamp;
    }

    /** Set lastProgressEndTimestamp. @param lastProgressEndTimestamp The lastProgressEndTimestamp to set. */
    public void setLastProgressEndTimestamp(Date startTimestamp) {
        this.lastProgressEndTimestamp = startTimestamp;
    }
    
    /** Returns earliestSubmissionTimestamp. @return Returns the earliestSubmissionTimestamp. */
    public Date getEarliestSubmissionTimestamp() {
        return earliestSubmissionTimestamp;
    }

    /** Set startTimestamp. @param startTimestamp The startTimestamp to set. */
    public void setEarliestSubmissionTimestamp(Date startTimestamp) {
        this.earliestSubmissionTimestamp = startTimestamp;
    }
    
    /** Returns startTimestamp. @return Returns the startTimestamp. */
    public Date getStartTimestamp() {
        return startTimestamp;
    }

    /** Set startTimestamp. @param startTimestamp The startTimestamp to set. */
    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }
    
    /** Returns endTimestamp. @return Returns the endTimestamp. */
    public Date getEndTimestamp() {
        return endTimestamp;
    }

    /** Set endTimestamp. @param endTimestamp The endTimestamp to set. */
    public void setEndTimestamp(Date endTimestamp) {
        this.endTimestamp = endTimestamp;
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
        buffer.append(objectToString("MOOCdbName", getMOOCdbName()));
        buffer.append(objectToString("createdBy", getCreatedBy()));
        buffer.append(objectToString("username", getUsername()));
        buffer.append(objectToString("password", getPassword()));
        buffer.append(objectToString("currentProgress", getCurrentProgress()));
        buffer.append(objectToString("lastProgress", getLastProgress()));
        buffer.append(objectToString("hashMappingFile", getHashMappingFile()));
        buffer.append(objectToString("generalFile", getGeneralFile()));
        buffer.append(objectToString("forumFile", getForumFile()));
        buffer.append(objectToString("moocdbFile", getMoocdbFile()));
        buffer.append(objectToString("lastProgressEndTimestamp", getLastProgressEndTimestamp()));
        buffer.append(objectToString("earliestSubmissionTimestamp", getEarliestSubmissionTimestamp()));
        buffer.append(objectToString("startTimestamp", getStartTimestamp()));
        buffer.append(objectToString("endTimestamp", getEndTimestamp()));
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
        if (obj instanceof MOOCdbItem) {
            MOOCdbItem otherItem = (MOOCdbItem)obj;
            if (!objectEquals(this.getMOOCdbName(), otherItem.getMOOCdbName())) {
                return false;
            }
            if (!objectEquals(this.getCreatedBy(), otherItem.getCreatedBy())) {
                return false;
            }
            if (!objectEquals(this.getUsername(), otherItem.getUsername())) {
                    return false;
                }
            if (!objectEquals(this.getPassword(), otherItem.getPassword())) {
                    return false;
                }
            if (!objectEquals(this.getCurrentProgress(), otherItem.getCurrentProgress())) {
                return false;
            }
            if (!objectEquals(this.getHashMappingFile(), otherItem.getHashMappingFile())) {
                    return false;
                }
            if (!objectEquals(this.getGeneralFile(), otherItem.getGeneralFile())) {
                    return false;
                }
            if (!objectEquals(this.getForumFile(), otherItem.getForumFile())) {
                    return false;
                }
            if (!objectEquals(this.getMoocdbFile(), otherItem.getMoocdbFile())) {
                    return false;
                }
            if (!objectEquals(this.getLastProgress(), otherItem.getLastProgress())) {
                    return false;
            }
            if (!objectEquals(this.getLastProgressEndTimestamp(), otherItem.getLastProgressEndTimestamp())) {
                    return false;
            }
            if (!objectEquals(this.getEarliestSubmissionTimestamp(), otherItem.getEarliestSubmissionTimestamp())) {
                    return false;
            }
            if (!objectEquals(this.getStartTimestamp(), otherItem.getStartTimestamp())) {
                 return false;
            }
            if (!objectEquals(this.getEndTimestamp(), otherItem.getEndTimestamp())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getMOOCdbName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreatedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUsername());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPassword());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCurrentProgress());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastProgress());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getHashMappingFile());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getForumFile());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getMoocdbFile());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getGeneralFile());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastProgressEndTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEarliestSubmissionTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStartTimestamp());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEndTimestamp());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        MOOCdbItem otherItem = (MOOCdbItem)obj;
        int value = 0;

        value = objectCompareTo(this.getMOOCdbName(), otherItem.getMOOCdbName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCreatedBy(), otherItem.getCreatedBy());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getUsername(), otherItem.getUsername());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPassword(), otherItem.getPassword());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getLastProgress(), otherItem.getLastProgress());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getHashMappingFile(), otherItem.getHashMappingFile());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getForumFile(), otherItem.getForumFile());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getMoocdbFile(), otherItem.getMoocdbFile());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getGeneralFile(), otherItem.getGeneralFile());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getLastProgressEndTimestamp(), otherItem.getLastProgressEndTimestamp());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getEarliestSubmissionTimestamp(), otherItem.getEarliestSubmissionTimestamp());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStartTimestamp(), otherItem.getStartTimestamp());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEndTimestamp(), otherItem.getEndTimestamp());
        if (value != 0) { return value; }

        return value;
    }
}
