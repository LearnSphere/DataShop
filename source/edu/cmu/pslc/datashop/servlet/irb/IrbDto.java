/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet.irb;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.item.UserItem;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * DTO for the IRB item, including the IRB/File map.
 *
 * @author Cindy Tipper
 * @version $Revision: 10720 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-04 21:47:10 -0500 (Tue, 04 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbDto {

    //----- CONSTANTS -----

    /** Constant for the request attribute. */
    public static final String ATTRIB_NAME = "irbDto";

    /** Constant for the format of Approval and Expiration dates. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");
    /** Constant for the format of Approval and Expiration dates. */
    private static final FastDateFormat TIME_FMT =
            FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss");

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** The title of this IRB. */
    private String title;
    /** The protocol number for this IRB. */
    private String protocolNumber;
    /** The name of the IRB PI. */
    private String piName;
    /** Date this IRB was approved. */
    private Date approvalDate;
    /** Date this IRB expires. */
    private Date expirationDate;
    /** Indicates whether the Expiration Date was set. */
    private Boolean expirationDateNaFlag;
    /** The granting institution for this IRB. */
    private String grantingInstitution;
    /** The notes for this IRB. */
    private String notes;
    /** User that added this IRB to DataShop. */
    private UserItem addedBy;
    /** Date this IRB was added to DataShop. */
    private Date addedTime;
    /** User that last updated this IRB to DataShop. */
    private UserItem updatedBy;
    /** Date this IRB was updated in DataShop. */
    private Date updatedTime;
    /** Number of projects associated with this IRB. */
    private Integer numProjects;
    /** Files associated with this IRB. */
    private List<IrbFileDto> fileList;

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public IrbDto() { }

    /**
     * Constructor.
     * @param id database generated unique id for the tool item
     * @param title the title of the tool
     * @param protocolNumber the IRB's protocol number
     * @param piName the name of the PI
     * @param approvalDate the approval date of the IRB
     * @param expirationDate the expiration date of the IRB
     * @param grantingInstitution the granting institution
     */
    public IrbDto(Integer id, String title, String protocolNumber, String piName,
                  Date approvalDate, Date expirationDate, Boolean expDateNaFlag,
                  String grantingInstitution) {
        this.id = id;
        this.title = title;
        this.protocolNumber = protocolNumber;
        this.piName = piName;
        this.approvalDate = approvalDate;
        this.expirationDate = expirationDate;
        if (expDateNaFlag == null) {
            this.expirationDateNaFlag = false;
        } else {
            this.expirationDateNaFlag = expDateNaFlag;
        }
        this.grantingInstitution = grantingInstitution;
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the id.
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set the id.
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get the title.
     * @return java.lang.String
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title.
     * @param title The title for this link
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Constant for max title length to display. */
    private static final int MAX_TITLE_LENGTH = 100;

    /**
     * Get the title, formatted for the JSP.
     * @return java.lang.String
     */
    public String getTitleString() {
        if (title.length() > MAX_TITLE_LENGTH) {
            String truncatedTitle = title.substring(0, MAX_TITLE_LENGTH);
            return truncatedTitle + "...";
        } else {
            return this.title;
        }
    }

    /**
     * Get the protocol number for this IRB.
     * @return String protocol number
     */
    public String getProtocolNumber() {
        return protocolNumber;
    }

    /**
     * Set the protocol number for this IRB.
     * @param pNumber the protocol number
     */
    public void setProtocolNumber(String pNumber) {
        this.protocolNumber = pNumber;
    }

    /**
     * Get the name of the PI for this IRB.
     * @return String name of the PI
     */
    public String getPiName() {
        return piName;
    }

    /**
     * Set the name of the PI for this IRB.
     * @param pi name of the PI
     */
    public void setPiName(String pi) {
        this.piName = pi;
    }

    /**
     * Get the date the IRB was approved.
     * @return Date approval date
     */
    public Date getApprovalDate() {
        return approvalDate;
    }

    /**
     * Set the date the IRB was approved.
     * @param approvalDate the date approved
     */
    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    /**
     * Get the approval date as a string.
     * @return the approvalDate as a string
     */
    public String getApprovalDateString() {
        if (approvalDate == null) {
            return "";
        } else {
            return DATE_FMT.format(approvalDate);
        }
    }

    /**
     * Get the date the IRB expires.
     * @return Date expiration date
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * Set the date the IRB expires.
     * @param expirationDate the expiration date
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Get the expiration date as a string.
     * @return the expirationDate as a string
     */
    public String getExpirationDateString() {
        if (expirationDate == null) {
            return "";
        } else {
            return DATE_FMT.format(expirationDate);
        }
    }

    /**
     * Gets the expirationDateNaFlag.
     * @return the expirationDateNaFlag
     */
    public Boolean getExpirationDateNaFlag() {
        return expirationDateNaFlag;
    }

    /**
     * Sets the expirationDateNaFlag.
     * @param expirationDateNaFlag the expirationDateNaFlag to set
     */
    public void setExpirationDateNaFlag(Boolean expirationDateNaFlag) {
        this.expirationDateNaFlag = expirationDateNaFlag;
    }

    /**
     * Get the granting institution for this IRB.
     * @return String granting institution
     */
    public String getGrantingInstitution() {
        return grantingInstitution;
    }

    /**
     * Set the granting institution for this IRB.
     * @param inst the granting institution
     */
    public void setGrantingInstitution(String inst) {
        this.grantingInstitution = inst;
    }

    /** Constant for max length to display. */
    private static final int MAX_INST_LENGTH = 50;

    /**
     * Get the granting institution for this IRB, formatted for the JSP.
     * @return String granting institution
     */
    public String getGrantingInstitutionString() {
        if (grantingInstitution == null) { return ""; }

        if (grantingInstitution.length() > MAX_INST_LENGTH) {
            String truncatedInst = grantingInstitution.substring(0, MAX_INST_LENGTH);
            return truncatedInst + "...";
        } else {
            return grantingInstitution;
        }
    }

    /**
     * Get the notes.
     * @return String the notes
     */
    public String getNotes() {
        if (notes == null) { return ""; }

        return notes;
    }

    /**
     * Set the notes.
     * @param notes the notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Get the user that added this IRB to DataShop.
     * @return UserItem user that added this IRB
     */
    public UserItem getAddedBy() {
        return addedBy;
    }

    /**
     * Set the user that added this IRB to DataShop.
     * @param addedBy user that added this IRB
     */
    public void setAddedBy(UserItem addedBy) {
        this.addedBy = addedBy;
    }

    /**
     * Get the name of the user that added this project.
     * @return String user name (first, last)
     */
    public String getAddedByString() {
        return addedBy.getName();
    }

    /**
     * Get the time the IRB was added to DataShop.
     * @return Date addedTime
     */
    public Date getAddedTime() {
        return addedTime;
    }

    /**
     * Set the time the IRB was added to DataShop.
     * @param addedTime the time added
     */
    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
    }

    /**
     * Get the added time as a string.
     * @return the adddedTime as a string
     */
    public String getAddedTimeString() {
        if (addedTime == null) {
            return "";
        } else {
            return DATE_FMT.format(addedTime);
        }
    }

    /**
     * Get the user that last updated this IRB to DataShop.
     * @return UserItem user that last updated this IRB
     */
    public UserItem getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the user that last updated this IRB to DataShop.
     * @param updatedBy user that last updated this IRB
     */
    public void setUpdatedBy(UserItem updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Get the name of the user that updated this project.
     * @return String user name (first, last)
     */
    public String getUpdatedByString() {
        return updatedBy.getName();
    }

    /**
     * Get the time the IRB was updated in DataShop.
     * @return Date updatedTime
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Set the time the IRB was updated inDataShop.
     * @param updatedTime the time updated
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Get the updated date as a string.
     * @return the updtedTime as a string, date only
     */
    public String getUpdatedDateString() {
        if (updatedTime == null) {
            return "-";
        } else {
            return DATE_FMT.format(updatedTime);
        }
    }

    /**
     * Get the updated time as a string.
     * @return the updtedTime as a string, date and time
     */
    public String getUpdatedTimeString() {
        if (updatedTime == null) {
            return "";
        } else {
            return TIME_FMT.format(updatedTime);
        }
    }

    /**
     * Get the number of projects associated with this IRB.
     * @return Integer number
     */
    public Integer getNumProjects() {
        return numProjects;
    }

    /**
     * Set the number of projects associated with this IRB.
     * @param numProjects the number
     */
    public void setNumProjects(Integer numProjects) {
        this.numProjects = numProjects;
    }

    /**
     * Get the number of files associated with this IRB.
     * @return Integer number
     */
    public Integer getNumFiles() {
        if (fileList == null) { return 0; }

        return fileList.size();
    }

    /**
     * Get the list of files for this IRB.
     * @return list of IRB File DTOs
     */
    public List<IrbFileDto> getFileList() {
        return fileList;
    }
    /**
     * Set the list of files for this IRB.
     * @param fileList list of IRB File DTOs
     */
    public void setFileList(List<IrbFileDto> fileList) {
        this.fileList = fileList;
    }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the 'Title' column. */
    public static final String COLUMN_TITLE = "Title";
    /** Constant for the 'IRB Number' column. */
    public static final String COLUMN_NUMBER = "IRB Number";
    /** Constant for the 'PI' column. */
    public static final String COLUMN_PI = "PI";
    /** Constant for the 'Approval Date' column. */
    public static final String COLUMN_APPROVAL = "Approval Date";
    /** Constant for the 'Expiration Date' column. */
    public static final String COLUMN_EXPIRATION = "Expiration Date";
    /** Constant for the 'Granting Institution' column. */
    public static final String COLUMN_INSTITUTION = "Granting Institution";
    /** Constant for the 'Projects' column. */
    public static final String COLUMN_PROJECTS = "Projects";
    /** Constant for the 'Files' column. */
    public static final String COLUMN_FILES = "Files";
    /** Constant for the 'Updated By' column. */
    public static final String COLUMN_UPDATED_BY = "Updated By";
    /** Constant for the 'Updated Date' column. */
    public static final String COLUMN_UPDATED_DATE = "Updated Date";


    /** Sort parameters array constant for sorting by title ascending. */
    private static final IrbDto.SortParameter[] ASC_TITLE_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by title descending. */
    private static final IrbDto.SortParameter[] DESC_TITLE_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_TITLE_DESC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by IRB number ascending. */
    private static final IrbDto.SortParameter[] ASC_NUMBER_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_NUMBER_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by IRB number descending. */
    private static final IrbDto.SortParameter[] DESC_NUMBER_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_NUMBER_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by pi ascending. */
    private static final IrbDto.SortParameter[] ASC_PI_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_PI_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by pi descending. */
    private static final IrbDto.SortParameter[] DESC_PI_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_PI_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by approval date ascending. */
    private static final IrbDto.SortParameter[] ASC_APPROVAL_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC };
    /** Sort parameters array constant for sorting by approval date descending. */
    private static final IrbDto.SortParameter[] DESC_APPROVAL_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_APPROVAL_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC };
    /** Sort parameters array constant for sorting by expiration date ascending. */
    private static final IrbDto.SortParameter[] ASC_EXPIRATION_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_EXPIRATION_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC };
    /** Sort parameters array constant for sorting by expiration date descending. */
    private static final IrbDto.SortParameter[] DESC_EXPIRATION_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_EXPIRATION_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC };
    /** Sort parameters array constant for sorting by granting institution ascending. */
    private static final IrbDto.SortParameter[] ASC_INSTITUTION_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_INSTITUTION_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by granting institution descending. */
    private static final IrbDto.SortParameter[] DESC_INSTITUTION_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_INSTITUTION_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };

    /** Sort parameters array constant for sorting by number of projects ascending. */
    private static final IrbDto.SortParameter[] ASC_PROJECTS_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_PROJECTS_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by number of projects descending. */
    private static final IrbDto.SortParameter[] DESC_PROJECTS_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_PROJECTS_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };

    /** Sort parameters array constant for sorting by number of files ascending. */
    private static final IrbDto.SortParameter[] ASC_FILES_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_FILES_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by number of files descending. */
    private static final IrbDto.SortParameter[] DESC_FILES_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_FILES_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };

    /** Sort parameters array constant for sorting by updated by ascending. */
    private static final IrbDto.SortParameter[] ASC_UPDATED_BY_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_UPDATED_BY_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by updated by descending. */
    private static final IrbDto.SortParameter[] DESC_UPDATED_BY_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_UPDATED_BY_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };

    /** Sort parameters array constant for sorting by updated date ascending. */
    private static final IrbDto.SortParameter[] ASC_UPDATED_DATE_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_UPDATED_DATE_ASC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };
    /** Sort parameters array constant for sorting by updated date descending. */
    private static final IrbDto.SortParameter[] DESC_UPDATED_DATE_PARAMS = {
                         IrbDto.SortParameter.SORT_BY_UPDATED_DATE_DESC,
                         IrbDto.SortParameter.SORT_BY_TITLE_ASC,
                         IrbDto.SortParameter.SORT_BY_APPROVAL_ASC };

    //----- METHODS FOR SORTING -----

    /**
     * Returns the relative path to the appropriate image.
     * @param columnName the column name to get an image for
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return relative path to image for given column
     */
    public static String getSortImage(
            String columnName, String sortByColumn, Boolean isAscending) {

        String imgIcon = "images/trans_spacer.gif";
        if (sortByColumn != null && sortByColumn.equals(columnName)) {
            imgIcon = isAscending ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /**
     * Gets the current sortBy parameters.
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return the current sortBy parameters.
     */
    public static IrbDto.SortParameter[] getSortByParameters(
            String sortByColumn, Boolean isAscending) {

        IrbDto.SortParameter[] sortParams = ASC_TITLE_PARAMS;

        if (sortByColumn.equals(COLUMN_TITLE)) {
            if (isAscending) {
                sortParams = ASC_TITLE_PARAMS;
            } else {
                sortParams = DESC_TITLE_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_NUMBER)) {
            if (isAscending) {
                sortParams = ASC_NUMBER_PARAMS;
            } else {
                sortParams = DESC_NUMBER_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_PI)) {
            if (isAscending) {
                sortParams = ASC_PI_PARAMS;
            } else {
                sortParams = DESC_PI_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_APPROVAL)) {
            if (isAscending) {
                sortParams = ASC_APPROVAL_PARAMS;
            } else {
                sortParams = DESC_APPROVAL_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_EXPIRATION)) {
            if (isAscending) {
                sortParams = ASC_EXPIRATION_PARAMS;
            } else {
                sortParams = DESC_EXPIRATION_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_INSTITUTION)) {
            if (isAscending) {
                sortParams = ASC_INSTITUTION_PARAMS;
            } else {
                sortParams = DESC_INSTITUTION_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_PROJECTS)) {
            if (isAscending) {
                sortParams = ASC_PROJECTS_PARAMS;
            } else {
                sortParams = DESC_PROJECTS_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_FILES)) {
            if (isAscending) {
                sortParams = ASC_FILES_PARAMS;
            } else {
                sortParams = DESC_FILES_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_UPDATED_BY)) {
            if (isAscending) {
                sortParams = ASC_UPDATED_BY_PARAMS;
            } else {
                sortParams = DESC_UPDATED_BY_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_UPDATED_DATE)) {
            if (isAscending) {
                sortParams = ASC_UPDATED_DATE_PARAMS;
            } else {
                sortParams = DESC_UPDATED_DATE_PARAMS;
            }
        }
        return sortParams;
    }

    //----- SORTING COMPARATOR -----

    /**
     * Comparator object used for sorting.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<IrbDto> getComparator(SortParameter... sortParameters) {
        return new IrbDtoComparator(sortParameters);
    }

    public enum SortParameter {
        /** Title Ascending. */
        SORT_BY_TITLE_ASC,
        /** Title Descending. */
        SORT_BY_TITLE_DESC,
        /** IRB Number Ascending. */
        SORT_BY_NUMBER_ASC,
        /** IRB Number Descending. */
        SORT_BY_NUMBER_DESC,
        /** PI Ascending. */
        SORT_BY_PI_ASC,
        /** PI Descending. */
        SORT_BY_PI_DESC,
        /** Approval Date Ascending. */
        SORT_BY_APPROVAL_ASC,
        /** Approval Date Descending. */
        SORT_BY_APPROVAL_DESC,
        /** Expiration Date Ascending. */
        SORT_BY_EXPIRATION_ASC,
        /** Expiration Date Descending. */
        SORT_BY_EXPIRATION_DESC,
        /** Granting Institution Ascending. */
        SORT_BY_INSTITUTION_ASC,
        /** Granting Institution Descending. */
        SORT_BY_INSTITUTION_DESC,
        /** Number of Projects Ascending. */
        SORT_BY_PROJECTS_ASC,
        /** Number of Projects Descending. */
        SORT_BY_PROJECTS_DESC,
        /** Number of Files Ascending. */
        SORT_BY_FILES_ASC,
        /** Number of Files Descending. */
        SORT_BY_FILES_DESC,
        /** Number of Updated By Ascending. */
        SORT_BY_UPDATED_BY_ASC,
        /** Number of Updated By Descending. */
        SORT_BY_UPDATED_BY_DESC,
        /** Number of Updated Date Ascending. */
        SORT_BY_UPDATED_DATE_ASC,
        /** Number of Updated Date Descending. */
        SORT_BY_UPDATED_DATE_DESC
    }

    /**
     * Comparator for IrbDto objects.
     */
    private static final class IrbDtoComparator implements Comparator<IrbDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private IrbDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(IrbDto o1, IrbDto o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_TITLE_ASC,
                        SortParameter.SORT_BY_APPROVAL_ASC
                        };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                //--- Ascending ---
                case SORT_BY_TITLE_ASC:
                    result = o1.getTitle().compareToIgnoreCase(o2.getTitle());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_NUMBER_ASC:
                    result = o1.getProtocolNumber().compareToIgnoreCase(o2.getProtocolNumber());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PI_ASC:
                    result = o1.getPiName().compareToIgnoreCase(o2.getPiName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_APPROVAL_ASC:
                    result = dateComparison(o1.getApprovalDate(), o2.getApprovalDate(), true);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_EXPIRATION_ASC:
                    result = dateComparison(o1.getExpirationDate(), o2.getExpirationDate(), true);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_INSTITUTION_ASC:
                    result = o1.getGrantingInstitution().compareToIgnoreCase(
                            o2.getGrantingInstitution());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PROJECTS_ASC:
                    result = o1.getNumProjects().compareTo(o2.getNumProjects());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILES_ASC:
                    result = o1.getNumFiles().compareTo(o2.getNumFiles());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_BY_ASC:
                    result = o1.getUpdatedBy().compareTo(o2.getUpdatedBy());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_DATE_ASC:
                    result = o1.getUpdatedTimeString().compareTo(o2.getUpdatedTimeString());
                    if (result != 0) { return result; }
                    break;
                //--- Descending ---
                case SORT_BY_TITLE_DESC:
                    result = o2.getTitle().compareToIgnoreCase(o1.getTitle());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_NUMBER_DESC:
                    result = o2.getProtocolNumber().compareToIgnoreCase(o1.getProtocolNumber());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PI_DESC:
                    result = o2.getPiName().compareToIgnoreCase(o1.getPiName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_APPROVAL_DESC:
                    result = dateComparison(o1.getApprovalDate(), o2.getApprovalDate(), false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_EXPIRATION_DESC:
                    result = dateComparison(o1.getExpirationDate(), o2.getExpirationDate(), false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_INSTITUTION_DESC:
                    result = o2.getGrantingInstitution().compareToIgnoreCase(
                            o1.getGrantingInstitution());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PROJECTS_DESC:
                    result = o2.getNumProjects().compareTo(o1.getNumProjects());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILES_DESC:
                    result = o2.getNumFiles().compareTo(o1.getNumFiles());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_BY_DESC:
                    result = o2.getUpdatedBy().compareTo(o1.getUpdatedBy());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_DATE_DESC:
                    result = o2.getUpdatedTimeString().compareTo(o1.getUpdatedTimeString());
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
