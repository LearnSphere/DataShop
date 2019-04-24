/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;

import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowImportHelper;

import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;

/**
 * Workflow Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentDaoHibernate
    extends AbstractDaoHibernate implements WorkflowComponentDao {

    /**
     * Standard get for a WorkflowComponentItem by id.
     * @param id The id of the WorkflowComponentItem
     * @return the matching WorkflowComponentItem or null if none found
     */
    public WorkflowComponentItem get(Long id) {
        return (WorkflowComponentItem)get(WorkflowComponentItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowComponentItems.
     * @return a List of WorkflowComponentItems
     */
    public List findAll() {
        return findAll(WorkflowComponentItem.class);
    }

    /**
     * Standard find for a WorkflowComponentItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowComponentItem.
     * @return the matching WorkflowComponentItem.
     */
    public WorkflowComponentItem find(Long id) {
        return (WorkflowComponentItem)find(WorkflowComponentItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Returns the WorkflowComponentItem given the componentName.
     * @param componentName the component name
     * @return the WorkflowComponentItem
     */
    @Override
    public WorkflowComponentItem findByName(String componentName) {
        Object[] params = {componentName.toUpperCase()};
        String query = "from WorkflowComponentItem wfci where"
                + " upper(wfci.componentName) = upper(?)";
        List components = getHibernateTemplate().find(query, params);
        if (components != null && !components.isEmpty()) {
            return (WorkflowComponentItem) components.get(0);
        } else {
            return null;
        }
    }

    /** Query by component type. */
    private static final String COMPONENT_TYPE_QUERY =
        "FROM WorkflowComponentItem wfci WHERE UPPER(wfci.componentType) = UPPER(?)"
        + " ORDER BY UPPER(wfci.componentName)";

    /**
     * Returns the WorkflowComponentItems based on the component type.
     * @param componentTYpe the component type
     * @return the WorkflowComponentItems based on the component type
     */
    public List<WorkflowComponentItem> findByComponentType(String componentType) {
        Object[] params = {componentType.toUpperCase()};
        List<WorkflowComponentItem> result =
            getHibernateTemplate().find(COMPONENT_TYPE_QUERY, params);

        return result;
    }

    /**
     * Returns the WorkflowComponentItems based on the enabled flag.
     * @param enabledFlag the enabled flag (true or false)
     * @return the WorkflowComponentItems based on the enabled flag
     */
    public List<WorkflowComponentItem> findByEnabled(Boolean enabledFlag) {
        Object[] params = {enabledFlag};
        String query = "from WorkflowComponentItem wfci where wfci.enabled = ?";
        return getHibernateTemplate().find(query, params);
    }

    /**
     * Returns the enabled or disabled distinct component types sorted alphabetically.
     * @param enabledFlag whether to return enabled or disabled component types
     * @return the distinct component types sorted alphabetically
     */
    public List<String> findDistinctComponentTypes(Boolean enabledFlag) {
        Object[] params = {enabledFlag};
        String query = "select distinct lower(componentType) from WorkflowComponentItem wfc"
            + " where enabled = ?"
            + " order by lower(componentType)";
        return (List<String>)getHibernateTemplate().find(query, params);
    }

}
