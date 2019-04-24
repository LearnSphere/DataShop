/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;
import edu.cmu.pslc.datashop.item.RemoteSkillModelItem;

/**
 * RemoteSkillModel Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface RemoteSkillModelDao extends AbstractDao {

    /**
     * Standard get for a RemoteSkillModelItem by id.
     * @param id The id of the RemoteSkillModelItem.
     * @return the matching RemoteSkillModelItem or null if none found
     */
    RemoteSkillModelItem get(Long id);

    /**
     * Standard find for an RemoteSkillModelItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired RemoteSkillModelItem.
     * @return the matching RemoteSkillModelItem.
     */
    RemoteSkillModelItem find(Long id);

    /**
     * Standard "find all" for RemoteSkillModelItems.
     * @return a List of objects
     */
    List<RemoteSkillModelItem> findAll();

    //
    // Non-standard methods begin.
    //
    /**
     * Find all the remote skill models for the given remote dataset info.
     * @param remoteDatasetInfo the remote dataset info item
     * @return a list of remote skill model items
     */
    List<RemoteSkillModelItem> find(RemoteDatasetInfoItem remoteDatasetInfo);
}
