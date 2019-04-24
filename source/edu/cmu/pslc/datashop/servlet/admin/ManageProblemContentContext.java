/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.util.LogException;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores session information for the Manage Problem Content page.
 *
 * @author Cindy Tipper
 * @version $Revision: 11126 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-05 16:25:50 -0400 (Thu, 05 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageProblemContentContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /** Session Key for the ManageProblemContentContext */
    private static final String MANAGE_PROBLEM_CONTENT_SESS_KEY = "manage_problem_content_context";

    /**
     * Get the ManageProblemContentContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the ManageProblemContentContext stored in the session
     */
    public static ManageProblemContentContext getContext(HttpServletRequest req) {
        ManageProblemContentContext context = (ManageProblemContentContext)
            req.getSession().getAttribute(MANAGE_PROBLEM_CONTENT_SESS_KEY);
        if (context == null) {
            context = new ManageProblemContentContext();
            setContext(req, context);
        }
        return context;
    }

    /**
     * Set the ManageProblemContentContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @param context the ManageProblemContentContext
     */
    public static void setContext(HttpServletRequest req, ManageProblemContentContext context) {
        req.getSession().setAttribute(MANAGE_PROBLEM_CONTENT_SESS_KEY, context);
    }

    //----- Constants -----

    /** The default string for the Conversion Tool. */
    public static final String DEFAULT_CONVERSION_TOOL = "";
    /** The default string for the Content Version filter. */
    public static final String DEFAULT_CONTENT_VERSION_SEARCH_BY = "";
    /** The default string for the Dataset filter. */
    public static final String DEFAULT_DATASET_SEARCH_BY = "";
    /** The default value for 'problem content'. */
    public static final String DEFAULT_PROBLEM_CONTENT =
        ManageProblemContentDto.PROBLEM_CONTENT_BOTH;

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current conversion tool string. */
    private static final String KEY_CONVERSION_TOOL = "conversion_tool";
    /** Session variable for storing the current search by string. */
    private static final String KEY_CONTENT_VERSION_SEARCH_BY = "content_version_search_by";
    /** Session variable for storing the current dataset search by string. */
    private static final String KEY_DATASET_SEARCH_BY = "dataset_search_by";
    /** Session variable for storing the 'problem content' value. */
    private static final String KEY_PROBLEM_CONTENT = "problem_content";
    /** Session variable for storing the current column to sort by. */
    private static final String KEY_SORT_BY = "sort_by";

    /** HashMap to store context values for the 'Problem List' page. Thread safe. */
    private Map<String, Object> map;

    /** List of allowed Problem Content values. */
    private static final List ALLOWED_PROBLEM_CONTENT = new ArrayList();
    static {
        ALLOWED_PROBLEM_CONTENT.add(ManageProblemContentDto.PROBLEM_CONTENT_BOTH);
        ALLOWED_PROBLEM_CONTENT.add(ManageProblemContentDto.PROBLEM_CONTENT_MAPPED);
        ALLOWED_PROBLEM_CONTENT.add(ManageProblemContentDto.PROBLEM_CONTENT_UNMAPPED);
    }

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public ManageProblemContentContext() {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());

        setConversionTool(DEFAULT_CONVERSION_TOOL);
        setContentVersionSearchBy(DEFAULT_CONTENT_VERSION_SEARCH_BY);
        setDatasetSearchBy(DEFAULT_DATASET_SEARCH_BY);
        setProblemContent(DEFAULT_PROBLEM_CONTENT);
        setSortBy(PcConversionDto.COLUMN_CONVERSION_TOOL, false);
    }

    //----- GETTERs and SETTERs -----

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
     * Get the content version searchBy parameter.
     * @return the content version searchBy parameter
     */
    public String getContentVersionSearchBy() {
        return (String)map.get(KEY_CONTENT_VERSION_SEARCH_BY);
    }

    /**
     * Set the content version searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setContentVersionSearchBy(String searchBy) {
        map.put(KEY_CONTENT_VERSION_SEARCH_BY, searchBy);
    }

    /**
     * Get the dataset searchBy parameter.
     * @return the dataset searchBy parameter
     */
    public String getDatasetSearchBy() {
        return (String)map.get(KEY_DATASET_SEARCH_BY);
    }

    /**
     * Set the dataset searchBy parameter.
     * @param searchBy the String to search by
     */
    public void setDatasetSearchBy(String searchBy) {
        map.put(KEY_DATASET_SEARCH_BY, searchBy);
    }

    /**
     * Get the problemContent parameter.
     * @return the problemContent parameter
     */
    public String getProblemContent() {
        return (String)map.get(KEY_PROBLEM_CONTENT);
    }

    /**
     * Set the problemContent parameter.
     * @param problemContent the String to specify problem content display
     */
    public void setProblemContent(String problemContent) {
        if (ALLOWED_PROBLEM_CONTENT.contains(problemContent)) {
            map.put(KEY_PROBLEM_CONTENT, problemContent);
        } else {
            throw new LogException("Invalid Problem Content value ignored: " + problemContent);
        }
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
