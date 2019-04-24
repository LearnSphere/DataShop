/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.WorkflowPersistenceDao;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPersistenceItem;

/**
 * Workflow Persistence Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowPersistenceDaoHibernate
    extends AbstractDaoHibernate implements WorkflowPersistenceDao {

    /**
     * Standard get for a WorkflowPersistenceItem by id.
     * @param id The id of the WorkflowPersistenceItem
     * @return the matching WorkflowPersistenceItem or null if none found
     */
    public WorkflowPersistenceItem get(Long id) {
        return (WorkflowPersistenceItem)get(WorkflowPersistenceItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowPersistenceItems.
     * @return a List of WorkflowPersistenceItems
     */
    public List findAll() {
        return findAll(WorkflowPersistenceItem.class);
    }

    /**
     * Standard find for a WorkflowPersistenceItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowPersistenceItem.
     * @return the matching WorkflowPersistenceItem.
     */
    public WorkflowPersistenceItem find(Long id) {
        return (WorkflowPersistenceItem)find(WorkflowPersistenceItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Find by name.
     * @param workflowName the name of the workflow
     * @return workflowPersistenceItem null if doesn't exist
     */
    public WorkflowPersistenceItem findByWorkflow(WorkflowItem workflowItem) {
        return (WorkflowPersistenceItem)
            findWithQuery("from WorkflowPersistenceItem wpi where workflow_id = ?",
               workflowItem.getId());
    }

}
