/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.errorreport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the contextual information to save to the session for a error report under
 * a given dataset.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6998 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2011-07-07 09:25:38 -0400 (Thu, 07 Jul 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorReportContext {

    /**
     * Servlet Session context stored in the map, this allows us to synchronize
     * the list to handle multiple thread requests.
     */
    private Map <String, Object> map;

    /** Map Key. */
    private static final String VIEW_BY_KEY = "er_view_by_key";

    /** Default Constructor. */
    public ErrorReportContext() {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());
    }

    /** Returns the current view by mode. @return the view by as a string. */
    public String getViewBy() {
        return (String)map.get(VIEW_BY_KEY);
    }

    /** Sets the view by. @param viewBy String of the current view by type.
     */
    public void setViewBy(String viewBy) {
        map.put(VIEW_BY_KEY, viewBy);
    }
}