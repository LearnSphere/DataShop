/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao;

import java.util.List;

import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessFileItem;

/**
 * Learnsphere Analysis Resource Use User Sess File Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 12890 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:20 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResourceUseOliUserSessFileDao extends edu.cmu.pslc.datashop.dao.AbstractDao {

    /**
     * Standard get for a Resource Use user-sess File item by id.
     * @param id the id of the desired ResourceUseUserSessFileItem
     * @return the matching ResourceUseOutcomeFileItem or null if none found
     */
    ResourceUseOliUserSessFileItem get(Integer id);

    /**
     * Standard find for a ResourceUseOliUserSessFileItem by id.
     * @param id id of the object to find
     * @return ResourceUseOliUserSessFileItem
     */
    ResourceUseOliUserSessFileItem find(Integer id);

    /**
     * Standard "find all" for ResourceUseUserSessFileItem.
     * @return a List of objects
     */
    List<ResourceUseOliUserSessFileItem> findAll();

    //
    // Non-standard methods begin.
    //
    int clear(Integer resourceUseOliUserSessFileId);
}
