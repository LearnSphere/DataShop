/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao.hibernate;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.hibernate.HibernateDaoFactory;
import edu.cmu.pslc.datashop.dto.LoggingActivityOverviewReport;
import edu.cmu.pslc.datashop.dto.LoggingActivitySession;
import edu.cmu.pslc.datashop.oli.dao.LogActionDao;
import edu.cmu.pslc.datashop.oli.item.LogActionItem;

/**
 * Hibernate and Spring implementation of the LogActionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15103 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-05-03 11:51:38 -0400 (Thu, 03 May 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LogActionDaoHibernate extends OliHibernateAbstractDao implements LogActionDao {

    /**
     * Standard get for a LogActionItem by id.
     * @param id The id of the user.
     * @return the matching LogActionItem or null if none found
     */
    public LogActionItem get(String id) {
        return (LogActionItem)get(LogActionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(LogActionItem.class);
    }

    /**
     * Standard find for an LogActionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired LogActionItem.
     * @return the matching LogActionItem.
     */
    public LogActionItem find(String id) {
        return (LogActionItem)find(LogActionItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Get actions for the given session.
     * @param sessionId the session id
     * @return a list of LogAction objects
     */
    public List getBySessionId(String sessionId) {
        Session session = getSession();
        String query = Query.QUERY_BY_SESSION_ID;
        List sessionList = null;
        try {
            sessionList = session.createSQLQuery(query)
            .addEntity(Query.LOG_ACTION_ENTITY, LogActionItem.class)
            .setString(Query.SESSION_ID_PARAM, sessionId)
            .list();
        } finally {
            releaseSession(session);
        }
        return sessionList;
    }

    /**
     * Get actions which do not have sessions in the session table.
     * @return a list of LogAction objects
     */
    public List getWithoutUser() {
        Session session = getSession();
        List sessionList = null;
        try {
            sessionList = session.createSQLQuery(Query.QUERY_WITHOUT_USER)
                .addEntity(Query.LOG_ACTION_ENTITY, LogActionItem.class)
                .list();
        } finally {
            releaseSession(session);
        }
        return sessionList;
    }

    /** Inner class to hold the Native SQL Query strings. */
    private static final class Query {
        /** Private constructor. */
        private Query() { };

        /** Analysis Database. */
        private static final String ANALYSIS_DB =
            HibernateDaoFactory.DEFAULT.getAnalysisDatabaseName();

        /** Log Database. */
        private static final String LOG_DB =
            HibernateDaoFactory.DEFAULT.getLogDatabaseName();

        /** Log Action Entity. */
        private static final String LOG_ACTION_ENTITY = "log_action";

        /** Log Session Entity. */
        private static final String LOG_SESSION_ENTITY = "log_session";

        /** Analysis DB's Processed OLI Log Entity. */
        private static final String PROCESSED_ENTITY = "processed";

        /** Session Id Parameter. */
        private static final String SESSION_ID_PARAM = "sessionId";

        /** The where clause for the getWithUserId method. */
        private static final String WHERE_CLAUSE = " WHERE (" + LOG_ACTION_ENTITY + ".source "
            + " IN ('PACT_CTAT', 'PACT_CTAT_FLASH', 'FLASH_PSEUDO_TUTOR')"
            + " OR " + LOG_ACTION_ENTITY + ".info_type LIKE 'tutor_message%.dtd')";

        /** The order clause for the getWithUserId method. */
        private static final String ORDER_CLAUSE = " order by " + LOG_ACTION_ENTITY + ".time";

        /** Query string for getBySessionId method. */
        private static final String QUERY_BY_SESSION_ID =
            "SELECT {" + LOG_ACTION_ENTITY + ".*} FROM " + LOG_DB + ".log_act " + LOG_ACTION_ENTITY
                + " LEFT JOIN " + ANALYSIS_DB + ".oli_log " + PROCESSED_ENTITY
                + " ON " + LOG_ACTION_ENTITY + ".guid = " + PROCESSED_ENTITY + ".guid "
                + WHERE_CLAUSE
                + " AND " + PROCESSED_ENTITY + ".guid IS NULL "
                + " AND " + LOG_ACTION_ENTITY + ".sess_ref = :" + SESSION_ID_PARAM
                + ORDER_CLAUSE;

        /** Query string for getWithoutUser method. */
        private static final String QUERY_WITHOUT_USER =
            "SELECT {" + LOG_ACTION_ENTITY + ".*}"
                + " FROM " + LOG_DB + ".log_act " + LOG_ACTION_ENTITY + ""
                + " LEFT JOIN " + ANALYSIS_DB + ".oli_log " + PROCESSED_ENTITY
                + " ON " + LOG_ACTION_ENTITY + ".guid = " + PROCESSED_ENTITY + ".guid "
                + " LEFT JOIN " + LOG_DB + ".log_sess " + LOG_SESSION_ENTITY
                + " ON " + LOG_ACTION_ENTITY + ".sess_ref = " + LOG_SESSION_ENTITY + ".user_sess"
                + WHERE_CLAUSE
                + " AND " + PROCESSED_ENTITY + ".guid IS NULL "
                + " AND " + LOG_SESSION_ENTITY + ".user_id IS NULL "
                + ORDER_CLAUSE;
    }

    /** Constant for the maximum number of minutes to look back at, that is 2 days in minutes. */
    private static final int MAX_MINUTES = 2880;

    /** The Native SQL to call the Logging Activity stored procedure. */
    private static final String CALL_LOGGING_ACTIVITY_SP
            = "call get_logging_activity_report(:cutoffTime)";

    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_SESS_REF_IDX = 0;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_MIN_TIME_IDX = 1;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_MAX_TIME_IDX = 2;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_DATASET_NAME_IDX = 3;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_TOTAL_IDX = 4;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_CONTEXT_IDX = 5;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_TOOL_IDX = 6;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_TUTOR_IDX = 7;
    /** Index into database results for call to logging activity stored procedure. */
    private static final int LOG_ACT_PLAIN_IDX = 8;

    /**
     * Queries the log database for the list of sessions that were just logged and returns
     * a single LoggingActivityOverviewReport object to be sent to the JSP.
     * @param numMinutes the number of minutes to look back in time
     * @return the data collection overview report, null if the input parameter is invalid
     */
    public LoggingActivityOverviewReport getLoggingActivityOverviewReportSP(int numMinutes) {
        // Validate the input parameters.
        if (numMinutes <= 0 || numMinutes > MAX_MINUTES) {
            logger.warn("Invalid number of minutes passed to getLoggingActivityOverviewReport:"
                    + numMinutes);
            return null;
        }

        // Subtract the number of minutes given.
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.MINUTE, (-1) * numMinutes);
        Date cutoff = cal.getTime();
        if (logger.isTraceEnabled()) {
            logger.trace("Now is " + now
                    + ", numMinutes is " + numMinutes
                    + ", and cutoff date is " + cutoff);
        }

        // Run the Stored Procedure
        Session session = getSession();
        List dbResults = null;
        try {
            SQLQuery query = session.createSQLQuery(CALL_LOGGING_ACTIVITY_SP);
            query.setTimestamp("cutoffTime", cutoff);
            dbResults = query.list();
        } finally {
            releaseSession(session);
        }


        // Parse the Results
        LoggingActivityOverviewReport report = new LoggingActivityOverviewReport(now, cutoff);
        for (Iterator it = dbResults.iterator(); it.hasNext();) {
            Object[] items = (Object[])it.next();
            String sessRef = (String)items[LOG_ACT_SESS_REF_IDX];
            Date minServerReceiptTime = (Date)items[LOG_ACT_MIN_TIME_IDX];
            Date maxServerReceiptTime = (Date)items[LOG_ACT_MAX_TIME_IDX];
            String datasetName = (String)items[LOG_ACT_DATASET_NAME_IDX];
            int total   = ((Integer)items[LOG_ACT_TOTAL_IDX]).intValue();
            int context = ((Integer)items[LOG_ACT_CONTEXT_IDX]).intValue();
            int tool    = ((Integer)items[LOG_ACT_TOOL_IDX]).intValue();
            int tutor   = ((Integer)items[LOG_ACT_TUTOR_IDX]).intValue();
            int plain   = ((Integer)items[LOG_ACT_PLAIN_IDX]).intValue();

            LoggingActivitySession sessionObject =
                new LoggingActivitySession(sessRef, minServerReceiptTime, maxServerReceiptTime);
            sessionObject.setNumTotalMessages(total);
            sessionObject.setNumContextMessages(context);
            sessionObject.setNumToolMessages(tool);
            sessionObject.setNumTutorMessages(tutor);
            sessionObject.setNumPlainMessages(plain);
            report.addSession(datasetName, sessionObject);
        }

        // Build and return report.
        return report;
    }
}
