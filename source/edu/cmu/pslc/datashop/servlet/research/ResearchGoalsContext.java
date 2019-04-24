/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.research;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores session information for the Research Goal page.
 *
 * @author alida
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalsContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /** Session Key for the ExternalToolsContext */
    private static final String SESS_KEY = "research_goal_context";

    /**
     * Get the ExternalToolsContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the ExternalToolsContext stored in the session
     */
    public static ResearchGoalsContext getContext(HttpServletRequest req) {
        ResearchGoalsContext context =
            (ResearchGoalsContext)req.getSession().getAttribute(SESS_KEY);
        if (context == null) {
            context = new ResearchGoalsContext();
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
            ResearchGoalsContext context) {
        req.getSession().setAttribute(SESS_KEY, context);
    }

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current column to sort by. */
    private static final String KEY_TYPE = "selected_researcher_type";

    /** HashMap to store context values for the tool list page. Thread safe. */
    private Map<String, Object> map;

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public ResearchGoalsContext() {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());
    }

    //----- MAP GETTERs and SETTERs -----

    /**
     * Gets the selected researcher type.
     * @return the selected researcher type
     */
    public Integer getSelectedResearcherType() {
        return (Integer)map.get(KEY_TYPE);
    }

    /**
     * Sets the selected researcher type.
     * @param id the selected researcher type id
     */
    public void setSelectedResearcherType(Integer id) {
        map.put(KEY_TYPE, id);
    }
}
