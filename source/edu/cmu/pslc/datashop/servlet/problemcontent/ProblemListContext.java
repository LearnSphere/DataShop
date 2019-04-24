/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.util.LogException;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores session information for the Problem List page.
 *
 * @author Cindy Tipper
 * @version $Revision: 11151 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-10 14:59:28 -0400 (Tue, 10 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemListContext implements Serializable {

    //----- Constants -----

    /** The default count for Rows Per Page. */
    public static final int DEFAULT_ROWS_PER_PAGE = 10;
    /** The default current page. */
    public static final int DEFAULT_CURRENT_PAGE = 1;
    /** The default value for 'problem content'. */
    public static final String DEFAULT_PROBLEM_CONTENT = ProblemListDto.PROBLEM_CONTENT_BOTH;

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current search by string. */
    private static final String KEY_SEARCH_BY = "search_by";
    /** Session variable for storing the current rows per page. */
    private static final String KEY_ROWS_PER_PAGE = "rows_per_page";
    /** Session variable for storing the current page. */
    private static final String KEY_CURRENT_PAGE = "current_page";
    /** Session variable for storing the 'problem content' value. */
    private static final String KEY_PROBLEM_CONTENT = "problem_content";

    /** HashMap to store context values for the 'Problem List' page. Thread safe. */
    private Map<String, Object> map;

    /** List of allowed Problem Content values. */
    private static final List ALLOWED_PROBLEM_CONTENT = new ArrayList();
    static {
        ALLOWED_PROBLEM_CONTENT.add(ProblemListDto.PROBLEM_CONTENT_BOTH);
        ALLOWED_PROBLEM_CONTENT.add(ProblemListDto.PROBLEM_CONTENT_MAPPED);
        ALLOWED_PROBLEM_CONTENT.add(ProblemListDto.PROBLEM_CONTENT_UNMAPPED);
    }

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public ProblemListContext() {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());

        setSearchBy("");
        setRowsPerPage(DEFAULT_ROWS_PER_PAGE);
        setCurrentPage(DEFAULT_CURRENT_PAGE);
        setProblemContent(DEFAULT_PROBLEM_CONTENT);
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
     * Get the rowsPerPage parameter.
     * @return the rowsPerPage parameter
     */
    public Integer getRowsPerPage() {
        return (Integer)map.get(KEY_ROWS_PER_PAGE);
    }

    /**
     * Set the rowsPerPage parameter.
     * @param rowsPerPage the Integer for limiting query size
     */
    public void setRowsPerPage(Integer rowsPerPage) {
        map.put(KEY_ROWS_PER_PAGE, rowsPerPage);
    }

    /**
     * Get the currentPage parameter.
     * @return the currentPage parameter
     */
    public Integer getCurrentPage() {
        return (Integer)map.get(KEY_CURRENT_PAGE);
    }

    /**
     * Set the currentPage parameter.
     * @param currentPage the Integer indicating current page
     */
    public void setCurrentPage(Integer currentPage) {
        map.put(KEY_CURRENT_PAGE, currentPage);
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
}
