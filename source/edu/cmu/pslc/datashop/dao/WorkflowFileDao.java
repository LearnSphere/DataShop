/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;

/**
 * Workflow File Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowFileDao extends AbstractDao {

    /**
     * Standard get for a WorkflowFileItem by id.
     * @param id The id of the WorkflowFileItem.
     * @return the matching WorkflowFileItem or null if none found
     */
    WorkflowFileItem get(Integer id);

    /**
     * Standard find for an WorkflowFileItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowFileItem.
     * @return the matching WorkflowFileItem.
     */
    WorkflowFileItem find(Integer id);

    /**
     * Standard "find all" for WorkflowFileItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Get a list of file items with the given file name and path.
     * @param path the path to the file
     * @param name the name of the file
     * @return a List of matching WorkflowFileItems, though only expect one item in list
     */
    List find(String path, String name);


}
