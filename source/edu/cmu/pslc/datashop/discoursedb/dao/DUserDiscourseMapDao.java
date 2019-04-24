/*
 * Carnegie Mellon University, Human Computer InterLogAction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.discoursedb.dao;

import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserDiscourseMapId;
import edu.cmu.pslc.datashop.discoursedb.item.DUserDiscourseMapItem;
import edu.cmu.pslc.datashop.discoursedb.item.DUserItem;

/**
 * DUserDiscourseMap Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12859 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-11 13:12:21 -0500 (Mon, 11 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DUserDiscourseMapDao extends DiscourseDbAbstractDao {

    /**
     * Standard get for a DUserDiscourseMapItem by id.
     * @param id The id of the DUserDiscourseMapItem.
     * @return the matching DUserDiscourseMapItem or null if none found
     */
    DUserDiscourseMapItem get(DUserDiscourseMapId id);

    /**
     * Standard find for an DUserDiscourseMapItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DUserDiscourseMapItem.
     * @return the matching DUserDiscourseMapItem.
     */
    DUserDiscourseMapItem find(DUserDiscourseMapId id);

    /**
     * Standard "find all" for DUserDiscourseMapItem.
     * @return a List of objects
     */
    List<DUserDiscourseMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of DUserDiscourseMapItem objects given a DataSourcesItem.
     * @param user the DiscourseDB user item
     * @return a list of DUserDiscourseMapItem objects
     */
    List<DUserDiscourseMapItem> findByUser(DUserItem user);

    /**
     * Return a list of DUserDiscourseMapItem objects given a DataSourcesItem.
     * @param discourse the DiscourseDB discourse item
     * @param offset the offset into the list for this batch
     * @param max the max batch size to return
     * @return a list of DUserDiscourseMapItem objects
     */
    List<DUserDiscourseMapItem> findByDiscourse(DiscourseItem user, int offset, int max);
}
