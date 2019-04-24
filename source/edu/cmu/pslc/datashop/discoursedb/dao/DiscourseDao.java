/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

/**
 * Discourse Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DiscourseDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DiscourseItem by id.
     * @param id The id of the DiscourseItem.
     * @return the matching DiscourseItem or null if none found
     */
    DiscourseItem get(Long id);

    /**
     * Standard find for an DiscourseItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DiscourseItem.
     * @return the matching DiscourseItem.
     */
    DiscourseItem find(Long id);

    /**
     * Standard "find all" for DiscourseItem.
     * @return a List of objects
     */
    List<DiscourseItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return the DiscourseItem for the specified name, null if
     * one doesn't exist.
     * @param name the discourse name
     * @return the DiscourseItem
     */
    DiscourseItem findByName(String name);

    /**
     * Return the min start_time for Contributions in the specified Discourse.
     * @param discourse the Discourse item
     * @return Date
     */
    Date getStartTimeByDiscourse(DiscourseItem discourse);

    /**
     * Return the max start_time for Contributions in the specified Discourse.
     * @param discourse the Discourse item
     * @return Date
     */
    Date getEndTimeByDiscourse(DiscourseItem discourse);

    /**
     * Return the DiscourseItem for the specified source id.
     * @param sourceId the id
     * @return the DiscourseItem
     */
    DiscourseItem findBySourceId(Long sourceId);

    /**
     * Clear the src_id column now that Discourse has been imported.
     */
    Integer clearSourceIds();

    /**
     * Find list of discourses marked for deletion.
     * @return List of DiscourseItem marked as deleted
     */
    List<DiscourseItem> findDeletedDiscourses();

    /**
     * Find list of discourses NOT marked for deletion.
     * @return List of DiscourseItem marked as deleted
     */
    List<DiscourseItem> findUndeletedDiscourses();

    /**
     * Find list of discourses for specified project.
     * @param projectId the project id
     * @return List of DiscourseItem assigned to project
     */
    List<DiscourseItem> findByProject(Integer projectId);

    /**
     * Deletes specified Discourse, using the delete_discourse SP.
     * @param discourse the DiscourseItem
     */
    void callDeleteDiscourseSP(DiscourseItem discourse);
}
