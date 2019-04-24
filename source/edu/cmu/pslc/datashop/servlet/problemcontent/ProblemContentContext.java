/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores session information for the Problem Content page.
 *
 * @author Cindy Tipper
 * @version $Revision: 10992 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-05-07 13:22:53 -0400 (Wed, 07 May 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemContentContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /** Session Key for the ProblemContentContext */
    private static final String PROBLEM_CONTENT_SESS_KEY = "problem_content_context";

    /**
     * Get the ProblemContentContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the ProblemContentContext stored in the session
     */
    public static ProblemContentContext getContext(HttpServletRequest req) {
        ProblemContentContext context =
            (ProblemContentContext)req.getSession().getAttribute(PROBLEM_CONTENT_SESS_KEY);
        if (context == null) {
            context = new ProblemContentContext();
            setContext(req, context);
        }
        return context;
    }

    /**
     * Set the ProblemContentContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @param context the ProblemContentContext
     */
    public static void setContext(HttpServletRequest req, ProblemContentContext context) {
        req.getSession().setAttribute(PROBLEM_CONTENT_SESS_KEY, context);
    }

    //----- Constants -----

    /** The default string for the Content Version filter. */
    public static final String DEFAULT_SEARCH_BY = "";
    /** The default string for the Conversion Tool. */
    public static final String DEFAULT_CONVERSION_TOOL = "";

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current search by string. */
    private static final String KEY_SEARCH_BY = "search_by";
    /** Session variable for storing the current conversion tool string. */
    private static final String KEY_CONVERSION_TOOL = "conversion_tool";
    /** Session variable for storing the current column to sort by. */
    private static final String KEY_SORT_BY = "sort_by";

    /** HashMap to store context values for the 'Problem Content' page. Thread safe. */
    private Map<String, Object> map;

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public ProblemContentContext() {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());
        setSearchBy(DEFAULT_SEARCH_BY);
        setConversionTool(DEFAULT_CONVERSION_TOOL);
        setSortBy(MappedContentDto.COLUMN_CONTENT_VERSION, true);
    }

    //----- GETTERs and SETTERs -----

    /**
     * Get the searchBy parameter.
     * @return the searchBy parameter
     */
    public String getSearchBy() {
        return (String)map.get(KEY_SEARCH_BY);
    }

    /**
     * Set the searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setSearchBy(String searchBy) {
        map.put(KEY_SEARCH_BY, searchBy);
    }

    /**
     * Get the conversionTool parameter.
     * @return the conversionTool parameter
     */
    public String getConversionTool() {
        return (String)map.get(KEY_CONVERSION_TOOL);
    }

    /**
     * Set the conversionTool parameter.
     * @param conversionTool the tool name
     */
    public void setConversionTool(String conversionTool) {
        map.put(KEY_CONVERSION_TOOL, conversionTool);
    }

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getSortBy() {
        return (String)map.get(KEY_SORT_BY);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setSortBy(String columnName, Boolean toggleFlag) {
        map.put(KEY_SORT_BY, columnName);
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
        if (map.containsKey(columnName)) {
            result = (Boolean) map.get(columnName);
        } else {
            map.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleSortOrder(String columnName) {
        if (map.containsKey(columnName)) {
            Boolean ascFlag = (Boolean) map.get(columnName);
            map.put(columnName, !ascFlag);
        } else {
            map.put(columnName, false);
        }
    }
}
