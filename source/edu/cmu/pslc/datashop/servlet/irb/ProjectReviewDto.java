/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * DTO for the IRB Review page for a single project. This object contains
 * portions of the ProjectItem necessary for display and passed to the
 * JSP from the servlet.
 *
 * @author Cindy Tipper
 * @version $Revision: 10720 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-04 21:47:10 -0500 (Tue, 04 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectReviewDto {

    //----- CONSTANTS -----

    /** Constant for the request attribute. */
    public static final String ATTRIB_NAME = "projectReviewDto";

    /** Constant for Data Collection Type 'not_specified'. */
    public static final String DATA_COLLECTION_NOT_SPECIFIED = "Not specified";
    /** Constant for Data Collection Type 'not_human_subject'. */
    public static final String DATA_COLLECTION_NOT_HUMAN_SUBJECT =
        "Not human subjects data (not originally collected for research purposes)";
    /** Constant for Data Collection Type 'study_data_consent_req'. */
    public static final String DATA_COLLECTION_CONSENT_REQUIRED =
        "Study data collected under an IRB where consent was required "
      + "(IRB approval letter and consent form required)";
    /** Constant for Data Collection Type 'study_data_consent_not_req'. */
    public static final String DATA_COLLECTION_CONSENT_NOT_REQD =
        "Study data collected under an IRB where consent was not required "
      + "(IRB approval letter required)";

    /** Constant for Subject to DataShop IRB 'not_specified'. */
    public static final String SUBJECT_TO_DS_IRB_NOT_SPECIFIED = "Not specified";
    /** Constant for Subject to DataShop IRB 'yes'. */
    public static final String SUBJECT_TO_DS_IRB_YES =
        "Yes, the data was added to DataShop after April 2012";
    /** Constant for Subject to DataShop IRB 'no'. */
    public static final String SUBJECT_TO_DS_IRB_NO =
        "No, the data was added to DataShop before April 2012";

    /** Date-only date format. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");
    /** Date and Time date format. */
    private static final FastDateFormat TIME_FMT =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    //----- ATTRIBUTES -----

    /** Id of the project. */
    private Integer projectId;
    /** Name of the project. */
    private String projectName;
    /** Data collection type for this project. */
    private String dataCollectionType;
    /** Subject to DS IRB value for this project. */
    private String subjectToDsIrb;
    /** Shareability status of this project. */
    private String shareabilityStatus;
    /** Research Manager's notes. */
    private String researchMgrNotes;
    /** Number of unreviewed datasets in this project. */
    private Integer numUnreviewedDatasets = new Integer(0);
    /** Number of datasets for this project. */
    private Integer numDatasets;
    /** PI/DB/PAs for the project. */
    private List<UserDto> userDtoList = new ArrayList<UserDto>();
    /** Public vs. Private. */
    private boolean publicFlag = false;
    /** Date the project was created. */
    private Date projectCreated;
    /** Date the last dataset was added. */
    private Date datasetLastAdded;
    /** Flag for needs attention. */
    private Boolean needsAttention;
    /** Search string for PI or DP which has the
     * user id, email, first name and last name of each, if set. */
    private String piDpSearchString;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param project the project item
     * @param piItem the PI user item
     * @param dpItem the Data Provider user item
     * @param paList the list of Project Admin items
     * @param publicFlag true if project is public, false otherwise
     */
    public ProjectReviewDto(ProjectItem project,
            UserItem piItem, UserItem dpItem, List<UserItem> paList,
            boolean publicFlag) {
        setProjectId((Integer)project.getId());
        setProjectName(project.getProjectName());
        this.dataCollectionType = project.getDataCollectionType();
        this.subjectToDsIrb = project.getSubjectToDsIrb();
        this.shareabilityStatus = project.getShareableStatus();
        this.publicFlag = publicFlag;
        this.projectCreated = project.getCreatedTime();
        this.datasetLastAdded = project.getDatasetLastAdded();
        this.needsAttention = project.getNeedsAttention();

        if (piItem != null) {
            this.piDpSearchString += piItem.getId() + piItem.getEmail()
                             + piItem.getFirstName() + piItem.getLastName();
        }
        if (dpItem != null) {
            this.piDpSearchString += dpItem.getId() + dpItem.getEmail()
                             + dpItem.getFirstName() + dpItem.getLastName();
        }

        boolean added = false;
        if (piItem != null) {
            this.addUser(new UserDto(piItem, "pi"));
            added = true;
        }
        if (dpItem != null && !added) {
            this.addUser(new UserDto(dpItem, "dp"));
            added = true;
        }
        if (!added) {
            for (UserItem paItem : paList) {
                this.addUser(new UserDto(paItem, "pa"));
            }
        }
    }

    //----- GETTERs and SETTERs -----

    /**
     * Get the project id.
     * @return Integer id
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Set the project id.
     * @param projectId the id
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Get the project name.
     * @return String name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param projectName the name
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Get the DataCollectionType.
     * @return String dataCollectionType
     */
    public String getDataCollectionType() {
        return dataCollectionType;
    }

    /**
     * Set the Data Collection Type.
     * @param dcType String
     */
    public void setDataCollectionType(String dcType) {
        this.dataCollectionType = dcType;
    }

    /**
     * Get the DataCollectionType, formatted for JSP.
     * @return String dataCollectionType
     */
    public String getDataCollectionTypeString() {
        if (dataCollectionType == null) { return ""; }

        if (dataCollectionType.equals(
                ProjectItem.DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT)) {
            return DATA_COLLECTION_NOT_HUMAN_SUBJECT;
        } else if (dataCollectionType.equals(
                ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ)) {
            return DATA_COLLECTION_CONSENT_REQUIRED;
        } else if (dataCollectionType.equals(
                ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ)) {
            return DATA_COLLECTION_CONSENT_NOT_REQD;
        } else {  // not_specified
            return DATA_COLLECTION_NOT_SPECIFIED;
        }
    }

    /**
     * Get the DataCollectionType, formatted for JSP, shorthand.
     * @return String dataCollectionType
     */
    public String getDataCollectionTypeStringShort() {
        if (dataCollectionType == null) { return ""; }

        if (dataCollectionType.equals(
                ProjectItem.DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT)) {
            return "Not human subjects data";
        } else if (dataCollectionType.equals(
                ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ)) {
            return "Study, consent req'd";
        } else if (dataCollectionType.equals(
                ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ)) {
            return "Study, consent not req'd";
        } else {  // not_specified
            return "Not specified";
        }
    }

    /**
     * Get the Shareability Status.
     * @return String shareabilityStatus
     */
    public String getShareabilityStatus() {
        return shareabilityStatus;
    }

    /**
     * Set the Shareability Status.
     * @param shareabilityStatus String
     */
    public void setShareabilityStatus(String shareabilityStatus) {
        this.shareabilityStatus = shareabilityStatus;
    }

    /**
     * Get the Shareability Status, formatted for the JSP.
     * @return String shareabilityStatus
     */
    public String getShareabilityStatusString() {
        return ProjectItem.getShareabilityStatusString(shareabilityStatus);
    }

    /**
     * Get the Subject to DataShop IRB indicator.
     * @return String subjectToDsIrb
     */
    public String getSubjectToDsIrb() {
        return subjectToDsIrb;
    }

    /**
     * Set the Subject to DataShop IRB indicator.
     * @param subjectToDsIrb String
     */
    public void setSubjectToDsIrb(String subjectToDsIrb) {
        this.subjectToDsIrb = subjectToDsIrb;
    }

    /**
     * Get the Subject to DataShop IRB indicator, formatted for the JSP.
     * @return String subjectToDsIrb
     */
    public String getSubjectToDsIrbString() {
        if (subjectToDsIrb == null) { return ""; }

        if (subjectToDsIrb.equals(ProjectItem.SUBJECT_TO_DS_IRB_YES)) {
            return SUBJECT_TO_DS_IRB_YES;
        } else if (subjectToDsIrb.equals(ProjectItem.SUBJECT_TO_DS_IRB_NO)) {
            return SUBJECT_TO_DS_IRB_NO;
        } else {  // not_specified
            return SUBJECT_TO_DS_IRB_NOT_SPECIFIED;
        }
    }

    /**
     * Get the Subject to DataShop IRB indicator, formatted for the JSP, shorthand.
     * @return String subjectToDsIrb
     */
    public String getSubjectToDsIrbStringShort() {
        if (subjectToDsIrb == null) { return ""; }

        if (subjectToDsIrb.equals(ProjectItem.SUBJECT_TO_DS_IRB_YES)) {
            return "Yes";
        } else if (subjectToDsIrb.equals(ProjectItem.SUBJECT_TO_DS_IRB_NO)) {
            return "No";
        } else {  // not_specified
            return "Not specified";
        }
    }

    /**
     * Get the Research Manager's notes.
     * @return String researchMgrNotes
     */
    public String getResearchManagersNotes() {
        if (researchMgrNotes == null) { return ""; }

        return researchMgrNotes;
    }

    /**
     * Set the Research Manager's notes.
     * @param rmNotes the notes
     */
    public void setResearchManagersNotes(String rmNotes) {
        this.researchMgrNotes = rmNotes;
    }

    /**
     * Get the number of unreviewed datasets.
     * @return Integer number
     */
    public Integer getNumUnreviewedDatasets() {
        return numUnreviewedDatasets;
    }

    /**
     * Set the number of unreviewed datasets.
     * @param num the number
     */
    public void setNumUnreviewedDatasets(Integer num) {
        this.numUnreviewedDatasets = num;
    }

    /**
     * Get the number of unreviewed datasets, formatted for the JSP.
     * @return String formatted string
     */
    public String getNumUnreviewedDatasetsString() {
        StringBuffer sb = new StringBuffer();
        sb.append(numUnreviewedDatasets);
        sb.append(" of ");
        sb.append(numDatasets);
        return sb.toString();
    }

    /**
     * Get the total number of datasets.
     * @return Integer number
     */
    public Integer getNumDatasets() {
        return numDatasets;
    }

    /**
     * Set the total number of datasets.
     * @param num the number
     */
    public void setNumDatasets(Integer num) {
        this.numDatasets = num;
    }

    /**
     * Returns the userDtoList.
     * @return the userDtoList
     */
    public List<UserDto> getUserDtoList() {
        return userDtoList;
    }

    /**
     * Adds to the list.
     * @param userDto a DTO to add to the list
     */
    public void addUser(UserDto userDto) {
        this.userDtoList.add(userDto);
    }

    /**
     * Returns the publicFlag.
     * @return the publicFlag
     */
    public boolean isPublicFlag() {
        return publicFlag;
    }

    /**
     * Sets the publicFlag.
     * @param publicFlag the publicFlag to set
     */
    public void setPublicFlag(boolean publicFlag) {
        this.publicFlag = publicFlag;
    }

    /**
     * Returns the projectCreated.
     * @return the projectCreated
     */
    public Date getProjectCreated() {
        return projectCreated;
    }

    /**
     * Returns the projectCreated Date only.
     * @return formatted string of the date only
     */
    public String getProjectCreatedDate() {
        if (projectCreated != null) {
            return DATE_FMT.format(projectCreated);
        }
        return "-";
    }

    /**
     * Returns the projectCreated Date and Time.
     * @return formatted string of the date and time
     */
    public String getProjectCreatedDateTime() {
        if (projectCreated != null) {
            return TIME_FMT.format(projectCreated);
        }
        return "";
    }

    /**
     * Sets the projectCreated.
     * @param projectCreated the projectCreated to set
     */
    public void setProjectCreated(Date projectCreated) {
        this.projectCreated = projectCreated;
    }

    /**
     * Returns the datasetLastAdded.
     * @return the datasetLastAdded
     */
    public Date getDatasetLastAdded() {
        return datasetLastAdded;
    }

    /**
     * Returns the datasetLastAdded Date only.
     * @return formatted string of the date only
     */
    public String getDatasetLastAddedDate() {
        if (datasetLastAdded != null) {
            return DATE_FMT.format(datasetLastAdded);
        }
        return "-";
    }

    /**
     * Returns the datasetLastAdded Date and Time.
     * @return formatted string of the date and time
     */
    public String getDatasetLastAddedDateTime() {
        if (datasetLastAdded != null) {
            return TIME_FMT.format(datasetLastAdded);
        }
        return "";
    }

    /**
     * Sets the datasetLastAdded.
     * @param datasetLastAdded the datasetLastAdded to set
     */
    public void setDatasetLastAdded(Date datasetLastAdded) {
        this.datasetLastAdded = datasetLastAdded;
    }

    /**
     * Returns the needsAttention.
     * @return the needsAttention
     */
    public Boolean getNeedsAttention() {
        return needsAttention;
    }

    /**
     * Sets the needsAttention.
     * @param needsAttention the needsAttention to set
     */
    public void setNeedsAttention(Boolean needsAttention) {
        this.needsAttention = needsAttention;
    }

    /**
     * Gets the piDpSearchString.
     * @return the piDpSearchString
     */
    public String getPiDpSearchString() {
        return piDpSearchString;
    }

    /**
     * Sets the piDpSearchString.
     * @param piDpSearchString the piDpSearchString to set
     */
    public void setPiDpSearchString(String piDpSearchString) {
        this.piDpSearchString = piDpSearchString;
    }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the 'Project Name' column. */
    public static final String COLUMN_NAME = "Project Name";


    /** Constant for the 'Subject to DS IRB' column. */
    public static final String COLUMN_DS_IRB = "Subject To DataShop 2012 IRB";
    /** Constant for the 'Shareability' column. */
    public static final String COLUMN_SHAREABILITY = "Shareability Review Status";
    /** Constant for the 'Data Collection Type' column. */
    public static final String COLUMN_DATA_COLLECTION = "Data Collection Type";
    /** Constant for the 'Unreviewed Datasets' column. */
    public static final String COLUMN_UNREVIEWED = "Unreviewed Datasets";

    /** Constant for the 'Project Created' column. */
    public static final String COLUMN_PROJ_CREATED = "Project Created";
    /** Constant for the 'Dataset Last Added' column. */
    public static final String COLUMN_DS_ADDED = "Dataset Last Added";
    /** Constant for the 'Needs Attention' column. */
    public static final String COLUMN_NEEDS_ATTN = "Needs Attention";


    /** Sort parameters array constant for sorting by name ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_NAME_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by name descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_NAME_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_DESC };

    /** Sort parameters array constant for sorting by subject_to_ds_irb ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_DS_IRB_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_DS_IRB_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by subject_to_ds_irb descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_DS_IRB_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_DS_IRB_DESC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };

    /** Sort parameters array constant for sorting by shareability ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_SHAREABILITY_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_SHAREABILITY_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by shareability descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_SHAREABILITY_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_SHAREABILITY_DESC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };

    /** Sort parameters array constant for sorting by data_collection_type ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_DATA_COLLECTION_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_DATA_COLLECTION_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by data_collection_type descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_DATA_COLLECTION_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_DATA_COLLECTION_DESC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };

    /** Sort parameters array constant for sorting by number of unreviewed datasets ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_UNREVIEWED_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_UNREVIEWED_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by number of unreviewed datasets descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_UNREVIEWED_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_UNREVIEWED_DESC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };

    /** Sort parameters array constant for sorting ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_PROJ_CREATED_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_PROJ_CREATED_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_PROJ_CREATED_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_PROJ_CREATED_DESC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };

    /** Sort parameters array constant for sorting ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_DS_ADDED_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_DS_ADDED_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_DS_ADDED_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_DS_ADDED_DESC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };

    /** Sort parameters array constant for sorting ascending. */
    private static final ProjectReviewDto.SortParameter[] ASC_NEEDS_ATTN_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_NEEDS_ATTN_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_PUBLIC_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting descending. */
    private static final ProjectReviewDto.SortParameter[] DESC_NEEDS_ATTN_PARAMS = {
                         ProjectReviewDto.SortParameter.SORT_BY_NEEDS_ATTN_DESC,
                         ProjectReviewDto.SortParameter.SORT_BY_PUBLIC_ASC,
                         ProjectReviewDto.SortParameter.SORT_BY_NAME_ASC };

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
    public static ProjectReviewDto.SortParameter[] getSortByParameters(
            String sortByColumn, Boolean isAscending) {

        ProjectReviewDto.SortParameter[] sortParams = ASC_NAME_PARAMS;

        if (sortByColumn.equals(COLUMN_NAME)) {
            if (isAscending) {
                sortParams = ASC_NAME_PARAMS;
            } else {
                sortParams = DESC_NAME_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_DS_IRB)) {
            if (isAscending) {
                sortParams = ASC_DS_IRB_PARAMS;
            } else {
                sortParams = DESC_DS_IRB_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_SHAREABILITY)) {
            if (isAscending) {
                sortParams = ASC_SHAREABILITY_PARAMS;
            } else {
                sortParams = DESC_SHAREABILITY_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_DATA_COLLECTION)) {
            if (isAscending) {
                sortParams = ASC_DATA_COLLECTION_PARAMS;
            } else {
                sortParams = DESC_DATA_COLLECTION_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_UNREVIEWED)) {
            if (isAscending) {
                sortParams = ASC_UNREVIEWED_PARAMS;
            } else {
                sortParams = DESC_UNREVIEWED_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_PROJ_CREATED)) {
            if (isAscending) {
                sortParams = ASC_PROJ_CREATED_PARAMS;
            } else {
                sortParams = DESC_PROJ_CREATED_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_DS_ADDED)) {
            if (isAscending) {
                sortParams = ASC_DS_ADDED_PARAMS;
            } else {
                sortParams = DESC_DS_ADDED_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_NEEDS_ATTN)) {
            if (isAscending) {
                sortParams = ASC_NEEDS_ATTN_PARAMS;
            } else {
                sortParams = DESC_NEEDS_ATTN_PARAMS;
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
    public static Comparator<ProjectReviewDto> getComparator(SortParameter... sortParameters) {
        return new ProjectReviewDtoComparator(sortParameters);
    }

    public enum SortParameter {
        /** Name Ascending. */
        SORT_BY_NAME_ASC,
        /** Name Descending. */
        SORT_BY_NAME_DESC,
        /** SubjectToDsIrb Ascending. */
        SORT_BY_DS_IRB_ASC,
        /** SubjectToDsIrb Descending. */
        SORT_BY_DS_IRB_DESC,
        /** Shareability Ascending. */
        SORT_BY_SHAREABILITY_ASC,
        /** Shareability Descending. */
        SORT_BY_SHAREABILITY_DESC,
        /** DataCollectionType Ascending. */
        SORT_BY_DATA_COLLECTION_ASC,
        /** DataCollectionType Descending. */
        SORT_BY_DATA_COLLECTION_DESC,
        /** NumUnreviewed Ascending. */
        SORT_BY_UNREVIEWED_ASC,
        /** NumUnreviewed Descending. */
        SORT_BY_UNREVIEWED_DESC,
        /** Project Created Ascending. */
        SORT_BY_PROJ_CREATED_ASC,
        /** Project Created Descending. */
        SORT_BY_PROJ_CREATED_DESC,
        /** Dataset Last Added Ascending. */
        SORT_BY_DS_ADDED_ASC,
        /** Dataset Last Added Descending. */
        SORT_BY_DS_ADDED_DESC,
        /** Needs Attention Ascending. */
        SORT_BY_NEEDS_ATTN_ASC,
        /** Needs Attention Descending. */
        SORT_BY_NEEDS_ATTN_DESC,
        /** Public Ascending. */
        SORT_BY_PUBLIC_ASC,
        /** Public Descending. */
        SORT_BY_PUBLIC_DESC
    }

    /**
     * Comparator for ProjectReviewDto objects.
     */
    private static final class ProjectReviewDtoComparator implements Comparator<ProjectReviewDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private ProjectReviewDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(ProjectReviewDto o1, ProjectReviewDto o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_NAME_ASC
                        };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                //--- Ascending ---
                case SORT_BY_NAME_ASC:
                    result = o1.getProjectName().compareToIgnoreCase(o2.getProjectName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DS_IRB_ASC:
                    result = o1.getSubjectToDsIrb().compareToIgnoreCase(o2.getSubjectToDsIrb());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_SHAREABILITY_ASC:
                    result = o1.getShareabilityStatus().compareToIgnoreCase(
                            o2.getShareabilityStatus());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATA_COLLECTION_ASC:
                    result = o1.getDataCollectionType().compareToIgnoreCase(
                            o2.getDataCollectionType());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UNREVIEWED_ASC:
                    result = o1.getNumUnreviewedDatasets().compareTo(o2.getNumUnreviewedDatasets());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PROJ_CREATED_ASC:
                    result = dateComparison(o1.getProjectCreated(),
                                            o2.getProjectCreated(), true, false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DS_ADDED_ASC:
                    result = dateComparison(o1.getDatasetLastAdded(),
                                            o2.getDatasetLastAdded(), true, false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_NEEDS_ATTN_ASC:
                    result = o1.getNeedsAttention().compareTo(o2.getNeedsAttention());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PUBLIC_ASC:
                    result = booleanComparison(o2.isPublicFlag(), o1.isPublicFlag(), true);
                    if (result != 0) { return result; }
                    break;
                //--- Descending ---
                case SORT_BY_NAME_DESC:
                    result = o2.getProjectName().compareToIgnoreCase(o1.getProjectName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DS_IRB_DESC:
                    result = o2.getSubjectToDsIrb().compareToIgnoreCase(o1.getSubjectToDsIrb());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_SHAREABILITY_DESC:
                    result = o2.getShareabilityStatus().compareToIgnoreCase(
                            o1.getShareabilityStatus());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATA_COLLECTION_DESC:
                    result = o2.getDataCollectionType().compareToIgnoreCase(
                            o1.getDataCollectionType());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UNREVIEWED_DESC:
                    result = o2.getNumUnreviewedDatasets().compareTo(o1.getNumUnreviewedDatasets());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PROJ_CREATED_DESC:
                    result = dateComparison(o1.getProjectCreated(),
                                            o2.getProjectCreated(), false, false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DS_ADDED_DESC:
                    result = dateComparison(o1.getDatasetLastAdded(),
                                            o2.getDatasetLastAdded(), false, false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_NEEDS_ATTN_DESC:
                    result = o2.getNeedsAttention().compareTo(o1.getNeedsAttention());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_PUBLIC_DESC:
                    result = booleanComparison(o1.isPublicFlag(), o2.isPublicFlag(), false);
                    if (result != 0) { return result; }
                    break;
                default:
                    // No-op
                }
            }
            return 0;
        }
    }

    /** Less than value used for comparator */
    private static final Integer LESS_THAN = -1;
    /** Greater than value used for comparator */
    private static final Integer GREATER_THAN = 1;
    /**
     * Compare two Boolean objects.
     * @param o1 the first object
     * @param o2 the second object
     * @param ascFlag flag indicating sort direction, ascending or descending
     * @return result the comparator value
     */
    public static int booleanComparison(Boolean o1, Boolean o2, boolean ascFlag) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return (ascFlag ? LESS_THAN : GREATER_THAN);
        } else if (o2 == null) {
            return (ascFlag ? GREATER_THAN : LESS_THAN);
        } else {
            if (o1 != o2 && o1) {
                return (ascFlag ? GREATER_THAN : LESS_THAN);
            } else if (o1 != o2 && o2) {
                return (ascFlag ? LESS_THAN : GREATER_THAN);
            } else {
                return 0;
            }
        }
    }
}
