/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ExternalLinkDao;
import edu.cmu.pslc.datashop.item.ExternalLinkItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * Hibernate and Spring implementation of the ExternalLinkDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 8035 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-10-31 10:27:03 -0400 (Wed, 31 Oct 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalLinkDaoHibernate
extends AbstractDaoHibernate<ExternalLinkItem> implements ExternalLinkDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ExternalLinkItem get(Integer id) {
        return (ExternalLinkItem)get(ExternalLinkItem.class, id);
    }
    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ExternalLinkItem find(Integer id) {
        return (ExternalLinkItem)find(ExternalLinkItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List<ExternalLinkItem> findAll() {
        return findAll(ExternalLinkItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant HQL. */
    private static final String FIND_BY_PROJECT_HQL =
            "from ExternalLinkItem tool " + " where tool.project = ?";

    /**
     * Find a list of external link items by project.
     * @param projectItem the project item
     * @return a List of item objects
     */
    public List<ExternalLinkItem> findByProject(ProjectItem projectItem) {
        Object[] params = {projectItem};
        List<ExternalLinkItem> list = getHibernateTemplate().find(
                FIND_BY_PROJECT_HQL, params);
        return list;
    }
}
