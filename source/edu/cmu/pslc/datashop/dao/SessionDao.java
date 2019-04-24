/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SessionItem;

/**
 * Session Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SessionDao extends AbstractDao {

    /**
     * Standard get for a SessionItem by id.
     * @param id The id of the SessionItem.
     * @return the matching SessionItem or null if none found
     */
    SessionItem get(Long id);

    /**
     * Standard find for an SessionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SessionItem.
     * @return the matching SessionItem.
     */
    SessionItem find(Long id);

    /**
     * Standard "find all" for SessionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a collection of SessionItems given a tag.
     * @param tag the actual session string
     * @return a collection of SessionItems
     */
    Collection find(String tag);

    /**
     * Returns one SessionItem given a tag and dataset.
     * @param tag the actual session string
     * @param datasetItem the dataset
     * @param studentId the student id
     * @return a collection of SessionItems
     */
    SessionItem get(String tag, DatasetItem datasetItem, Long studentId);

    /**
     * Get a count of the number of distinct sessions in a dataset.
     * @param datasetItem the dataset we wish to examine.
     * @return the number of sessions for the given dataset.
     */
    long getNumSessionsInDataset(DatasetItem datasetItem);

}
