/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.WorkflowDatasetMapDao;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.item.DatasetItem;

/**
 * Hibernate and Spring implementation of the WorkflowDatasetMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowDatasetMapDaoHibernate
        extends AbstractDaoHibernate implements WorkflowDatasetMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public WorkflowDatasetMapItem get(WorkflowDatasetMapId id) {
        return (WorkflowDatasetMapItem)get(WorkflowDatasetMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public WorkflowDatasetMapItem find(WorkflowDatasetMapId id) {
        return (WorkflowDatasetMapItem)find(WorkflowDatasetMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List<WorkflowDatasetMapItem> findAll() {
        return findAll(WorkflowDatasetMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_Workflow_HQL
            = "from WorkflowDatasetMapItem map"
            + " where workflow = ?";

    /**
     *  Return a list of WorkflowDatasetMapItems.
     *  @param WorkflowItem the given Workflow item
     *  @return a list of items
     */
    public List<WorkflowDatasetMapItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = {workflowItem};
        List<WorkflowDatasetMapItem> itemList = getHibernateTemplate().find(FIND_BY_Workflow_HQL, params);
        return itemList;
    }

    /** HQL query for the findBydataset method. */
    private static final String FIND_BY_dataset_HQL
            = "from WorkflowDatasetMapItem map"
            + " where dataset = ?";

    /**
     *  Return a list of WorkflowDatasetMapItems.
     *  @param datasetItem the given dataset item
     *  @return a list of items
     */
    public List<WorkflowDatasetMapItem> findByDataset(DatasetItem datasetItem) {
        Object[] params = {datasetItem};
        List<WorkflowDatasetMapItem> itemList = getHibernateTemplate().find(FIND_BY_dataset_HQL, params);
        return itemList;
    }

}
