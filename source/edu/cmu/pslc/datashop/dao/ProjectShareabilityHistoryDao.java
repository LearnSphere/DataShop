/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectShareabilityHistoryItem;


/**
 * Project Shareability History Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProjectShareabilityHistoryDao extends AbstractDao {

    /**
     * Standard get for a ProjectShareabilityHistoryItem by id.
     * @param id The id of the ProjectShareabilityHistoryItem.
     * @return the matching ProjectShareabilityHistoryItem or null if none found
     */
    ProjectShareabilityHistoryItem get(Integer id);

    /**
     * Standard find for an ProjectShareabilityHistoryItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ProjectShareabilityHistoryItem.
     * @return the matching ProjectShareabilityHistoryItem.
     */
    ProjectShareabilityHistoryItem find(Integer id);

    /**
     * Standard "find all" for ProjectShareabilityHistoryItems.
     * @return a List of objects
     */
    List<ProjectShareabilityHistoryItem> findAll();

    /**
     * Returns a set of project shareability history items given a specific project.
     * @param project The project item
     * @return The set of history items associated with a given project
     */
    List<ProjectShareabilityHistoryItem> findByProject(ProjectItem project);

    /**
     * Returns a set of project shareability history items given a specific project,
     * ordered by date, newest to oldest.
     * @param project The project item
     * @return The set of history items associated with a given project
     */
    List<ProjectShareabilityHistoryItem> findByProjectNewestFirst(ProjectItem project);
}
