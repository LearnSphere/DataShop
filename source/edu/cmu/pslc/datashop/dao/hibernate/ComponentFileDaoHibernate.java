/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
/**
 * Hibernate and Spring implementation of the ComponentFileDao.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class ComponentFileDaoHibernate
        extends AbstractDaoHibernate implements ComponentFileDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ComponentFileItem get(Long id) {
        return (ComponentFileItem)get(ComponentFileItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ComponentFileItem find(Long id) {
        return (ComponentFileItem)find(ComponentFileItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ComponentFileItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_WORKFLOW_HQL
            = "from ComponentFileItem map"
            + " where workflow = ?";

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_DATASET_HQL
            = "from ComponentFileItem map"
            + " where dataset = ?";

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_FILE_HQL
            = "from ComponentFileItem map"
            + " where file = ?";


    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_COMPONENT_HQL
            = "from ComponentFileItem map"
            + " where workflow = ? and componentId = ?";

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_WORKFLOW_AND_FILE_HQL
            = "from ComponentFileItem map"
            + " where workflow = ? and file = ?";

    /**
     *  Return a list of ComponentFileItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    public List<ComponentFileItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = { workflowItem };
        List<ComponentFileItem> itemList =
                getHibernateTemplate().find(FIND_BY_WORKFLOW_HQL, params);
        return itemList;
    }

    /**
     *  Return a list of ComponentFileItems.
     *  @param fileItem the given file item
     *  @return a list of items
     */
    public List<ComponentFileItem> findByFile(WorkflowFileItem fileItem) {
        Object[] params = { fileItem };
        List<ComponentFileItem> itemList =
                getHibernateTemplate().find(FIND_BY_FILE_HQL, params);
        return itemList;
    }


    /**
     *  Return a list of ComponentFileItems.
     *  @param datasetItem the given dataset item
     *  @return a list of items
     */
    public List<ComponentFileItem> findByDataset(DatasetItem datasetItem) {
        Object[] params = { datasetItem };
        List<ComponentFileItem> itemList =
                getHibernateTemplate().find(FIND_BY_DATASET_HQL, params);
        return itemList;
    }

    /**
     * Return the ComponentFileItems associated with the given workflow and componentId
     * @param workflowItem the workflow item
     * @param componentId the component ID
     * @return the ComponentFileItems associated with the given workflow and componentId
     */
    public List<ComponentFileItem> findImportByComponent(WorkflowItem workflowItem, String componentId) {

        Object[] params = { workflowItem, componentId };
        List<ComponentFileItem> itemList =
                getHibernateTemplate().find(FIND_BY_COMPONENT_HQL, params);
        return itemList;
    }

    /**
     *  Return a unique ComponentFileItem.
     *  @param workflowItem the given workflow item
     *  @param wfFileItem the WorkflowFileItem
     *  @return the unique row
     */
    public ComponentFileItem findByWorkflowAndFile(WorkflowItem workflowItem, WorkflowFileItem wfFileItem) {
        Object[] params = { workflowItem, wfFileItem };
        List<ComponentFileItem> itemList =
                getHibernateTemplate().find(FIND_BY_WORKFLOW_AND_FILE_HQL, params);
        if (itemList != null && !itemList.isEmpty()) {
        	return itemList.get(0);
        }
        return null;
    }

}
