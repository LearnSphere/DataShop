/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.WorkflowTagMapDao;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowTagMapItem;

/**
 * Hibernate and Spring implementation of the WorkflowTagMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 15470 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-09-12 11:19:05 -0400 (Wed, 12 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowTagMapDaoHibernate
    extends AbstractDaoHibernate implements WorkflowTagMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public WorkflowTagMapItem get(WorkflowTagMapId id) {
        return (WorkflowTagMapItem)get(WorkflowTagMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public WorkflowTagMapItem find(WorkflowTagMapId id) {
        return (WorkflowTagMapItem)find(WorkflowTagMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List<WorkflowTagMapItem> findAll() {
        return findAll(WorkflowTagMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByWorkflow method. */
    private static final String FIND_BY_WORKFLOW_HQL
        = "from WorkflowTagMapItem map"
        + " where workflow = ?";

    /**
     *  Return a list of WorkflowTagMapItems.
     *  @param WorkflowItem the given Workflow item
     *  @return a list of items
     */
    public List<WorkflowTagMapItem> findByWorkflow(WorkflowItem workflowItem) {
        Object[] params = { workflowItem };
        List<WorkflowTagMapItem> itemList = getHibernateTemplate().find(FIND_BY_WORKFLOW_HQL, params);
        return itemList;
    }

    /** HQL query for the findBytag method. */
    private static final String FIND_BY_TAG_HQL
        = "from WorkflowTagMapItem map"
        + " where tag = ?";

    /**
     *  Return a list of WorkflowTagMapItems.
     *  @param tagItem the given tag item
     *  @return a list of items
     */
    public List<WorkflowTagMapItem> findByTag(WorkflowTagItem tagItem) {
        Object[] params = { tagItem };
        List<WorkflowTagMapItem> itemList = getHibernateTemplate().find(FIND_BY_TAG_HQL, params);
        return itemList;
    }
}
