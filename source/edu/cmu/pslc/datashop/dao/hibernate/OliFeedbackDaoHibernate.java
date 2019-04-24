/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.OliFeedbackDao;
import edu.cmu.pslc.datashop.item.OliFeedbackItem;

/**
 * OliFeedbackDao Spring/Hibernate Implementation.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliFeedbackDaoHibernate extends AbstractDaoHibernate
     implements OliFeedbackDao {

    /**
     * Hibernate/Spring get for a item by id.
     * @param id the id of the desired project item
     * @return the matching ProjectItem or null if none found
     */
    public OliFeedbackItem get(Long id) {
        return (OliFeedbackItem)get(OliFeedbackItem.class, id);
    }
    /**
     * Hibernate/Spring find for a item by id.
     * @param id id of the object to find
     * @return ProjectItem
     */
    public OliFeedbackItem find(Long id) {
        return (OliFeedbackItem)find(OliFeedbackItem.class, id);
    }

    /**
     * Hibernate/Spring "find all" for items.
     * @return a List of objects which are ProjectItems
     */
    public List findAll() {
        return findAll(OliFeedbackItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Use a native SQL query to get OLI feedback data from the OLI log database.
     * @return a list of OliFeedbackItem objects
     */
    public List getFeedbackData() {
        List feedbackList = new ArrayList();
        Session session = getSession();
        List sessionList = session.createSQLQuery(NativeQueryGetData.QUERY)
                .addScalar("user_id", Hibernate.STRING)
                .addScalar("session_id", Hibernate.STRING)
                .addScalar("feedback_time", Hibernate.STRING)
                .addScalar("time_zone", Hibernate.STRING)
                .addScalar("action_guid", Hibernate.STRING)
                .addScalar("page_id", Hibernate.STRING)
                .addScalar("question_id", Hibernate.STRING)
                .addScalar("choice", Hibernate.STRING)
                .list();
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            Object[] row = (Object[])iter.next();

            OliFeedbackItem item = new OliFeedbackItem();

            int x = 0;
            item.setUserId((String)row[x]); x++;
            item.setSessionTag((String)row[x]); x++;

            String timeString = (String)row[x]; x++;
            Date feedbackTime = getDateStd(timeString);
            item.setFeedbackTime(feedbackTime);

            item.setTimeZone((String)row[x]); x++;
            item.setActionGuid((String)row[x]); x++;
            item.setPageId((String)row[x]); x++;
            item.setQuestionId((String)row[x]); x++;
            item.setChoice((String)row[x]); x++;

            feedbackList.add(item);
        }
        releaseSession(session);
        return feedbackList;
    }

    /** Inner class to hold the Native SQL Query string. */
    private static final class NativeQueryGetData {

        /** Private constructor. */
        private NativeQueryGetData() { }

        /** Log Database. */
        private static final String LOG_DB =
            HibernateDaoFactory.DEFAULT.getLogDatabaseName();

        /** Query string. */
        //TODO need to get admit code from a different db/table
        private static final String QUERY =
            "SELECT session.user_id as user_id, "
            + " session.user_sess as session_id, "
            + " la.time as feedback_time, "
            + " la.timezone as time_zone, "
            + " la.guid as action_guid, "
            + " la.info as page_id, "
            + " supplement.info_type as question_id, "
            + " supplement.info as choice"
            + " FROM " + LOG_DB + ".log_act la, "
                       + LOG_DB + ".log_sess session, "
                       + LOG_DB + ".log_supplement supplement"
            + " LEFT JOIN oli_feedback f"
            + " ON la.guid = f.action_guid"
            + " WHERE f.action_guid IS NULL"
            + " AND la.sess_ref = session.user_sess"
            + " AND la.action = 'FEEDBACK_SUBMIT'"
            + " AND la.guid = supplement.action_guid"
            + " ORDER by session.user_id, la.sess_ref, la.info, supplement.info_type";
    }

}
