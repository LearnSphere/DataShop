package edu.cmu.pslc.datashop.servlet.irb;

import java.util.Date;

import edu.cmu.pslc.datashop.item.UserItem;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This is a POJO for an IRB file. It includes the
 * interesting bits of a FileItem, for use by the JSPs.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbFileDto {

    //----- ATTRIBUTES -----

    /** File ID. */
    private Integer fileId;
    /** File Name. */
    private String fileName;
    /** File Size as a long. */
    private Long fileSize;
    /** File Size as a string. */
    private String fileSizeString;
    /** Added time. */
    private Date addedTime;
    /** Owner. */
    private UserItem fileOwner;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param fileId database generated unique id for the file item
     * @param fileName file name
     * @param fileSize size of the file
     * @param fileSizeString size of the file
     * @param addedTime date file was added to DataShop
     * @param owner user that added the file
     */
    public IrbFileDto(Integer fileId, String fileName, Long fileSize,
                      String fileSizeString, Date addedTime, UserItem owner) {
        this.fileId = fileId;
        setFileName(fileName);
        setFileSize(fileSize);
        setFileSizeString(fileSizeString);
        setAddedTime(addedTime);
        setFileOwner(owner);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the file id.
     * @return the file id
     */
    public Integer getFileId() {
        return fileId;
    }
    /**
     * Set the file id.
     * @param fileId the file id to set
     */
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    /**
     * Get the file name.
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the file name.
     * @param fileName the file name to set
     */
    private void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * Get the file size.
     * @return the file size
     */
    public Long getFileSize() {
        return fileSize;
    }
    /**
     * Set the file size.
     * @param fileSize the file size to set
     */
    private void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    /**
     * Get the file size as a string.
     * @return the file size as a string
     */
    public String getFileSizeString() {
        return fileSizeString;
    }
    /**
     * Set the fileSizeString.
     * @param fileSizeString the fileSizeString to set
     */
    public void setFileSizeString(String fileSizeString) {
        this.fileSizeString = fileSizeString;
    }

    /**
     * Get the date this file was added.
     * @return Date the file was added
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set the date this file was added.
     * @param addedTime the Date
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    /** Constant for the format of Added date. */
    private static final FastDateFormat ADDED_DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    /**
     * Get the date this file was added, formatted for the JSP.
     * @return String date file was added
     */
    public String getAddedTimeString() {
        if (addedTime == null) {
            return "";
        } else {
            return ADDED_DATE_FMT.format(addedTime);
        }
    }

    /**
     * Get the user that owns this file.
     * @return UserItem user that owns this file
     */
    public UserItem getFileOwner() {
        return fileOwner;
    }

    /**
     * Set the user that owns this file.
     * @param owner the user that owns this file
     */
    public void setFileOwner(UserItem owner) {
        this.fileOwner = owner;
    }

    /**
     * Get the name of the user that updated this project.
     * @return String user name (first, last)
     */
    public String getFileOwnerString() {
        return fileOwner.getName();
    }
}
