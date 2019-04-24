/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;
import edu.cmu.pslc.datashop.dao.WorkflowUserLogDao;
import edu.cmu.pslc.datashop.workflows.WorkflowUserLogItem;

/**
 * Hibernate and Spring implementation of the WorkflowUserLogDao.
 *
 * @author
 * @version $Revision: 12111 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowUserLogDaoHibernate extends AbstractDaoHibernate
        implements WorkflowUserLogDao {

    /**
     * Standard get for a WorkflowUserLogItem by id.
     * @param id The id of the user.
     * @return the matching WorkflowUserLogItem or null if none found
     */
    public WorkflowUserLogItem get(Integer id) {
        return (WorkflowUserLogItem)get(WorkflowUserLogItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(WorkflowUserLogItem.class);
    }

    /**
     * Standard find for an WorkflowUserLogItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowUserLogItem.
     * @return the matching WorkflowUserLogItem.
     */
    public WorkflowUserLogItem find(Integer id) {
        return (WorkflowUserLogItem)find(WorkflowUserLogItem.class, id);
    }

}
