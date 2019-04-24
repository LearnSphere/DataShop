/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.dao;

import java.util.List;

import edu.cmu.pslc.datashop.importdb.dao.ImportDbAbstractDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.coursera.item.CourseraClickstreamVideoItem;

/**
 * Learnsphere Analysis Resource Use Coursera Clickstream Video Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 13095 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-04-14 11:35:29 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CourseraClickstreamVideoDao extends CourseraDbAbstractDao {

    /**
     * Standard get for a CourseraClickstreamVideoItem by id.
     * @param id the id of the desired CourseraClickstreamVideoItem
     * @return the matching CourseraClickstreamVideoItem or null if none found
     */
        CourseraClickstreamVideoItem get(Long id);

    /**
     * Standard find for a CourseraClickstreamVideoItem by id.
     * @param id id of the object to find
     * @return CourseraClickstreamVideoItem
     */
        CourseraClickstreamVideoItem find(Long id);

    /**
     * Standard "find all" for CourseraClickstreamVideoItem.
     * @return a List of objects
     */
    List<CourseraClickstreamVideoItem> findAll();

    //
    // Non-standard methods begin.
    //
    
}
