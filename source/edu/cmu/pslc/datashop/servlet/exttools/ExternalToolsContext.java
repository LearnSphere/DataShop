/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.exttools;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores session information for the External Tools pages.
 *
 * @author alida
 * @version $Revision: 10721 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-05 08:26:26 -0500 (Wed, 05 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolsContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /** Session Key for the ExternalToolsContext */
    private static final String EXT_TOOLS_SESS_KEY = "external_tools_context";

    /**
     * Get the ExternalToolsContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the ExternalToolsContext stored in the session
     */
    public static ExternalToolsContext getContext(HttpServletRequest req) {
        ExternalToolsContext context =
            (ExternalToolsContext)req.getSession().getAttribute(EXT_TOOLS_SESS_KEY);
        if (context == null) {
            context = new ExternalToolsContext();
            setContext(req, context);
        }
        return context;
    }

    /**
     * Set the ExternalToolsContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @param context the ExternalToolsContext
     */
    public static void setContext(HttpServletRequest req,
            ExternalToolsContext context) {
        req.getSession().setAttribute(EXT_TOOLS_SESS_KEY, context);
    }

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current column to sort by. */
    private static final String KEY_SORT_BY_COLUMN = "sort_by_column";

    /** HashMap to store context values for the tool list page. Thread safe. */
    private Map<String, Object> toolMap;

    /** HashMap to store context values for the tool list page. Thread safe. */
    private Map<String, Object> fileMap;

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public ExternalToolsContext() {
        toolMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        fileMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        setToolSortByColumn(ExternalToolDto.COLUMN_UPDATED, true);
        setFileSortByColumn(ExternalToolFileDto.COLUMN_NAME, false);
    }

    //----- TOOL MAP GETTERs and SETTERs -----

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getToolSortByColumn() {
        return (String)toolMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setToolSortByColumn(String columnName, Boolean toggleFlag) {
        toolMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleToolSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isToolAscending(String columnName) {
        Boolean result = true;
        if (toolMap.containsKey(columnName)) {
            result = (Boolean) toolMap.get(columnName);
        } else {
            toolMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleToolSortOrder(String columnName) {
        if (toolMap.containsKey(columnName)) {
            Boolean ascFlag = (Boolean) toolMap.get(columnName);
            toolMap.put(columnName, !ascFlag);
        } else {
            toolMap.put(columnName, false);
        }
    }

    //----- FILE MAP GETTERs and SETTERs -----

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getFileSortByColumn() {
        return (String)fileMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setFileSortByColumn(String columnName, Boolean toggleFlag) {
        fileMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleFileSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isFileAscending(String columnName) {
        Boolean result = true;
        if (fileMap.containsKey(columnName)) {
            result = (Boolean) fileMap.get(columnName);
        } else {
            fileMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleFileSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (fileMap.containsKey(columnName)) {
            ascFlag = (Boolean) fileMap.get(columnName);
            fileMap.put(columnName, !ascFlag);
        } else {
            fileMap.put(columnName, ascFlag);
        }
    }
}
