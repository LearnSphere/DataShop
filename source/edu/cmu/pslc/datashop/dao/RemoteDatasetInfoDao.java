/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;

/**
 * RemoteInstance Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface RemoteDatasetInfoDao extends AbstractDao<RemoteDatasetInfoItem> {

    /**
     * Standard get for a RemoteDatasetInfoItem by id.
     * @param id The id of the RemoteDatasetInfoItem.
     * @return the matching RemoteDatasetInfoItem or null if none found
     */
    RemoteDatasetInfoItem get(Long id);

    /**
     * Standard find for an RemoteDatasetInfoItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired RemoteDatasetInfoItem.
     * @return the matching RemoteDatasetInfoItem.
     */
    RemoteDatasetInfoItem find(Long id);

    /**
     * Standard "find all" for UserItems.
     * @return a List of objects
     */
    List<RemoteDatasetInfoItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of RemoteDatasetInfoItem objects, by dataset.
     *  @param dataset the DatasetItem
     *  @return a list of items
     */
    List<RemoteDatasetInfoItem> findByDataset(DatasetItem dataset);
}
