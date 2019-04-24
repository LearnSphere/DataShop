/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.SessionDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SessionItem;

/**
 * Hibernate and Spring implementation of the SessionDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SessionDaoHibernate extends AbstractDaoHibernate implements SessionDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a SessionItem by id.
     * @param id The id of the user.
     * @return the matching SessionItem or null if none found
     */
    public SessionItem get(Long id) {
        return (SessionItem)get(SessionItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(SessionItem.class);
    }

    /**
     * Standard find for an SessionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SessionItem.
     * @return the matching SessionItem.
     */
    public SessionItem find(Long id) {
        return (SessionItem)find(SessionItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns SessionItem given a tag.
     * @param tag the actual session string
     * @return a collection of SessionItems
     */
    public Collection find(String tag) {
        return getHibernateTemplate().find(
                "from SessionItem session where session.sessionTag = ?", tag);
    }

    /**
     * Returns one SessionItem given a tag and dataset.
     * @param tag the actual session string
     * @param datasetItem the dataset
     * @param studentId the student id
     * @return a collection of SessionItems
     */
    public SessionItem get(String tag, DatasetItem datasetItem, Long studentId) {
        String query = "select distinct sess from SessionItem sess"
            + " join sess.dataset dat JOIN sess.student stu"
            + " where dat.id = ?"
            + " and sess.sessionTag = ? AND stu.id = ?";

        Object[] params = new Object[] {datasetItem.getId(), tag, studentId};

        List list = getHibernateTemplate().find(query, params);

        if (list.size() <= 0) {
            return null;
        } else if (list.size() == 1) {
            return (SessionItem)list.get(0);
        } else {
            logger.error("Duplicate record for session " + tag + "/"
                    + studentId + " found for dataset " + datasetItem + ".");
            return (SessionItem) list.get(0);
        }
    }

    /**
     * Get a count of the number of distinct sessions in a dataset.
     * @param datasetItem the dataset we wish to examine.
     * @return the number of sessions for the given dataset.
     */
    public long getNumSessionsInDataset(DatasetItem datasetItem) {
        long numSessions = 0;
        String query = "SELECT COUNT(DISTINCT sess) FROM SessionItem sess"
            + " JOIN sess.dataset dat"
            + " WHERE dat.id = ?";

        Object[] params = new Object[1];
        params[0] = datasetItem.getId();

        List list = getHibernateTemplate().find(query, params);
        if (list.size() > 0) {
            numSessions = (Long)list.get(0);
        }

        return numSessions;
    }

} // end SessionDaoHibernate.java
