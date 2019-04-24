/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.project;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cmu.pslc.datashop.dto.ProjectRequestDTO;

/**
 * Stores session information for the Project Permissions pages.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectPermissionsContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /** Session Key for the ProjectPermissionsContext */
    private static final String PROJECT_PERM_SESS_KEY = "project_perm_context";

    /**
     * Get the ProjectPermissionsContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the ProjectPermissionsContext stored in the session
     */
    public static ProjectPermissionsContext getContext(HttpServletRequest req) {
        ProjectPermissionsContext context =
            (ProjectPermissionsContext)req.getSession().getAttribute(PROJECT_PERM_SESS_KEY);
        if (context == null) {
            context = new ProjectPermissionsContext();
            setContext(req, context);
        }
        return context;
    }

    /**
     * Set the ProjectPermissionsContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @param context the ProjectPermissionsContext
     */
    public static void setContext(HttpServletRequest req, ProjectPermissionsContext context) {
        req.getSession().setAttribute(PROJECT_PERM_SESS_KEY, context);
    }

    //----- Constants -----

    /** The default count for Rows Per Page. */
    public static final int DEFAULT_ROWS_PER_PAGE = 10;
    /** The default current page. */
    public static final int DEFAULT_CURRENT_PAGE = 1;
    /** The default showAdmins value. */
    public static final boolean DEFAULT_SHOW_ADMINS = false;
    /** Current Permissions tab. */
    public static final String CURRENT_PERMISSIONS_TAB = "Current Permissions";
    /** Access Report tab. */
    public static final String ACCESS_REPORT_TAB = "Access Report";

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current search by string. */
    private static final String KEY_SEARCH_BY = "search_by";
    /** Session variable for storing the current column to sort by. */
    private static final String KEY_SORT_BY_COLUMN = "sort_by_column";
    /** Session variable for storing the current rows per page. */
    private static final String KEY_ROWS_PER_PAGE = "rows_per_page";
    /** Session variable for storing the current page. */
    private static final String KEY_CURRENT_PAGE = "current_page";

    /** HashMap to store context values for the 'Requests for Access' table. Thread safe. */
    private Map<String, Object> rfaMap;
    /** HashMap to store context values for the 'Current Permissions' tab. Thread safe. */
    private Map<String, Object> cpMap;
    /** HashMap to store context values for the 'Access Request' tab. Thread safe. */
    private Map<String, Object> arMap;
    /** Variable for tracking current tab: Current Permissions or Access Report. */
    private String currentTab;
    /** Variable for tracking 'showAdmins' value, shared across tabs. */
    private Boolean showAdmins;

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public ProjectPermissionsContext() {
        cpMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        arMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        rfaMap = Collections.synchronizedMap(new HashMap <String, Object> ());

        currentTab = CURRENT_PERMISSIONS_TAB;

        setRequestsForAccessSortByColumn(ProjectRequestDTO.COLUMN_LASTACTIVITYDATE, true);
        // Initially, this is a DESC sort.
        toggleRequestsForAccessSortOrder(ProjectRequestDTO.COLUMN_LASTACTIVITYDATE);

        // Single 'showAdmins' value for both tabs.
        setShowAdmins(DEFAULT_SHOW_ADMINS);

        setCurrentPermissionsSearchBy("");
        setCurrentPermissionsSortByColumn(ProjectRequestDTO.COLUMN_USER, true);
        setCurrentPermissionsRowsPerPage(DEFAULT_ROWS_PER_PAGE);
        setCurrentPermissionsCurrentPage(DEFAULT_CURRENT_PAGE);

        setAccessReportSearchBy("");
        setAccessReportSortByColumn(ProjectRequestDTO.COLUMN_USER, true);
        setAccessReportRowsPerPage(DEFAULT_ROWS_PER_PAGE);
        setAccessReportCurrentPage(DEFAULT_CURRENT_PAGE);
    }

    //----- GETTERs and SETTERs -----

    /**
     * Gets the current tab, either 'Currrent Permissions' or 'Access Report'.
     * @return String currentTab
     */
    public String getCurrentTab() {
        return currentTab;
    }

    /**
     * Sets the current tab, either 'Current Permissions' or 'Access Report'.
     * @param currentTab the current tab selected
     */
    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }

    /**
     * Get the showAdmins parameter.
     * @return boolean the showAdmins parameter
     */
    public Boolean getShowAdmins() {
        return showAdmins;
    }

    /**
     * Set the showAdmins parameter.
     * @param showAdmins the Boolean indicating showAdmins value
     */
    public void setShowAdmins(Boolean showAdmins) {
        this.showAdmins = showAdmins;
    }

    /**
     * Gets the 'Requests for Access' sortBy parameters.
     * @return the 'Requests for Access' sortBy parameters.
     */
    public String getRequestsForAccessSortByColumn() {
        return (String)rfaMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setRequestsForAccessSortByColumn(String columnName, Boolean toggleFlag) {
        rfaMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleRequestsForAccessSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isRequestsForAccessAscending(String columnName) {
        Boolean result = true;
        if (rfaMap.containsKey(columnName)) {
            result = (Boolean) rfaMap.get(columnName);
        } else {
            rfaMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleRequestsForAccessSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (rfaMap.containsKey(columnName)) {
            ascFlag = (Boolean)rfaMap.get(columnName);
            rfaMap.put(columnName, !ascFlag);
        } else {
            rfaMap.put(columnName, ascFlag);
        }
    }

    /**
     * Get the 'Current Permissions' searchBy parameter.
     * @return the 'Current Permissions' searchBy parameter
     */
    public String getCurrentPermissionsSearchBy() {
        return (String)cpMap.get(KEY_SEARCH_BY);
    }

    /**
     * Set the 'Current Permissions' searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setCurrentPermissionsSearchBy(String searchBy) {
        cpMap.put(KEY_SEARCH_BY, searchBy);
    }

    /**
     * Gets the 'Current Permissions' sortBy parameters.
     * @return the 'Current Permissions' sortBy parameters.
     */
    public String getCurrentPermissionsSortByColumn() {
        return (String)cpMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setCurrentPermissionsSortByColumn(String columnName, Boolean toggleFlag) {
        cpMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleCurrentPermissionsSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isCurrentPermissionsAscending(String columnName) {
        Boolean result = true;
        if (cpMap.containsKey(columnName)) {
            result = (Boolean) cpMap.get(columnName);
        } else {
            cpMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleCurrentPermissionsSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (cpMap.containsKey(columnName)) {
            ascFlag = (Boolean)cpMap.get(columnName);
            cpMap.put(columnName, !ascFlag);
        } else {
            cpMap.put(columnName, ascFlag);
        }
    }

    /**
     * Get the 'Current Permissions' rowsPerPage parameter.
     * @return the 'Current Permissions' rowsPerPage parameter
     */
    public Integer getCurrentPermissionsRowsPerPage() {
        return (Integer)cpMap.get(KEY_ROWS_PER_PAGE);
    }

    /**
     * Set the 'Current Permissions' rowsPerPage parameter.
     * @param rowsPerPage the Integer for limiting query size
     */
    public void setCurrentPermissionsRowsPerPage(Integer rowsPerPage) {
        cpMap.put(KEY_ROWS_PER_PAGE, rowsPerPage);
    }

    /**
     * Get the 'Current Permissions' currentPage parameter.
     * @return the 'Current Permissions' currentPage parameter
     */
    public Integer getCurrentPermissionsCurrentPage() {
        return (Integer)cpMap.get(KEY_CURRENT_PAGE);
    }

    /**
     * Set the 'Current Permissions' currentPage parameter.
     * @param currentPage the Integer indicating current page
     */
    public void setCurrentPermissionsCurrentPage(Integer currentPage) {
        cpMap.put(KEY_CURRENT_PAGE, currentPage);
    }

    /**
     * Get the 'Access Report' searchBy parameter.
     * @return the 'Access Report' searchBy parameter
     */
    public String getAccessReportSearchBy() {
        return (String)arMap.get(KEY_SEARCH_BY);
    }

    /**
     * Set the 'Access Report' searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setAccessReportSearchBy(String searchBy) {
        arMap.put(KEY_SEARCH_BY, searchBy);
    }

    /**
     * Gets the 'Access Report' sortBy parameters.
     * @return the 'Access Report' sortBy parameters.
     */
    public String getAccessReportSortByColumn() {
        return (String)arMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setAccessReportSortByColumn(String columnName, Boolean toggleFlag) {
        arMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleAccessReportSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isAccessReportAscending(String columnName) {
        Boolean result = true;
        if (arMap.containsKey(columnName)) {
            result = (Boolean) arMap.get(columnName);
        } else {
            arMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleAccessReportSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (arMap.containsKey(columnName)) {
            ascFlag = (Boolean)arMap.get(columnName);
            arMap.put(columnName, !ascFlag);
        } else {
            arMap.put(columnName, ascFlag);
        }
    }

    /**
     * Get the 'Access Report' rowsPerPage parameter.
     * @return the 'Access Report' rowsPerPage parameter
     */
    public Integer getAccessReportRowsPerPage() {
        return (Integer)arMap.get(KEY_ROWS_PER_PAGE);
    }

    /**
     * Set the 'Access Report' rowsPerPage parameter.
     * @param rowsPerPage the Integer for limiting query size
     */
    public void setAccessReportRowsPerPage(Integer rowsPerPage) {
        arMap.put(KEY_ROWS_PER_PAGE, rowsPerPage);
    }

    /**
     * Get the 'Access Report' currentPage parameter.
     * @return the 'Access Report' currentPage parameter
     */
    public Integer getAccessReportCurrentPage() {
        return (Integer)arMap.get(KEY_CURRENT_PAGE);
    }

    /**
     * Set the 'Access Report' currentPage parameter.
     * @param currentPage the Integer indicating current page
     */
    public void setAccessReportCurrentPage(Integer currentPage) {
        arMap.put(KEY_CURRENT_PAGE, currentPage);
    }
}
