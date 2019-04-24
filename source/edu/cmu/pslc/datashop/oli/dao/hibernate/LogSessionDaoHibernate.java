/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.oli.dao.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.datashop.dto.UserSession;
import edu.cmu.pslc.datashop.oli.dao.LogSessionDao;
import edu.cmu.pslc.datashop.oli.item.LogSessionItem;

/**
 * Hibernate and Spring implementation of the LogSessionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 3405 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-10-24 10:28:33 -0400 (Tue, 24 Oct 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LogSessionDaoHibernate extends OliHibernateAbstractDao implements LogSessionDao {

    /**
     * Standard get for a LogSessionItem by id.
     * @param id The id of the user.
     * @return the matching LogSessionItem or null if none found
     */
    public LogSessionItem get(String id) {
        return (LogSessionItem)get(LogSessionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(LogSessionItem.class);
    }

    /**
     * Standard find for an LogSessionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired LogSessionItem.
     * @return the matching LogSessionItem.
     */
    public LogSessionItem find(String id) {
        return (LogSessionItem)find(LogSessionItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Get the distinct user/session pairs from the Log database.
     * @return a list of UserSession objects
     */
    public List getDistinctUserSessions() {
        List userSessionList = new ArrayList();
        String query = "select distinct log_sess.userId, log_sess.sessionId"
            + " from LogSessionItem log_sess order by log_sess.userId, log_sess.sessionId";
        //String query = "select new edu.cmu.pslc.datashop.dto.UserSession
        //(log_sess.userId, log_sess.sessionId) from LogSessionItem log_sess";
        List sessionList = getHibernateTemplate().find(query);
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            //UserSession userSession = (UserSession)iter.next();

            Object[] row = (Object[])iter.next();
            UserSession userSession = new UserSession((String)row[0], (String)row[1]);
            userSessionList.add(userSession);
        }
        return userSessionList;
    }

    /**
     * Get an actual user id given a session id.
     * @param sessionId the given session id
     * @return actual user id as a String
     */
    public String getUserId(String sessionId) {
        return null;
    }
}
