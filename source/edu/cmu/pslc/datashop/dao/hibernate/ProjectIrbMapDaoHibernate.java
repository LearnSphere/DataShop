/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ProjectIrbMapDao;
import edu.cmu.pslc.datashop.item.ProjectIrbMapId;
import edu.cmu.pslc.datashop.item.ProjectIrbMapItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.IrbItem;

/**
 * Hibernate and Spring implementation of the ProjectIrbMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectIrbMapDaoHibernate
        extends AbstractDaoHibernate implements ProjectIrbMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ProjectIrbMapItem get(ProjectIrbMapId id) {
        return (ProjectIrbMapItem)get(ProjectIrbMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ProjectIrbMapItem find(ProjectIrbMapId id) {
        return (ProjectIrbMapItem)find(ProjectIrbMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List<ProjectIrbMapItem> findAll() {
        return findAll(ProjectIrbMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByProject method. */
    private static final String FIND_BY_PROJECT_HQL
            = "from ProjectIrbMapItem map"
            + " where project = ?";

    /**
     *  Return a list of ProjectIrbMapItems.
     *  @param projectItem the given project item
     *  @return a list of items
     */
    public List<ProjectIrbMapItem> findByProject(ProjectItem projectItem) {
        Object[] params = {projectItem};
        List<ProjectIrbMapItem> itemList = getHibernateTemplate().find(FIND_BY_PROJECT_HQL, params);
        return itemList;
    }

    /** HQL query for the findByIrb method. */
    private static final String FIND_BY_IRB_HQL
            = "from ProjectIrbMapItem map"
            + " where irb = ?";

    /**
     *  Return a list of ProjectIrbMapItems.
     *  @param irbItem the given IRB item
     *  @return a list of items
     */
    public List<ProjectIrbMapItem> findByIrb(IrbItem irbItem) {
        Object[] params = {irbItem};
        List<ProjectIrbMapItem> itemList = getHibernateTemplate().find(FIND_BY_IRB_HQL, params);
        return itemList;
    }
}
