/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * Used to transfer Project Access Request data.
 *
 * @author Mike Komisin
 * @version $Revision: 10810 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-03-17 14:07:52 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectRequestDTO implements java.io.Serializable {
    /** Project Request History list */
    private List<ProjectRequestHistoryDTO> projectRequestHistory;
    /** Project Id */
    private int projectId;
    /** Project name */
    private String projectName;
    /** Full user name */
    private String userName;
    /** User first name */
    private String firstName;
    /** User last name */
    private String lastName;
    /** User Id */
    private String userId;
    /** User email */
    private String email;
    /** Institution */
    private String institution;
    /** Access Request level */
    private String level;
    /** Access Request status */
    private String status;
    /** Last activity date */
    private Date lastActivityDate;
    /** First access date */
    private Date firstAccess;
    /** Last access date */
    private Date lastAccess;
    /** Terms of Use name */
    private String touName;
    /** Terms of Use version agreed to */
    private int touVersionAgreed;
    /** Current Terms of Use version for this project */
    private int touVersionCurrent;
    /** Terms of Use date agreed */
    private Date touDateAgreed;
    /** Access Request pending flag */
    private boolean isPending;
    /** Access Request recent flag */
    private boolean isRecent;
    /** Access Request show history flag */
    private boolean showHistory;
    /** Access Request button visible flag */
    private boolean isButtonVisible;
    /** Access Request ownership state */
    private String ownership;
    /** The project PI full name */
    private String piName;
    /** The project DP full name */
    private String dpName;
    /** The level displayed for projects with public access. */
    public static final String PUBLIC_LEVEL = "Public";
    /** The level displayed for projects that are private. */
    public static final String PRIVATE_LEVEL = "Denied";

    /** Default constructor. */
    public ProjectRequestDTO() {
    }

    /**
     * Returns a list of Project Request History DTO's.
     * @return a list of ProjectRequestHistoryDTO's
     */
    public List<ProjectRequestHistoryDTO> getProjectRequestHistory() {
        return projectRequestHistory;
    }

    /**
     * Sets the Project Request History.
     * @param projectRequestHistory a list of ProjectRequestHistoryDTO's
     */
    public void setProjectRequestHistory(
            List<ProjectRequestHistoryDTO> projectRequestHistory) {
        this.projectRequestHistory = projectRequestHistory;
    }

    /**
     * Returns the project Id.
     * @return the project Id
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * Sets the project Id.
     * @param projectId the project Id
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Returns the project name.
     * @return the project name
     */
    public String getProjectName() {
        if (projectName == null) {
            return "";
        }
        return projectName;
    }

    /**
     * Returns the project name, truncated to n characters.
     * @param n the number of characters to keep
     * @return the truncated project name
     */
    public String getProjectNameTrunc(int n) {
        String dots = "";
        if (projectName == null || projectName.equals("")) {
            return "";
        } else if (n > projectName.length()) {
            n = projectName.length();
        } else {
            dots = "...";
        }

        return projectName.substring(0, n) + dots;
    }

    /**
     * Sets the project name.
     * @param projectName the project name
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the full user name or the user Id if the full name is not defined.
     * @param userName the string describing the full user name, or user Id if no name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the full user name or the user Id if the full name is not defined.
     * @return the full user name or the user Id if the full name is not defined
     */
    public String getUserFullName() {
        if (userName == null) {
            return "";
        }
        return userName;
    }

    /**
     * Returns the first name of the user.
     * @return the first name of the user
     */
    public String getFirstName() {
        if (firstName == null) {
            return "";
        }
        return firstName;
    }

    /**
     * Sets the first name of the user.
     * @param firstName the first name of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the user.
     * @return the last name of the user
     */
    public String getLastName() {
        if (lastName == null) {
            return "";
        }
        return lastName;
    }

    /**
     * Sets the last name of the user.
     * @param lastName the last name of the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the user Id.
     * @return the user Id
     */
    public String getUserId() {
        if (userId == null) {
            return "";
        }
        return userId;
    }

    /**
     * Sets the user Id.
     * @param userId the user Id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the email address.
     * @return the email address
     */
    public String getEmail() {
        if (email == null) {
            return "";
        }
        return email;
    }

    /**
     * Sets the email address.
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the institution.
     * @return the institution
     */
    public String getInstitution() {
        if (institution == null) {
            return "";
        }
        return institution;
    }

    /**
     * Sets the institution.
     * @param institution the institution
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * Returns the Access Request level.
     * @return the Access Request level
     */
    public String getLevel() {
        if (level == null) {
            return "";
        }
        return level;
    }

    /**
     * Returns the Access Request level capitalized.
     * @return the Access Request level capitalized
     */
    public String getLevelFormatted() {
        if (level == null || level.equals("")) {
            return "";
        }
        // Account for public access
        //if (userId.equals(UserItem.DEFAULT_USER)) {
            //return PUBLIC_LEVEL;
        //}

        String capitalized = level.substring(0, 1).toUpperCase();
        if (level.length() > 1) {
            capitalized = capitalized + level.substring(1, level.length()).toLowerCase();
        }
        return capitalized;
    }

    /**
     * Sets the Access Request level.
     * @param level the Access Request level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Returns the Access Request status.
     * @return the Access Request status
     */
    public String getStatus() {
        if (status == null) {
            return "";
        }
        return status;
    }

    /**
     * Returns the Access Request status formatted.
     * @return the Access Request status formatted
     */
    public String getStatusFormatted() {
        if (status == null || status.equals("")) {
            return "";
        }
        String capitalized = status.substring(0, 1).toUpperCase();
        if (status.length() > 1) {
            capitalized = capitalized + status.substring(1, status.length()).toLowerCase();
        }
        return capitalized;
    }

    /**
     * Sets the Access Request status.
     * @param status the Access Request status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the first access date.
     * @return the first access date
     */
    public Date getFirstAccess() {
        return firstAccess;
    }

    /**
     * Sets the first access date.
     * @param firstAccess the first access date
     */
    public void setFirstAccess(Date firstAccess) {
        this.firstAccess = firstAccess;
    }

    /** Constant for the format of Approval and Expiration dates. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    /**
     * Returns the first access date, formatted.
     * @return the first access date
     */
    public String getFirstAccessString() {
        if (firstAccess != null) {
            return DATE_FMT.format(firstAccess);
        } else if (userId.equals(UserItem.DEFAULT_USER)) {
            return "";
        } else {
            return "-";
        }
    }

    /**
     * Returns the last activity date.
     * @return the last activity date
     */
    public Date getLastActivityDate() {
        return lastActivityDate;
    }

    /**
     * Returns the last activity date, formatted.
     * @return the last activity date
     */
    public String getLastActivityString() {
        if (lastActivityDate != null) {
            return DATE_FMT.format(lastActivityDate);
        } else {
            return "";
        }
    }

    /**
     * Sets the last activity date.
     * @param lastActivityDate the last activity date
     */
    public void setLastActivityDate(Date lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    /**
     * Returns the last access date.
     * @return the last access date
     */
    public Date getLastAccess() {
        return lastAccess;
    }

    /**
     * Sets the last access date.
     * @param lastAccess the last access date
     */
    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    /**
     * Returns the last access date, formatted.
     * @return the last access date
     */
    public String getLastAccessString() {
        if (lastAccess != null) {
            return DATE_FMT.format(lastAccess);
        } else if (userId.equals(UserItem.DEFAULT_USER)) {
            return "";
        } else {
            return "-";
        }
    }

    /**
     * Returns the Terms of Use name.
     * @return the Terms of Use name
     */
    public String getTouName() {
        if (touName == null) {
            return "";
        }
        return touName;
    }

    /**
     * Sets the Terms of Use name.
     * @param touName the Terms of Use name
     */
    public void setTouName(String touName) {
        this.touName = touName;
    }

    /**
     * Returns the Terms of Use version agreed to.
     * @return the Terms of Use version agreed to
     */
    public int getTouVersionAgreed() {
        return touVersionAgreed;
    }

    /**
     * Returns the Terms of Use version agreed to.
     * @return the Terms of Use version agreed to
     */
    public String getTouVersionAgreedString() {
        if ((touName == null) || (getUserId().equals(UserItem.DEFAULT_USER))) {
            return "";
        } else {
            if (touVersionAgreed == 0) {
                // Terms exist but haven't yet been agreed to.
                return "-";
            } else {
                return Integer.toString(touVersionAgreed);
            }
        }
    }

    /**
     * Sets the Terms of Use version agreed to.
     * @param touVersionAgreed the Terms of Use version agreed to
     */
    public void setTouVersionAgreed(int touVersionAgreed) {
        this.touVersionAgreed = touVersionAgreed;
    }

    /**
     * Sets the current Terms of Use version for this project.
     * @param touVersionCurrent the current Terms of Use version for this project
     */
    public void setTouVersionCurrent(int touVersionCurrent) {
        this.touVersionCurrent = touVersionCurrent;
    }

    /**
     * Returns the current Terms of Use version for this project.
     * @return the current Terms of Use for this project
     */
    public int getTouVersionCurrent() {
        return touVersionCurrent;
    }

    /**
     * Returns the Terms of Use date agreed.
     * @return the Terms of Use date agreed
     */
    public Date getTouDateAgreed() {
        return touDateAgreed;
    }

    /**
     * Returns the Terms of Use date agreed, as a String.
     * @return String the Terms of Use date agreed
     */
    public String getTouDateAgreedString() {
        if ((touName == null) || (getUserId().equals(UserItem.DEFAULT_USER))) {
            return "";
        } else {
            if (touVersionAgreed == 0) {
                // Terms exist but haven't yet been agreed to.
                return "-";
            } else {
                return DATE_FMT.format(touDateAgreed);
            }
        }
    }

    /**
     * Sets the Terms of Use date agreed.
     * @param touDateAgreed the Terms of Use date agreed
     */
    public void setTouDateAgreed(Date touDateAgreed) {
        this.touDateAgreed = touDateAgreed;
    }

    /**
     * Returns the Access Request pending flag.
     * @return the Access Request pending flag
     */
    public boolean isPending() {
        return isPending;
    }

    /**
     * Sets the Access Request pending flag.
     * @param isPending the Access Request pending flag
     */
    public void setPending(boolean isPending) {
        this.isPending = isPending;
    }

    /**
     * Returns the Access Request recent flag.
     * @return the Access Request recent flag
     */
    public boolean isRecent() {
        return isRecent;
    }

    /**
     * Sets the Access Request recent flag.
     * @param isRecent the Access Request recent flag
     */
    public void setRecent(boolean isRecent) {
        this.isRecent = isRecent;
    }

    /**
     * Returns the Access Request show history flag.
     * @return the Access Request show history flag
     */
    public boolean isShowHistory() {
        return showHistory;
    }

    /**
     * Sets the Access Request button visible flag.
     * @param isButtonVisible the Access Request button visible flag
     */
    public void setButtonVisible(boolean isButtonVisible) {
        this.isButtonVisible = isButtonVisible;
    }

    /**
     * Returns the Access Request button visible flag.
     * @return the Access Request button visible flag
     */
    public boolean isButtonVisible() {
        return isButtonVisible;
    }

    /**
     * Sets the Access Request show history flag.
     * @param showHistory the Access Request show history flag
     */
    public void setShowHistory(boolean showHistory) {
        this.showHistory = showHistory;
    }

    /**
     * Returns the Access Request ownership state.
     * @return the Access Request ownership state
     */
    public String getOwnership() {
        if (ownership == null) {
            return ProjectItem.PROJECT_OWNERSHIP_NONE;
        }
        return ownership;
    }

    /**
     * Return the principal investigator name.
     * @return the principal investigator name
     */
    public String getPiName() {
        if (piName == null) {
            return "";
        }
        return piName;
    }

    /**
     * Sets the principal investigator name.
     * @param piName the principal investigator name
     */
    public void setPiName(String piName) {
        this.piName = piName;
    }

    /**
     * Returns the data provider name.
     * @return the data provider name
     */
    public String getDpName() {
        if (dpName == null) {
            return "";
        }
        return dpName;
    }

    /**
     * Sets the data provider name.
     * @param dpName the data provider name
     */
    public void setDpName(String dpName) {
        this.dpName = dpName;
    }

    /**
     * Sets the Access Request ownership state.
     * @param ownership the Access Request ownership state
     */
    public void setOwnership(String ownership) {
        if (ownership == null
                ||  (!ownership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_EQ_DP)
                    && !ownership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_NE_DP)
                    && !ownership.equals(ProjectItem.PROJECT_OWNERSHIP_PI_ONLY)
                    && !ownership.equals(ProjectItem.PROJECT_OWNERSHIP_DP_ONLY))) {
            ownership = ProjectItem.PROJECT_OWNERSHIP_NONE;
        }
        this.ownership = ownership;
    }

    /**
     * Displays either an up or down arrow.
     * @param sortByParam the sort by parameter (column header to sort by)
     * @param column the actual column header selected
     * @param ascFlag is ascending
     * @return the path to the appropriate image
     */
    public static String showSortOrder(String sortByParam, String column, Boolean ascFlag) {
        String imgIcon = "images/trans_spacer.gif";
        if (sortByParam != null && sortByParam.equals(column)) {
            imgIcon = ascFlag
                    ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /**
     * Defines which sorting parameters to use for sorting UserRequestDTO's
     * based on an user selected column; handles ascending or descending.
     * @param sortByString name of the column to sort by
     * @param isAscending flag indication ascending or descending sort
     * @return the SortParameter array
     */
    public static SortParameter[] selectSortParameters(String sortByString, Boolean isAscending) {
        // Assign sort parameters based on the column the user elects to sort.
        if (sortByString.equals(COLUMN_PROJECT)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_PROJECT_DESC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_USER)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_USER_ASC,
                                              SortParameter.SORT_BY_PROJECT_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_USER_DESC,
                                              SortParameter.SORT_BY_PROJECT_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_INSTITUTION)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_INSTITUTION_ASC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_INSTITUTION_DESC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_LEVEL)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_LEVEL_ASC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_LEVEL_DESC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_STATUS)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_STATUS_ASC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_STATUS_DESC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_FIRSTACCESS)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_FIRST_ACCESS_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_FIRST_ACCESS_DESC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_LASTACCESS)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_LAST_ACCESS_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_LAST_ACCESS_DESC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_TERMS)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_TOU_NAME_ASC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_TOU_VERSION_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_TOU_NAME_DESC,
                                              SortParameter.SORT_BY_PROJECT_ASC,
                                              SortParameter.SORT_BY_TOU_VERSION_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_TERMSVERSION)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_TOU_NAME_ASC,
                                              SortParameter.SORT_BY_TOU_VERSION_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_TOU_NAME_DESC,
                                              SortParameter.SORT_BY_TOU_VERSION_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_TERMSDATE)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_TOU_DATE_AGREED_ASC,
                                              SortParameter.SORT_BY_TOU_NAME_ASC,
                                              SortParameter.SORT_BY_TOU_VERSION_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_TOU_DATE_AGREED_DESC,
                                              SortParameter.SORT_BY_TOU_NAME_ASC,
                                              SortParameter.SORT_BY_TOU_VERSION_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_LASTACTIVITYDATE)) {
            if (isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_LAST_ACTIVITY_DATE_ASC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = {SortParameter.SORT_BY_LAST_ACTIVITY_DATE_DESC,
                                              SortParameter.SORT_BY_USER_ASC};
                return sortParams;
            }
        }
        return null;
    }

    /** The column header for project requests - Project header. */
    public static final String COLUMN_PROJECT = "Project";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_USER = "User";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_INSTITUTION = "Institution";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_LEVEL = "Level";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_STATUS = "Status";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_FIRSTACCESS = "First Access";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_LASTACCESS = "Last Access";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_TERMS = "Terms";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_TERMSVERSION = "Version Accepted";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_TERMSDATE = "Date Accepted";
    /** The column header for project requests - Project header. */
    public static final String COLUMN_LASTACTIVITYDATE = "Last Activity";

    /**
     * Comparator object used for sorting by parameters.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<ProjectRequestDTO> getComparator(SortParameter... sortParameters) {
        return new ProjectRequestComparator(sortParameters);
    }

    public enum SortParameter {
        /** Sort by project ascending. */
        SORT_BY_PROJECT_ASC,
        /** Sort by user id ascending. */
        SORT_BY_USER_ASC,
        /** Sort by institute ascending. */
        SORT_BY_INSTITUTION_ASC,
        /** Sort by level ascending. */
        SORT_BY_LEVEL_ASC,
        /** Sort by status ascending. */
        SORT_BY_STATUS_ASC,
        /** Sort by first access ascending. */
        SORT_BY_FIRST_ACCESS_ASC,
        /** Sort by last access ascending. */
        SORT_BY_LAST_ACCESS_ASC,
        /** Sort by TOU name ascending. */
        SORT_BY_TOU_NAME_ASC,
        /** Sort by TOU version ascending. */
        SORT_BY_TOU_VERSION_ASC,
        /** Sort by date agreed ascending. */
        SORT_BY_TOU_DATE_AGREED_ASC,
        /** Sort by last activity date ascending. */
        SORT_BY_LAST_ACTIVITY_DATE_ASC,
        /** Sort by project descending. */
        SORT_BY_PROJECT_DESC,
        /** Sort by user id descending. */
        SORT_BY_USER_DESC,
        /** Sort by institute descending. */
        SORT_BY_INSTITUTION_DESC,
        /** Sort by level descending. */
        SORT_BY_LEVEL_DESC,
        /** Sort by status descending. */
        SORT_BY_STATUS_DESC,
        /** Sort by first access descending. */
        SORT_BY_FIRST_ACCESS_DESC,
        /** Sort by last access descending. */
        SORT_BY_LAST_ACCESS_DESC,
        /** Sort by TOU name descending. */
        SORT_BY_TOU_NAME_DESC,
        /** Sort by TOU version descending. */
        SORT_BY_TOU_VERSION_DESC,
        /** Sort by date agreed descending. */
        SORT_BY_TOU_DATE_AGREED_DESC,
        /** Sort by last activity date descending. */
        SORT_BY_LAST_ACTIVITY_DATE_DESC
    }

    /**
     * A class that supports comparison between two ProjectRequestDTO's
     * using sort attributes supplied to the constructor.
     *
     */
    private static final class ProjectRequestComparator implements Comparator<ProjectRequestDTO> {
        /** Sort parameters. */
        private SortParameter[] parameters;
        /**
         * Constructor.
         * @param parameters the sort parameters
         */
        private ProjectRequestComparator(SortParameter[] parameters) {
            this.parameters = parameters;
        }

        /**
         * Comparator.
         * @param o1 the first object being compared
         * @param o2 the second object being compared
         * @return the comparator value
         */
        public int compare(ProjectRequestDTO o1, ProjectRequestDTO o2) {
            if (parameters == null) {
                SortParameter[] param = {SortParameter.SORT_BY_PROJECT_ASC};
                parameters = param;
            }

            int comparison = 0;

            for (SortParameter parameter : parameters) {
                switch (parameter) {
                    case SORT_BY_PROJECT_ASC:
                        comparison = o1.getProjectName().compareToIgnoreCase(o2.getProjectName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_USER_ASC:
                        comparison = o1.getUserFullName().compareToIgnoreCase(o2.getUserFullName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_INSTITUTION_ASC:
                        comparison = o1.getInstitution().compareToIgnoreCase(o2.getInstitution());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LEVEL_ASC:
                        if (o1.getUserId().equals(UserItem.DEFAULT_USER)
                                && !o2.getUserId().equals(UserItem.DEFAULT_USER)) {
                            comparison = 1;
                        } else if (!o1.getUserId().equals(UserItem.DEFAULT_USER)
                                && o2.getUserId().equals(UserItem.DEFAULT_USER)) {
                            comparison = -1;
                        } else {
                            comparison = o1.getLevel().compareToIgnoreCase(o2.getLevel());
                        }
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_STATUS_ASC:
                        comparison = AccessRequestStatusItem.arStringEquivalent(
                                o1.getStatus(), false).compareToIgnoreCase(
                                AccessRequestStatusItem.arStringEquivalent(o2.getStatus(), false));
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_FIRST_ACCESS_ASC:
                        comparison = dateComparison(o1.getFirstAccess(), o2.getFirstAccess(), true);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LAST_ACCESS_ASC:
                        comparison = dateComparison(o1.getLastAccess(), o2.getLastAccess(), true);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOU_NAME_ASC:
                        comparison = o1.getTouName().compareToIgnoreCase(o2.getTouName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOU_VERSION_ASC:
                        comparison = ((Integer)o1.getTouVersionAgreed())
                            .compareTo((Integer)o2.getTouVersionAgreed());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOU_DATE_AGREED_ASC:
                        comparison = dateComparison(o1.getTouDateAgreed(),
                                o2.getTouDateAgreed(), true);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LAST_ACTIVITY_DATE_ASC:
                        comparison = dateComparison(o1.getLastActivityDate(),
                                o2.getLastActivityDate(), true);
                        if (comparison != 0) { return comparison; }
                        break;

                    case SORT_BY_PROJECT_DESC:
                        comparison = o2.getProjectName().compareToIgnoreCase(o1.getProjectName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_USER_DESC:
                        comparison = o2.getUserFullName().compareToIgnoreCase(o1.getUserFullName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_INSTITUTION_DESC:
                        comparison = o2.getInstitution().compareToIgnoreCase(o1.getInstitution());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LEVEL_DESC:
                        if (o2.getUserId().equals(UserItem.DEFAULT_USER)
                                && !o1.getUserId().equals(UserItem.DEFAULT_USER)) {
                            comparison = 1;
                        } else if (!o2.getUserId().equals(UserItem.DEFAULT_USER)
                                && o1.getUserId().equals(UserItem.DEFAULT_USER)) {
                            comparison = -1;
                        } else {
                            comparison = o2.getLevel().compareToIgnoreCase(o1.getLevel());
                        }
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_STATUS_DESC:
                        comparison = AccessRequestStatusItem.arStringEquivalent(
                                o2.getStatus(), false).compareToIgnoreCase(
                                AccessRequestStatusItem.arStringEquivalent(o1.getStatus(), false));
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_FIRST_ACCESS_DESC:
                        comparison = dateComparison(o1.getFirstAccess(),
                                o2.getFirstAccess(), false);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LAST_ACCESS_DESC:
                        comparison = dateComparison(o1.getLastAccess(),
                                o2.getLastAccess(), false);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOU_NAME_DESC:
                        comparison = o2.getTouName().compareToIgnoreCase(o1.getTouName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOU_VERSION_DESC:
                        comparison = ((Integer)o2.getTouVersionAgreed())
                           .compareTo((Integer)o1.getTouVersionAgreed());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOU_DATE_AGREED_DESC:
                        comparison = dateComparison(o1.getTouDateAgreed(),
                                o2.getTouDateAgreed(), false);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LAST_ACTIVITY_DATE_DESC:
                        comparison = dateComparison(o1.getLastActivityDate(),
                                o2.getLastActivityDate(), false);
                        if (comparison != 0) { return comparison; }
                        break;
                    default:
                        // Nothing is default
                }
            }

            return 0;
        }
    }
}
