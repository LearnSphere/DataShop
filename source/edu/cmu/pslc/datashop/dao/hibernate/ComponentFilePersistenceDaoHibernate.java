/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
/**
 * Workflow Import Persistence DAO.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class ComponentFilePersistenceDaoHibernate
    extends AbstractDaoHibernate
    implements ComponentFilePersistenceDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ComponentFilePersistenceItem get(Long id) {
        return (ComponentFilePersistenceItem)get(ComponentFilePersistenceItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ComponentFilePersistenceItem find(Long id) {
        return (ComponentFilePersistenceItem)find(ComponentFilePersistenceItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ComponentFilePersistenceItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_WORKFLOW_HQL
            = "from ComponentFilePersistenceItem map"
            + " where workflow = ?";

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_DATASET_HQL
            = "from ComponentFilePersistenceItem map"
            + " where dataset = ?";

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_FILE_HQL
            = "from ComponentFilePersistenceItem map"
            + " where file = ?";


    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_COMPONENT_HQL
            = "from ComponentFilePersistenceItem map"
            + " where workflow = ? and componentId = ?";

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_WORKFLOW_AND_FILE_HQL
            = "from ComponentFilePersistenceItem map"
            + " where workflow = ? and file = ?";

    /**
     *  Return a list of ComponentFilePersistenceItems.
     *  @param workflowItem the given workflow item
     *  @return a list of items
     */
    public List<ComponentFilePersistenceItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = { workflowItem };
        List<ComponentFilePersistenceItem> itemList =
                getHibernateTemplate().find(FIND_BY_WORKFLOW_HQL, params);
        return itemList;
    }

    /**
     *  Return a list of ComponentFilePersistenceItems.
     *  @param fileItem the given file item
     *  @return a list of items
     */
    public List<ComponentFilePersistenceItem> findByFile(WorkflowFileItem fileItem) {
        Object[] params = { fileItem };
        List<ComponentFilePersistenceItem> itemList =
                getHibernateTemplate().find(FIND_BY_FILE_HQL, params);
        return itemList;
    }


    /**
     *  Return a list of ComponentFilePersistenceItems.
     *  @param datasetItem the given dataset item
     *  @return a list of items
     */
    public List<ComponentFilePersistenceItem> findByDataset(DatasetItem datasetItem) {
        Object[] params = { datasetItem };
        List<ComponentFilePersistenceItem> itemList =
                getHibernateTemplate().find(FIND_BY_DATASET_HQL, params);
        return itemList;
    }

    /**
     * Return the ComponentFilePersistenceItems associated with the given workflow and componentId
     * @param workflowItem the workflow item
     * @param componentId the component ID
     * @return the ComponentFilePersistenceItems associated with the given workflow and componentId
     */
    public List<ComponentFilePersistenceItem> findImportByComponent(WorkflowItem workflowItem, String componentId) {

        Object[] params = { workflowItem, componentId };
        List<ComponentFilePersistenceItem> itemList =
                getHibernateTemplate().find(FIND_BY_COMPONENT_HQL, params);
        return itemList;
    }

    /**
     *  Return a unique ComponentFilePersistenceItem.
     *  @param workflowItem the given workflow item
     *  @param wfFileItem the WorkflowFileItem
     *  @return the unique row
     */
    public ComponentFilePersistenceItem findByWorkflowAndFile(WorkflowItem workflowItem, WorkflowFileItem wfFileItem) {
        Object[] params = { workflowItem, wfFileItem };
        List<ComponentFilePersistenceItem> itemList =
                getHibernateTemplate().find(FIND_BY_WORKFLOW_AND_FILE_HQL, params);
        if (itemList != null && !itemList.isEmpty()) {
            return itemList.get(0);
        }
        return null;
    }

    /**
     * Delete items associated with a given workflow.
     * @param workflowItem the workflow item
     * @return
     */
    public void deleteByWorkflow(WorkflowItem workflowItem) {
        if (workflowItem == null) {
            throw new IllegalArgumentException("workflow cannot be null.");
        }
        String query = "delete from component_file_persistence where workflow_id = ?";
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.setLong(1, ((Long)workflowItem.getId()).longValue());
            ps.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("delete componentFilePersistence for workflow " + workflowItem.getId());
            }
        } catch (SQLException exception) {
            logger.error("delete componentFilePersistence for workflow " + workflowItem.getId()
                    + " : SQLException occurred.", exception);
        } finally {
            releaseSession(session);
        }
    }


}
