/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;
import edu.cmu.pslc.datashop.dao.WorkflowComponentUserLogDao;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentUserLogItem;

/**
 * Hibernate and Spring implementation of the WorkflowComponentUserLogDao.
 *
 * @author
 * @version $Revision: 12111 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentUserLogDaoHibernate extends AbstractDaoHibernate
        implements WorkflowComponentUserLogDao {

    /**
     * Standard get for a WorkflowComponentUserLogItem by id.
     * @param id The id of the user.
     * @return the matching WorkflowComponentUserLogItem or null if none found
     */
    public WorkflowComponentUserLogItem get(Integer id) {
        return (WorkflowComponentUserLogItem)get(WorkflowComponentUserLogItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(WorkflowComponentUserLogItem.class);
    }

    /**
     * Standard find for an WorkflowComponentUserLogItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentUserLogItem.
     * @return the matching WorkflowComponentUserLogItem.
     */
    public WorkflowComponentUserLogItem find(Integer id) {
        return (WorkflowComponentUserLogItem)find(WorkflowComponentUserLogItem.class, id);
    }

}
