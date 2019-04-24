/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;

/**
 * Hibernate and Spring implementation of the WorkflowFileDao.
 *
 * @author Mike Komisin
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowFileDaoHibernate extends AbstractDaoHibernate implements WorkflowFileDao {

    /**
     * Standard get for a WorkflowFileItem by id.
     * @param id The id of the user.
     * @return the matching WorkflowFileItem or null if none found
     */
    public WorkflowFileItem get(Integer id) {
        return (WorkflowFileItem)get(WorkflowFileItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(WorkflowFileItem.class);
    }

    /**
     * Standard find for an WorkflowFileItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowFileItem.
     * @return the matching WorkflowFileItem.
     */
    public WorkflowFileItem find(Integer id) {
        return (WorkflowFileItem)find(WorkflowFileItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query to get all custom fields for a given student and problem */
    private static final String FIND_BY_PATH_AND_NAME =
          "from WorkflowFileItem f"
        + " where f.filePath = ?"
        + " and f.fileName = ?";

    /**
     * Get a list of file items with the given file name and path.
     * @param path the path to the file
     * @param name the name of the file
     * @return a List of matching WorkflowFileItems, though only expect one item in list
     */
    public List find(String path, String name) {
        Object [] params = new Object [2];
        params[0] = path;
        params[1] = name;

        return getHibernateTemplate().find(FIND_BY_PATH_AND_NAME, params);
    }


}
