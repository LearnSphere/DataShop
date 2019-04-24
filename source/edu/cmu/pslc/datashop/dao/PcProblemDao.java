/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;

/**
 * PcProblem Data Access Object Interface.
 *
 * @author
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface PcProblemDao extends AbstractDao {

    /**
     * Standard get for a PcProblemItem by id.
     * @param id The id of the PcProblemItem.
     * @return the matching PcProblemItem or null if none found
     */
    PcProblemItem get(Long id);

    /**
     * Standard find for an PcProblemItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired PcProblemItem.
     * @return the matching PcProblemItem.
     */
    PcProblemItem find(Long id);

    /**
     * Standard "find all" for PcProblemItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Find PcProblemItem given problem name and PcConversionItem.
     * @param problemName the problem name
     * @param pcConversion the PcConversionItem
     * @return matching PcProblemItem
     */
    PcProblemItem findByNameAndConversion(String problemName, PcConversionItem pcConversion);

    /**
     * Get a list of PcProblemItems by Problem Content Conversion.
     * @param pcConversion the PcConversionItem to match on
     * @return list of matching PcProblemItems
     */
    List<PcProblemItem> findProblemsByConversion(PcConversionItem pcConversion);
}
