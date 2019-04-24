/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DatasetInstanceMapDao;
import edu.cmu.pslc.datashop.item.DatasetInstanceMapId;
import edu.cmu.pslc.datashop.item.DatasetInstanceMapItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

/**
 * Hibernate and Spring implementation of the DatasetInstanceMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInstanceMapDaoHibernate
        extends AbstractDaoHibernate implements DatasetInstanceMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public DatasetInstanceMapItem get(DatasetInstanceMapId id) {
        return (DatasetInstanceMapItem)get(DatasetInstanceMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public DatasetInstanceMapItem find(DatasetInstanceMapId id) {
        return (DatasetInstanceMapItem)find(DatasetInstanceMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(DatasetInstanceMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByDataset method. */
    private static final String FIND_BY_DATASET_HQL
        = "FROM DatasetInstanceMapItem map WHERE dataset = ?";

    /**
     *  Return a list of DatasetInstanceMapItems.
     *  @param datasetItem the given dataset item
     *  @return a list of items
     */
    public List<DatasetInstanceMapItem> findByDataset(DatasetItem datasetItem) {
        Object[] params = { datasetItem };
        List<DatasetInstanceMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_DATASET_HQL, params);
        return itemList;
    }

    /** HQL query for the findByInstance method. */
    private static final String FIND_BY_INSTANCE_HQL
        = "FROM DatasetInstanceMapItem map WHERE remoteInstance = ?";

    /**
     *  Return a list of DatasetInstanceMapItems.
     *  @param instanceItem the given remote instance item
     *  @return a list of items
     */
    public List<DatasetInstanceMapItem> findByInstance(RemoteInstanceItem instanceItem) {
        Object[] params = { instanceItem };
        List<DatasetInstanceMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_INSTANCE_HQL, params);
        return itemList;
    }

    /**
     *  Returns whether or not the dataset is remote.
     *  @param dataset the DatasetItem
     *  @return whether or not the dataset is remote
     */
    public boolean isDatasetRemote(DatasetItem dataset) {
        List<DatasetInstanceMapItem> mapList = findByDataset(dataset);
        return ((mapList != null) && (mapList.size() > 0));
    }

}
