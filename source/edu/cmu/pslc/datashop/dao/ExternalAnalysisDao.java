/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;

/**
 * ExternalAnalysis Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 7569 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-03-30 12:40:09 -0400 (Fri, 30 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ExternalAnalysisDao extends AbstractDao<ExternalAnalysisItem> {

    /**
     * Standard get for a ExternalAnalysisItem by id.
     * @param id The id of the ExternalAnalysisItem.
     * @return the matching ExternalAnalysisItem or null if none found
     */
    ExternalAnalysisItem get(Integer id);

    /**
     * Standard find for an ExternalAnalysisItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ExternalAnalysisItem.
     * @return the matching ExternalAnalysisItem.
     */
    ExternalAnalysisItem find(Integer id);

    /**
     * Standard "find all" for ExternalAnalysisItems.
     * @return a List of objects
     */
    List<ExternalAnalysisItem> findAll();
}
