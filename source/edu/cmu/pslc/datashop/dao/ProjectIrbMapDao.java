/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ProjectIrbMapId;
import edu.cmu.pslc.datashop.item.ProjectIrbMapItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.IrbItem;

/**
 * Project/IRB Map Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProjectIrbMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ProjectIrbMapItem get(ProjectIrbMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ProjectIrbMapItem find(ProjectIrbMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List<ProjectIrbMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of ProjectIrbMapItems.
     *  @param projectItem the given project item
     *  @return a list of items
     */
    List<ProjectIrbMapItem> findByProject(ProjectItem projectItem);

    /**
     *  Return a list of ProjectIrbMapItems.
     *  @param irbItem the given IRB item
     *  @return a list of items
     */
    List<ProjectIrbMapItem> findByIrb(IrbItem irbItem);

}
