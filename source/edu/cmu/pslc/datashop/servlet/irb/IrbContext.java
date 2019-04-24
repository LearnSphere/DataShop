/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores session information for the IRB pages.
 *
 * @author Cindy Tipper
 * @version $Revision: 10747 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-06 13:57:40 -0500 (Thu, 06 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /** Session Key for the IrbContext */
    private static final String IRB_SESS_KEY = "irb_context";

    /**
     * Get the IrbContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the IrbContext stored in the session
     */
    public static IrbContext getContext(HttpServletRequest req) {
        IrbContext context = (IrbContext)req.getSession().getAttribute(IRB_SESS_KEY);
        if (context == null) {
            context = new IrbContext();
            setContext(req, context);
        }
        return context;
    }

    /**
     * Set the IrbContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @param context the IrbContext
     */
    public static void setContext(HttpServletRequest req, IrbContext context) {
        req.getSession().setAttribute(IRB_SESS_KEY, context);
    }

    //----- SESSION VARIABLES -----

    /** Session variable for storing the search by string for Project Name. */
    private static final String KEY_SEARCH_BY = "search_by";
    /** Session variable for storing the search by string for PI/DP. */
    private static final String KEY_SEARCH_BY_PI_DP = "search_by_pi_dp";
    /** Session variable for storing the current column to sort by. */
    private static final String KEY_SORT_BY_COLUMN = "sort_by_column";
    /** Session variable for storing the filter string. */
    private static final String KEY_FILTER = "filter";

    /** HashMap to store context values for the IRB Review page. Thread safe. */
    private Map<String, Object> reviewMap;

    /** HashMap to store context values for the All IRBs page. Thread safe. */
    private Map<String, Object> allIRBsMap;

    /** Session variable for storing search by string used in 'Add existing IRB'. */
    private String addIRBSearchBy;

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public IrbContext() {
        reviewMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        allIRBsMap = Collections.synchronizedMap(new HashMap <String, Object> ());

        setReviewSearchBy("");
        setReviewSearchByPiDp("");
        //Set the default filters
        setReviewFilter(new IrbReviewFilter(null, null, null, null, null, "true"));
        //Set the default sort
        setReviewSortByColumn(ProjectReviewDto.COLUMN_NEEDS_ATTN, true);

        setAllIRBsSearchBy("");
        setAllIRBsSortByColumn(IrbDto.COLUMN_TITLE, false);
        setAddIRBSearchBy("");
    }

    //----- IRB Review MAP GETTERs and SETTERs -----

    /**
     * Get the current searchBy parameter.
     * @return the current searchBy parameter
     */
    public String getReviewSearchBy() {
        return (String)reviewMap.get(KEY_SEARCH_BY);
    }

    /**
     * Set the current searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setReviewSearchBy(String searchBy) {
        if (searchBy != null) { searchBy = searchBy.trim(); }
        reviewMap.put(KEY_SEARCH_BY, searchBy);
    }

    /**
     * Get the current searchByPiDp parameter.
     * @return the current searchByPiDp parameter
     */
    public String getReviewSearchByPiDp() {
        return (String)reviewMap.get(KEY_SEARCH_BY_PI_DP);
    }

    /**
     * Set the current searchByPiDp parameter.
     * @param searchByPiDp the String to search by
     */
    public void setReviewSearchByPiDp(String searchByPiDp) {
        reviewMap.put(KEY_SEARCH_BY_PI_DP, searchByPiDp);
    }

    /**
     * Get the current filter parameter.
     * @return the current filter parameter
     */
    public IrbReviewFilter getReviewFilter() {
        return (IrbReviewFilter)reviewMap.get(KEY_FILTER);
    }

    /**
     * Clear the current filter parameter.
     */
    public void setReviewFilter() {
        reviewMap.put(KEY_FILTER, null);
    }

    /**
     * Set the current filter parameter.
     * @param filter the Strings on which to filter
     */
    public void setReviewFilter(IrbReviewFilter filter) {
        reviewMap.put(KEY_FILTER, filter);
    }

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getReviewSortByColumn() {
        return (String)reviewMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setReviewSortByColumn(String columnName, Boolean toggleFlag) {
        reviewMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleReviewSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isReviewAscending(String columnName) {
        Boolean result = true;
        if (reviewMap.containsKey(columnName)) {
            result = (Boolean) reviewMap.get(columnName);
        } else {
            reviewMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleReviewSortOrder(String columnName) {
        if (reviewMap.containsKey(columnName)) {
            Boolean ascFlag = (Boolean) reviewMap.get(columnName);
            reviewMap.put(columnName, !ascFlag);
        } else {
            reviewMap.put(columnName, false);
        }
    }

    //----- All IRBs MAP GETTERs and SETTERs -----

    /**
     * Get the current searchBy parameter.
     * @return String searchBy string
     */
    public String getAllIRBsSearchBy() {
        return (String)allIRBsMap.get(KEY_SEARCH_BY);
    }

    /**
     * Set the current searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setAllIRBsSearchBy(String searchBy) {
        allIRBsMap.put(KEY_SEARCH_BY, searchBy);
    }

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getAllIRBsSortByColumn() {
        return (String)allIRBsMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setAllIRBsSortByColumn(String columnName, Boolean toggleFlag) {
        allIRBsMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleAllIRBsSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isAllIRBsAscending(String columnName) {
        Boolean result = true;
        if (allIRBsMap.containsKey(columnName)) {
            result = (Boolean) allIRBsMap.get(columnName);
        } else {
            allIRBsMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleAllIRBsSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (allIRBsMap.containsKey(columnName)) {
            ascFlag = (Boolean) allIRBsMap.get(columnName);
            allIRBsMap.put(columnName, !ascFlag);
        } else {
            allIRBsMap.put(columnName, ascFlag);
        }
    }

    //----- Add IRB 'search by' GETTER and SETTER -----

    /**
     * Get the current searchBy parameter.
     * @return String searchBy string
     */
    public String getAddIRBSearchBy() {
        return addIRBSearchBy;
    }

    /**
     * Set the current searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setAddIRBSearchBy(String searchBy) {
        this.addIRBSearchBy = searchBy;
    }
}
