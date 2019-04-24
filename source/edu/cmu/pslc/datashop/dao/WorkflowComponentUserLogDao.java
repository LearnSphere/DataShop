/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentUserLogItem;

/**
 * WorkflowComponentUserLog Data Access Object Interface.
 *
 * @author
 * @version $Revision:  $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowComponentUserLogDao extends AbstractDao {

    /**
     * Standard get for a WorkflowComponentUserLogItem by id.
     * @param id The id of the WorkflowComponentUserLogItem.
     * @return the matching WorkflowComponentUserLogItem or null if none found
     */
    WorkflowComponentUserLogItem get(Integer id);

    /**
     * Standard find for an WorkflowComponentUserLogItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentUserLogItem.
     * @return the matching WorkflowComponentUserLogItem.
     */
    WorkflowComponentUserLogItem find(Integer id);

    /**
     * Standard "find all" for WorkflowComponentUserLogItems.
     * @return a List of objects
     */
    List findAll();

}