/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;
import edu.cmu.pslc.datashop.workflows.WorkflowUserLogItem;

/**
 * WorkflowUserLog Data Access Object Interface.
 *
 * @author
 * @version $Revision:  $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowUserLogDao extends AbstractDao {

    /**
     * Standard get for a WorkflowUserLogItem by id.
     * @param id The id of the WorkflowUserLogItem.
     * @return the matching WorkflowUserLogItem or null if none found
     */
    WorkflowUserLogItem get(Integer id);

    /**
     * Standard find for an WorkflowUserLogItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowUserLogItem.
     * @return the matching WorkflowUserLogItem.
     */
    WorkflowUserLogItem find(Integer id);

    /**
     * Standard "find all" for WorkflowUserLogItems.
     * @return a List of objects
     */
    List findAll();

}