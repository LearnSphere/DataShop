/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao;

import java.util.List;

import edu.cmu.pslc.datashop.importdb.dao.ImportDbAbstractDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.item.CourseraClickstreamItem;

/**
 * Learnsphere Analysis Resource Use Coursera Clickstream Transaction Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CourseraClickstreamDao extends CourseraDbAbstractDao {

    /**
     * Standard get for a CourseraClickstream item by id.
     * @param id the id of the desired CourseraClickstreamItem
     * @return the matching CourseraClickstreamItem or null if none found
     */
        CourseraClickstreamItem get(Long id);

    /**
     * Standard find for a CourseraClickstreamItem by id.
     * @param id id of the object to find
     * @return CourseraClickstreamItem
     */
        CourseraClickstreamItem find(Long id);

    /**
     * Standard "find all" for CourseraClickstreamItem.
     * @return a List of objects
     */
    List<CourseraClickstreamItem> findAll();

    //
    // Non-standard methods begin.
    //
    List<CourseraClickstreamItem> getCourseraClickStream() ;
}
