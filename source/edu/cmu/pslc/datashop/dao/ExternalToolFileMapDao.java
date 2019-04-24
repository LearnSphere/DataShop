/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ExternalToolFileMapId;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapItem;
import edu.cmu.pslc.datashop.item.ExternalToolItem;

/**
 * External Tool File Map Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7819 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-09 13:40:05 -0400 (Thu, 09 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ExternalToolFileMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ExternalToolFileMapItem get(ExternalToolFileMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ExternalToolFileMapItem find(ExternalToolFileMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of ExternalToolFileMapItems.
     *  @param toolItem the given tool item
     *  @return a list of items
     */
    List<ExternalToolFileMapItem> findByTool(ExternalToolItem toolItem);

}
