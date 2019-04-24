/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.importdb.dao.ImportFileInfoDao;
import edu.cmu.pslc.datashop.importdb.dao.hibernate.ImportDbHibernateAbstractDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.CourseraDbDaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.CourseraClickstreamDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.item.CourseraClickstreamItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.item.CourseraClickstreamVideoItem;

/**
 * Data access object to retrieve the data from the coursera resource_use
 * database table via Hibernate.
 *
 * @author Hui Cheng
 * @version $Revision: 15103 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-05-03 11:51:38 -0400 (Thu, 03 May 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CourseraClickstreamDaoHibernate extends CourseraDbHibernateAbstractDao
        implements CourseraClickstreamDao {
    /**
     * Standard get for a CourseraClickstreamItem by id.
     * @param id the id of the desired CourseraClickstreamItem
     * @return the matching CourseraClickstreamItem or null if none found
     */
    public CourseraClickstreamItem get(Long id) {
            return (CourseraClickstreamItem)get(CourseraClickstreamItem.class, id);
    }

    /**
     * Standard find for a CourseraClickstreamItem by id.
     * @param id id of the object to find
     * @return CourseraClickstreamItem
     */
    public CourseraClickstreamItem find(Long id) {
            return (CourseraClickstreamItem)find(CourseraClickstreamItem.class, id);
    }

    /**
     * Standard "find all" for CourseraClickstreamItem
     * @return a List of objects
     */
    public List<CourseraClickstreamItem> findAll() {
            return getHibernateTemplate().find("from " + CourseraClickstreamItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //
    public List<CourseraClickstreamItem> getCourseraClickStream() {
            String clickstreamTable = "coursera_clickstream";
            String clickstreamVideoTable = "coursera_clickstream_video";
            String STUDENT_DATA_QUERY_SELECT = "SELECT " +
                                    clickstreamTable + ".*, " +
                                    clickstreamVideoTable + ".* " +
                                    "FROM " + clickstreamTable + " LEFT OUTER JOIN " + clickstreamVideoTable + " " +
                                    "ON " + clickstreamTable + ".value = " + clickstreamVideoTable + ".id " +
                                    "ORDER BY " + clickstreamTable + ".username, " + clickstreamTable + ".timestamp, " + clickstreamVideoTable + ".eventTimestamp";
            Session session = getSession();
            List<CourseraClickstreamItem> result = null;
            try {
                SQLQuery query = session.createSQLQuery(STUDENT_DATA_QUERY_SELECT).addEntity(CourseraClickstreamItem.class);
                result = query.list();
            } finally {
                releaseSession(session);
            }
            return result;
    }

}
