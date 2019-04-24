/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ExternalToolFileMapDao;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapId;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapItem;
import edu.cmu.pslc.datashop.item.ExternalToolItem;

/**
 * Hibernate and Spring implementation of the ExternalToolFileMapDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7819 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-08-09 13:40:05 -0400 (Thu, 09 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolFileMapDaoHibernate
        extends AbstractDaoHibernate implements ExternalToolFileMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ExternalToolFileMapItem get(ExternalToolFileMapId id) {
        return (ExternalToolFileMapItem)get(ExternalToolFileMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ExternalToolFileMapItem find(ExternalToolFileMapId id) {
        return (ExternalToolFileMapItem)find(ExternalToolFileMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ExternalToolFileMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByTool method. */
    private static final String FIND_BY_TOOL_HQL
            = "from ExternalToolFileMapItem map"
            + " where externalTool = ?";

    /**
     *  Return a list of ExternalToolFileMapItems.
     *  @param toolItem the given tool item
     *  @return a list of items
     */
    public List<ExternalToolFileMapItem> findByTool(ExternalToolItem toolItem) {
        Object[] params = {toolItem};
        List<ExternalToolFileMapItem> itemList =
                getHibernateTemplate().find(FIND_BY_TOOL_HQL, params);
        return itemList;
    }
}
