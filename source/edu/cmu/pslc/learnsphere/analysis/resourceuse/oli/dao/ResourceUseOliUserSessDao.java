/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao;

import java.util.HashMap;
import java.util.List;

import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliTransactionFileItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessFileItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessItem;

/**
 * Learnsphere Analysis Resource Use User Sess Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 12890 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:20 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResourceUseOliUserSessDao extends edu.cmu.pslc.datashop.dao.AbstractDao {

    /**
     * Standard get for a Resource Use OLI User Sess item by id.
     * @param id the id of the desired ResourceUseOliUserSessItem
     * @return the matching ResourceUseOliUserSessItem or null if none found
     */
     ResourceUseOliUserSessItem get(Long id);

    /**
     * Standard find for a ResourceUseOliUserSessItem by id.
     * @param id id of the object to find
     * @return ResourceUseOliUserSessItem
     */
     ResourceUseOliUserSessItem find(Long id);

    /**
     * Standard "find all" for ResourceUseOliUserSessItem.
     * @return a List of objects
     */
    List<ResourceUseOliUserSessItem> findAll();

    //
    // Non-standard methods begin.
    //
    /**
     * Return a list of ResourceUseOliUserSessItem for a given resourceUseOliUserSessFileItem
     * @param ResourceUseOliUserSessFileItem resourceUseOliUserSessFileItem resource use OLI user_sess file item
     * @return list of ResourceUseOliUserSessItem*/
    List<ResourceUseOliUserSessItem> findByResourceUseOliUserSessFile(ResourceUseOliUserSessFileItem resourceUseOliUserSessFileItem);
    
    /**
     * Return a list of anon student id for a given resourceUseOliTransactionFileItem
     * @param ResourceUseOliTransactionFileItem resourceUseOliTransactionFileItem resource use OLI transaction file item
     * @return list of String for student*/
    List<String> findAnonStudentByResourceUseOliTransactionFile(Integer resourceUseOliTransactionFileId);
    int clear(Integer resourceUseOliUserSessFileId);

}
