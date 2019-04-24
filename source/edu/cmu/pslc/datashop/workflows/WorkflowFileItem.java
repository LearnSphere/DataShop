/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A workflow file.
 *
 * @author Mike Komisin
 * @version $Revision: 9337 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-06-04 15:19:35 -0400 (Tue, 04 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowFileItem extends Item implements java.io.Serializable, Comparable   {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Database generated unique Id for this file. */
    private Integer id;
    /** Actual name of the file */
    private String fileName;
    /** Local path to the file. */
    private String filePath;
    /** Display title for the file. */
    private String title;
    /** Description of the file. */
    private String description;
    /** The fileType of file. */
    private String fileType;
    /** The timestamp the file was added. */
    private Date addedTime;
    /** Owner of this file. */
    private UserItem owner;
    /** Size of the file in bytes */
    private Long fileSize;
    /** Default constructor. */
    public WorkflowFileItem() {  }

    /**
     * Constructor that takes the fileId.
     * @param fileId the database Id of this file.
     */
    public WorkflowFileItem(Integer fileId) { this.id = fileId; }

    /**
     * Get the problem id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set file Id.
     * @param fileId Database generated unique Id for this file.
     */
    public void setId(Integer fileId) {
        this.id = fileId;
    }

    /**
     * Returns addedTime.
     * @return Returns the addedTime.
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set addedTime.
     * @param addedTime The addedTime to set.
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    /**
     * Returns description.
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description.
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns fileName.
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set fileName.
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns filePath.
     * @return Returns the filePath.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set filePath.
     * @param filePath The filePath to set.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns owner.
     * @return Returns the owner.
     */
    public UserItem getOwner() {
        return owner;
    }

    /**
     * Set owner.
     * @param owner The owner to set.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /**
     * Returns title.
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /** Constant for the maximum length of the title. */
    private static final Integer MAX_TITLE_LENGTH = new Integer(255);

    /**
     * Set title.
     * @param title The title to set.
     */
    public void setTitle(String title) {
        if (title != null) {
            title = title.replaceAll("[\r\n\t ]+",  " ");
            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH);
            }
            this.title = title;
        }
    }

    /**
     * Returns fileType.
     * @return Returns the fileType.
     */
    public String getFileType() {
        return fileType;
    }

    /** Constant for the maximum length of the file/content type. */
    private static final Integer MAX_FILE_TYPE_LENGTH = new Integer(255);

    /**
     * Set fileType.
     * @param type The type of the file.
     */
    public void setFileType(String type) {
        if (type.length() > MAX_FILE_TYPE_LENGTH) {
            type = type.substring(0, MAX_FILE_TYPE_LENGTH);
        }
        this.fileType = type;
    }

    /**
     * Returns fileSize.
     * @return Returns the fileSize.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Set fileSize.
     * @param fileSize The fileSize to set.
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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
         buffer.append(objectToString("FileId", getId()));
         buffer.append(objectToString("FileName", getFileName()));
         buffer.append(objectToString("FilePath", getFilePath()));
         buffer.append(objectToString("Size", getFileSize()));
         buffer.append(objectToString("FileType", getFileType()));
         buffer.append(objectToString("Title", getTitle()));
         buffer.append(objectToString("Description", getDescription()));
         buffer.append(objectToStringFK("Owner", getOwner()));
         buffer.append(objectToString("AddedTime", getAddedTime()));
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
        if (obj instanceof WorkflowFileItem) {
            WorkflowFileItem otherItem = (WorkflowFileItem)obj;

            if (!objectEquals(this.getTitle(), otherItem.getTitle())) {
                return false;
            }
            if (!objectEquals(this.getFileName(), otherItem.getFileName())) {
                return false;
            }
            if (!objectEquals(this.getFilePath(), otherItem.getFilePath())) {
                return false;
            }
            if (!objectEquals(this.getFileType(), otherItem.getFileType())) {
                return false;
            }
            if (!objectEquals(this.getFileSize(), otherItem.getFileSize())) {
                return false;
            }
            if (!objectEquals(this.getAddedTime(), otherItem.getAddedTime())) {
                return false;
            }
            if (!objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }
            if (!objectEquals(this.getDescription(), otherItem.getDescription())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFileName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFilePath());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTitle());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFileType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFileSize());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAddedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>Title</li>
     * <li>File Name</li>
     * <li>File Path</li>
     * <li>File Type</li>
     * <li>File Size</li>
     * <li>Added Time</li>
     * <li>Owner</li>
     * <li>Description</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowFileItem otherItem = (WorkflowFileItem)obj;
        int value = 0;

        value = objectCompareTo(this.getTitle(), otherItem.getTitle());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFileName(), otherItem.getFileName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFilePath(), otherItem.getFilePath());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFileType(), otherItem.getFileType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFileSize(), otherItem.getFileSize());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAddedTime(), otherItem.getAddedTime());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        return value;
    }

    /**
     * Concatenate the file path and the file name for simplicity in the JSP.
     * @param baseDir the base directory where this file can be found
     * @return the file path and file name
     */
    public String getUrl(String baseDir) {
        String url = baseDir;

        //make sure there is a slash between the base directory and the path
        if (filePath.length() > 0 && filePath.charAt(0) != '/') {
            url += "/";
        }

        url += getFilePath();

        //make sure there's one and only one slash between the path and the file name
        if (filePath.charAt(filePath.length() - 1) != '/') {
            url += "/";
        }

        url += getFileName();
        return url;
    }

    /** Constant. */
    private static final int KILO = 1024;
    /** Constant. */
    private static final int MEGA = 1024 * 1024;

    /**
     * Get the file size for display.
     * @return the file size as a displayable string
     */
    public String getDisplayFileSize() {
        return getDisplayFileSize(fileSize);
    }

    /**
     * Static method to get the file size in case we have a size but
     * not a file item.  First such case is the ImportQueueDto.
     * @param size the file size
     * @return a displayable version of the file size as a string
     */
    public static String getDisplayFileSize(Long size) {
        if (size == null) { return ""; }

        NumberFormat numFormat;
        int divisor = 0;
        // Display the file in MB or KB, and choose format.
        if (size.longValue() > MEGA) {
            divisor = MEGA;
            numFormat = new DecimalFormat("###,###.#");
        } else {
            if (size.longValue() < KILO) {
                size = new Long(KILO);
            }
            divisor = KILO;
            numFormat = new DecimalFormat("###,##0");
        }
        // Calculate file size in MB or KB, and remove trailing 0's
        double fileSizeValue = size.longValue() / (double)divisor;
        String fileSizeString = numFormat.format(fileSizeValue);
        fileSizeString += (divisor == KILO ? " KB" : " MB");
        return fileSizeString;
    }

    /**
     * Get the display version of the file name which includes the size in kilobytes
     * and the file type if there is one.
     *
     * As of "Other Analyses" feature (DS1375), the fileType is no longer part of
     * the displayed file name.
     *
     * @return a string representation of the file name, including size and type
     */
    public String getDisplayFileName() {
        return fileName + " (" + getDisplayFileSize() + ")";
    }

    /**
     * Get the full path for this FileItem, given the base directory.
     * @param baseDir the base directory where this file can be found
     * @return path
     */
    public String getFullPathName(String baseDir) {
        return baseDir + File.separator + filePath;
    }

    /**
     * Get the full file name for this FileItem, given the base directory.
     * @param baseDir the base directory where this file can be found
     * @return file name, with path
     */
    public String getFullFileName(String baseDir) {
        return getFullPathName(baseDir) + File.separator + fileName;
    }

    /**
     * Delete the file from the file system associated with this FileItem.
     * @param baseDir the base directory where this file can be found
     * @return true if successful, false otherwise
     */
    public boolean deleteFile(String baseDir) {
        String fullFileName = getFullFileName(baseDir);
        File theFile = new File(fullFileName);
        boolean flag = FileUtils.deleteFile(theFile);
        if (logger.isDebugEnabled()) {
            if (flag) {
                logger.debug("Deleted file: + " + fullFileName);
            } else {
                logger.debug("Failed to delete file: + " + fullFileName);
            }
        }
        return flag;
    }

    /**
     * Comparator object used for sorting.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<WorkflowFileItem> getComparator(SortParameter... sortParameters) {
        return new WorkflowFileItemComparator(sortParameters);
    }

    public enum SortParameter {
        /** Sort by Title ascending. */
        SORT_BY_TITLE_ASC,
        /** Sort by File Name ascending. */
        SORT_BY_FILE_NAME_ASC,
        /** Sort by Uploaded By ascending. */
        SORT_BY_UPLOADED_BY_ASC,
        /** Sort by Date ascending. */
        SORT_BY_DATE_ASC,
        /** Sort by Preferred Citation ascending. */
        SORT_BY_PREF_CITATION_ASC,
        /** Sort by Citation ascending. */
        SORT_BY_CITATION_ASC,
        /** Sort by KC Model ascending. */
        SORT_BY_KC_MODEL_ASC,
        /** Sort by Statistical Model ascending. */
        SORT_BY_STATISTICAL_MODEL_ASC,
        /** Sort by Title descending. */
        SORT_BY_TITLE_DESC,
        /** Sort by File Name descending. */
        SORT_BY_FILE_NAME_DESC,
        /** Sort by Uploaded By descending. */
        SORT_BY_UPLOADED_BY_DESC,
        /** Sort by Date descending. */
        SORT_BY_DATE_DESC,
        /** Sort by Preferred Citation descending. */
        SORT_BY_PREF_CITATION_DESC,
        /** Sort by Citation descending. */
        SORT_BY_CITATION_DESC,
        /** Sort by KC Model descending. */
        SORT_BY_KC_MODEL_DESC,
        /** Sort by Statistical Model descending. */
        SORT_BY_STATISTICAL_MODEL_DESC
    }

    /**
     * A class that supports comparison between two FileItem
     * objects using sort parameters specified in the constructor.
     */
    private static final class WorkflowFileItemComparator
        implements Comparator<WorkflowFileItem> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private WorkflowFileItemComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(WorkflowFileItem o1, WorkflowFileItem o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_TITLE_ASC,
                        SortParameter.SORT_BY_FILE_NAME_ASC
                        };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                case SORT_BY_TITLE_ASC:
                    result = o1.getTitle().compareToIgnoreCase(o2.getTitle());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILE_NAME_ASC:
                    result = o1.getFileName().compareToIgnoreCase(o2.getFileName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPLOADED_BY_ASC:
                    result = o1.getOwner().compareTo(o2.getOwner());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATE_ASC:
                    result = o1.getAddedTime().compareTo(o2.getAddedTime());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_TITLE_DESC:
                    result = o2.getTitle().compareToIgnoreCase(o1.getTitle());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILE_NAME_DESC:
                    result = o2.getFileName().compareToIgnoreCase(o1.getFileName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPLOADED_BY_DESC:
                    result = o2.getOwner().compareTo(o1.getOwner());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATE_DESC:
                    result = o2.getAddedTime().compareTo(o1.getAddedTime());
                    if (result != 0) { return result; }
                    break;
                default:
                    // No-op
                }
            }
            return 0;
        }
    }
}
