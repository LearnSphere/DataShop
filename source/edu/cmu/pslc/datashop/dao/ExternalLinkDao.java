/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ExternalLinkItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * External Link Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 8035 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-10-31 10:27:03 -0400 (Wed, 31 Oct 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ExternalLinkDao extends AbstractDao<ExternalLinkItem> {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ExternalLinkItem get(Integer id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ExternalLinkItem find(Integer id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List<ExternalLinkItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Find a list of external link items by project.
     * @param projectItem the project item
     * @return a List of item objects
     */
    List<ExternalLinkItem> findByProject(ProjectItem projectItem);
}
