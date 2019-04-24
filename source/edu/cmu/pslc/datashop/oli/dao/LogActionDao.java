/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dto.LoggingActivityOverviewReport;
import edu.cmu.pslc.datashop.oli.item.LogActionItem;

/**
 * LogAction Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 5736 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2009-09-21 10:22:17 -0400 (Mon, 21 Sep 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface LogActionDao extends OliAbstractDao {

    /**
     * Standard get for a LogActionItem by id.
     * @param id The id of the LogActionItem.
     * @return the matching LogActionItem or null if none found
     */
    LogActionItem get(String id);

    /**
     * Standard find for an LogActionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired LogActionItem.
     * @return the matching LogActionItem.
     */
    LogActionItem find(String id);

    /**
     * Standard "find all" for LogActionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Get actions for the given session.
     * @param sessionId the session id
     * @return a list of LogAction objects
     */
    List getBySessionId(String sessionId);

    /**
     * Get actions which do not have sessions in the session table.
     * @return a list of LogAction objects
     */
    List getWithoutUser();

    /**
     * Queries the log database for the list of sessions that were just logged and returns
     * a single LoggingActivityOverviewReport object to be sent to the JSP.
     * This method uses a stored procedure.
     * @param numMinutes the number of minutes to look back in time
     * @return the data collection overview report, null if the input parameter is invalid
     */
    LoggingActivityOverviewReport getLoggingActivityOverviewReportSP(int numMinutes);

}
