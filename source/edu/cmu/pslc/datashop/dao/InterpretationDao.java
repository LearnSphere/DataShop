/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.InterpretationItem;

/**
 * Interpretation Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 2960 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-06-19 16:21:21 -0400 (Mon, 19 Jun 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface InterpretationDao extends AbstractDao {

    /**
     * Standard get for a InterpretationItem by id.
     * @param id The id of the InterpretationItem.
     * @return the matching InterpretationItem or null if none found
     */
    InterpretationItem get(Long id);

    /**
     * Standard find for an InterpretationItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired InterpretationItem.
     * @return the matching InterpretationItem.
     */
    InterpretationItem find(Long id);

    /**
     * Standard "find all" for InterpretationItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Return one interpretation item (if it is found) for the
     * pair of step sequences.
     * @param correctStepSeq the list of steps in the correct sequence
     * @param incorrectStepSeq the list of steps in the incorrect sequence
     * @return the existing interpretation or null if not found
     */
    InterpretationItem find(List correctStepSeq, List incorrectStepSeq);

     /**
      * Return one interpretation item (if it is found) for
      * one step sequence.
      * @param stepSeq the list of steps in the sequence
      * @param correctFlag indicates whether sequence is correct or not
      * @return the existing interpretation or null if not found
      */
     InterpretationItem find(List stepSeq, Boolean correctFlag);
}
