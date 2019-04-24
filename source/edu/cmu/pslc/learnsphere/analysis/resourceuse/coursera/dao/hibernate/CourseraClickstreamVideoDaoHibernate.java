/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao.CourseraClickstreamVideoDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.item.CourseraClickstreamVideoItem;

/**
 * Data access object to retrieve the data from the coursera_clickstream_video
 * database table via Hibernate.
 *
 * @author Hui Cheng
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CourseraClickstreamVideoDaoHibernate extends CourseraDbHibernateAbstractDao
        implements CourseraClickstreamVideoDao {
    /**
     * Standard get for a CourseraClickstreamVideoItem by id.
     * @param id the id of the desired CourseraClickstreamVideoItem
     * @return the matching CourseraClickstreamVideoItem or null if none found
     */
    public CourseraClickstreamVideoItem get(Long id) {
            return (CourseraClickstreamVideoItem)get(CourseraClickstreamVideoItem.class, id);
    }

    /**
     * Standard find for CourseraClickstreamVideoItem by id
     * @param id id of the object to find
     * @return CourseraClickstreamVideoItem
     */
    public CourseraClickstreamVideoItem find(Long id) {
            return (CourseraClickstreamVideoItem)find(CourseraClickstreamVideoItem.class, id);
    }

    /**
     * Standard "find all" for CourseraClickstreamVideoItem.
     * @return a List of objects
     */
    public List<CourseraClickstreamVideoItem> findAll() {
            return getHibernateTemplate().find("from " + CourseraClickstreamVideoItem.class.getName());
    }

}
