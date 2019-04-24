/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

import edu.cmu.pslc.datashop.discoursedb.dao.DUserDao;
import edu.cmu.pslc.datashop.discoursedb.item.DataSourcesItem;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;

import org.springframework.orm.hibernate3.HibernateCallback;

import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.LONG;
import static org.hibernate.Hibernate.STRING;
import static org.hibernate.Hibernate.TIMESTAMP;

/**
 * Hibernate and Spring implementation of the DUserDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12869 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 15:13:10 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DUserDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DUserDao
{
    /**
     * Standard get for a DUserItem by id.
     * @param id The id of the user.
     * @return the matching DUserItem or null if none found
     */
    public DUserItem get(Long id) {
        return (DUserItem)get(DUserItem.class, id);
    }

    /**
     * Standard find for an DUserItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DUserItem.
     * @return the matching DUserItem.
     */
    public DUserItem find(Long id) {
        return (DUserItem)find(DUserItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DUserItem> findAll() {
        return findAll(DUserItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for batch of users. */
    private static final String USERS_QUERY =
        "SELECT DISTINCT user FROM DUserItem user";

    /**
     * Return a list of the users, starting at the specified offset,
     * limited to the max count.
     *
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return List<DUserItem> the batch of items
     */
    public List<DUserItem> getUsers(int offset, int max) {

        CallbackCreatorHelper helper = new CallbackCreatorHelper(USERS_QUERY, offset, max);
        HibernateCallback callback = helper.getCallback();
        List<DUserItem> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Return a list of DUserItem objects given a DataSourcesItem.
     * @param item the given data sources item
     * @return a list of DUserItem objects
     */
    public List<DUserItem> findByDataSources(DataSourcesItem item) {
        return getHibernateTemplate().find("FROM DUserItem WHERE dataSources = ?", item);
    }

    /** Constant SQL query for Contribution count by Discourse. */
    private static final String COUNT_BY_DISCOURSE =
        "SELECT COUNT(map.id_user) AS theCount FROM user_memberof_discourse map "
        + "WHERE map.id_discourse = :discourseId";

    /**
     * Return a count of the users in a given discourse.
     * @param discourse the Discourse item
     * @return count
     */
    public Long getCountByDiscourse(DiscourseItem discourse) {

        Long result = new Long(0);

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(COUNT_BY_DISCOURSE);
            query.addScalar("theCount", Hibernate.LONG);
            query.setParameter("discourseId", (Long)discourse.getId());

            List dbResults = query.list();
            if (dbResults.size() > 0) {
                result = (Long)dbResults.get(0);
            }
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /**
     * Return the DUserItem for the specified source id.
     * @param sourceId the id
     * @return the DUserItem
     */
    public DUserItem findBySourceId(Long sourceId) {
        DUserItem result = null;

        List<DUserItem> list =
            getHibernateTemplate().find("FROM DUserItem WHERE source_id = ?", sourceId);

        // Really only one of these...
        if (list.size() > 0) {
            result = list.get(0);
        }

        return result;
    }

    /**
     * Clear the source_id column now that Discourse has been imported.
     */
    public Integer clearSourceIds() {
        String updateStr = "UPDATE DUserItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }
}
