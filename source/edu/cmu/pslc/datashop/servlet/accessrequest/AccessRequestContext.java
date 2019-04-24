/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.accessrequest;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Contextual information for the Access Requests Page.
 *
 * @author Mike Komisin
 * @version $Revision: 7606 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2012-04-06 14:34:34 -0400 (Fri, 06 Apr 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AccessRequestContext implements Serializable {

    /** The current order to sort a list of Project Access Requests. */
    private static final String ACCESS_REQUEST_SORT_BY = "ar_sortby";

    /** Access Request sub-tab map key - "ar_ctx_subtab". */
    public static final String ACCESS_REQUEST_SUBTAB = "ar_ctx_subtab";
    /** Flag which indicates whether to show older user requests. */
    public static final String ACCESS_REQUEST_SHOW_OLDER_USER_REQ = "ar_show_older_user_req";
    /** Flag which indicates whether to show older project requests. */
    public static final String ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ = "ar_show_older_project_req";
    /** Rows to be displayed per page. */
    public static final String ACCESS_REQUEST_ROWS_PER_PAGE = "ar_rows_per_page";
    /** The default count for Rows Per Page. */
    public static final int DEFAULT_ROWS_PER_PAGE = 10;
    /** The maximum Rows Per Page. */
    public static final int MAX_ROWS_PER_PAGE = 100;
    /** Rows to be displayed per page. */
    public static final String ACCESS_REQUEST_CURRENT_PAGE = "ar_current_page";
    /** The default count for Show Older User or Project Requests. */
    public static final int DEFAULT_OLDER_THAN = 90;

    /** Administrator filter for pending requests. */
    public static final String ACCESS_REQUEST_FILTER_BY = "ar_filter_by";
    /** The default filter-by selection for the Pending page. */
    public static final String DEFAULT_FILTER_BY = "All Pending";
    /** Administrator filter for searching project and user names. */
    public static final String ACCESS_REQUEST_SEARCH_STRING = "ar_search_string";
    /** Administrator options for showing administrators in the Access Report list. */
    public static final String ACCESS_REQUEST_SHOW_ADMINS = "ar_show_admins";

    /** HashMap that holds the context and is thread safe. */
    private Map <String, Object> mainMap;
    /** HashMap that holds the Pending sub-tab context and is thread safe. */
    private Map <String, Object> pendingMap;
    /** HashMap that holds the Recent Activity sub-tab context and is thread safe. */
    private Map <String, Object> recentMap;
    /** HashMap that holds the Access Report sub-tab context and is thread safe. */
    private Map <String, Object> reportMap;

    /** The flag describing if the logged in user was or is the PI or DP. */
    private Boolean wasEverOwner;

    /** Administrator option to show administrators in Access Report list. */
    private Boolean showAdmins;

    /**
     * Default Constructor.
     */
    public AccessRequestContext() {

        pendingMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        pendingMap.put(ACCESS_REQUEST_SHOW_OLDER_USER_REQ, false);
        pendingMap.put(ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ, false);

        recentMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        recentMap.put(ACCESS_REQUEST_SHOW_OLDER_USER_REQ, false);
        recentMap.put(ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ, false);

        reportMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        reportMap.put(ACCESS_REQUEST_ROWS_PER_PAGE, DEFAULT_ROWS_PER_PAGE);

        mainMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        mainMap.put(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED, pendingMap);
        mainMap.put(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT, recentMap);
        mainMap.put(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT, reportMap);
        mainMap.put(ACCESS_REQUEST_SUBTAB, AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED);

        wasEverOwner = false;
        showAdmins = false;
    }

    /**
     * Sets the current sub-tab name.
     * @param subtab the current sub-tab name
     */
    public void setCurrentTab(String subtab) {
        if (subtab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED)
                || subtab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT)
                || subtab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
            mainMap.put(ACCESS_REQUEST_SUBTAB, subtab);
        }
    }

    /**
     * Returns the current sub-tab name or 'not_reviewed' if null.
     * @return the current sub-tab name or 'not_reviewed' if null
     */
    public String getCurrentTab() {
        String currentTab = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        if (currentTab == null) {
            return AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED;
        }
        return currentTab;
    }

    /**
     * Sets the showOlderUserRequests flag for the current sub-tab.
     * @param flag the showOlderUserRequests flag
     */
    public void setShowOlderUserRequests(boolean flag) {
        mainMap.put(ACCESS_REQUEST_SHOW_OLDER_USER_REQ, flag);
    }

    /**
     * Returns the showOlderUserRequests flag given the current sub-tab.
     * @return the showOlderUserRequests flag
     */
    public boolean getShowOlderUserRequests() {
        boolean flag = false;
        if (mainMap.containsKey(ACCESS_REQUEST_SHOW_OLDER_USER_REQ)) {
            flag = (Boolean) mainMap.get(ACCESS_REQUEST_SHOW_OLDER_USER_REQ);
        }
        return flag;
    }

    /**
     * Sets the showOlderProjectRequests flag for the current sub-tab.
     * @param flag the showOlderProjectRequests flag
     */
    public void setShowOlderProjectRequests(boolean flag) {
        mainMap.put(ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ, flag);
    }

    /**
     * Returns the showOlderProjectRequests flag given the current sub-tab.
     * @return the showOlderProjectRequests flag
     */
    public boolean getShowOlderProjectRequests() {
        boolean flag = false;
        if (mainMap.containsKey(ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ)) {
            flag = (Boolean) mainMap.get(ACCESS_REQUEST_SHOW_OLDER_PROJECT_REQ);
        }
        return flag;
    }

    /**
     * Returns the rowsPerPage for the current sub-tab.
     * @return the rowsPerPage for the current sub-tab
     */
    public int getRowsPerPage() {
        int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        if (subMap.containsKey(ACCESS_REQUEST_ROWS_PER_PAGE)) {
            rowsPerPage = (Integer) subMap.get(ACCESS_REQUEST_ROWS_PER_PAGE);
        }
        return rowsPerPage;
    }

    /**
     * Sets the rowsPerPage for the current sub-tab.
     * @param rowsPerPage the number of rows to be displayed
     */
    public void setRowsPerPage(int rowsPerPage) {
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        subMap.put(ACCESS_REQUEST_ROWS_PER_PAGE, rowsPerPage);
    }

    /**
     * Returns the currentPage for the current sub-tab.
     * @return the currentPage for the current sub-tab
     */
    public int getCurrentPage() {
        int currentPage = 1;
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        if (subMap.containsKey(ACCESS_REQUEST_CURRENT_PAGE)) {
            currentPage = (Integer) subMap.get(ACCESS_REQUEST_CURRENT_PAGE);
        }
        return currentPage;
    }

    /**
     * Sets the currentPage for the current sub-tab.
     * @param currentPage the current page to be displayed
     */
    public void setCurrentPage(int currentPage) {
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        subMap.put(ACCESS_REQUEST_CURRENT_PAGE, currentPage);
    }

    /**
     * Returns the filterBy for the Pending and Recent Activity sub-tabs.
     * @return the filterBy for the Pending and Recent Activity sub-tabs
     */
    public String getFilterBy() {
        String filterBy = DEFAULT_FILTER_BY;

        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        if (subMap.containsKey(ACCESS_REQUEST_FILTER_BY)) {
            filterBy = (String) subMap.get(ACCESS_REQUEST_FILTER_BY);
        }
        return filterBy;
    }

    /**
     * Sets the filterBy for the Pending and Recent Activity sub-tabs.
     * @param filterBy the filter to be applied to the Pending and Recent Activity sub-tabs
     */
    public void setFilterBy(String filterBy) {
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        subMap.put(ACCESS_REQUEST_FILTER_BY, filterBy);
    }

    /**
     * Returns the search string filter to be applied to project and user names.
     * @return the search string filter to be applied to project and user names
     */
    public String getSearchString() {
        String searchString = "";
        if (mainMap.containsKey(ACCESS_REQUEST_SEARCH_STRING)) {
            searchString = (String) mainMap.get(ACCESS_REQUEST_SEARCH_STRING);
        }
        return searchString.replaceAll("\"", "");
    }

    /**
     * Sets the filterBy for the Pending and Recent Activity sub-tabs.
     * @param searchString the search string filter to be applied to project and user names
     */
    public void setSearchString(String searchString) {
        if (searchString.equals("")) {
            mainMap.remove(ACCESS_REQUEST_SEARCH_STRING);
        } else {
            mainMap.put(ACCESS_REQUEST_SEARCH_STRING, searchString);
        }
    }

    /** Returns sort type. @return the sort type as a String. */
    public String getSortBy() {
        String sortBy = null;
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        if (subMap.containsKey(ACCESS_REQUEST_SORT_BY)) {
            sortBy = (String)subMap.get(ACCESS_REQUEST_SORT_BY);
        }
        return sortBy;
    }

    /** Set sort type.@param sortBy the selected sort type as a String. */
    public void setSortBy(String sortBy) {
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        subMap.put(ACCESS_REQUEST_SORT_BY, sortBy);
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleSortOrder(String columnName) {
        Boolean ascFlag = true;
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        if (subMap.containsKey(columnName)) {
            ascFlag = (Boolean) subMap.get(columnName);
            subMap.put(columnName, !ascFlag);
        } else {
            subMap.put(columnName, ascFlag);
        }
    }

    /** Get the sort order for a specific column.
     * @param columnName the column header
     * @return true for ascending order and false for descending
     */
    public Boolean getSortOrder(String columnName) {
        Boolean ascFlag = true;
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        if (subMap.containsKey(columnName)) {
            ascFlag = (Boolean) subMap.get(columnName);
        } else {
            subMap.put(columnName, false);
        }
        return ascFlag;
    }

    /**
     * Gets the flag describing if the logged in user was or is the PI or DP.
     * @return the flag describing if the logged in user was or is the PI or DP
     */
    public Boolean getWasEverOwner() {
        return wasEverOwner;
    }

    /**
     * Sets the flag describing if the logged in user was or is the PI or DP.
     * @param wasEverOwner the flag describing if the logged in user was or is the PI or DP
     */
    public void setWasEverOwner(Boolean wasEverOwner) {
        this.wasEverOwner = wasEverOwner;
    }

    /**
     * Gets the flag describing if the administrator wishes to show
     * administrators in the Access Report list.
     * @return the flag describing if the administrator wishes to show
     * administrators in the Access Report list
     */
    public Boolean getShowAdmins() {
        return showAdmins;
    }

    /**
     * Sets the flag describing if the administrator wishes to show
     * administrators in the Access Report list.
     * @param showAdmins the flag describing if the administrator wishes to show
     * administrators in the Access Report list
     */
    public void setShowAdmins(Boolean showAdmins) {
        this.showAdmins = showAdmins;
    }

    /**
     * Sets the flag describing if the sort order is ascending.
     * @param columnName the columnName
     * @param sortOrder the flag describing if the sort order is ascending
     */
    public void setSortOrder(String columnName, Boolean sortOrder) {
        String subtabName = (String) mainMap.get(ACCESS_REQUEST_SUBTAB);
        Map<String, Object> subMap = (Map<String, Object>) mainMap.get(subtabName);
        subMap.put(columnName, sortOrder);
    }
}
