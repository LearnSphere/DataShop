/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.List;

/**
 * Data necessary for Access Report queries on the 'Project Permissions' page.
 *
 * @author Cindy Tipper
 * @version $Revision: 10852 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-31 10:47:33 -0400 (Mon, 31 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessReportInfo {

    //----- CONSTANTS -----
    /** Constant for ascending sort of user name: first + last + userId. */
    private static final String USER_NAME_ORDER_BY_ASC = " LOWER(sortableName) ASC";

    // Sorting... ascending
    /** Constant for the 'order by' project name. */
    public static final String SQL_ORDER_BY_PROJECT_ASC =
        " LOWER(projectName) ASC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' user's first name. */
    public static final String SQL_ORDER_BY_USER_ASC = USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' institution. */
    public static final String SQL_ORDER_BY_INSTITUTION_ASC =
        " LOWER(institution), " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' authorization level. */
    public static final String SQL_ORDER_BY_LEVEL_ASC =
        " authLevel, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' status. */
    public static final String SQL_ORDER_BY_STATUS_ASC =
        " arsStatus, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' last activity. */
    public static final String SQL_ORDER_BY_LAST_ACTIVITY_ASC =
        " lastActivity, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' first access. */
    public static final String SQL_ORDER_BY_FIRST_ACCESS_ASC =
        " firstAccess, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' last access. */
    public static final String SQL_ORDER_BY_LAST_ACCESS_ASC =
        " lastAccess, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' Terms. */
    public static final String SQL_ORDER_BY_TOU_NAME_ASC =
        " LOWER(touName) ASC, LOWER(projectName) ASC, touVersion ASC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' Terms Version. */
    public static final String SQL_ORDER_BY_TOU_VERSION_ASC =
        " LOWER(touName) ASC, touVersion ASC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' Terms Date. */
    public static final String SQL_ORDER_BY_TOU_DATE_ASC =
        " touDate ASC, LOWER(touName) ASC, touVersion ASC, " + USER_NAME_ORDER_BY_ASC;
    // Sorting... descending
    /** Constant for the 'order by' project name. */
    public static final String SQL_ORDER_BY_PROJECT_DESC =
        " LOWER(projectName) DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' user's first name. */
    public static final String SQL_ORDER_BY_USER_DESC = " LOWER(sortableName) DESC";
    /** Constant for the 'order by' institution. */
    public static final String SQL_ORDER_BY_INSTITUTION_DESC =
        " LOWER(institution) DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' authorization level. */
    public static final String SQL_ORDER_BY_LEVEL_DESC =
        " authLevel DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' status. */
    public static final String SQL_ORDER_BY_STATUS_DESC =
        " arsStatus DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' last activity. */
    public static final String SQL_ORDER_BY_LAST_ACTIVITY_DESC =
        " lastActivity DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' first access. */
    public static final String SQL_ORDER_BY_FIRST_ACCESS_DESC =
        " firstAccess DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' last access. */
    public static final String SQL_ORDER_BY_LAST_ACCESS_DESC =
        " lastAccess DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' Terms. */
    public static final String SQL_ORDER_BY_TOU_NAME_DESC =
        " LOWER(touName) DESC, LOWER(projectName) ASC, touVersion ASC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' Terms Version. */
    public static final String SQL_ORDER_BY_TOU_VERSION_DESC =
        " LOWER(touName) DESC, touVersion DESC, " + USER_NAME_ORDER_BY_ASC;
    /** Constant for the 'order by' Terms Date. */
    public static final String SQL_ORDER_BY_TOU_DATE_DESC =
        " touDate DESC, LOWER(touName) ASC, touVersion ASC, " + USER_NAME_ORDER_BY_ASC;

    //----- ATTRIBUTES -----

    /** List of Access Request status values to match. */
    private List<String> arStatusList;
    /** User id for PI or DataProvider to match. */
    private String piDpId;
    /** List of Project id values to match. */
    private List<Integer> projectIdList;
    /** Project name to match. */
    private String projectName;
    /** User id to match. */
    private String user;
    /** Institution to match. */
    private String institution;
    /** Flag to indicate if admin users should be included. */
    private Boolean showAdmin;
    /** Boolean query op: "AND" or "OR". */
    private String boolQueryOp = "OR";

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param arStatusList the list of Access Request statuses
     * @param piDpId the PI or DataProvider user id
     * @param projectIdList the list of Project ids
     * @param projectName the project name
     * @param user the user id
     * @param institution the user's institution
     * @param showAdmin flag to include admin users
     * @param boolQueryOp query is conjunction ("AND") or disjunction ("OR")
     */
    public AccessReportInfo(List<String> arStatusList, String piDpId, List<Integer> projectIdList,
                            String projectName, String user, String institution,
                            Boolean showAdmin, String boolQueryOp) {
        setArStatusList(arStatusList);
        setPiDpId(piDpId);
        setProjectIdList(projectIdList);
        setProjectName(projectName);
        setUser(user);
        setInstitution(institution);
        setShowAdmin(showAdmin);
        setBoolQueryOp(boolQueryOp);
    }

    //----- GETTERs and SETTERs -----

    /**
     * Set the list of Access Request statuses to query for.
     * @param arsList the list
     */
    public void setArStatusList(List<String> arsList) {
        this.arStatusList = arsList;
    }

    /**
     * Get the list of Access Request statuses.
     * @return List<String> the list
     */
    public List<String> getArStatusList() {
        return arStatusList;
    }

    /**
     * Set the user id for the PI or DataProvider.
     * @param piDpId the id
     */
    public void setPiDpId(String piDpId) {
        this.piDpId = piDpId;
    }

    /**
     * Get the user id for the PI or DataProvider.
     * @return String the id
     */
    public String getPiDpId() {
        return piDpId;
    }

    /**
     * Set the list of Project ids to query for.
     * @param projectIdList the list
     */
    public void setProjectIdList(List<Integer> projectIdList) {
        this.projectIdList = projectIdList;
    }

    /**
     * Get the list of Project ids.
     * @return List<Integer> the list
     */
    public List<Integer> getProjectIdList() {
        return projectIdList;
    }

    /**
     * Set the project name to query for.
     * @param projectName the project
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Get the project name.
     * @return String the project
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the id of the user to query for.
     * @param user the id
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Get the user id.
     * @return String the id
     */
    public String getUser() {
        return user;
    }

    /**
     * Set the institution to query for.
     * @param institution the name of the institution
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * Get the institution.
     * @return String the institution
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * Set the showAdmin flag for including admin users in query.
     * @param showAdmin the flag
     */
    public void setShowAdmin(Boolean showAdmin) {
        this.showAdmin = showAdmin;
    }

    /**
     * Get the showAdmin flag.
     * @return Boolean the flag
     */
    public Boolean getShowAdmin() {
        return showAdmin;
    }

    /**
     * Set the boolean query operation. Acceptable values
     * are "AND" and "OR" with the default being "OR".
     * @param boolQueryOp String value of boolean
     */
    public void setBoolQueryOp(String boolQueryOp) {
        // Default behavior.
        if (boolQueryOp == null) { boolQueryOp = "OR"; }

        if (!boolQueryOp.equalsIgnoreCase("AND")
                && !boolQueryOp.equalsIgnoreCase("OR")) {
            throw new IllegalArgumentException("Invalid BoolQueryOp value: " + boolQueryOp);
        }

        this.boolQueryOp = boolQueryOp;
    }

    /**
     * Get the boolean query option.
     * @return String the op.
     */
    public String getBoolQueryOp() {
        return boolQueryOp;
    }

    /**
     * toString method for AccessReportInfo.
     * @return String the object info
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("AccessReportInfo [");
        sb.append("arStatusList = ");
        sb.append(arStatusList);
        sb.append(", piDpId = ");
        sb.append(piDpId);
        sb.append(", projectIdList = ");
        sb.append(projectIdList);
        sb.append(", projectName = ");
        sb.append(projectName);
        sb.append(", user = ");
        sb.append(user);
        sb.append(", institution = ");
        sb.append(institution);
        sb.append(", showAdmin = ");
        sb.append(showAdmin);
        sb.append(", boolQueryOp = ");
        sb.append(boolQueryOp);
        sb.append("]");

        return sb.toString();
    }
}
