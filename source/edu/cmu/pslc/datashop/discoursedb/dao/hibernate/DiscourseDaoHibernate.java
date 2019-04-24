/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

/**
 * Hibernate and Spring implementation of the DiscourseDao
 *
 * @author Cindy Tipper
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseDaoHibernate
    extends DiscourseDbAbstractDaoHibernate
    implements DiscourseDao
{
    /**
     * Standard get for a DiscourseItem by id.
     * @param id The id of the user.
     * @return the matching DiscourseItem or null if none found
     */
    public DiscourseItem get(Long id) {
        return (DiscourseItem)get(DiscourseItem.class, id);
    }

    /**
     * Standard find for an DiscourseItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DiscourseItem.
     * @return the matching DiscourseItem.
     */
    public DiscourseItem find(Long id) {
        return (DiscourseItem)find(DiscourseItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DiscourseItem> findAll() {
        return findAll(DiscourseItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Return the DiscourseItem for the specified name, null if
     * one doesn't exist.
     * @param name the discourse name
     * @return the DiscourseItem
     */
    public DiscourseItem findByName(String name) {
        DiscourseItem result = null;

        List<DiscourseItem> list =
            getHibernateTemplate().find("FROM DiscourseItem WHERE name = ?", name);

        // Really only one of these...
        if (list.size() > 0) {
            result = list.get(0);
        }

        return result;
    }

    /** Constant. */
    private static final String START_TIME = "startTime";
    /** Constant. */
    private static final String END_TIME = "endTime";
    /** Constant. */
    private static final String MAX_START_TIME = "maxStartTime";

    /** Constant SQL query for Discourse date range by Contribution. */
    private static final String DATE_RANGE_BY_DISCOURSE =
        "SELECT MIN(c.start_time) as " + START_TIME
        + ", MAX(c.end_time) as " + END_TIME
        + ", MAX(c.start_time) as " + MAX_START_TIME
        + " FROM contribution c "
        + "JOIN contribution_partof_discourse_part map ON "
        + "(map.fk_contribution = c.id_contribution) "
        + "JOIN discourse_part dp ON (map.fk_discourse_part = dp.id_discourse_part) "
        + "JOIN discourse_has_discourse_part d ON "
        + "(dp.id_discourse_part = d.fk_discourse_part) "
        + "WHERE d.fk_discourse = :discourseId";

    /**
     * Return the specified time for Contributions in the specified Dicourse.
     * @param discourse the Discourse item
     * @param label the label for 'start' or 'end' time
     * @return Date
     */
    private Date getDiscourseTime(DiscourseItem discourse, String label) {
        Date result = null;

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(DATE_RANGE_BY_DISCOURSE);
            query.addScalar(START_TIME, Hibernate.TIMESTAMP);
            query.addScalar(END_TIME, Hibernate.TIMESTAMP);
            query.addScalar(MAX_START_TIME, Hibernate.TIMESTAMP);
            query.setParameter("discourseId", (Long)discourse.getId());

            Date startTime = null;
            Date endTime = null;
            Date maxStartTime = null;

            List<Object[]> dbResults = query.list();
            for (Object[] o : dbResults) {
                int index = 0;
                startTime = (Date)o[index++];
                endTime = (Date)o[index++];
                maxStartTime = (Date)o[index++];
            }

            if (label.equals(START_TIME)) {
                result = startTime;
            } else {
                result = endTime;
                // For some/all contributions, the end_time is NULL. Use max(start_time).
                if (endTime == null) {
                    result = maxStartTime;
                }
            }
        } finally {
            if (session != null) { releaseSession(session); }
        }

        return result;
    }

    /**
     * Return the min start_time for Contributions in the specified Discourse.
     * @param discourse the Discourse item
     * @return Date
     */
    public Date getStartTimeByDiscourse(DiscourseItem discourse) {
        return getDiscourseTime(discourse, START_TIME);
    }

    /**
     * Return the max start_time for Contributions in the specified Discourse.
     * @param discourse the Discourse item
     * @return Date
     */
    public Date getEndTimeByDiscourse(DiscourseItem discourse) {
        return getDiscourseTime(discourse, END_TIME);
    }

    /**
     * Return the DiscourseItem for the specified source id.
     * @param sourceId the id
     * @return the DiscourseItem
     */
    public DiscourseItem findBySourceId(Long sourceId) {
        DiscourseItem result = null;

        List<DiscourseItem> list =
            getHibernateTemplate().find("FROM DiscourseItem WHERE source_id = ?", sourceId);

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
        String updateStr = "UPDATE DiscourseItem SET source_id = NULL";
        int rowsUpdated = getHibernateTemplate().bulkUpdate(updateStr);
        return rowsUpdated;
    }

    /**
     * Find list of discourses marked for deletion.
     * @return List of DiscourseItem marked as deleted
     */
    public List<DiscourseItem> findDeletedDiscourses() {
        return getHibernateTemplate().find("FROM DiscourseItem WHERE deletedFlag = true");
    }

    /** Constant. Query for discourses not marked for deletion. */
    private static final String UNDELETED_DISCOURSES_QUERY =
        "FROM DiscourseItem WHERE deletedFlag is NULL OR deletedFlag = false";

    /**
     * Find list of discourses NOT marked for deletion.
     * @return List of DiscourseItem marked as deleted
     */
    public List<DiscourseItem> findUndeletedDiscourses() {
        return getHibernateTemplate().find(UNDELETED_DISCOURSES_QUERY);
    }

    /** Constant. Query for discourses by project and not marked for deletion. */
    private static final String DISCOURSES_BY_PROJECT_QUERY =
        "FROM DiscourseItem WHERE projectId = ? AND (deletedFlag is NULL OR deletedFlag = false)";

    /**
     * Find list of discourses for specified project.
     * @param projectId the project id
     * @return List of DiscourseItem assigned to project
     */
    public List<DiscourseItem> findByProject(Integer projectId) {
        return getHibernateTemplate().find(DISCOURSES_BY_PROJECT_QUERY, projectId);
    }

    /** Stored procedure name. */
    private static final String DELETE_DISCOURSE_SP_NAME = "delete_discourse";

    /**
     * Deletes specified Discourse, using the delete_discourse SP.
     * @param discourse the DiscourseItem
     */
    public void callDeleteDiscourseSP(DiscourseItem discourse) {
        if (discourse == null) {
            throw new IllegalArgumentException("DiscourseItem cannot be null.");
        }

        logger.info("callDeleteDiscourseSP: discourse = " + discourse);

        String query  = buildSPCall(DELETE_DISCOURSE_SP_NAME, discourse.getId());
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException exception) {
            logger.error("Exception caught while executing the "
                         + DELETE_DISCOURSE_SP_NAME + " stored procedure.",
                         exception);
        } finally {
            releaseSession(session);
        }
    }

}
