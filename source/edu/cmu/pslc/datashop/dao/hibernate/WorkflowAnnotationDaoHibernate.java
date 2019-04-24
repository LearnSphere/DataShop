/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.WorkflowAnnotationDao;
import edu.cmu.pslc.datashop.workflows.WorkflowAnnotationItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

/**
 * Hibernate and Spring implementation of the WorkflowAnnotationDao.
 *
 * @author Peter Schaldenbrand
 * @version $Revision:  $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowAnnotationDaoHibernate extends AbstractDaoHibernate<WorkflowAnnotationItem>
implements WorkflowAnnotationDao {

    /**
     * Standard get for a WorkflowAnnotationItem by id.
     * @param id The id of the user.
     * @return the matching WorkflowAnnotationItem or null if none found
     */
    public WorkflowAnnotationItem get(Long id) {
        return (WorkflowAnnotationItem)get(WorkflowAnnotationItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<WorkflowAnnotationItem> findAll() {
        return findAll(WorkflowAnnotationItem.class);
    }

    /**
     * Standard find for a WorkflowAnnotationItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowAnnotationItem.
     * @return the matching WorkflowAnnotationItem.
     */
    public WorkflowAnnotationItem find(Long id) {
        return (WorkflowAnnotationItem)find(WorkflowAnnotationItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Find all annotation items for a given workflow.
     * @param workflowItem the workflow
     * @return a list of annotation items
     */
    public List find(WorkflowItem workflowItem) {
        Object[] params = new Object[] { workflowItem };
        String query = "from WorkflowAnnotationItem wai"
            + " where workflow = ?";
        return getHibernateTemplate().find(
                query, params);
    }
}
