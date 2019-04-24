/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;


import java.util.List;
import edu.cmu.pslc.datashop.dao.ProjectShareabilityHistoryDao;
import edu.cmu.pslc.datashop.item.ProjectShareabilityHistoryItem;
import edu.cmu.pslc.datashop.item.ProjectItem;


/**
 * Project Shareability History Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectShareabilityHistoryDaoHibernate extends AbstractDaoHibernate
        implements ProjectShareabilityHistoryDao {

    /**
     * Standard get for a ProjectShareabilityHistoryItem by id.
     * @param id The id of the ProjectShareabilityHistoryItem.
     * @return the matching ProjectShareabilityHistoryItem or null if none found
     */
    public ProjectShareabilityHistoryItem get(Integer id) {
        return (ProjectShareabilityHistoryItem)get(ProjectShareabilityHistoryItem.class, id);
    }

    /**
     * Standard find for an ProjectShareabilityHistoryItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ProjectShareabilityHistoryItem.
     * @return the matching ProjectShareabilityHistoryItem.
     */
    public ProjectShareabilityHistoryItem find(Integer id) {
        return (ProjectShareabilityHistoryItem)find(ProjectShareabilityHistoryItem.class, id);
    }

    /**
     * Standard "find all" for ProjectShareabilityHistoryItems.
     * @return a List of objects
     */
    public List<ProjectShareabilityHistoryItem> findAll() {
        return findAll(ProjectShareabilityHistoryItem.class);
    }

    /** Constant for by-project query string. */
    private static final String BY_PROJECT_QUERY =
            "from ProjectShareabilityHistoryItem history where history.project = ?";

    /**
     * Returns a set of project shareability history items given a specific project.
     * @param project The project item
     * @return The set of history items associated with a given project
     */
    public List<ProjectShareabilityHistoryItem> findByProject(ProjectItem project) {
        return getHibernateTemplate().find(BY_PROJECT_QUERY, project);
    }

    /**
     * Returns a set of project shareability history items given a specific project,
     * ordered by date, newest to oldest.
     * @param project The project item
     * @return The set of history items associated with a given project
     */
    public List<ProjectShareabilityHistoryItem> findByProjectNewestFirst(ProjectItem project) {
        String query = BY_PROJECT_QUERY + " order by history.updatedTime DESC";
        return getHibernateTemplate().find(query, project);
    }

}
