/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao;

import java.util.List;

import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dto.OliUserTransactionDTO;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliTransactionItem;

/**
 * Learnsphere Analysis Resource Use Transaction Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 13977 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-03-07 15:53:24 -0500 (Tue, 07 Mar 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResourceUseOliTransactionDao extends edu.cmu.pslc.datashop.dao.AbstractDao {

    /**
     * Standard get for a Resource Use OLI Transaction item by id.
     * @param id the id of the desired ResourceUseOliTransactionItem
     * @return the matching ResourceUseTransactionItem or null if none found
     */
    ResourceUseOliTransactionItem get(Long id);

    /**
     * Standard find for a ResourceUseOliTransactionItem by id.
     * @param id id of the object to find
     * @return ResourceUseTransactionItem
     */
    ResourceUseOliTransactionItem find(Long id);

    /**
     * Standard "find all" for ResourceUseOliTransactionItem.
     * @return a List of objects
     */
    List<ResourceUseOliTransactionItem> findAll();

    //
    // Non-standard methods begin.
    //
    List<ResourceUseOliTransactionItem> getTransactionByAnonStudentId(Integer resourceUseOliTransactionFileId, String anonStudentId);
    List<OliUserTransactionDTO> getAllTransactions(Integer resourceUseOliUserSessFileId, Integer resourceUseOliTransactionFileId);
    int clear(Integer resourceUseOliTransactionFileId);
}
