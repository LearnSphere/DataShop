/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.customfield;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.cmu.pslc.datashop.servlet.DatasetContext;

/**
 * Stores session information for the Custom Field pages.
 *
 * @author Cindy Tipper
 * @version $Revision: 12077 $
 * <BR>Last modified by: $Author: epennin $
 * <BR>Last modified on: $Date: 2015-03-12 14:58:14 -0400 (Thu, 12 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /**
     * Get the CustomFieldContext from the active DatasetContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the CustomFieldContext stored in the session
     */
    public static CustomFieldContext getContext(HttpServletRequest req) {
        HttpSession session = req.getSession();
        DatasetContext datasetContext = (DatasetContext)session.getAttribute("datasetContext_"
                + req.getParameter("datasetId"));
        CustomFieldContext context = datasetContext.getCustomFieldContext();
        if (context == null) {
            context = new CustomFieldContext();
            setContext(req, context);
        }
        return context;
    }

    /**
     * Set the CustomFieldContext for the active DatasetContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @param context the CustomFieldContext
     */
    public static void setContext(HttpServletRequest req, CustomFieldContext context) {
        HttpSession session = req.getSession();
        DatasetContext datasetContext = (DatasetContext)session.getAttribute("datasetContext_"
                + req.getParameter("datasetId"));
        datasetContext.setCustomFieldContext(context);
    }

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current column to sort by. */
    private static final String KEY_SORT_BY_COLUMN = "sort_by_column";

    /** HashMap to store context values for the Custom Fields page. Thread safe. */
    private Map<String, Object> cfMap;

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public CustomFieldContext() {
        cfMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        setSortByColumn(CustomFieldDto.COLUMN_NAME, true);
    }

    //----- MAP GETTERs and SETTERs -----

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getSortByColumn() {
        return (String)cfMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setSortByColumn(String columnName, Boolean toggleFlag) {
        cfMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isAscending(String columnName) {
        Boolean result = true;
        if (cfMap.containsKey(columnName)) {
            result = (Boolean)cfMap.get(columnName);
        } else {
            cfMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (cfMap.containsKey(columnName)) {
            ascFlag = (Boolean)cfMap.get(columnName);
            cfMap.put(columnName, !ascFlag);
        } else {
            cfMap.put(columnName, ascFlag);
        }
    }
}
