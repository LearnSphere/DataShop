/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.CogStepSeqItem;

/**
 * CogStepSequence Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 2960 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-06-19 16:21:21 -0400 (Mon, 19 Jun 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface CogStepSeqDao extends AbstractDao {

    /**
     * Standard get for a CogStepSeqItem by id.
     * @param id The id of the CogStepSeqItem.
     * @return the matching CogStepSeqItem or null if none found
     */
    CogStepSeqItem get(Long id);

    /**
     * Standard find for an CogStepSeqItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired CogStepSeqItem.
     * @return the matching CogStepSeqItem.
     */
    CogStepSeqItem find(Long id);

    /**
     * Standard "find all" for CogStepSequenceItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

}
