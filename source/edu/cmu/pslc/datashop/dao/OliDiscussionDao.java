/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.OliDiscussionItem;

/**
 * OliDiscussion Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2327 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-02-08 10:58:36 -0500 (Wed, 08 Feb 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface OliDiscussionDao extends AbstractDao {

    /**
     * Standard get for a selection item by selectionId.
     * @param selectionId the id of the desired selectionItem.
     * @return the matching OliDiscussionItem or null if none found
     */
    OliDiscussionItem get(Long selectionId);

    /**
     * Standard "find all" for selection items.
     * @return a List of objects
     */
    List findAll();

    /**
     * Standard find for a selection item by id.
     * Only guarantees the id of the item will be filled in.
     * @param selectionId the id of the desired selectionItem.
     * @return the matching OliDiscussionItem.
     */
    OliDiscussionItem find(Long selectionId);

    //
    // Non-standard methods begin.
    //

    /**
     * Use a native SQL query to get OLI discussion data from the OLI log database.
     * @return a list of OliDiscussionItem objects
     */
    List getDiscussionData();

}
