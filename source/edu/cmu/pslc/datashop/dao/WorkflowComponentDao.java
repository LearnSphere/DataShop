/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;

/**
 * WorkflowComponent Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowComponentDao extends AbstractDao {

    /**
     * Standard get for a WorkflowComponentItem by id.
     * @param id The id of the WorkflowComponentItem.
     * @return the matching WorkflowComponentItem or null if none found
     */
    WorkflowComponentItem get(Long id);

    /**
     * Standard find for an WorkflowComponentItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentItem.
     * @return the matching WorkflowComponentItem.
     */
    WorkflowComponentItem find(Long id);

    /**
     * Standard "find all" for WorkflowComponentItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Returns the WorkflowComponentItem given the componentName.
     * @param componentName the component name
     * @return the WorkflowComponentItem
     */
    WorkflowComponentItem findByName(String componentName);

    /**
     * Returns the WorkflowComponentItems based on the component type.
     * @param componentTYpe the component type
     * @return the WorkflowComponentItems based on the component type
     */
    List<WorkflowComponentItem> findByComponentType(String componentTYpe);

    /**
     * Returns the WorkflowComponentItems based on the enabled flag.
     * @param enabledFlag the enabled flag (true or false)
     * @return the WorkflowComponentItems based on the enabled flag
     */
    List<WorkflowComponentItem> findByEnabled(Boolean enabledFlag);

    /**
     * Returns the enabled or disabled distinct component types sorted alphabetically.
     * @param enabledFlag whether to return enabled or disabled component types
     * @return the distinct component types sorted alphabetically
     */
    List<String> findDistinctComponentTypes(Boolean enabledFlag);

}
