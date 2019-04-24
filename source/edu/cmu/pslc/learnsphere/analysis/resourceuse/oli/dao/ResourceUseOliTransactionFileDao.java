/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dao.AbstractDao;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapId;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapItem;
import edu.cmu.pslc.datashop.item.ExternalToolItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliTransactionFileItem;

/**
 * Resource Use File Map Data Access Object Interface.
 *
 * @author Hui cheng
 * @version $Revision: 12890 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-01 23:57:20 -0500 (Mon, 01 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResourceUseOliTransactionFileDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ResourceUseOliTransactionFileItem get(Long id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ResourceUseOliTransactionFileItem find(Long id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //
    int clear(Integer resourceUseOliTransactionFileId);
}
