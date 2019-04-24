/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.cmu.pslc.datashop.dao.WorkflowErrorTranslationDao;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.workflows.WorkflowErrorTranslationItem;

/**
 * Workflow Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowErrorTranslationDaoHibernate
    extends AbstractDaoHibernate implements WorkflowErrorTranslationDao {

    /**
     * Standard get for a WorkflowErrorTranslationItem by id.
     * @param id The id of the WorkflowErrorTranslationItem
     * @return the matching WorkflowErrorTranslationItem or null if none found
     */
    public WorkflowErrorTranslationItem get(Integer id) {
        return (WorkflowErrorTranslationItem)get(WorkflowErrorTranslationItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowErrorTranslationItems.
     * @return a List of WorkflowErrorTranslationItems
     */
    public List findAll() {
        return findAll(WorkflowErrorTranslationItem.class);
    }

    /**
     * Standard find for a WorkflowErrorTranslationItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowErrorTranslationItem.
     * @return the matching WorkflowErrorTranslationItem.
     */
    public WorkflowErrorTranslationItem find(Integer id) {
        return (WorkflowErrorTranslationItem)find(WorkflowErrorTranslationItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Returns the WorkflowErrorTranslationItems given the message.
     * @param message the message
     * @return the WorkflowErrorTranslationItem
     */
    public List<WorkflowErrorTranslationItem> findByMessage(String message) {
        if (message == null) {
            return null;
        }

        Object[] params = {message};
        String query = "from WorkflowErrorTranslationItem where"
                + " ? like '%' || signature || '%'";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (List<WorkflowErrorTranslationItem>) components;
        } else {
            return null;
        }
    }

}
