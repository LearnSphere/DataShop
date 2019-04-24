/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetInstanceMapId;
import edu.cmu.pslc.datashop.item.DatasetInstanceMapItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * DatasetRemoteInstanceMap Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetInstanceMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    DatasetInstanceMapItem get(DatasetInstanceMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    DatasetInstanceMapItem find(DatasetInstanceMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of DatasetInstanceMapItems.
     *  @param datasetItem the given dataset item
     *  @return a list of items
     */
    List<DatasetInstanceMapItem> findByDataset(DatasetItem datasetItem);

    /**
     *  Return a list of DatasetInstanceMapItems.
     *  @param instanceItem the given remote instance item
     *  @return a list of items
     */
    List<DatasetInstanceMapItem> findByInstance(RemoteInstanceItem instanceItem);

    /**
     *  Returns whether or not the dataset is remote.
     *  @param dataset the DatasetItem
     *  @return whether or not the dataset is remote
     */
    boolean isDatasetRemote(DatasetItem dataset);
}
